package Ori.Coval.Logging.Logger;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class KoalaLog {

    // ---------------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------------

    /** Sentinel that tells the worker thread to exit cleanly. */
    private static final Runnable POISON_PILL = () -> {};

    /**
     * Bounded queue of pending log tasks.
     * 2 000 slots = plenty for a ~100 Hz loop, while bounding memory.
     * If the worker can't keep up, offer() silently drops (non-blocking).
     */
    private static final BlockingQueue<Runnable> QUEUE =
            new LinkedBlockingQueue<>(2000);

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static Thread workerThread;

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    /**
     * Calls KoalaLog.setup(hardwareMap) synchronously (must be on main thread).
     * Does NOT start the background thread — call start() after this.
     */
    public static void setup(HardwareMap hardwareMap) {
        SingleCoreKoalaLog.setup(hardwareMap);
    }

    public static void setup(HardwareMap hardwareMap, String filename) {
        SingleCoreKoalaLog.setup(hardwareMap, filename);
    }

    public static void setup(HardwareMap hardwareMap, boolean fakeLog) {
        SingleCoreKoalaLog.setup(hardwareMap, fakeLog);
    }

    public static void setup(HardwareMap hardwareMap, String filename, boolean fakeLog) {
        SingleCoreKoalaLog.setup(hardwareMap, filename, fakeLog);
    }

    public static synchronized void start() {
        if (RUNNING.get()) return; // already running
        QUEUE.clear();
        RUNNING.set(true);
        workerThread = new Thread(KoalaLog::drainLoop, "KoalaLog-Worker");
        workerThread.setDaemon(true); // won't block program exit
        workerThread.setPriority(Thread.MIN_PRIORITY); // yield to main thread
        workerThread.start();
    }

    public static void stop() {
        if (!RUNNING.compareAndSet(true, false)) return;
        QUEUE.offer(POISON_PILL);
        if (workerThread != null) {
            try {
                workerThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            workerThread = null;
        }
        SingleCoreKoalaLog.close();
    }

    // ---------------------------------------------------------------
    // Scalar log methods — mirror KoalaLog's API exactly
    // ---------------------------------------------------------------

    public static boolean log(String name, boolean value, boolean post) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post));
        return value;
    }

    public static boolean log(String name, boolean value, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata));
        return value;
    }

    public static long log(String name, long value, boolean post) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post));
        return value;
    }

    public static long log(String name, long value, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata));
        return value;
    }

    public static int log(String name, int value, boolean post) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post));
        return value;
    }

    public static int log(String name, int value, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata));
        return value;
    }

    public static float log(String name, float value, boolean post) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post));
        return value;
    }

    public static float log(String name, float value, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata));
        return value;
    }

    public static double log(String name, double value, boolean post) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post));
        return value;
    }

    public static double log(String name, double value, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata));
        return value;
    }

    public static String log(String name, String value, boolean post) {
        // Capture value eagerly — strings are immutable, safe to close over
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post));
        return value;
    }

    public static String log(String name, String value, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata));
        return value;
    }

    // ---------------------------------------------------------------
    // Array log methods — arrays are mutable, so we copy defensively
    // ---------------------------------------------------------------

    public static boolean[] log(String name, boolean[] value, boolean post) {
        final boolean[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post));
        return value;
    }

    public static boolean[] log(String name, boolean[] value, boolean post, String metadata) {
        final boolean[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata));
        return value;
    }

    public static long[] log(String name, long[] value, boolean post) {
        final long[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post));
        return value;
    }

    public static long[] log(String name, long[] value, boolean post, String metadata) {
        final long[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata));
        return value;
    }

    public static int[] log(String name, int[] value, boolean post) {
        final int[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post));
        return value;
    }

    public static int[] log(String name, int[] value, boolean post, String metadata) {
        final int[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata));
        return value;
    }

    public static float[] log(String name, float[] value, boolean post) {
        final float[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post));
        return value;
    }

    public static float[] log(String name, float[] value, boolean post, String metadata) {
        final float[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata));
        return value;
    }

    public static double[] log(String name, double[] value, boolean post) {
        final double[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post));
        return value;
    }

    public static double[] log(String name, double[] value, boolean post, String metadata) {
        final double[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata));
        return value;
    }

    public static String[] log(String name, String[] value, boolean post) {
        final String[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post));
        return value;
    }

    public static String[] log(String name, String[] value, boolean post, String metadata) {
        final String[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata));
        return value;
    }

    // ---------------------------------------------------------------
    // Geometry helpers
    // ---------------------------------------------------------------

    public static void logTranslation2d(String name, double x, double y, boolean post) {
        enqueue(() -> SingleCoreKoalaLog.logTranslation2d(name, x, y, post));
    }

    public static void logTranslation2d(String name, double x, double y, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.logTranslation2d(name, x, y, post, metadata));
    }

    public static void logRotation2d(String name, double rotation, boolean post) {
        enqueue(() -> SingleCoreKoalaLog.logRotation2d(name, rotation, post));
    }

    public static void logRotation2d(String name, double rotation, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.logRotation2d(name, rotation, post, metadata));
    }

    public static void logPose2d(String name, double x, double y, double rot, boolean post) {
        enqueue(() -> SingleCoreKoalaLog.logPose2d(name, x, y, rot, post));
    }

    public static void logPose2d(String name, double x, double y, double rot, boolean post, String metadata) {
        enqueue(() -> SingleCoreKoalaLog.logPose2d(name, x, y, rot, post, metadata));
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    /**
     * Submits a task to the background thread.
     * Uses offer() so it NEVER blocks the main thread.
     * If the queue is full the log entry is silently dropped.
     */
    private static void enqueue(Runnable task) {
        if (!RUNNING.get()) return;
        QUEUE.offer(task); // non-blocking
    }

    /** Background thread loop: drains tasks until poisoned. */
    private static void drainLoop() {
        while (true) {
            try {
                // Block for up to 100 ms waiting for work
                Runnable task = QUEUE.poll(100, TimeUnit.MILLISECONDS);
                if (task == null) {
                    // Timed out — check if we should exit
                    if (!RUNNING.get() && QUEUE.isEmpty()) break;
                    continue;
                }
                if (task == POISON_PILL) break;
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Never let a logging error kill the worker thread
                // Optionally: e.printStackTrace();
            }
        }

        // Drain any remaining tasks that arrived before the poison pill
        Runnable leftover;
        while ((leftover = QUEUE.poll()) != null && leftover != POISON_PILL) {
            try { leftover.run(); } catch (Exception ignored) {}
        }
    }
}
