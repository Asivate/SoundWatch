package edu.washington.cs.soundwatch.wear.application;

import android.app.Application;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainApplication extends Application {
    public ArrayList<String> enabledSounds = new ArrayList<>(Arrays.asList(
            "Fire/Smoke Alarm",
            "Speech",
            "Door In-Use",
            "Water Running",
            "Knocking",
            "Microwave",
            "Dog Bark",
            "Cat Meow",
            "Car Honk",
            "Vehicle",
            "Baby Cry"));
    private Set<Integer> blockedSounds = new HashSet<>();
    private boolean appInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        appInForeground = false;
    }

    public void addBlockedSounds(int soundId) {
        blockedSounds.add(soundId);
    }

    public void removeBlockedSounds(int soundId) {
        blockedSounds.remove(soundId);
    }

    public Set<Integer> getBlockedSounds() {
        return blockedSounds;
    }

    public void setAppInForeground(boolean inForeground) {
        appInForeground = inForeground;
    }

    public boolean isAppInForeground() {
        return appInForeground;
    }

    public void addEnabledSound(String sound) {
        if (!this.enabledSounds.contains(sound)) {
            this.enabledSounds.add(sound);
        }
    }

    public void removeEnabledSound(String sound) {
        if (this.enabledSounds.contains(sound)) {
            this.enabledSounds.remove(sound);
        }
    }

    public int getIntegerValueOfSound(String soundLabel) {
        return soundLabel.hashCode();
    }
}