package Ori.Coval.Logging.Logger;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Logs one or two FTC {@link Gamepad} objects in the exact same WPILOG format
 * that WPILib uses for joysticks, so that AdvantageScope's
 * <strong>Controller</strong> tab works out of the box.
 *
 * <h3>Quickstart</h3>
 * <pre>{@code
 * @Override public void init() {
 *     KoalaLog.setup(hardwareMap);
 *     KoalaLog.start();
 *     GamepadLogger.register(gamepad1, gamepad2); // starts logging automatically
 * }
 * }</pre>
 */
public final class KoalaGamepadLogger {

    // -----------------------------------------------------------------------
    // Polling cadence
    // -----------------------------------------------------------------------

    /** Samples per second. 50 Hz matches FTC's own loop rate. */
    private static final int POLL_HZ = 50;

    // -----------------------------------------------------------------------
    // Axis indices  (order = column index AdvantageScope shows)
    // -----------------------------------------------------------------------
    private static final int AXIS_LEFT_X       = 0;
    private static final int AXIS_LEFT_Y       = 1;
    private static final int AXIS_LEFT_TRIG    = 2;
    private static final int AXIS_RIGHT_TRIG   = 3;
    private static final int AXIS_RIGHT_X      = 4;
    private static final int AXIS_RIGHT_Y      = 5;
    private static final int AXIS_COUNT        = 6;

    // -----------------------------------------------------------------------
    // Button indices  (1-indexed in AdvantageScope UI, 0-indexed here)
    // -----------------------------------------------------------------------
    private static final int BTN_A           = 0;
    private static final int BTN_B           = 1;
    private static final int BTN_X           = 2;
    private static final int BTN_Y           = 3;
    private static final int BTN_LEFT_BMP    = 4;
    private static final int BTN_RIGHT_BMP   = 5;
    private static final int BTN_BACK        = 6;
    private static final int BTN_START       = 7;
    private static final int BTN_LEFT_STICK  = 8;
    private static final int BTN_RIGHT_STICK = 9;
    private static final int BTN_GUIDE       = 10;
    private static final int BUTTON_COUNT    = 11;

    // -----------------------------------------------------------------------
    // Per-slot state (one slot per gamepad)
    // -----------------------------------------------------------------------
    private static final class SlotState {
        final int      slot;
        float[]        prevAxes    = null;  // null → first write always fires
        boolean[]      prevButtons = null;
        long[]         prevPovs    = null;

        SlotState(int slot) { this.slot = slot; }

        String keyAxes()    { return "DS:joystick" + slot + "/axes";    }
        String keyButtons() { return "DS:joystick" + slot + "/buttons"; }
        String keyPovs()    { return "DS:joystick" + slot + "/povs";    }
    }

    // -----------------------------------------------------------------------
    // Static state
    // -----------------------------------------------------------------------

    private static Gamepad  gamepad1;
    private static Gamepad  gamepad2;
    private static SlotState slot0;
    private static SlotState slot1;

    private static ScheduledExecutorService scheduler;
    private static ScheduledFuture<?>        pollTask;

    /** Prevent instantiation — this class is fully static. */
    private KoalaGamepadLogger() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Register gamepad1 only (logged as {@code DS:joystick0}).
     * Starts the background polling thread immediately.
     *
     * @param gp1 {@code this.gamepad1} from your OpMode — must not be null.
     */
    public static void register(Gamepad gp1) {
        register(gp1, null);
    }

