package com.example.tuoicay;

public class HouseHistoryModel {
    private String id;
    private String startTime;
    private String duration;
    private String mode;
    private String status;
    private String endtime;

    public HouseHistoryModel() {
    }

    public HouseHistoryModel(String id,String startTime, String duration, String mode, String status, String endtime) {
        this.id = id;
        this.startTime = startTime;
        this.duration = duration;
        this.mode = mode;
        this.status = status;
        this.endtime = endtime;
    }


    public String getStartTime() {
        return startTime;
    }

    public String getDuration() {
        return duration;
    }

    public String getMode() {
        return mode;
    }

    public String getStatus() {
        return status;
    }

    public String getEndtime() {
        return endtime;
    }
}
