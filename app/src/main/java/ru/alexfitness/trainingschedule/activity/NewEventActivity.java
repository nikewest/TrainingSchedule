package ru.alexfitness.trainingschedule.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ru.alexfitness.trainingschedule.model.Subscription;
import ru.alexfitness.trainingschedule.model.Trainer;
import ru.alexfitness.trainingschedule.model.TrainingsBalance;
import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;
import ru.alexfitness.trainingschedule.util.AFApplication;
import ru.alexfitness.trainingschedule.util.AFStopScanActivity;
import ru.alexfitness.trainingschedule.util.CalendarSupport;
import ru.alexfitness.trainingschedule.util.Converter;
import ru.alexfitness.trainingschedule.util.ServiceApiStringRequest;

public class NewEventActivity extends AFStopScanActivity implements TimePickerDialog.OnTimeSetListener {

    public static final String EVENT_ADDED_EXTRA_KEY = "NewEventActivity.extra.eventAdded";

    TrainingsBalance trainingInfo;
    Calendar time;
    Date startDate;
    Subscription subscription;

    FloatingActionButton addNewEventButton;
    ProgressBar addNewEventProgressBar;
    //EditText dateEditText;
    TextView dateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        Intent intent = getIntent();
        if(intent!=null) {
            trainingInfo = (TrainingsBalance) intent.getSerializableExtra(SelectClientActivity.TRAININGS_BALANCE_EXTRA_KEY);
            time = (Calendar) intent.getSerializableExtra(ScheduleActivity.NEW_EVENT_TIME_EXTRA_KEY);
            subscription = (Subscription) intent.getSerializableExtra(SelectSubscriptionActivity.SUBSCRIPTION_EXTRA_KEY);
        }

        addNewEventButton = findViewById(R.id.addNewEventButton);
        addNewEventProgressBar = findViewById(R.id.addNewEventProgressBar);

        TextView clientTextView = findViewById(R.id.clientTextView);
        clientTextView.setText(trainingInfo.getClientName());
        TextView trainingTextView = findViewById(R.id.trainingTextView);
        trainingTextView.setText(trainingInfo.getTrainingName());

        dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setLongClickable(true);
        dateTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewEventActivity.this, android.R.style.Theme_Holo_Dialog, NewEventActivity.this, startDate.getHours(), startDate.getMinutes(), true);
                timePickerDialog.show();
                return true;
            }
        });
        startDate = CalendarSupport.getStartOfHour(time.getTime());
        updateDateView();

    }

    public void addEvent(View view) {
        setWaitingState(true);
        AFApplication application =  (AFApplication) getApplication();
        Trainer trainer = application.getTrainer();
        boolean trainingPaid = trainingInfo.getBalance() > 0;

        /*try {
            startDate = new SimpleDateFormat().parse(dateEditText.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        String subscriptionId = subscription==null?null:subscription.getUid();
        ServiceApiStringRequest request = new ServiceApiStringRequest(Request.Method.POST, ApiUrlBuilder.getNewEventUrl(trainer.getUid(), trainingInfo.getClientUid(), trainingInfo.getTrainingUid(), Converter.dateToString1C(startDate), trainingPaid, subscriptionId),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        finishWithResult(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(NewEventActivity.this);
                        dialogBuilder.setMessage(new String(error.networkResponse.data));
                        dialogBuilder.show();
                        finishWithResult(false);
                    }
                });
        Volley.newRequestQueue(NewEventActivity.this).add(request);
    }

    private void finishWithResult(boolean result){
        Intent intent = new Intent(NewEventActivity.this, ScheduleActivity.class);
        intent.putExtra(EVENT_ADDED_EXTRA_KEY, result);
        finish();
        startActivity(intent);
    }

    private void setWaitingState(boolean state){
        if(state){
            addNewEventProgressBar.setVisibility(View.VISIBLE);
            addNewEventButton.setAlpha((float) 0.3);
        } else {
            addNewEventProgressBar.setVisibility(View.GONE);
            addNewEventButton.setAlpha((float) 1);
        }
        addNewEventButton.setClickable(!state);
    }

    private void updateDateView(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        dateTextView.setText(simpleDateFormat.format(startDate));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        startDate.setHours(hourOfDay);
        startDate.setMinutes(minute);
        updateDateView();
    }
}
