package com.example.tuoicay;

import java.util.List;

public class HistoryModel {
    private List<String> zones;
    private String startTime;
    private String endTime;
    private String duration;
    private List<String> repeatDays;
    private String status;
    private String timestamp;
    private String type;

    // Constructor rỗng (Firebase yêu cầu)
    public HistoryModel() {}

    // Constructor đầy đủ
    public HistoryModel(List<String> zones, String startTime,String endTime, String duration, List<String> repeatDays,
                        String status, String timestamp, String type) {
        this.zones = zones;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.repeatDays = repeatDays;
        this.status = status;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getter và Setter
    public List<String> getZones() {
        return zones;
    }

    public void setZones(List<String> zone) {
        this.zones = zone;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime(){
        return endTime;
    }

    public void setEndTime(String endTime){
        this.endTime = endTime;
    }
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<String> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(List<String> repeatDays) {
        this.repeatDays = repeatDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
