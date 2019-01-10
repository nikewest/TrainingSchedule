package ru.alexfitness.trainingschedule.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;

public class AFStopScanActivity extends Activity {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
}
