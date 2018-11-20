package ru.alexfitness.trainingschedule.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import ru.alexfitness.trainingschedule.util.ServiceApiJsonObjectRequest;

public class AuthenticationActivity extends AppCompatActivity {

    public static final int NFCSCAN_REQUEST_CODE = 1;

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
                                    Toast.makeText(AuthenticationActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                                    enableLogin(true);
                                }
                            });
                    //serviceApiRequest.setRetryPolicy(new DefaultRetryPolicy(5 * 1000, 0, 1.0f));
                    getRequestQueue().add(serviceApiRequest);
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.nfc_canceled, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //super.onBackPressed();
        finishAndRemoveTask();
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
                startActivity(new Intent(this, SettingsActivity.class));
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
