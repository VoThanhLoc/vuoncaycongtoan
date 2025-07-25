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
}