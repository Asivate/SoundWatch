package edu.washington.cs.soundwatch.wear.models;

public class AudioLabel {
    public String label;
    public double confidence;
    public String time;
    public String db;
    public String recordTime;

    public AudioLabel(String label, String confidence, String time, String db, String recordTime) {
        this.label = label;
        this.confidence = Double.parseDouble(confidence);
        this.time = time;
        this.db = db;
        this.recordTime = recordTime;
    }

    public String getLabel() {
        return label;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getTime() {
        return time;
    }

    public String getDb() {
        return db;
    }
}
