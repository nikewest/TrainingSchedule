package ru.alexfitness.trainingschedule.util;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.activity.AuthenticationActivity;

public class AFPreferenceActivity extends PreferenceActivity {

    private int authEndTimeOut;
    private boolean enableAuthEndTimeOut = true;
    private Handler authHandler = new Handler();
    private Runnable authEnder = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(AFPreferenceActivity.this, AuthenticationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };
    private AFApplication app;

    public void setEnableAuthEndTimeOut(boolean value){
        enableAuthEndTimeOut = value;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (AFApplication) getApplication();
        app.setPauseTimeStamp(SystemClock.elapsedRealtime());
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
            resetHandler();
            checkPauseAuthEnd();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNfcScan();
        if(enableAuthEndTimeOut) {
            stopHandler();
            setPauseAuthEnd();
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if(enableAuthEndTimeOut) {
            resetHandler();
        }
    }

    private void checkPauseAuthEnd(){
        if((SystemClock.elapsedRealtime() - app.getPauseTimeStamp()) > authEndTimeOut){
            endAuth();
        }
    }

    private void setPauseAuthEnd(){
        app.setPauseTimeStamp(SystemClock.elapsedRealtime());
    }

    private void endAuth(){
        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
}
