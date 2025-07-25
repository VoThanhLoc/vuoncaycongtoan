package com.example.tuoicay;

import java.util.List;

public class ScheduleModel {
    public String id;
    public List<String> zone;
    public String startTime;
    public String duration;
    public List<String> repeatDays;
    public String status;

    public ScheduleModel() {} // Firebase cần constructor rỗng

    public ScheduleModel(String id, List<String> zone, String startTime, String duration, List<String> repeatDays, String status) {
        this.id = id;
        this.zone = zone;
        this.startTime = startTime;
        this.duration = duration;
        this.repeatDays = repeatDays;
        this.status = status;
    }

    public List<String> getZone() {
        return zone;
    }

    public void setZone(List<String> zone) {
        this.zone = zone;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
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
}