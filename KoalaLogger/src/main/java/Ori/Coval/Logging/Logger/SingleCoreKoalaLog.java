package Ori.Coval.Logging.Logger;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.HardwareMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * WpiLog: write WPILOG-format files for Advantage Scope.
 * Supports scalar and array data types.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class SingleCoreKoalaLog {
    /**
     * Set up logging to a file named 'robot.wpilog' in SD or internal.
     */
    public static void setup(HardwareMap hardwareMap) {
        KoalaLogCore.setup(hardwareMap);
    }

    /**
     * Set up logging to the given filename, choosing SD if present.
     */
    public static void setup(HardwareMap hardwareMap, String filename) {
        KoalaLogCore.setup(hardwareMap, filename);
    }

    /**
     * Set up logging but optionally run in fake (no-op) mode.
     *
     * When fakeLog == true, no log file is created and calls to log(...) are
     * extremely cheap no-ops (they simply return the passed value).
     */
    public static void setup(HardwareMap hardwareMap, boolean fakeLog) {
        KoalaLogCore.setup(hardwareMap, fakeLog);
    }
    
    /**
     * Set up logging to the given filename (chooses SD if present) and optionally
     * run in fake (no-op) mode.
     */
    public static void setup(HardwareMap hardwareMap, String filename, boolean fakeLog) {
        KoalaLogCore.setup(hardwareMap, filename, fakeLog);
    }

    /**
     * Close the currently open log (if any) and reset internal state so that
     * the next call to setup(...) starts a new log file.
     */
    public static void close() {
        KoalaLogCore.closeLog();
    }

    // Scalars
    public static boolean log(String name, boolean value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "boolean",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packBooleans(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                timeStamp
        );
    }

    public static boolean log(String name, boolean value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "boolean",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packBooleans(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                metadata,
                timeStamp
        );
    }

    public static long log(String name, long value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packLongs(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                timeStamp
        );
    }

    public static long log(String name, long value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packLongs(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                metadata,
                timeStamp
        );
    }

    public static int log(String name, int value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int32",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packInts(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                timeStamp
        );
    }

    public static int log(String name, int value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int32",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packInts(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                metadata,
                timeStamp
        );
    }

    public static Integer log(String name, Integer value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int32",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packInts(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                timeStamp
        );
    }

    public static Integer log(String name, Integer value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int32",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packInts(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                metadata,
                timeStamp
        );
    }

    public static float log(String name, float value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "float",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packFloats(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                timeStamp
        );
    }

    public static float log(String name, float value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "float",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packFloats(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                metadata,
                timeStamp
        );
    }

    public static double log(String name, double value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "double",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packDoubles(new double[]{v}), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                timeStamp
        );
    }

    public static double log(String name, double value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "double",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packDoubles(new double[]{v}), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                metadata,
                timeStamp
        );
    }

    public static String log(String name, String value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "string",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packString(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                timeStamp
        );
    }

    public static String log(String name, String value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "string",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packString(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, v),
                post,
                metadata,
                timeStamp
        );
    }

    // Arrays
    public static boolean[] log(String name, boolean[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "boolean[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packBooleans(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static boolean[] log(String name, boolean[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "boolean[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packBooleans(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static Boolean[] log(String name, Boolean[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "boolean[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packBooleans(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static Boolean[] log(String name, Boolean[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "boolean[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packBooleans(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static long[] log(String name, long[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packLongs(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static long[] log(String name, long[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packLongs(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static Long[] log(String name, Long[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packLongs(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static Long[] log(String name, Long[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packLongs(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static int[] log(String name, int[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packInts(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static int[] log(String name, int[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packInts(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static Integer[] log(String name, Integer[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packInts(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static Integer[] log(String name, Integer[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "int64[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packInts(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static float[] log(String name, float[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "float[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packFloats(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static float[] log(String name, float[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "float[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packFloats(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static Float[] log(String name, Float[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "float[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packFloats(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static Float[] log(String name, Float[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "float[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packFloats(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static double[] log(String name, double[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "double[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packDoubles(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static double[] log(String name, double[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "double[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packDoubles(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static Double[] log(String name, Double[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "double[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packDoubles(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static Double[] log(String name, Double[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "double[]",
                (id, v) -> KoalaLogCore.writeRecord(id, BytePacker.packDoubles(v), timeStamp),
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    public static String[] log(String name, String[] value, boolean post, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "string[]",
                (id, v) -> {
                    try {
                        KoalaLogCore.writeRecord(id, BytePacker.packStrings(v), timeStamp);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                timeStamp
        );
    }

    public static String[] log(String name, String[] value, boolean post, String metadata, long timeStamp) {
        return KoalaLogCore.doLog(
                name, value, "string[]",
                (id, v) -> {
                    try {
                        KoalaLogCore.writeRecord(id, BytePacker.packStrings(v), timeStamp);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                (n, v) -> FtcDashboard.getInstance().getTelemetry().addData(n, Arrays.toString(v)),
                post,
                metadata,
                timeStamp
        );
    }

    /**
     * Logs a 2D translation (two doubles) as struct:Translation2d
     */
    public static void logTranslation2d(String name, double x, double y, boolean post, long timeStamp) {
        KoalaLogCore.doLog(
                name,
                new double[]{x, y},
                "struct:Translation2d",
                (id, v) -> {
                    // pack two doubles in little‐endian
                    byte[] payload = BytePacker.packDoubles(v);
                    KoalaLogCore.writeRecord(id, payload, timeStamp);
                },
                (n, v) -> FtcDashboard
                        .getInstance()
                        .getTelemetry()
                        .addData(n, String.format(Locale.US, "x=%.2f,y=%.2f", v[0], v[1])),
                post,
                timeStamp
        );
    }

    public static void logTranslation2d(String name, double x, double y, boolean post, String metadata, long timeStamp) {
        KoalaLogCore.doLog(
                name,
                new double[]{x, y},
                "struct:Translation2d",
                (id, v) -> {
                    // pack two doubles in little‐endian
                    byte[] payload = BytePacker.packDoubles(v);
                    KoalaLogCore.writeRecord(id, payload, timeStamp);
                },
                (n, v) -> FtcDashboard
                        .getInstance()
                        .getTelemetry()
                        .addData(n, String.format(Locale.US, "x=%.2f,y=%.2f", v[0], v[1])),
                post,
                metadata,
                timeStamp
        );
    }

    /**
     * Logs a 2D rotation (one double) as struct:Rotation2d
     */
    public static void logRotation2d(String name, double rotation, boolean post, long timeStamp) {
        KoalaLogCore.doLog(
                name,
                new double[]{rotation},
                "struct:Rotation2d",
                (id, v) -> {
                    // pack one double in little‐endian
                    byte[] payload = BytePacker.packDoubles(v);
                    KoalaLogCore.writeRecord(id, payload, timeStamp);
                },
                (n, v) -> FtcDashboard
                        .getInstance()
                        .getTelemetry()
                        .addData(n, String.format(Locale.US, "θ=%.2f", v[0])),
                post,
                timeStamp
        );
    }

    public static void logRotation2d(String name, double rotation, boolean post, String metadata, long timeStamp) {
        KoalaLogCore.doLog(
                name,
                new double[]{rotation},
                "struct:Rotation2d",
                (id, v) -> {
                    // pack one double in little‐endian
                    byte[] payload = BytePacker.packDoubles(v);
                    KoalaLogCore.writeRecord(id, payload, timeStamp);
                },
                (n, v) -> FtcDashboard
                        .getInstance()
                        .getTelemetry()
                        .addData(n, String.format(Locale.US, "θ=%.2f", v[0])),
                post,
                metadata,
                timeStamp
        );
    }

    /**
     * Logs a full Pose2d (three doubles) as struct:Pose2d
     */
    public static void logPose2d(String name, double x, double y, double rot, boolean post, long timeStamp) {
        KoalaLogCore.doLog(
                name,
                new double[]{x, y, rot},
                "struct:Pose2d",
                (id, v) -> {
                    // pack three doubles in little‐endian
                    byte[] payload = BytePacker.packDoubles(v);
                    KoalaLogCore.writeRecord(id, payload, timeStamp);
                },
                (n, v) -> FtcDashboard
                        .getInstance()
                        .getTelemetry()
                        .addData(n, String.format(Locale.US, "x=%.2f,y=%.2f,θ=%.2f", v[0], v[1], v[2])),
                post,
                timeStamp
        );
    }

    public static void logPose2d(String name, double x, double y, double rot, boolean post, String metadata, long timeStamp) {
        KoalaLogCore.doLog(
                name,
                new double[]{x, y, rot},
                "struct:Pose2d",
                (id, v) -> {
                    // pack three doubles in little‐endian
                    byte[] payload = BytePacker.packDoubles(v);
                    KoalaLogCore.writeRecord(id, payload, timeStamp);
                },
                (n, v) -> FtcDashboard
                        .getInstance()
                        .getTelemetry()
                        .addData(n, String.format(Locale.US, "x=%.2f,y=%.2f,θ=%.2f", v[0], v[1], v[2])),
                post,
                metadata,
                timeStamp
        );
    }

}
