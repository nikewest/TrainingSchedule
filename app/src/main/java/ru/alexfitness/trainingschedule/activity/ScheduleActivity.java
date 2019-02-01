package ru.alexfitness.trainingschedule.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.alexfitness.trainingschedule.model.AFWeekViewEvent;
import ru.alexfitness.trainingschedule.model.Trainer;
import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;
import ru.alexfitness.trainingschedule.util.AFApplication;
import ru.alexfitness.trainingschedule.util.AFStopScanActivity;
import ru.alexfitness.trainingschedule.util.AFWeekViewEventFactory;
import ru.alexfitness.trainingschedule.util.CalendarSupport;
import ru.alexfitness.trainingschedule.util.Converter;
import ru.alexfitness.trainingschedule.util.ErrorDialogBuilder;
import ru.alexfitness.trainingschedule.util.EventDragShadowBuilder;
import ru.alexfitness.trainingschedule.util.ServiceApiJsonArrayRequest;
import ru.alexfitness.trainingschedule.util.ServiceApiStringRequest;

public class ScheduleActivity extends AFStopScanActivity implements MonthLoader.MonthChangeListener {

    public static final int NFCSCAN_REQUEST_CODE = 1;
    public static final String EVENT_ID_EXTRA_KEY = "ScheduleActivity.extra.eventId";
    public static final String TRAINER_EXTRA_KEY = "ScheduleActivity.extra.trainer";
    public static final String NEW_EVENT_TIME_EXTRA_KEY = "ScheduleActivity.extra.time";

    private WeekView weekView;
    private ProgressBar progressbar;

    private Trainer trainer;
    private AtomicBoolean waitingForResponse = new AtomicBoolean(false);

    private volatile ArrayList<AFWeekViewEvent> eventsList = new ArrayList<>();

