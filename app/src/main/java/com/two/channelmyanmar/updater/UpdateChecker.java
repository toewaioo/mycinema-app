package com.two.channelmyanmar.updater;

/*
 * Created by Toewaioo on 4/20/25
 * Description: [Add class description here]
 */
// UpdateChecker.java
import android.os.Handler;
import android.os.Looper;

import com.two.channelmyanmar.preference.PreferenceHelper;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;

public class UpdateChecker {
    private static final long DAY_MILLIS = TimeUnit.DAYS.toMillis(1);

    private final String apiEndpoint;
    private final String currentVersion;
    private final PreferenceHelper prefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * @param apiEndpoint     e.g. "https://yourserver.com/update"
     * @param currentVersion  your app's VersionName (e.g. "1.0")
     * @param prefs           your helper for SharedPreferences
     */
    public UpdateChecker(String apiEndpoint,
                         String currentVersion,
                         PreferenceHelper prefs) {
        this.apiEndpoint    = apiEndpoint;
        this.currentVersion = currentVersion;
        this.prefs          = prefs;
    }

    /** Starts the update check. */
    public void checkForUpdate(UpdateListener listener) {
        long lastCheck = prefs.getLastUpdateCheckTime();
        if (System.currentTimeMillis() - lastCheck < DAY_MILLIS) {
            // Already checked in the last 24h, skip
            listener.onNotFound();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(apiEndpoint);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.connect();

                int code = conn.getResponseCode();
                if (code != 200) {
                    throw new Exception("HTTP error code: " + code);
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                String remoteVer = json.getString("version");

                if (isNewer(remoteVer, currentVersion)) {
                    // Save API key from response
                    boolean needApi = json.getBoolean("needApi");
                    prefs.useCrossPlatform(needApi);
                    String apiKey = json.getString("apiKey");
                    prefs.setApiKey(apiKey);

                    // Build UpdateInfo and dispatch on main thread
                    UpdateInfo info = new UpdateInfo(
                            json.getString("whatNew"),
                            remoteVer,
                            json.getString("downloadLink"),
                            apiKey,
                            json.getString("message"),
                            json.getBoolean("block")
                    );
                    mainHandler.post(() -> listener.onNewFound(info));

                } else {
                    boolean needApi = json.getBoolean("needApi");
                    prefs.useCrossPlatform(needApi);
                    String apiKey = json.getString("apiKey");
                    prefs.setApiKey(apiKey);
                    // No update—record the check time
                    prefs.setLastUpdateCheckTime(System.currentTimeMillis());
                    mainHandler.post(listener::onNotFound);
                }
            } catch (Exception e) {
                mainHandler.post(() -> listener.onError(e));
            }
        }).start();
    }

    /** Simple semver‑style comparison (“1.2.3” > “1.2.2”) */
    private boolean isNewer(String remote, String local) {
        String[] r = remote.split("\\."), l = local.split("\\.");
        int len = Math.max(r.length, l.length);
        for (int i = 0; i < len; i++) {
            int rv = i < r.length ? Integer.parseInt(r[i]) : 0;
            int lv = i < l.length ? Integer.parseInt(l[i]) : 0;
            if (rv != lv) return rv > lv;
        }
        return false;
    }
}
