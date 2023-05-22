package bgu.spl.net.objects;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UserData {
    private String username;
    private String password;
    private int day;
    private int month;
    private int year;
    private int numOfPosts;
    private boolean isOnline;


    public UserData(String username, String password, String date) {
        this.username = username;
        this.password = password;
        String[] birthday = date.split("-");
        this.day = Integer.parseInt(birthday[0]);
        this.month = Integer.parseInt(birthday[1]);
        this.year = Integer.parseInt(birthday[2]);
        this.numOfPosts = 0;
        this.isOnline = false;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getNumOfPosts() {
        return numOfPosts;
    }

    public void setNumOfPosts(int numOfPosts) {
        this.numOfPosts = numOfPosts;
    }

    public int getAge(){
        LocalDate birthday = LocalDate.of(getYear(), getMonth(), getDay());
        Period period = Period.between(birthday, LocalDate.now());
        return period.getYears();
    }
}
