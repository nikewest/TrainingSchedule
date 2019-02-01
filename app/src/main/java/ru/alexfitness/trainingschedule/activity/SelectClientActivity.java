package ru.alexfitness.trainingschedule.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;

import ru.alexfitness.trainingschedule.model.TrainingsBalance;
import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;
import ru.alexfitness.trainingschedule.util.AFApplication;
import ru.alexfitness.trainingschedule.util.AFStopScanActivity;
import ru.alexfitness.trainingschedule.util.ErrorDialogBuilder;
import ru.alexfitness.trainingschedule.util.ServiceApiJsonArrayRequest;

public class SelectClientActivity extends AFStopScanActivity {

    public static final String TRAININGS_BALANCE_EXTRA_KEY = "SelectClientActivity.extra.trainingsBalance";

    private ListView clientsListView;
    private FloatingActionButton addClientButton;

    ArrayList<TrainingsBalance> trainingsBalances;
    private Calendar startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_client);

        clientsListView = findViewById(R.id.clientsListView);
        addClientButton = findViewById(R.id.addClientButton);
        addClientButton.setVisibility(View.INVISIBLE);
        addClientButton.setClickable(false);

        Intent intent = getIntent();
        if(intent!=null){
            startTime = (Calendar) intent.getSerializableExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY);
        }

        //load info
        AFApplication app = (AFApplication) getApplicationContext();
        String trainerUid = app.getTrainer().getUid();
        JsonArrayRequest jsonArrayRequest = new ServiceApiJsonArrayRequest(Request.Method.GET, ApiUrlBuilder.getClientsTrainingsUrl(trainerUid),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        trainingsBalances = new ArrayList<>();
                        int l = response.length();
                        for(int i=0; i<l; i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                trainingsBalances.add(TrainingsBalance.buildFromJSON(jsonObject));
                            } catch (JSONException e) {
                                Log.e(null, e.getMessage());
                            }
                        }
                        ArrayAdapter<TrainingsBalance> adapter = new ArrayAdapter<TrainingsBalance>(SelectClientActivity.this, R.layout.clients_trainings_item, trainingsBalances){
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                View itemView;
                                if(convertView==null){
                                    itemView = LayoutInflater.from(getContext()).inflate(R.layout.clients_trainings_item, null);
                                } else {
                                    itemView = convertView;
                                }
                                TrainingsBalance currentTrainingsBalance = getItem(position);
                                if(currentTrainingsBalance!=null) {
                                    TextView textClient = itemView.findViewById(R.id.text1);
                                    textClient.setText(currentTrainingsBalance.getClientName());
                                    TextView textTraining = itemView.findViewById(R.id.text2);
                                    textTraining.setText(currentTrainingsBalance.getTrainingName());
                                    TextView textBalance = itemView.findViewById(R.id.text3);
                                    textBalance.setText(String.valueOf(currentTrainingsBalance.getBalance()));
                                }
                                return itemView;
                            }
                        };
                        clientsListView.setAdapter(adapter);
                        addClientButton.setVisibility(View.VISIBLE);
                        addClientButton.setClickable(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ErrorDialogBuilder.showDialog(SelectClientActivity.this, error, new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        });
                        Log.e(null, error.toString());
                    }
                });
        Volley.newRequestQueue(this).add(jsonArrayRequest);

        clientsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SelectClientActivity.this, NewEventActivity.class);
                intent.putExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY, startTime);
                TrainingsBalance clickedItem = trainingsBalances.get(position);
                intent.putExtra(TRAININGS_BALANCE_EXTRA_KEY, clickedItem);
                startActivity(intent);
                finish();
            }
        });
    }

    public void addClientForTraining(View view) {
        Intent intent = new Intent(this, SelectSubscriptionActivity.class);
        intent.putExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY, startTime);
        startActivity(intent);
    }
}
