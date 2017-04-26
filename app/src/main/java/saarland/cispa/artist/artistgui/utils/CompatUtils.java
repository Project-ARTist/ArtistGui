package saarland.cispa.artist.artistgui.utils;

import android.os.Build;


/**
 * @author Oliver Schranz (oliver.schranz@cispa.saarland)
 */

public class CompatUtils {

    private static final int[] supportedSdks = {Build.VERSION_CODES.M, Build.VERSION_CODES.N, Build.VERSION_CODES.N_MR1};

    public static boolean supportedByArtist() {
        final int currentSdk = Build.VERSION.SDK_INT;
        for (int sdk : supportedSdks) {
            if (sdk == currentSdk) {
                return true;
            }
        }
        return false;
    }
}
