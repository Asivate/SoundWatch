package edu.washington.cs.soundwatch.wear.application;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import androidx.multidex.MultiDex;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.DefaultLifecycleObserver;
import edu.washington.cs.soundwatch.wear.BuildConfig;

public class SoundWatchApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable strict mode in debug builds
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }

        // Initialize lifecycle monitoring
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
        });

        // Initialize crash reporting
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                // Log crash to Firebase or your analytics platform
            });
        }
    }
}