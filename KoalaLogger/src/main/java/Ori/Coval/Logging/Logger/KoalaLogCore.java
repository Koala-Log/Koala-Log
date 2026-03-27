package Ori.Coval.Logging.Logger;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Core logger for WPILOG format.
 * Handles registration of entries and logging of data values.
 */
public class KoalaLogCore implements Closeable {

    private static FileOutputStream fos;
    private static final HashMap<String, Integer> recordIDs = new HashMap<>();
    private static int largestId = 0;
    private static long startTime = System.nanoTime() / 1000;

    /**
     * When true, logging is disabled — doLog and writeRecord are no-ops.
     * This is the "fakeLog" mode requested by the user.
     */
    private static boolean fake = false;

    // --- Setup ---

    /**
     * Set up logging to a file named by the current timestamp (non-fake).
     */
    public static void setup(HardwareMap hardwareMap) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            .format(new Date());
        setup(hardwareMap, timeStamp + ".wpilog", false);
    }

    /**
     * Set up logging to a specific file.
     */
    public static void setup(HardwareMap hardwareMap, String filename) {
        setup(hardwareMap, filename, false);
    }

    /**
     * Set up logging; if fakeLog == true then no file is created and logging is disabled.
     */
    public static void setup(HardwareMap hardwareMap, boolean fakeLog) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            .format(new Date());
        setup(hardwareMap, timeStamp + ".wpilog", fakeLog);
    }

    /**
     * Set up logging to a specific file, with an option to run in fake (no-op) mode.
     */
    public static void setup(HardwareMap hardwareMap, String filename, boolean fakeLog) {
        synchronized (KoalaLogCore.class) {
            // close any existing log
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException ignored) {}
                fos = null;
            }

            // reset state
            recordIDs.clear();
            largestId = 0;
            startTime = System.nanoTime() / 1000;
            fake = fakeLog;

            if (fake) {
                // In fake mode we intentionally do not call LogFileManager.setup()
                // and do not allocate an output stream. Logging operations will be
                // short-circuited by checks of 'fake'.
                return;
            }

            // Normal (non-fake) initialization
            LogFileManager.setup(hardwareMap.appContext, filename);
            fos = LogFileManager.getOutputStream();
            startTime = System.nanoTime() / 1000;
            recordIDs.clear();
            largestId = 0;
            SchemaRegistry.registerPose2dSchema();
        }
    }

    public static void closeLog() {
        synchronized (KoalaLogCore.class) {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException ignored) {}
                fos = null;
            }
            recordIDs.clear();
            largestId = 0;
            startTime = System.nanoTime() / 1000;
            // do NOT change 'fake' here; let the caller choose to set it again on next setup.
        }
    }

    // --- Entry Management ---

    private static int getID(String logName) {
        return recordIDs.computeIfAbsent(logName, key -> ++largestId);
    }

    private static void startEntry(int entryId, String name, String type, String metadata, long ts) throws IOException {
        if (fake) return; // no writes in fake mode

        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        bb.write(0); // control=Start
        bb.write(Utils.le32(entryId));

        byte[] nameB = name.getBytes(StandardCharsets.UTF_8);
        bb.write(Utils.le32(nameB.length));
        bb.write(nameB);

        byte[] typeB = type.getBytes(StandardCharsets.UTF_8);
        bb.write(Utils.le32(typeB.length));
        bb.write(typeB);

        byte[] metaB = metadata.getBytes(StandardCharsets.UTF_8);
        bb.write(Utils.le32(metaB.length));
        bb.write(metaB);

        writeRecord(0, bb.toByteArray(), ts);
    }

    public static void appendRaw(String name, String type, byte[] payload) throws IOException {
        if (fake) return;
        int id = recordIDs.computeIfAbsent(name, KoalaLogCore::getID);
        startEntry(id, name, type, "", nowMicros());
        writeRecord(id, payload, nowMicros());
    }

    // --- Logging API ---

    /**
     * General-purpose log function used for all value types.
     */
    static <T> T doLog(
        String name,
        T value,
        String wpiType,
        BiConsumer<Integer, T> wpiLogger,
        BiConsumer<String, T> dashboardPoster,
        boolean postToDashboard,
        long timeStamp
    ) {
        return doLog(name, value, wpiType, wpiLogger, dashboardPoster, postToDashboard, "", timeStamp);
    }

    /**
     * General-purpose log function used for all value types.
     */
    static <T> T doLog(
        String name,
        T value,
        String wpiType,
        BiConsumer<Integer, T> wpiLogger,
        BiConsumer<String, T> dashboardPoster,
        boolean postToDashboard,
        String metadata,
        long timeStamp
    ) {
        // If fake mode is enabled, skip everything and return value immediately.
        if (fake) {
            return value;
        }

        boolean isNew = !recordIDs.containsKey(name);
        int id = recordIDs.computeIfAbsent(name, KoalaLogCore::getID);

        try {
            if (isNew) startEntry(id, name, wpiType, metadata, timeStamp);
            wpiLogger.accept(id, value);
            if (postToDashboard) dashboardPoster.accept(name, value);
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    /**
     * Write a binary payload to the log.
     */
    static void writeRecord(int entryId, byte[] payload, long ts) {
        if (fake) return;

        try {
            fos.write(0x7F);
            fos.write(Utils.le32(entryId));
            fos.write(Utils.le32(payload.length));
            fos.write(Utils.le64(ts));
            fos.write(payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- Timekeeping ---

    /**
     * Returns time in microseconds since logger start.
     */
    static long nowMicros() {
        return System.nanoTime() / 1000 - startTime;
    }

    // --- close ---

    @Override
    public void close() throws IOException {
        if (fos != null) fos.close();
    }

    // --- helper ---

    /**
     * Expose fake flag for callers/tests if needed.
     */
    public static boolean isFake() {
        return fake;
    }
}