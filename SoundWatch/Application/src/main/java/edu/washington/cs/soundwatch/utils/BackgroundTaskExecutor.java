package edu.washington.cs.soundwatch.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BackgroundTaskExecutor {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public abstract void execute();

    public static void executeTask(Runnable backgroundTask) {
        executor.execute(backgroundTask);
    }

    public static void executeWithCallback(Runnable backgroundTask, Runnable completionCallback) {
        executor.execute(() -> {
            backgroundTask.run();
            mainHandler.post(completionCallback);
        });
    }

    public static void shutdown() {
        executor.shutdown();
    }
}