package Ori.Coval.Logging.Logger;

import com.acmerobotics.dashboard.FtcDashboard;

import java.util.ArrayList;
import java.util.List;

import Ori.Coval.Logging.Logged;
import Ori.Coval.Logging.ReflectionLogger;

public class AutoLogManager {
    static final List<Logged> loggedClasses = new ArrayList<>();

    public static void register(Logged logged){
        loggedClasses.add(logged);
    }

    /** Records values from all registered fields. */
    static void periodic() {
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
