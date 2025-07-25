package com.example.tuoicay;

import java.util.List;

public class HistoryModel {
    public List<String> zone;
    public String startTime;
    public String duration;
    public String status;

    public HistoryModel() {}

    public HistoryModel(List<String> zone, String startTime, String duration, String status) {
        this.zone = zone;
        this.startTime = startTime;
        this.duration = duration;
        this.status = status;
    }
}