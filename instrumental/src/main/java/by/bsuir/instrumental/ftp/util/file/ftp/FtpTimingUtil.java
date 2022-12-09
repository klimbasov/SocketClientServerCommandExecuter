package by.bsuir.instrumental.ftp.util.file.ftp;

import static java.lang.Math.*;

public class FtpTimingUtil {
    private static long MAX_DELAY_VAL = 5000;
    private static int COF = 5;
    private static long OFFSET = 800;

    public static long getDelayFuncVal(long x){
//        long val = OFFSET + x * COF;
        long val = OFFSET + round(exp(x) * COF);
        return Math.min(val, MAX_DELAY_VAL);
    }
}
