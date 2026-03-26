package Ori.Coval.Logging;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

import Ori.Coval.Logging.Logger.SingleCoreKoalaLog;

public class SupplierLog {
    public static BooleanSupplier wrap(String name, BooleanSupplier s, boolean postToFtcDashboard) {
        return () -> {
            boolean v = s.getAsBoolean();
            SingleCoreKoalaLog.log(name, v, postToFtcDashboard);
            return v;
        };
    }
    public static IntSupplier wrap(String name, IntSupplier s, boolean postToFtcDashboard) {
        return () -> {
            int v = s.getAsInt();
            SingleCoreKoalaLog.log(name, (long)v, postToFtcDashboard);
            return v;
        };
    }
    public static LongSupplier wrap(String name, LongSupplier s, boolean postToFtcDashboard) {
        return () -> {
            long v = s.getAsLong();
            SingleCoreKoalaLog.log(name, v, postToFtcDashboard);
            return v;
        };
    }
    public static DoubleSupplier wrap(String name, DoubleSupplier s, boolean postToFtcDashboard) {
        return () -> {
            double v = s.getAsDouble();
            SingleCoreKoalaLog.log(name, v, postToFtcDashboard);
            return v;
        };
    }
}
