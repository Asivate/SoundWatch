package edu.washington.cs.soundwatch.models;

public class SoundNotification {
    public String label;
    public boolean isEnabled;
    public boolean isSnoozed;

    public SoundNotification(String label, boolean isEnabled, boolean isSnoozed) {
        this.label = label;
        this.isEnabled = isEnabled;
        this.isSnoozed = isSnoozed;
    }

    public String getSound() {
        return label;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public String toString() {
        return "SoundNotification{" +
                "label='" + label + '\'' +
                ", isEnabled=" + isEnabled +
                ", isSnoozed=" + isSnoozed +
                '}';
    }
}
