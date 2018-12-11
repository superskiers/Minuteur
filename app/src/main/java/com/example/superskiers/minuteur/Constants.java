package com.example.superskiers.minuteur;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Constants {

    public interface SHAREDPREFS {
        //Constants for SharedPreferences
        public static final String SHARED_PREFS = "sharedPrefs";
        public static final String START_TIME_MILLIS = "startTimeInMillis";
        public static final String MILLIS_LEFT = "millisLeft";
        public static final String TIMER_RUNNING = "timerRunning";
        public static final String END_TIME = "endTime";
    }
    public interface NOTIFICATION_ID {
        public static final String FOREGROUND_SERVICE = "channel1";
    }
    public interface ACTION {
        public static final String STARTFOREGROUND_ACTION = "com.example.superskiers.foregroundservices.action.startforeground";
        public static final String STOPFOREGROUND_ACTION = "com.example.superskiers.foregroundservices.action.stopforeground";

        int NOTIFICATION_TIMER_COUNTDOWN = 1;
        int NOTIFICATION_TIMER_EXPIRED = 2;

        String ACTION_DELETE_ALARM = "com.example.superskiers.minuteur.ACTION_DELETE";
        String ACTION_RESTART_ALARM = "com.example.superskiers.minuteur.ACTION_RESTART";

    }
}
