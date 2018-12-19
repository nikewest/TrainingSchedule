package ru.alexfitness.trainingschedule.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.model.Card;
import ru.alexfitness.trainingschedule.model.Subscription;
import ru.alexfitness.trainingschedule.model.TrainingsBalance;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;
import ru.alexfitness.trainingschedule.util.AFStopScanActivity;
import ru.alexfitness.trainingschedule.util.ServiceApiJsonArrayRequest;
import ru.alexfitness.trainingschedule.util.ServiceApiJsonObjectRequest;

public class SelectSubscriptionActivity extends AFStopScanActivity {

    public static final int NFC_REQUEST_CODE = 1;
    public static final String SUBSCRIPTION_EXTRA_KEY = "SelectSubscriptionActivity.extra.subscription";

    private ListView trainingsListView;
    private ProgressBar selectSubProgressBar;

    private Calendar startTime;
    private ArrayList<Subscription> subscriptionsArrayList;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_subscription);

        Intent intent = getIntent();
        if(intent!=null){
            startTime = (Calendar) intent.getSerializableExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY);
        }
        trainingsListView = findViewById(R.id.subscriptionsListView);
        selectSubProgressBar = findViewById(R.id.selectSubProgressBar);

        JsonArrayRequest jsonArrayRequest = new ServiceApiJsonArrayRequest(Request.Method.GET, ApiUrlBuilder.getSubscriptionsUrl(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                            subscriptionsArrayList = new ArrayList<>();
                            int l = response.length();
                            for(int i=0; i<l; i++){
                                try{
                                    JSONObject jsonObject = response.getJSONObject(i);
                                    subscriptionsArrayList.add(Subscription.buildFromJSON(jsonObject));
                                } catch (JSONException e) {
                                    Log.e(null, e.getMessage());
                                }
                            }
                        ArrayAdapter<Subscription> adapter = new ArrayAdapter<Subscription>(SelectSubscriptionActivity.this, android.R.layout.simple_list_item_1, subscriptionsArrayList);
                        trainingsListView.setAdapter(adapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(null, error.toString());
                        finish();
                    }
                });
        Volley.newRequestQueue(this).add(jsonArrayRequest);

        trainingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setWaitingState(true);
                Intent nfcIntent = new Intent(SelectSubscriptionActivity.this, NFCScanActivity.class);
                nfcIntent.putExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY, startTime);
                //nfcIntent.putExtra(TRAINING_EXTRA_KEY, trainingsArrayList.get(position));
                subscription = subscriptionsArrayList.get(position);
                startActivityForResult(nfcIntent, NFC_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NFC_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                String cardHexCode = data.getStringExtra(NFCScanActivity.CARD_ID_EXTRA_KEY);
                startTime = (Calendar) data.getSerializableExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY);
                ServiceApiJsonObjectRequest jsonObjectRequest = new ServiceApiJsonObjectRequest(Request.Method.GET, ApiUrlBuilder.getCardUrl(cardHexCode), null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Card clientCard = Card.buildFromJSON(response);
                                    Intent intent = new Intent(SelectSubscriptionActivity.this, NewEventActivity.class);
                                    intent.putExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY, startTime);
                                    TrainingsBalance trainingsBalance = new TrainingsBalance();
                                    trainingsBalance.setClientUid(clientCard.getClient().getUid());
                                    trainingsBalance.setClientName(clientCard.getClient().getName());
                                    trainingsBalance.setTrainingUid(null);
                                    trainingsBalance.setTrainingName(null);
                                    trainingsBalance.setBalance(0);
                                    intent.putExtra(SelectClientActivity.TRAININGS_BALANCE_EXTRA_KEY, trainingsBalance);
                                    intent.putExtra(SUBSCRIPTION_EXTRA_KEY, subscription);
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException e) {
                                    Log.e(null, e.getMessage());
                                    Toast.makeText(SelectSubscriptionActivity.this, R.string.error_load_card_info, Toast.LENGTH_LONG).show();
                                    setWaitingState(false);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(null, error.toString());
                                Toast.makeText(SelectSubscriptionActivity.this, R.string.error_load_card_info, Toast.LENGTH_LONG).show();
                                setWaitingState(false);
                            }
                        });
                Volley.newRequestQueue(this).add(jsonObjectRequest);
            } else {
                Toast.makeText(this, R.string.nfc_canceled, Toast.LENGTH_LONG).show();
                setWaitingState(false);
            }
        }
    }

    private void setWaitingState(boolean state){
        if(state){
            trainingsListView.setVisibility(View.INVISIBLE);
            selectSubProgressBar.setVisibility(View.VISIBLE);
        } else {
            trainingsListView.setVisibility(View.VISIBLE);
            selectSubProgressBar.setVisibility(View.GONE);
        }
        trainingsListView.setClickable(!state);
    }
}
