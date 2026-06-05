package Ori.Coval.Logging.Logger;

import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

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
    private static final int BUFFER_SIZE = 8192;
    private static final ByteArrayOutputStream batchBuffer = new ByteArrayOutputStream(BUFFER_SIZE);
    private static final Object writeLock = new Object();

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
        setup(hardwareMap, defaultFilename(), false);
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
        setup(hardwareMap, defaultFilename(), fakeLog);
    }

    /**
     * Build the default log filename: a timestamp, optionally suffixed with the
     * name of the currently active op mode, e.g. {@code 2026-05-30_14-30-00_MyAuto.wpilog}.
     */
    private static String defaultFilename() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
        String opModeName = activeOpModeName();
        return opModeName == null
            ? timeStamp + ".wpilog"
            : timeStamp + "_" + opModeName + ".wpilog";
    }

    /**
     * Return a filename-safe name of the currently active op mode, or {@code null} when
     * no user op mode is running.
     */
    private static String activeOpModeName() {
        try {
            OpModeManagerImpl opModeManager = OpModeManagerImpl.getOpModeManagerOfActivity(AppUtil.getInstance().getRootActivity());
            if (opModeManager == null) {
                return null;
            }
            String name = opModeManager.getActiveOpModeName();
            if (name == null || name.isEmpty() || name.equals(OpModeManager.DEFAULT_OP_MODE_NAME)) {
                return null;
            }
            // Strip characters that are not safe in a filename.
            return name.replaceAll("[^a-zA-Z0-9_-]", "_");
        } catch (Exception e) {
            return null;
        }
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
            SchemaRegistry.reset();

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
            try {
                flush(); // 🔥 flush everything first

                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fos = null;
            }

            recordIDs.clear();
            largestId = 0;
            SchemaRegistry.reset();
        }
    }
    public static void flush() {
        synchronized (writeLock) {
            try {
                if (fos != null && batchBuffer.size() > 0) {
                    fos.write(batchBuffer.toByteArray());
                    batchBuffer.reset();
                    fos.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // --- Entry Management ---

    private static int getID(String logName) {
        return ++largestId;
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
            ByteArrayOutputStream record = new ByteArrayOutputStream();

            record.write(0x7F);
            record.write(Utils.le32(entryId));
            record.write(Utils.le32(payload.length));
            record.write(Utils.le64(ts));
            record.write(payload);

            synchronized (writeLock) {
                record.writeTo(batchBuffer);

                // 🚀 flush in batches instead of every log
                if (batchBuffer.size() >= BUFFER_SIZE) {
                    fos.write(batchBuffer.toByteArray());
                    batchBuffer.reset();
                }
            }

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
