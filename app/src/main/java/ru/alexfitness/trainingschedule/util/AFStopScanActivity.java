package ru.alexfitness.trainingschedule.util;

import android.app.Activity;

import android.app.PendingIntent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.activity.AuthenticationActivity;

public class AFStopScanActivity extends Activity {

    //private final long CLOSE_TIMEOUT = 30 * 1000;
    //private AtomicBoolean timeExpired = new AtomicBoolean(false);
    private Thread closeTimer = null;

    private int authEndTimeOut;
    private boolean enableAuthEndTimeOut = true;

    //private Handler userInteractionHandler = new Handler();
    private Handler authHandler = new Handler();

    private Runnable authEnder = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(AFStopScanActivity.this, AuthenticationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            if(wakeLock!=null){
                if(wakeLock.isHeld()){
                    wakeLock.release();
                }
            }
        }
    };

    PowerManager.WakeLock wakeLock;

    public void setEnableAuthEndTimeOut(boolean value){
        enableAuthEndTimeOut = value;
    }

    /*
    public void stopTimer(){
        Log.i("AUTH_END", "STOP TIMER" + this.getClass().getName());
        if(closeTimer!=null){
            if(closeTimer.isAlive()){
                closeTimer.interrupt();
                try {
                    closeTimer.join();
                } catch (InterruptedException e) {
                    Log.e(null, e.toString());
                }
            }
            closeTimer = null;
        }
        if(wakeLock!=null && wakeLock.isHeld()){
            wakeLock.release();
            wakeLock = null;
        }
    }*/

    /*public void startTimer(){
        Log.i("AUTH_END", "START TIMER" + this.getClass().getName() + " " +  Calendar.getInstance().getTime().toString());
        closeTimer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(authEndTimeOut);
                    timeExpired.set(true);
                    Log.i("AUTH_END", "Background timer have expired!");
                    if(wakeLock!=null && wakeLock.isHeld()){
                        wakeLock.release();
                        Log.i("AUTH_END", "wake lock realease");
                    }
                } catch (InterruptedException e) {
                    Log.e("AUTH_END_TIMER", e.toString());
                }
            }
        };
        closeTimer.start();
        PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getLocalClassName());
        wakeLock.acquire(120000);
        Log.i("AUTH_END", "wake lock acquire");
    }*/

    private void startTimer(){
        authHandler.removeCallbacks(authEnder);
        authHandler.postDelayed(authEnder, authEndTimeOut);

        PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getLocalClassName());
            wakeLock.acquire(authEndTimeOut + 60 * 1000);
        }
    }

    private void stopTimer(){
        authHandler.removeCallbacks(authEnder);
        if(wakeLock!=null){
            if(wakeLock.isHeld()){
                wakeLock.release();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            authEndTimeOut = Integer.parseInt(sp.getString(getString(R.string.pref_auth_timeout_key), "30")) * 1000;
            Log.i("AUTH_END", String.valueOf(authEndTimeOut));
        } catch (Exception ex){
            setEnableAuthEndTimeOut(false);
            ex.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startNfcScan();
        if(enableAuthEndTimeOut) {
            stopTimer();
            resetHandler();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNfcScan();
        if(enableAuthEndTimeOut) {
            stopHandler();
            startTimer();
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if(enableAuthEndTimeOut) {
            resetHandler();
        }
    }

    public void startNfcScan() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    public void stopNfcScan(){
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void stopHandler(){
        authHandler.removeCallbacks(authEnder);
    }

    private void resetHandler(){
        authHandler.removeCallbacks(authEnder);
        authHandler.postDelayed(authEnder, authEndTimeOut);
    }

    @Override
    protected void onDestroy() {
        if(enableAuthEndTimeOut){
            stopTimer();
        }
        super.onDestroy();
    }
}
