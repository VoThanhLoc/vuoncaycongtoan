package com.example.tuoicay;

public class HouseHistoryItem {
    public String id;
    public String startTime;
    public String duration;
    public String mode;
    public String status;
    public String endtime;

    public HouseHistoryItem() {} // cần thiết cho Firebase

    public HouseHistoryItem(String id,String startTime, String duration, String mode, String status, String endtime) {
        this.id = id;
        this.startTime = startTime;
        this.duration = duration;
        this.mode = mode;
        this.status = status;
        this.endtime = endtime;
    }

}