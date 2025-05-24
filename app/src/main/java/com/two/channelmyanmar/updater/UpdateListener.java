package com.two.channelmyanmar.updater;

/*
 * Created by Toewaioo on 4/20/25
 * Description: [Add class description here]
 */
// UpdateListener.java
public interface UpdateListener {
    /** Called when a newer version is found */
    void onNewFound(UpdateInfo info);

    /** Called when the app is already up‑to‑date */
    void onNotFound();

    /** Called on any error (network, parsing, HTTP code ≠ 200) */
    void onError(Exception e);
}

