package com.example.smartair.ui.models;

import java.util.List;

public class SymptomEntry {

    public String sleep;
    public String activity;
    public String cough;
    public List<String> triggers;
    public long timestamp;
    public String enteredBy;

    public SymptomEntry() {
    }

    public SymptomEntry(String sleep, String activity, String cough,
                        List<String> triggers, long timestamp, String enteredBy) {
        this.sleep = sleep;
        this.activity = activity;
        this.cough = cough;
        this.triggers = triggers;
        this.timestamp = timestamp;
        this.enteredBy = enteredBy;
    }
}
