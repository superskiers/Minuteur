package com.example.superskiers.minuteur;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import static com.example.superskiers.minuteur.Constants.SHAREDPREFS.END_TIME;
import static com.example.superskiers.minuteur.Constants.SHAREDPREFS.MILLIS_LEFT;
import static com.example.superskiers.minuteur.Constants.SHAREDPREFS.SHARED_PREFS;
import static com.example.superskiers.minuteur.Constants.SHAREDPREFS.START_TIME_MILLIS;
import static com.example.superskiers.minuteur.Constants.SHAREDPREFS.TIMER_RUNNING;
public class MainActivity extends AppCompatActivity {

    //Member variables
    private NumberPicker mNumberPicker;
    private TextView mTextViewCountDown;
    private Button mButtonStartPause;
    private Button mButtonCancel;
    private ProgressBar mProgressBar;

    private MediaPlayer mMediaPlayer;

    private CountDownTimer mCountDownTimer;
    private Chronometer mChronometer;

    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    public long getmStartTimeInMillis = mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;

    //Create member constants for the Notification ID
    private static final int NOTIFICATION_ID = 0;
    //Notification channel ID
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize NotificationManager
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
//        //Set up the Notification Broadcast Intent
//        Intent notifyIntent = new Intent(this, AlarmReceiver.class);

        //Create a notification channel for Oreo and higher
        createNotificationChannel();


        //Hide the ActionBar
        getSupportActionBar().hide();
        //Assign variables
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonCancel = findViewById(R.id.button_cancel);
        mNumberPicker = findViewById(R.id.number_picker);
        mProgressBar = findViewById(R.id.progress_bar_circle);
        mChronometer = findViewById(R.id.chronometer);
        //Set visibility on ProgressBarCircle
        mProgressBar.setVisibility(View.INVISIBLE);
        //Set visibility and format for Chronometer
        mChronometer.setVisibility(View.INVISIBLE);
        mChronometer.setFormat("-" + "%s");
        //Set Max & Min Values on NumberPicker
        mNumberPicker.setMaxValue(10);
        mNumberPicker.setMinValue(0);
        //Set Price is Right wheel on NumberPicker
        mNumberPicker.setWrapSelectorWheel(true);
        //Set OnValueChange for NumberPicker
        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                mTimerRunning = false;
                int numberInput = mNumberPicker.getValue();


                //FOR TESTING PURPOSES USE WRONG MILLI CONVERSION
                long millisInput = (long) numberInput * 2000;
                setTime(millisInput);
            }
        });
        //onClick for Start/Pause Button
        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimerRunning) {
                    pauseTimer();
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    startTimer();
                    mButtonCancel.setVisibility(View.VISIBLE);
                    mNumberPicker.setVisibility(View.INVISIBLE);
                    mChronometer.setVisibility(View.INVISIBLE);
                }
            }
        });
        //onClick for Cancel Button
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimerRunning) {
                    mCountDownTimer.cancel();
                }
                cancelTimer();
                cancelRingtone();
                cancelVisibility();
            }
        });
    }

    //cancelVisibility puts all the different visibilities in one method
    //when cancel button is pressed
    private void cancelVisibility() {
        mNumberPicker.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mTextViewCountDown.setVisibility(View.VISIBLE);
        mChronometer.setVisibility(View.INVISIBLE);
        mChronometer.stop();
        mButtonStartPause.setText("START");
    }

    //This method sets the time selected
    private void setTime(long milliSeconds) {
        mStartTimeInMillis = milliSeconds;
        cancelTimer();
    }

    //This is where we initialize the CountDownTimer
    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Set the ProgressBar to update with each tick
                mProgressBar.setProgress((int) millisUntilFinished / 1000);
                mTimeLeftInMillis = millisUntilFinished;
                //Update TextView for each tick
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                //alarmForTimesUp();
                startChronometer();
                updateTimerInterface();
                mChronometer.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                // startAlarm();
            }
        }.start();
        mTimerRunning = true;
        mChronometer.setVisibility(View.INVISIBLE);
        updateTimerInterface();
    }

    //This method will pause the CountDownTimer
    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateTimerInterface();
    }

    //This method is for the Cancel Button
    public void cancelTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        stopChronometer();
        //cancelAlarm();

    }

    //This method will update the TextView of current count
    private void updateCountDownText() {
        //Divide seconds by 3600 as that is the amount of milliseconds in
        //one hour.
        //Once minutes go above 60 it goes into hours which is why we use the
        //modulus operator.
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        //timeLeftFormatted is for the mTextViewCountDown
        String timeLeftFormatted;
        timeLeftFormatted = String.format(Locale.getDefault(),
                "%d:%02d:%02d", hours, minutes, seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
    }

    //This method sets the ticks/intervals with which the ProgressBar Cirlce will take place
    private void setProgressBarCircle() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mProgressBar.setMax((int) mTimeLeftInMillis / 1000);
        mProgressBar.setProgress((int) mTimeLeftInMillis / 1000);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    //startChronometer will initialize the chronometer when the countdowntimer finishes
    private void startChronometer() {
        if (!mTimerRunning) {
            alarmForTimesUp();
            mTextViewCountDown.setVisibility(View.INVISIBLE);
            mButtonStartPause.setVisibility(View.INVISIBLE);
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.setTextColor(getResources().getColor(R.color.red));
            mChronometer.start();
            deliverNotification(this);

        }
    }

    //stopChronometer will stop the chronometer
    private void stopChronometer() {
        mChronometer.setVisibility(View.INVISIBLE);
        mButtonStartPause.setVisibility(View.VISIBLE);
        mChronometer.stop();
    }

    //This method updates the UI based on user selection and the current state of the app
    private void updateTimerInterface() {
        if (mTimerRunning) {
            setProgressBarCircle();
            mButtonStartPause.setText("Pause");
        } else {
            mButtonStartPause.setText("Start");
        }
    }

    //Creates a Notification channel, for OREO and higher.
    public void createNotificationChannel() {
        // Create a notification manager object.
        mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            "Time's Up",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Notification starts when timer ends");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    //deliverNotification will complete the notification along with attributes
    private void deliverNotification(Context context) {

        long myChronometer = mChronometer.getBase();
        String chronoToText = Long.toString(myChronometer);

        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (context, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle("Times Up!")
                .setContentText(chronoToText)
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTimeoutAfter(3000)
                .setUsesChronometer(true)
                .addAction(R.drawable.ic_timer, "dismiss", contentPendingIntent);

        //Use NotificationManager to deliver the notification
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    //This method sets the Ringtone/Alarm for when timer finishes
    private void alarmForTimesUp() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        //Create a MediaPlayer to control the output of ringtone/notification
        mMediaPlayer = MediaPlayer.create(this, alarmUri);
        mMediaPlayer.start();
        vibrateDevice();
    }

    //Use onStop to stop the ringtone/notification from playing when starting a new activity
    @Override
    public void onStop() {
        super.onStop();
        mMediaPlayer.pause();
    }

    //CancelRingtone will stop the ringtone/notification sound
    public void cancelRingtone() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.stop();
    }

    //Created a separate method just to manage the device vibrating on timer completion
    private void vibrateDevice() {
        //This intent is used to vibrate the phone when the alarm is finished
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (mStartTimeInMillis * 1000), pendingIntent);
    }
}