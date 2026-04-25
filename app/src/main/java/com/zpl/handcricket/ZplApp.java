package com.zpl.handcricket;

import android.app.Application;

import com.zpl.handcricket.utils.AppState;

public class ZplApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppState.get().init(this);
    }
}
