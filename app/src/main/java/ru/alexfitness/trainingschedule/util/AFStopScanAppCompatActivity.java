package ru.alexfitness.trainingschedule.util;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;

public class AFStopScanAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null){
            String intentAction = intent.getAction();
            if((intentAction!=null)&&(intentAction.equals(NfcAdapter.ACTION_TAG_DISCOVERED))){
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startNfcScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNfcScan();
    }

    private void startNfcScan() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void stopNfcScan(){
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
}
