package saarland.cispa.artist.artistgui.system;

import java.io.File;
import java.util.List;

import saarland.cispa.artist.artistgui.database.Package;
import saarland.cispa.artist.artistgui.instrumentation.config.ArtistRunConfig;

public class FrameworkPackages {

    private static Package getPackage(String filePath, String appName, int appIconId) {
        Package mPackage = new Package(filePath);
        mPackage.appName = appName;
        mPackage.appIconId = appIconId >= 0?appIconId:android.R.mipmap.sym_def_app_icon;
        return mPackage;
    }

    private static Package getPackage(String filePath, String appName) {
        return getPackage(filePath, appName, -1);
    }

    private static Package getPackage(String filePath) {
        return getPackage(filePath, new File(filePath).getName(), -1);
    }

    private static final String SERVICES_JAR = "/system/framework/services.jar";
    private static final String BOOT_OAT = "/system/framework/framework.jar";

    public static void addJarsToPackageList(List<Package> packageList) {
        packageList.add(getPackage(SERVICES_JAR, "systemserver"));
        // packageList.add(getPackage(BOOT_OAT, "boot.oat")); // TODO: prevent removal of instrumented boot.oat at reboot
    }

    
    public static String[] getFiles(String app_package_name) {
        switch (app_package_name) {
            case BOOT_OAT:
                String bootclasspath = System.getenv("BOOTCLASSPATH");
                if (bootclasspath == null) {
                    return new String[]{"/system/framework/framework.jar", "/system/framework/core-oj.jar", "/system/framework/core-libart.jar", "/system/framework/conscrypt.jar", "/system/framework/okhttp.jar", "/system/framework/core-junit.jar", "/system/framework/bouncycastle.jar", "/system/framework/ext.jar", "/system/framework/telephony-common.jar", "/system/framework/voip-common.jar", "/system/framework/ims-common.jar", "/system/framework/apache-xml.jar", "/system/framework/org.apache.http.legacy.boot.jar", "/system/framework/telephony-ext.jar"};
                }
                else {
                    return bootclasspath.split(":");
                }
            default:
                return new String[]{app_package_name};
        }
    }

    public static String getOatFile(String app_package_name, ArtistRunConfig artistConfig) {
        if (!app_package_name.startsWith("/")) {
            return null;
        }
        switch (app_package_name) {
            case BOOT_OAT:
                return artistConfig.app_oat_folder_path
                        + artistConfig.app_oat_architecture
                        + File.separator + "system@framework@boot.oat";
            default:
                return artistConfig.app_oat_folder_path
                        + artistConfig.app_oat_architecture
                        + File.separator + getFiles(artistConfig.app_package_name)[0].replace(File.separator, "@").substring(1) + "@classes.dex";
        }
    }

    public static String getAdditionalCmdlineParams(String app_package_name) {
        String cmd_dex2oat_compile = "";
        if (!app_package_name.startsWith("/")) {
            return cmd_dex2oat_compile;
        }
        switch (app_package_name){
            case SERVICES_JAR:
                cmd_dex2oat_compile += " --runtime-arg -Xmx512m";
                cmd_dex2oat_compile += " --runtime-arg -Xms64m";
                String systemserverclasspath = System.getenv("SYSTEMSERVERCLASSPATH");
                if (systemserverclasspath != null) {
                    int offset = systemserverclasspath.indexOf("/system/framework/services.jar");
                    if (offset > 0) {
                        systemserverclasspath = systemserverclasspath.substring(0, offset-1);
                        cmd_dex2oat_compile += " --runtime-arg -classpath";
                        cmd_dex2oat_compile += " --runtime-arg " + systemserverclasspath;
                    }
                }
                break;
            case BOOT_OAT:
                cmd_dex2oat_compile += " --image=/data/dalvik-cache/arm/system@framework@boot.art";
                cmd_dex2oat_compile += " --base=0x70000000";
                cmd_dex2oat_compile += " --instruction-set=arm --instruction-set-features=smp,div,atomic_ldrd_strd --instruction-set-variant=cortex-a53 --instruction-set-features=default";
                cmd_dex2oat_compile += " --runtime-arg -Xms64m --runtime-arg -Xmx64m --image-classes=/system/etc/preloaded-classes --compiled-classes=/system/etc/compiled-classes";
                break;
            default:
                break;
        }
        return cmd_dex2oat_compile;
    }

    public static boolean shouldReboot(String packageName) {
        switch (packageName){
            case SERVICES_JAR:
                return true;
            default:
                return false;
        }
    }
}
