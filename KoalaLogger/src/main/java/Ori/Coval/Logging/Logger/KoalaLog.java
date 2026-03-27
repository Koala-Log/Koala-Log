package Ori.Coval.Logging.Logger;

import static Ori.Coval.Logging.Logger.KoalaLogCore.nowMicros;

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
    private static final int MAX_QUEUE_SIZE = 5000;

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

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
        if (RUNNING.get()) return;
        QUEUE.clear();
        RUNNING.set(true);
        workerThread = new Thread(KoalaLog::drainLoop, "KoalaLog-Worker");
        workerThread.setDaemon(true);
        workerThread.setPriority(Thread.NORM_PRIORITY);
        workerThread.start();
    }

    public static void stop() {
        if (!RUNNING.compareAndSet(true, false)) return;

        // signal end
        QUEUE.offer(POISON_PILL);

        long start = System.currentTimeMillis();
        long timeoutMs = 2000;

        // wait for worker to FINISH
        while (workerThread != null && workerThread.isAlive()) {
            try {
                workerThread.join(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (System.currentTimeMillis() - start > timeoutMs) {
                System.out.println("KoalaLog: forced shutdown (logs may be incomplete)");
                break;
            }
        }

        // 🔴 CRITICAL: only close AFTER worker is done or timed out
        try {
            SingleCoreKoalaLog.flush(); // <-- ADD THIS METHOD
        } catch (Exception e) {
            e.printStackTrace();
        }

        SingleCoreKoalaLog.close();

        workerThread = null;
    }

    // ---------------------------------------------------------------
    // Scalar log methods
    // Timestamp is captured HERE on the calling (main) thread,
    // then passed into SingleCoreKoalaLog so writeRecord uses it.
    // ---------------------------------------------------------------

    public static boolean log(String name, boolean value, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, ts));
        return value;
    }

    public static boolean log(String name, boolean value, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata, ts));
        return value;
    }

    public static long log(String name, long value, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, ts));
        return value;
    }

    public static long log(String name, long value, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata, ts));
        return value;
    }

    public static int log(String name, int value, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, ts));
        return value;
    }

    public static int log(String name, int value, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata, ts));
        return value;
    }

    public static float log(String name, float value, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, ts));
        return value;
    }

    public static float log(String name, float value, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata, ts));
        return value;
    }

    public static double log(String name, double value, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, ts));
        return value;
    }

    public static double log(String name, double value, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata, ts));
        return value;
    }

    public static String log(String name, String value, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, ts));
        return value;
    }

    public static String log(String name, String value, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.log(name, value, post, metadata, ts));
        return value;
    }

    // ---------------------------------------------------------------
    // Array log methods — arrays are mutable, so we copy defensively
    // ---------------------------------------------------------------

    public static boolean[] log(String name, boolean[] value, boolean post) {
        final long ts = nowMicros();
        final boolean[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, ts));
        return value;
    }

    public static boolean[] log(String name, boolean[] value, boolean post, String metadata) {
        final long ts = nowMicros();
        final boolean[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata, ts));
        return value;
    }

    public static long[] log(String name, long[] value, boolean post) {
        final long ts = nowMicros();
        final long[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, ts));
        return value;
    }

    public static long[] log(String name, long[] value, boolean post, String metadata) {
        final long ts = nowMicros();
        final long[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata, ts));
        return value;
    }

    public static int[] log(String name, int[] value, boolean post) {
        final long ts = nowMicros();
        final int[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, ts));
        return value;
    }

    public static int[] log(String name, int[] value, boolean post, String metadata) {
        final long ts = nowMicros();
        final int[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata, ts));
        return value;
    }

    public static float[] log(String name, float[] value, boolean post) {
        final long ts = nowMicros();
        final float[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, ts));
        return value;
    }

    public static float[] log(String name, float[] value, boolean post, String metadata) {
        final long ts = nowMicros();
        final float[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata, ts));
        return value;
    }

    public static double[] log(String name, double[] value, boolean post) {
        final long ts = nowMicros();
        final double[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, ts));
        return value;
    }

    public static double[] log(String name, double[] value, boolean post, String metadata) {
        final long ts = nowMicros();
        final double[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata, ts));
        return value;
    }

    public static String[] log(String name, String[] value, boolean post) {
        final long ts = nowMicros();
        final String[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, ts));
        return value;
    }

    public static String[] log(String name, String[] value, boolean post, String metadata) {
        final long ts = nowMicros();
        final String[] copy = value.clone();
        enqueue(() -> SingleCoreKoalaLog.log(name, copy, post, metadata, ts));
        return value;
    }

    // ---------------------------------------------------------------
    // Geometry helpers
    // ---------------------------------------------------------------

    public static void logTranslation2d(String name, double x, double y, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.logTranslation2d(name, x, y, post, ts));
    }

    public static void logTranslation2d(String name, double x, double y, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.logTranslation2d(name, x, y, post, metadata, ts));
    }

    public static void logRotation2d(String name, double rotation, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.logRotation2d(name, rotation, post, ts));
    }

    public static void logRotation2d(String name, double rotation, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.logRotation2d(name, rotation, post, metadata, ts));
    }

    public static void logPose2d(String name, double x, double y, double rot, boolean post) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.logPose2d(name, x, y, rot, post, ts));
    }

    public static void logPose2d(String name, double x, double y, double rot, boolean post, String metadata) {
        final long ts = nowMicros();
        enqueue(() -> SingleCoreKoalaLog.logPose2d(name, x, y, rot, post, metadata, ts));
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private static void enqueue(Runnable task) {
        if (!RUNNING.get()) return;
        if (QUEUE.size() < MAX_QUEUE_SIZE) {
            QUEUE.offer(task);
        }
    }

    private static void drainLoop() {
        final ArrayList<Runnable> batch = new ArrayList<>(BATCH_SIZE);

        while (true) {
            try {
                Runnable first = QUEUE.poll(100, TimeUnit.MILLISECONDS);

                if (first == null) {
                    if (!RUNNING.get() && QUEUE.isEmpty()) break;
                    continue;
                }

                if (first == POISON_PILL) break;

                batch.clear();
                batch.add(first);
                QUEUE.drainTo(batch, BATCH_SIZE - 1);

                for (int i = 0; i < batch.size(); i++) {
                    Runnable task = batch.get(i);
                    if (task == POISON_PILL) return;
                    task.run();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Never let a logging error kill the worker thread
            }
        }

        Runnable leftover;
        while ((leftover = QUEUE.poll()) != null && leftover != POISON_PILL) {
            try { leftover.run(); } catch (Exception ignored) {}
        }
    }
}