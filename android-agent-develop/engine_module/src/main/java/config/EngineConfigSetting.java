package config;

import java.nio.charset.Charset;

/**
 * Created by Hyungu-PC on 2016-05-04.
 */
public class EngineConfigSetting {

    public static boolean isSoftEncoding = false;
    public static boolean isPC_Viewer = false;
    public static boolean isKnox = false;

    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    private static boolean isHCIBuild = false;
    private static boolean isSamsungPrinter = false;
    private static boolean isZidoo = false;

    public static void setIsHCIBuild(boolean set) {
        isHCIBuild = set;
    }

    public static boolean isHCIBuild() {
        return isHCIBuild;
    }

    public static boolean isSamsungPrinter() {
        return isSamsungPrinter;
    }

    public static void setIsSamsungPrinter(boolean isSamsungPrinter) {
        EngineConfigSetting.isSamsungPrinter = isSamsungPrinter;
    }

    public static void setIsKnox(boolean set) {
        isKnox = set;
    }

    public static void setIsZidoo(boolean set) {
        isZidoo = set;
    }

    public static boolean isZidoo() {
        return isZidoo;
    }
}
