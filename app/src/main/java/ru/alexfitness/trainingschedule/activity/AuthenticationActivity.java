package ru.alexfitness.trainingschedule.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import ru.alexfitness.trainingschedule.model.Trainer;
import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;

import ru.alexfitness.trainingschedule.util.AFStopScanAppCompatActivity;
import ru.alexfitness.trainingschedule.util.ErrorDialogBuilder;
import ru.alexfitness.trainingschedule.util.ServiceApiJsonObjectRequest;

public class AuthenticationActivity extends AFStopScanAppCompatActivity {

    public static final int NFCSCAN_REQUEST_CODE = 1;
    public static final String SETTINGS_PWD = "11235813";

    private ProgressBar loginProgressBar;
    private TextView loginTextView;

    private RequestQueue requestQueue;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NFCSCAN_REQUEST_CODE){
            if(resultCode == RESULT_OK){

                enableLogin(false);

                ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if(conMgr==null){
                    return;
                }
                NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    String cardId = data.getStringExtra(NFCScanActivity.CARD_ID_EXTRA_KEY);
                    ServiceApiJsonObjectRequest serviceApiRequest = new ServiceApiJsonObjectRequest(Request.Method.GET, ApiUrlBuilder.getLoginUrl(cardId), null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Trainer trainer;
                                    try {
                                        trainer = Trainer.buildFromJSON(response);
                                    } catch (JSONException e) {
                                        Log.e(getString(R.string.util_tag), e.getMessage());
                                        Toast.makeText(AuthenticationActivity.this, R.string.login_failed, Toast.LENGTH_LONG).show();
                                        enableLogin(true);
                                        return;
                                    }
                                    Intent intent = new Intent(AuthenticationActivity.this, ScheduleActivity.class);
                                    intent.putExtra(ScheduleActivity.TRAINER_EXTRA_KEY, trainer);
                                    startActivity(intent);
                                    enableLogin(true);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    ErrorDialogBuilder.showDialog(AuthenticationActivity.this, error, null);
                                    Log.e(getString(R.string.util_tag), error.toString());
                                    Toast.makeText(AuthenticationActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                                    enableLogin(true);
                                }
                            });
                    getRequestQueue().add(serviceApiRequest);
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
                    enableLogin(true);
                }
            } else {
                Toast.makeText(this, R.string.nfc_canceled, Toast.LENGTH_LONG).show();
                enableLogin(true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnableAuthEndTimeOut(false);

        setContentView(R.layout.activity_authentication);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.auth_toolbar));

        loginTextView = findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthenticationActivity.this, NFCScanActivity.class);
                startActivityForResult(intent, NFCSCAN_REQUEST_CODE);
            }
        });

        loginProgressBar = findViewById(R.id.loginProgressBar);

    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelRequests();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_settings:

                final AlertDialog.Builder dialogBuider = new AlertDialog.Builder(this);
                LayoutInflater layoutInflater = getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.dialog_settings_pwd, null);
                final EditText pwdEditText = dialogView.findViewById(R.id.settingPwdEditText);
                dialogBuider.setView(dialogView);
                dialogBuider.setMessage(R.string.settings_pwd_dialog_message);
                dialogBuider.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("SETTINGS PWD", pwdEditText.getText().toString());
                        if(pwdEditText.getText().toString().equals(SETTINGS_PWD)){
                            startActivity(new Intent(AuthenticationActivity.this, SettingsActivity.class));
                        } else {
                            Toast.makeText(AuthenticationActivity.this, R.string.wrong_admin_pwd, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialogBuider.setNegativeButton(android.R.string.cancel, null);
                dialogBuider.create().show();
                return true;
        }
        return false;
    }

    private RequestQueue getRequestQueue(){
        if(requestQueue==null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        return requestQueue;
    }

    private void cancelRequests(){
        if(requestQueue!=null){
            requestQueue.cancelAll(this);
        }
        loginProgressBar.setVisibility(View.INVISIBLE);
    }

    private void enableLogin(boolean state){
        if(state) {
            loginProgressBar.setVisibility(View.GONE);
        } else {
            loginProgressBar.setVisibility(View.VISIBLE);
        }
        loginTextView.setEnabled(state);
    }

}
