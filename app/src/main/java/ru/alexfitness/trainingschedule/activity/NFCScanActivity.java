package ru.alexfitness.trainingschedule.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.widget.Toast;

import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.util.AFStopScanActivity;
import ru.alexfitness.trainingschedule.util.Converter;

public class NFCScanActivity extends AFStopScanActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListArray;

    //private String eventId;
    private Bundle extras;

    public static final String CARD_ID_EXTRA_KEY = "nfcscanactivity.extra.cardId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfcscan);

        NfcManager nfcManager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        assert nfcManager!=null;
        nfcAdapter = nfcManager.getDefaultAdapter();
        if(nfcAdapter!=null && nfcAdapter.isEnabled()){
            pendingIntent = PendingIntent.getActivity(this,0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            intentFiltersArray = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED), new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
            techListArray = new String[][]{new String[]{NfcA.class.getName(), MifareClassic.class.getName(), NdefFormatable.class.getName()}};

            Intent intent = getIntent();
            /*if(intent.hasExtra(ScheduleActivity.EVENT_ID_EXTRA_KEY)){
                eventId = intent.getStringExtra(ScheduleActivity.EVENT_ID_EXTRA_KEY);
            }*/
            extras = intent.getExtras();
        } else {
            Toast.makeText(this, R.string.nfc_not_available, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null){
            String intentAction = intent.getAction();
            if((intentAction!=null)&&(intentAction.equals(NfcAdapter.ACTION_TECH_DISCOVERED) || intentAction.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || intentAction.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))){
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] tagId = tag.getId();
                String tagIdHex = Converter.tagIdToHexString(tagId);

                Intent resultIntent = new Intent();
                if(extras!=null){
                    resultIntent.putExtras(extras);
                }
                resultIntent.putExtra(CARD_ID_EXTRA_KEY, tagIdHex);
                /*if(eventId!=null){
                    resultIntent.putExtra(ScheduleActivity.EVENT_ID_EXTRA_KEY, eventId);
                }*/
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }
    }

    /*@Override
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
        if(nfcAdapter!=null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListArray);
        }
    }

    private void stopNfcScan(){
        if(nfcAdapter!=null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }*/
}
