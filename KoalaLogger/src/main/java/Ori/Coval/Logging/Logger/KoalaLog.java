package Ori.Coval.Logging.Logger;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
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
     * Unbounded queue — we no longer drop entries due to capacity.
     * The batching drain loop keeps memory in check by processing
     * entries faster than a one-at-a-time loop ever could.
     */
    private static final BlockingQueue<Runnable> QUEUE =
        new LinkedBlockingQueue<>();

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static Thread workerThread;

    /** How many tasks to drain per batch. */
    private static final int BATCH_SIZE = 500;

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    /**
     * Calls SingleCoreKoalaLog.setup(hardwareMap) synchronously (must be on main thread).
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
        workerThread.setDaemon(true);           // won't block program exit
        workerThread.setPriority(Thread.NORM_PRIORITY);
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
    // Scalar log methods
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
     * Queue is unbounded so offer() will only return false on an
     * OutOfMemoryError — effectively never under normal operation.
     */
    private static void enqueue(Runnable task) {
        if (!RUNNING.get()) return;
        QUEUE.offer(task);
    }

    /**
     * Background thread loop.
     *
     * Strategy:
     *   1. Block on poll() until at least one task arrives (up to 100 ms).
     *   2. Immediately drain everything else currently in the queue into
     *      a reusable batch list (drainTo is a single atomic sweep).
     *   3. Execute the whole batch before going back to sleep.
     *
     * This means a burst of 300 log calls queued while the worker was
     * busy is processed in one pass rather than 300 separate wakeups,
     * dramatically reducing per-entry overhead.
     */
    private static void drainLoop() {
        final ArrayList<Runnable> batch = new ArrayList<>(BATCH_SIZE);

        while (true) {
            try {
                // Wait for the first task (blocks up to 100 ms)
                Runnable first = QUEUE.poll(100, TimeUnit.MILLISECONDS);

                if (first == null) {
                    // Timed out with nothing in the queue
                    if (!RUNNING.get() && QUEUE.isEmpty()) break;
                    continue;
                }

                if (first == POISON_PILL) break;

                // Atomically drain everything currently available
                batch.clear();
                batch.add(first);
                QUEUE.drainTo(batch, BATCH_SIZE - 1); // grab up to 499 more

                // Execute the batch
                for (int i = 0; i < batch.size(); i++) {
                    Runnable task = batch.get(i);
                    if (task == POISON_PILL) return; // exit immediately
                    task.run();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Never let a logging error kill the worker thread
            }
        }

        // Final drain — flush everything that arrived before the poison pill
        Runnable leftover;
        while ((leftover = QUEUE.poll()) != null && leftover != POISON_PILL) {
            try { leftover.run(); } catch (Exception ignored) {}
        }
    }
}