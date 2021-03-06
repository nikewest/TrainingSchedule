package ru.alexfitness.trainingschedule.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.model.Club;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;
import ru.alexfitness.trainingschedule.util.AFStopScanActivity;
import ru.alexfitness.trainingschedule.util.ErrorDialogBuilder;
import ru.alexfitness.trainingschedule.util.ServiceApiJsonArrayRequest;

public class MainActivity extends AFStopScanActivity {

    private ListView clubsListView;
    private ArrayAdapter<Club> clubsListAdapter;
    private ProgressBar progressBar;

    class ClubsInfoArrayAdapter extends ArrayAdapter<Club> {

        public ClubsInfoArrayAdapter(@NonNull Context context, int resource, @NonNull List<Club> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView;
            if(convertView==null){
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.club_info_row, null);
            } else {
                itemView = convertView;
            }
            Club currentItem = getItem(position);
            TextView clubNameTextView = itemView.findViewById(R.id.clubName);
            clubNameTextView.setText(currentItem.getName());
            return itemView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setEnableAuthEndTimeOut(false);
        setContentView(R.layout.activity_main);
        setActionBar((Toolbar) findViewById(R.id.mainToolbar));
        progressBar = findViewById(R.id.mainProgressBar);
        clubsListView = findViewById(R.id.clubsListView);
        clubsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sp.edit();
                String clubApiAddress = clubsListAdapter.getItem(position).getApiUrl();
                editor.putString(getString(R.string.pref_club_api_url_key), clubApiAddress);
                if(editor.commit()){
                    ApiUrlBuilder.setHostUrl(clubApiAddress);
                }
                Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ApiUrlBuilder.setHostUrl(sharedPreferences.getString(getString(R.string.pref_service_address_key), ""));

        setWaitingState(true);
        ServiceApiJsonArrayRequest request = new ServiceApiJsonArrayRequest(Request.Method.GET, ApiUrlBuilder.getClubsUrl(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ArrayList<Club> clubsArray = new ArrayList<>();
                for(int i=0; i < response.length(); i++){
                    try {
                        clubsArray.add(Club.buildFromJSON(response.getJSONObject(i)));
                    } catch (JSONException e) {
                        Log.e(null, e.getMessage());
                    }
                }
                clubsListAdapter = new ClubsInfoArrayAdapter(MainActivity.this, R.layout.club_info_row, clubsArray);
                clubsListView.setAdapter(clubsListAdapter);
                setWaitingState(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ErrorDialogBuilder.showDialog(MainActivity.this, error, null);
                setWaitingState(false);
            }
        });
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
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
                        if(pwdEditText.getText().toString().equals(getString(R.string.admin_pwd))){
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this, R.string.wrong_admin_pwd, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialogBuider.setNegativeButton(android.R.string.cancel, null);
                dialogBuider.create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWaitingState(boolean state){
        if(state){
            progressBar.setVisibility(View.VISIBLE);
            clubsListView.setVisibility(View.INVISIBLE);
            if(clubsListAdapter!=null){
                clubsListAdapter.clear();
            }
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            clubsListView.setVisibility(View.VISIBLE);
        }
    }
}
