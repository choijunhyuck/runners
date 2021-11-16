package com.runners.choi.runners.record;

public class RecordItem {

    private String repeat;
    private String point;
    private String time;
    private String distance;
    private String bonus;
    private String speed;
    private String date;

    public RecordItem(String repeat, String point, String time, String distance, String bonus, String speed, String date) {

        super();

        this.repeat = repeat;
        this.point = point;
        this.time = time;
        this.distance = distance;
        this.bonus = bonus;
        this.speed = speed;
        this.date = date;
    }

    public String getRepeat() {
        return repeat;
    }

    public String getPoint() {
        return point;
    }

    public String getTime() {
        return time;
    }

    public String getDistance() {
        return distance;
    }

    public String getBonus() {
        return bonus;
    }

    public String getSpeed() {
        return speed;
    }

    public String getDate() {
        return date;
    }

}