    /**
     * Register both gamepads and start the background polling thread.
     * Safe to call multiple times — stops any existing polling first.
     *
     * @param gp1 {@code this.gamepad1} — logged as joystick 0. Must not be null.
     * @param gp2 {@code this.gamepad2} — logged as joystick 1. Pass {@code null} to omit.
     */
    public static void register(Gamepad gp1, Gamepad gp2) {
        // Stop any previous polling before re-registering
        stop();

        gamepad1 = gp1;
        gamepad2 = gp2;
        slot0    = (gp1 != null) ? new SlotState(0) : null;
        slot1    = (gp2 != null) ? new SlotState(1) : null;

        // Launch a dedicated daemon thread that polls at POLL_HZ
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "KoalaLog-Gamepad");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });

        long periodUs = 1_000_000L / POLL_HZ;
        pollTask = scheduler.scheduleWithFixedDelay(
            KoalaGamepadLogger::pollOnce,
            0, periodUs, TimeUnit.MICROSECONDS
        );
    }

    /**
     * Stop polling and release all resources.
     * Called automatically by {@code KoalaLog.stop()} — you rarely need to call this directly.
     */
    public static void stop() {
        if (pollTask  != null) { pollTask.cancel(false);  pollTask  = null; }
        if (scheduler != null) { scheduler.shutdownNow(); scheduler = null; }

        // Clear references so gamepads can be GC'd between OpModes
        gamepad1 = null;
        gamepad2 = null;
        slot0    = null;
        slot1    = null;
    }

    /**
     * Returns {@code true} if the polling thread is currently running.
     */
    public static boolean isRunning() {
        return pollTask != null && !pollTask.isDone();
    }

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    /** Single poll tick — runs on the background thread. */
    private static void pollOnce() {
        try {
            if (slot0 != null && gamepad1 != null) sendGamepad(slot0, gamepad1);
            if (slot1 != null && gamepad2 != null) sendGamepad(slot1, gamepad2);
        } catch (Exception ignored) {
            // Never let an exception kill the scheduler thread
        }
    }

    private static void sendGamepad(SlotState state, Gamepad gp) {
        // axes
        float[] axes = sampleAxes(gp);
        if (!Arrays.equals(axes, state.prevAxes)) {
            KoalaLog.log(state.keyAxes(), axes, true);
            state.prevAxes = axes.clone();
        }

        // buttons
        boolean[] buttons = sampleButtons(gp);
        if (!Arrays.equals(buttons, state.prevButtons)) {
            KoalaLog.log(state.keyButtons(), buttons, true);
            state.prevButtons = buttons.clone();
        }

        // povs (dpad → hat angle)
        long[] povs = new long[]{ dpadAngle(gp) };
        if (!Arrays.equals(povs, state.prevPovs)) {
            KoalaLog.log(state.keyPovs(), povs, true);
            state.prevPovs = povs.clone();
        }
    }

    // -----------------------------------------------------------------------
    // Sampling helpers
    // -----------------------------------------------------------------------

    private static float[] sampleAxes(Gamepad gp) {
        float[] a = new float[AXIS_COUNT];
        a[AXIS_LEFT_X]     =  gp.left_stick_x;
        a[AXIS_LEFT_Y]     = -gp.left_stick_y;   // FTC Y is inverted vs WPILib convention
        a[AXIS_RIGHT_X]    =  gp.right_stick_x;
        a[AXIS_RIGHT_Y]    = -gp.right_stick_y;
        a[AXIS_LEFT_TRIG]  =  gp.left_trigger;
        a[AXIS_RIGHT_TRIG] =  gp.right_trigger;
        return a;
    }

    private static boolean[] sampleButtons(Gamepad gp) {
        boolean[] b = new boolean[BUTTON_COUNT];
        b[BTN_A]           = gp.a;
        b[BTN_B]           = gp.b;
        b[BTN_X]           = gp.x;
        b[BTN_Y]           = gp.y;
        b[BTN_LEFT_BMP]    = gp.left_bumper;
        b[BTN_RIGHT_BMP]   = gp.right_bumper;
        b[BTN_LEFT_STICK]  = gp.left_stick_button;
        b[BTN_RIGHT_STICK] = gp.right_stick_button;
        b[BTN_START]       = gp.start;
        b[BTN_BACK]        = gp.back;
        b[BTN_GUIDE]       = gp.guide;
        return b;
    }

    /**
     * Converts dpad booleans → compass angle (0 = up, clockwise), -1 = centred.
     * Matches WPILib's POV hat convention exactly.
     */
    private static long dpadAngle(Gamepad gp) {
        boolean u = gp.dpad_up,   d = gp.dpad_down;
        boolean l = gp.dpad_left, r = gp.dpad_right;

        if ( u && !d && !l && !r) return   0;
        if ( u && !d && !l &&  r) return  45;
        if (!u && !d && !l &&  r) return  90;
        if ( d && !u && !l &&  r) return 135;
        if ( d && !u && !l && !r) return 180;
        if ( d && !u &&  l && !r) return 225;
        if (!u && !d &&  l && !r) return 270;
        if ( u && !d &&  l && !r) return 315;
        return -1;
    }
}
