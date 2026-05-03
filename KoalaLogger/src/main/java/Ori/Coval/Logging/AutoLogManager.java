package Ori.Coval.Logging;

import com.acmerobotics.dashboard.FtcDashboard;

import java.util.ArrayList;
import java.util.List;

public class AutoLogManager {
    private static final List<Logged> loggedClasses = new ArrayList<>();

    public static void register(Logged logged){
        loggedClasses.add(logged);
    }

    /** Records values from all registered fields. */
    public static void periodic() {
        for (int i = 0; i < loggedClasses.size(); i++) {
            loggedClasses.get(i).toLog();
        }

        ReflectionLogger.update();
    }

    static {
        try {
            Class.forName("Ori.Coval.AutoLog.AutoLogStaticRegistry");
        } catch (ClassNotFoundException e) {
            // no statics registered—ignore
        }
    }
}
