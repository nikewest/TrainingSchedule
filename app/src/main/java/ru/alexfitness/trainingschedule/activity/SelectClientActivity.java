package ru.alexfitness.trainingschedule.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.model.TrainingsBalance;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;
import ru.alexfitness.trainingschedule.util.AFApplication;
import ru.alexfitness.trainingschedule.util.AFStopScanAppCompatActivity;
import ru.alexfitness.trainingschedule.util.ErrorDialogBuilder;
import ru.alexfitness.trainingschedule.util.ServiceApiJsonArrayRequest;

public class SelectClientActivity extends AFStopScanAppCompatActivity implements AdapterView.OnItemClickListener, Response.Listener<JSONArray> {

    public static final String TRAININGS_BALANCE_EXTRA_KEY = "SelectClientActivity.extra.trainingsBalance";

    private ListView clientsListView;
    private FloatingActionButton addClientButton;
    private ArrayList<TrainingsBalance> trainingsBalances = new ArrayList<>();
    private TrainingsBalanceArrayAdapter adapter;
    private Calendar startTime;

    protected class TrainingsBalanceArrayAdapter extends ArrayAdapter<TrainingsBalance>{

        private ArrayList<TrainingsBalance> arrayList = new ArrayList<>();

        public TrainingsBalanceArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<TrainingsBalance> objects) {
            super(context, resource, objects);
            arrayList = objects;
        }

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

        @NonNull
        @Override
        public Filter getFilter() {
            Filter filter = new Filter(){

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    Filter.FilterResults results = new FilterResults();
                    ArrayList<TrainingsBalance> filteredTrainingsBalances = new ArrayList<>();
                    for (TrainingsBalance currentTrainingsBalance:trainingsBalances) {
                        String clientName = currentTrainingsBalance.getClientName();
                        if(clientName.toLowerCase().contains(constraint.toString().toLowerCase())){
                            filteredTrainingsBalances.add(currentTrainingsBalance);
                        }
                    }
                    results.values = filteredTrainingsBalances;
                    results.count = filteredTrainingsBalances.size();

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    arrayList.clear();
                    arrayList.addAll((Collection<? extends TrainingsBalance>) results.values);
                    TrainingsBalanceArrayAdapter.this.notifyDataSetChanged();
                }
            };
            return filter;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_client);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.main_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        clientsListView = findViewById(R.id.clientsListView);
        addClientButton = findViewById(R.id.addClientButton);

        setButtonVisibility(false);

        Intent intent = getIntent();
        if(intent!=null){
            startTime = (Calendar) intent.getSerializableExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY);
        }

        //load info
        AFApplication app = (AFApplication) getApplicationContext();
        String trainerUid = app.getTrainer().getUid();
        JsonArrayRequest jsonArrayRequest = new ServiceApiJsonArrayRequest(Request.Method.GET, ApiUrlBuilder.getClientsTrainingsUrl(trainerUid),
                this,
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

        clientsListView.setOnItemClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent!=null){
            if(Intent.ACTION_SEARCH.equals(intent.getAction())){
                getAdapter().getFilter().filter(intent.getStringExtra(SearchManager.QUERY));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_client_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_client_search:
                onSearchRequested();
                return true;
            case R.id.menu_item_cancel_client_search:
                adapter.clear();
                adapter.addAll(trainingsBalances);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TrainingsBalance clickedItem = getAdapter().getItem(position);
        startAddNewEventActivity(clickedItem);
    }

    @Override
    public void onResponse(JSONArray response) {
        int l = response.length();
        for(int i=0; i<l; i++) {
            try {
                JSONObject jsonObject = response.getJSONObject(i);
                trainingsBalances.add(TrainingsBalance.buildFromJSON(jsonObject));
            } catch (JSONException e) {
                Log.e(null, e.getMessage());
            }
        }
        ArrayList<TrainingsBalance> objects = new ArrayList<TrainingsBalance>();
        objects.addAll(trainingsBalances);
        adapter = new TrainingsBalanceArrayAdapter(SelectClientActivity.this, R.layout.clients_trainings_item, objects);
        clientsListView.setAdapter(adapter);

        setButtonVisibility(true);
    }


    public TrainingsBalanceArrayAdapter getAdapter() {
        return adapter;
    }

    private void setButtonVisibility(boolean value){
        if(value){
            addClientButton.setVisibility(View.VISIBLE);
        } else addClientButton.setVisibility(View.INVISIBLE);

        addClientButton.setClickable(value);
    }

    public void addClientForTraining(View view) {
        Intent intent = new Intent(this, SelectSubscriptionActivity.class);
        intent.putExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY, startTime);
        startActivity(intent);
    }

    private void startAddNewEventActivity(TrainingsBalance item){
        Intent intent = new Intent(SelectClientActivity.this, NewEventActivity.class);
        intent.putExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY, startTime);
        intent.putExtra(TRAININGS_BALANCE_EXTRA_KEY, item);
        startActivity(intent);
        finish();
    }

}
