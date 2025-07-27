package com.example.tuoicay;

import java.util.List;

public class ScheduleHouseModel {
    private String id;
    private String startTime;
    private String duration;
    private List<String> repeatDays;
    private String status;

    public ScheduleHouseModel() {}

    public ScheduleHouseModel(String startTime, String duration, List<String> repeatDays, String status) {
        this.startTime = startTime;
        this.duration = duration;
        this.repeatDays = repeatDays;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStartTime() { return startTime; }
    public String getDuration() { return duration; }
    public List<String> getRepeatDays() { return repeatDays; }
    public String getStatus() { return status; }

    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setRepeatDays(List<String> repeatDays) { this.repeatDays = repeatDays; }
    public void setStatus(String status) { this.status = status; }
}