    private int scheduleTimeStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule);

        Intent intent = getIntent();
        if(intent!=null){
            trainer = (Trainer) intent.getSerializableExtra(TRAINER_EXTRA_KEY);
            if(trainer!=null) {
                AFApplication app = (AFApplication) getApplicationContext();
                app.setTrainer(trainer);
            }
        }

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setActionBar(toolbar);

        progressbar = findViewById(R.id.scheduleProgressBar);

        weekView = findViewById(R.id.weekView);
        weekView.setMonthChangeListener(this);
        weekView.setEventCornerRadius(5);
        weekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(final WeekViewEvent event, RectF eventRect) {

                if(waitingForResponse.get()){
                    return;
                }

                AFWeekViewEvent afevent = (AFWeekViewEvent) event;
                ArrayList<String> items = new ArrayList<>();
                String[] menuitems = getResources().getStringArray(R.array.event_context_menu);
                if(!afevent.isWrittenOff()){
                    items.add(menuitems[0]);
                    items.add(menuitems[1]);
                } else {
                    return;
                }

                AlertDialog.Builder dBuilder = new AlertDialog.Builder(ScheduleActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                dBuilder.setItems(items.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                writeOffEvent((AFWeekViewEvent) event);
                                break;
                            case 1:
                                cancelEvent((AFWeekViewEvent) event);
                                break;
                            default:
                                break;
                        }
                    }
                });
                AlertDialog dialog = dBuilder.create();
                dialog.show();
            }
        });
        weekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(final WeekViewEvent event, RectF eventRect) {

                if(waitingForResponse.get()){
                    return;
                }

                if(((AFWeekViewEvent) event).isWrittenOff()){
                    Toast.makeText(ScheduleActivity.this,R.string.writtenoff_cantedit, Toast.LENGTH_LONG).show();
                    return;
                }
                ClipData.Item itemId = new ClipData.Item(String.valueOf(event.getId()));
                ClipData dragData = new ClipData(event.getName(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, itemId);
                dragData.addItem(new ClipData.Item(String.valueOf(eventRect.height()/2)));
                View.DragShadowBuilder dragShadowBuilder = new EventDragShadowBuilder(event, eventRect);
                weekView.startDragAndDrop(dragData, dragShadowBuilder, null, 0);
            }
        });
        weekView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, final DragEvent event) {
                final int action = event.getAction();
                float x, y;
                switch (action){
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        setWaitingState(true);
                        long eventId = Long.parseLong(event.getClipData().getItemAt(0).getText().toString());
                        for (final AFWeekViewEvent curEvent: eventsList) {
                            if(curEvent.getId() == eventId){
                                x = event.getX();
                                y = event.getY() - Float.parseFloat(event.getClipData().getItemAt(1).getText().toString());

                                final int diff = (int) (curEvent.getEndTime().getTimeInMillis() - curEvent.getStartTime().getTimeInMillis());

                                final Calendar dropStartTime = weekView.getTimeFromPoint(x, y);
                                if (dropStartTime==null){
                                    Toast.makeText(ScheduleActivity.this, R.string.dropincorrect, Toast.LENGTH_LONG).show();
                                    return false;
                                }

                                //round start and end time to closer hour step
                                int stepDiff = dropStartTime.get(Calendar.MINUTE) % scheduleTimeStep;
                                if(stepDiff > scheduleTimeStep/2){
                                    dropStartTime.add(Calendar.MINUTE, scheduleTimeStep - stepDiff);
                                } else {
                                    dropStartTime.add(Calendar.MINUTE, -stepDiff);
                                }
                                dropStartTime.set(Calendar.SECOND, 0);
                                /*if(dropStartTime.get(Calendar.MINUTE)>=30){
                                    dropStartTime.add(Calendar.HOUR, 1);
                                }
                                dropStartTime.set(Calendar.MINUTE, 0);
                                dropStartTime.set(Calendar.SECOND, 0);*/

                                final Calendar dropEndTime = Calendar.getInstance();
                                dropEndTime.setTimeInMillis(dropStartTime.getTimeInMillis() + diff);

                                StringRequest stringRequest = new ServiceApiStringRequest(Request.Method.PUT, ApiUrlBuilder.getEventEditDatesUrl(curEvent.getUid(), Converter.dateToString1C(dropStartTime.getTime()), Converter.dateToString1C(dropEndTime.getTime())),
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                curEvent.setStartTime(dropStartTime);
                                                curEvent.setEndTime(dropEndTime);
                                                weekView.loadEvents(eventsList);
                                                weekView.reDrawWeekView();
                                                Toast.makeText(ScheduleActivity.this, R.string.training_moved, Toast.LENGTH_LONG).show();
                                                setWaitingState(false);
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ScheduleActivity.this);
                                                dialogBuilder.setMessage(new String(error.networkResponse.data));
                                                dialogBuilder.show();
                                                Toast.makeText(ScheduleActivity.this, R.string.cant_edit, Toast.LENGTH_LONG).show();
                                                setWaitingState(false);
                                            }
                                        });
                                //stringRequest.setRetryPolicy(new DefaultRetryPolicy(5 * 1000, 1, 1.0f));
                                Volley.newRequestQueue(ScheduleActivity.this).add(stringRequest);
                                break;
                            }
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });

        weekView.setEmptyViewClickListener(new WeekView.EmptyViewClickListener() {
            @Override
            public void onEmptyViewClicked(final Calendar time) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ScheduleActivity.this);
                dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ScheduleActivity.this, SelectClientActivity.class);
                        intent.putExtra(NEW_EVENT_TIME_EXTRA_KEY, time);
                        startActivity(intent);
                    }
                }).setCancelable(true).setMessage(R.string.add_new_training)
                .setNegativeButton(android.R.string.no, null);
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });
        goToCurrentHour();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        scheduleTimeStep = Integer.parseInt(prefs.getString(getString(R.string.pref_schedule_time_step_key), "60"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.one_day_view:
                weekView.setNumberOfVisibleDays(1);
                goToCurrentHour();
                return true;
            case R.id.three_day_view:
                weekView.setNumberOfVisibleDays(3);
                goToCurrentHour();
                return true;
            case R.id.whole_week_view:
                weekView.setNumberOfVisibleDays(7);
                goToCurrentHour();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NFCSCAN_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                String cardId = data.getStringExtra(NFCScanActivity.CARD_ID_EXTRA_KEY);
                final String eventId = data.getStringExtra(EVENT_ID_EXTRA_KEY);

                StringRequest stringRequest = new ServiceApiStringRequest(Request.Method.POST, ApiUrlBuilder.getEventWriteOffUrl(eventId, cardId),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                for (AFWeekViewEvent event: eventsList) {
                                    if(event.getUid().equals(eventId)) {
                                        event.setWrittenOff(true);
                                        event.setAppearence();
                                        weekView.loadEvents(eventsList);
                                        weekView.reDrawWeekView();
                                        break;
                                    }
                                }
                                Toast.makeText(ScheduleActivity.this, R.string.training_writtenoff, Toast.LENGTH_SHORT).show();
                                setWaitingState(false);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ErrorDialogBuilder.showDialog(ScheduleActivity.this, error, null);
                                Toast.makeText(ScheduleActivity.this, R.string.cant_write_off, Toast.LENGTH_LONG).show();
                                setWaitingState(false);
                            }
                        });
                //stringRequest.setRetryPolicy(new DefaultRetryPolicy(5 * 1000, 1, 1.0f));
                Volley.newRequestQueue(ScheduleActivity.this).add(stringRequest);
            } else {
                Toast.makeText(this, R.string.nfc_canceled, Toast.LENGTH_LONG).show();
                setWaitingState(false);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra(NewEventActivity.EVENT_ADDED_EXTRA_KEY)) {
            boolean eventAdded = intent.getBooleanExtra(NewEventActivity.EVENT_ADDED_EXTRA_KEY, false);
            if(eventAdded){
                Toast.makeText(this, R.string.new_training_added, Toast.LENGTH_LONG).show();
                weekView.notifyDatasetChanged();
            } else {
                Toast.makeText(this, R.string.training_not_added, Toast.LENGTH_LONG).show();
            }
        }
    }

    //
    // INTERFACES
    //

    /*
     * MonthLoader.MonthChangeListener
     */

    @Override
    public void onMonthChange(int startNewYear, int startNewMonth, int endNewYear, int endNewMonth) {

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, startNewYear);
        calendar.set(Calendar.MONTH, startNewMonth-1);
        Date beginDate = CalendarSupport.getBegginningOfMonth(calendar.getTime());

        calendar.set(Calendar.YEAR, endNewYear);
        calendar.set(Calendar.MONTH, endNewMonth-1);
        Date endDate = CalendarSupport.getEndOfMonth(calendar.getTime());

        ServiceApiJsonArrayRequest serviceApiJsonArrayRequest = new ServiceApiJsonArrayRequest(Request.Method.GET, ApiUrlBuilder.getEventsByTrainerUrl(trainer.getUid(), beginDate, endDate),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            eventsList.clear();
                            int l = response.length();
                            for (int i = 0; i < l; i++) {
                                JSONObject jsonEvent = response.getJSONObject(i);
                                AFWeekViewEvent afWeekViewEvent = AFWeekViewEventFactory.getInstance().fromJSONObject(jsonEvent);
                                eventsList.add(afWeekViewEvent);
                            }
                        } catch (JSONException e) {
                            Log.e(null, e.getMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setWaitingState(false);
                        weekView.loadEvents(eventsList);
                        weekView.reDrawWeekView();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ErrorDialogBuilder.showDialog(ScheduleActivity.this, error, null);
                        setWaitingState(false);
                        Toast.makeText(ScheduleActivity.this, R.string.cant_load_events, Toast.LENGTH_LONG).show();
                    }
                });
        Volley.newRequestQueue(ScheduleActivity.this).add(serviceApiJsonArrayRequest);
        setWaitingState(true);
    }

    /*
     * Event context menu handlers
     */

    private void writeOffEvent(final AFWeekViewEvent event){
        setWaitingState(true);
        if(event.isWrittenOff()){
            return;
        }
        Intent intent = new Intent(ScheduleActivity.this, NFCScanActivity.class);
        intent.putExtra(EVENT_ID_EXTRA_KEY, event.getUid());
        startActivityForResult(intent, NFCSCAN_REQUEST_CODE);
    }

    private void cancelEvent(final AFWeekViewEvent event){
        setWaitingState(true);
        StringRequest stringRequest = new ServiceApiStringRequest(Request.Method.DELETE, ApiUrlBuilder.getEventUrl(event.getUid()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        eventsList.remove(event);
                        weekView.loadEvents(eventsList);
                        weekView.reDrawWeekView();
                        Toast.makeText(ScheduleActivity.this, R.string.training_canceled, Toast.LENGTH_LONG).show();
                        setWaitingState(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ErrorDialogBuilder.showDialog(ScheduleActivity.this, error, null);
                        Toast.makeText(ScheduleActivity.this, R.string.cant_cancel, Toast.LENGTH_LONG).show();
                        setWaitingState(false);
                    }
                });
        Volley.newRequestQueue(ScheduleActivity.this).add(stringRequest);
    }

    //
    // UI UTILITIES
    //

    private void goToCurrentHour() {
        weekView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    private void setWaitingState(boolean state){
        if(state){
            progressbar.setVisibility(View.VISIBLE);
            progressbar.bringToFront();
        } else {
            progressbar.setVisibility(View.GONE);
        }
        waitingForResponse.set(state);

        weekView.setWaiting(state);
    }




}
