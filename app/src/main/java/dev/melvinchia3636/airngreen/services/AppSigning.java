package dev.melvinchia3636.airngreen.services;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Headers that let an "Android apps" restricted Google API key authorise a
 * REST call made directly by this app.
 */
public final class AppSigning {

    private AppSigning() {
    }

    public static Map<String, String> googleKeyHeaders(Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Android-Package", context.getPackageName());
        String certificate = certificateSha1(context);
        if (certificate != null) {
            headers.put("X-Android-Cert", certificate);
        }
        return headers;
    }

    private static String certificateSha1(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info;
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info = manager.getPackageInfo(context.getPackageName(),
                        PackageManager.GET_SIGNING_CERTIFICATES);
                signatures = info.signingInfo == null
                        ? new Signature[0]
                        : info.signingInfo.getApkContentsSigners();
            } else {
                info = manager.getPackageInfo(context.getPackageName(),
                        PackageManager.GET_SIGNATURES);
                signatures = info.signatures;
            }
            if (signatures == null || signatures.length == 0) {
                return null;
            }

            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest(signatures[0].toByteArray());
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format("%02X", value));
            }
            return hex.toString();
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException failure) {
            return null;
        }
    }
}
