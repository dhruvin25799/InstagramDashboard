package com.example.instagramdashboard;

public class RankInfo {
    private String username,rank,index;

    public RankInfo(String username, String rank, String index) {
        this.username = username;
        this.rank = rank;
        this.index=index;
    }
    String getUsername(){
        return username;
    }
    String getRank(){
        return rank;
    }
    String getIndex(){
        return index;
    }
}
