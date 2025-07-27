package com.example.tuoicay;

import java.util.List;

public class HouseHistoryGroup {
    private String date;
    private List<HouseHistoryModel> historyList;

    public HouseHistoryGroup(String date, List<HouseHistoryModel> historyList) {
        this.date = date;
        this.historyList = historyList;
    }

    public String getDate() {
        return date;
    }

    public List<HouseHistoryModel> getHistoryList() {
        return historyList;
    }
}
