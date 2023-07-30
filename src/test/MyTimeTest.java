package test;

import com.gpxmanager.MyTime;

public class MyTimeTest {

    public static void main(String[] args) {
        MyTime myTime = MyTime.fromSeconds(23 * 3600 + 59 * 60);
        System.out.println(myTime);
        myTime = MyTime.fromSeconds(48 * 3600 + 59 * 60 + 10);
        System.out.println(myTime);
    }
}
