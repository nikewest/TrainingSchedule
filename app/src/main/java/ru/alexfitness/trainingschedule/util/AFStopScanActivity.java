package ru.alexfitness.trainingschedule.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import ru.alexfitness.trainingschedule.activity.AuthenticationActivity;

public class AFStopScanActivity extends Activity {

    private final long CLOSE_TIMEOUT = 30 * 1000;
    private AtomicBoolean timeExpired = new AtomicBoolean(false);
    private Thread closeTimer = null;

    private Handler userInteractionHandler = new Handler();
    private Runnable goToAuthHandler = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(AFStopScanActivity.this, AuthenticationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    public void stopTimer(){
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
    }

    @Override
    public void startActivity(Intent intent) {
        stopTimer();
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        stopTimer();
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startNfcScan();
        stopTimer();
        resetHandler();
        if(timeExpired.get()){
            Intent intent = new Intent(AFStopScanActivity.this, AuthenticationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNfcScan();
        stopHandler();
        closeTimer = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(CLOSE_TIMEOUT);
                    timeExpired.set(true);
                } catch (InterruptedException e) {
                    Log.e(null, e.toString());
                }
            }
        };
        closeTimer.start();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetHandler();
    }

    private void startNfcScan() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0, new Intent(this, this.getClass()), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void stopNfcScan(){
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void stopHandler(){
        userInteractionHandler.removeCallbacks(goToAuthHandler);
    }

    private void resetHandler(){
        userInteractionHandler.removeCallbacks(goToAuthHandler);
        userInteractionHandler.postDelayed(goToAuthHandler, CLOSE_TIMEOUT);
    }
}
