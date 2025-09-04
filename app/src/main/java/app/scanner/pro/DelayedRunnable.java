package app.scanner.pro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Button;

import app.scanner.pro.MainActivity;
import app.scanner.pro.R;

public class DelayedRunnable{

    SharedPreferences sp;
    private CameraScanActivity activity;
    private Runnable capturer;
    private Runnable countdowner;
    private boolean isCapturing, isPaused, isDelayModified;
    private final Handler captureHandler = new Handler();
    private int s = 1000;
    private int countdown;
    private int delay;

    private Button timeBtn, captureBtn;

    DelayedRunnable(CameraScanActivity activity){
        this.activity = activity;
        timeBtn = find(R.id.time_btn);
        captureBtn = activity.captureBtn;

        capturer = this::continuousTimedCapture;
        countdowner = this::timerCountdown;

        timeBtn.setOnClickListener(v -> toggleTimedCapture());
        activity.textureView.setOnClickListener(v -> togglePause());
        captureBtn.setOnClickListener(v -> {
            safelyPause();
            activity.takePicture();
        });

        find(R.id.decrease_time_btn).setOnClickListener(v -> changeDelay(--delay));
        find(R.id.increase_time_btn).setOnClickListener(v -> changeDelay(++delay));
        find(R.id.view_btn).setOnClickListener(v -> activity.startActivity(new Intent(activity, ImageViewer.class)));
        sp = activity.getSharedPreferences("settings", 0);
        delay = sp.getInt("delay", 12);
        timeBtn.setText("" + delay + "s");
    }

    void changeDelay(int i) {
        if (i < 5) {delay = 5; return;}
        timeBtn.setText("" + i + "s");
        if (!isDelayModified) isDelayModified = true;
    }

    void toggleTimedCapture() {
        if (isCapturing) stop();
        else {
            countdown = delay;
            resume();
        }
    }

    void togglePause() {
        if (isCapturing) {
            if (isPaused) resume();
            else pause();
        }
    }

    private Button find(int id) {
        return activity.findViewById(id);
    }

    private void continuousTimedCapture() {
        countdown = delay;
        activity.takePicture();
        captureHandler.postDelayed(capturer, delay*s);
    }

    private void timerCountdown() {
        captureBtn.setText("Capture in: " + countdown-- + "s");
        captureHandler.postDelayed(countdowner, s);
    }

    private void pause() {
        removeCallbacks();
        timeBtn.setBackgroundColor(0xffccccff);
        isPaused = true;
    }

    public void safelyPause() {
        if (isCapturing && !isPaused) pause();
        if (isDelayModified) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("delay", delay);
            editor.apply();
            isDelayModified = false;
        }
    }

    public void safelyResume() {
        if (isCapturing && isPaused) resume();
    }

    private void resume() {
        captureHandler.postDelayed(capturer, countdown*s);
        timeBtn.setBackgroundColor(0xffff9800);
        isPaused = false;
        isCapturing = true;
        timerCountdown();
    }

    private void stop() {
        removeCallbacks();
        timeBtn.setBackgroundColor(0xffeeeeee);
        captureBtn.setText("Capture");
        isCapturing = false;
    }

    private void removeCallbacks() {
        captureHandler.removeCallbacks(capturer);
        captureHandler.removeCallbacks(countdowner);
    }

}
