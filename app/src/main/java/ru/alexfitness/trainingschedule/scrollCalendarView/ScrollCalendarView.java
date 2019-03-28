package ru.alexfitness.trainingschedule.scrollCalendarView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScrollCalendarView extends View implements EventsLoaderListener {

    //TODO refactor scaling
    //TODO refactor drag n drop
    //TODO refactor events keeping

    private static int CUSTOM_ROW_HEIGHT = 100;
    private static int CUSTOM_TIME_STEP = 60;
    private static int CUSTOM_VISIBLE_COLUMN_COUNT = 3;
    private static int CUSTOM_TODAY_BACKGROUND_COLOR = 0xFF989797;
    private static int CUSTOM_NOWLINE_COLOR = Color.RED;
    private static int CUSTOM_NOWLINE_WIDTH = 2;
    private static int CUSTOM_EVENT_PADDING = 1;
    private static int CUSTOM_EMPTY_EVENT_BACKGROUND_COLOR = Color.LTGRAY;
    private static int CUSTOM_EVENT_TEXT_PADDING = 5;
    private static int CUSTOM_EVENT_TEXT_COLOR = Color.BLACK;
    private static int CUSTOM_ROW_HEADER_PADDING = 5;
    private static int CUSTOM_ROW_HEADER_COLOR = Color.WHITE;
    private static int CUSTOM_ROW_HEADER_TEXT_COLOR = Color.BLACK;
    private static int CUSTOM_COLUMN_HEADER_PADDING = 10;
    private static int CUSTOM_COLUMN_HEADER_TEXT_COLOR = Color.BLACK;
    private static int CUSTOM_COLUMN_HEADER_COLOR = Color.WHITE;
    private static int CUSTOM_HOURLINE_WIDTH = 2;
    private static int CUSTOM_HOURLINE_COLOR = Color.YELLOW;
    private static int CUSTOM_COLUMN_HEADER_TEXT_PCT = 3;
    private static int CUSTOM_ROW_HEADER_TEXT_PCT = 3;
    private static int CUSTOM_EVENT_TEXT_PCT = 3;

    private Context context;

    private float tableHeight;
    private int viewHeight; //view visible area height
    private int viewWidth; // view visible area width
    private float positionX = 0; //original x
    private float positionY = 0; // original y

    private float rowHeight = CUSTOM_ROW_HEIGHT;
    private float minRowHeight = 0;
    private float columnWidth;
    private int timeStep = CUSTOM_TIME_STEP; //startMinute
    private int visibleColumnCount = CUSTOM_VISIBLE_COLUMN_COUNT;
    private int currentTopRowIndex;
    private int currentLeftColumnIndex = 0;
    private int currentPeriodIndex;
    private int periodLength = 30; //days
    private int scrollDaysDifference;

    private float startDragX = 0; //x
    private int dragColumnIndex;
    private int initialDragColumnIndex;
    private TableEvent draggedEvent = null;
    private TableEvent initialDragEvent;
    private float dragTouchOffset;
    private boolean isDragEvent = false;
    private float actualDragPosition;

    private OverScroller mScroller;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private DateTimeFormatter dateTimeFormatter;
    private ScrollDirection scrollDirection = ScrollDirection.NONE;
    private EventSingleClickListener eventSingleClickListener;
    private TableSingleClickListener tableSingleClickListener;
    private EventDoubleClickListener eventDoubleClickListener;
    private CalendarDoubleClickListener calendarDoubleClickListener;
    private TableLongPressListener tableLongPressListener;
    private EventDragListener eventDragListener;

    private boolean isScale = false;
    private boolean isFited = false;
    private EventsLoader eventsLoader;

    private SparseArray<ArrayList<TableEvent>> events = new SparseArray<>();

    private boolean waitingState = false;
    private WaitingStateListener waitingStateListener;

    //TODAY
    private int todayBackgroundColor = CUSTOM_TODAY_BACKGROUND_COLOR;
    private Date today;
    private int todayIndex;
    private int todayTimePosition;

    //NOW LINE
    private int nowLineColor = CUSTOM_NOWLINE_COLOR;
    private int nowLineWidth = CUSTOM_NOWLINE_WIDTH;

    //EVENT
    private int eventPadding = CUSTOM_EVENT_PADDING;
    private int emptyEventBackgroundColor = CUSTOM_EMPTY_EVENT_BACKGROUND_COLOR;
    private int eventTextPadding = CUSTOM_EVENT_TEXT_PADDING;
    private int eventTextColor = CUSTOM_EVENT_TEXT_COLOR;
    private int eventTextSize;
    private int eventTextPct;

    //HOUR LINE
    private int hourLineWidth = CUSTOM_HOURLINE_WIDTH;
    private int hourLineColor = CUSTOM_HOURLINE_COLOR;

    //ROW HEADER
    private int rowHeaderWidth;
    private int rowHeaderTextSize;
    private int rowHeaderTextPct;
    private int rowHeaderTextHeight;
    private int rowHeaderPadding = CUSTOM_ROW_HEADER_PADDING;
    private int rowHeaderColor = CUSTOM_ROW_HEADER_COLOR;
    private int rowHeaderTextColor = CUSTOM_ROW_HEADER_TEXT_COLOR;

    //COLUMN HEADER
    private int columnHeaderHeight;
    private int columnHeaderTextHeight;
    private int columnHeaderTextSize;
    private int columnHeaderTextPct;
    private int columnHeaderPadding = CUSTOM_COLUMN_HEADER_PADDING;
    private int columnHeaderTextColor = CUSTOM_COLUMN_HEADER_TEXT_COLOR;
    private int columnHeaderColor = CUSTOM_COLUMN_HEADER_COLOR;

    //PAINTS
    private Paint linePaint;
    private Paint backgroundPaint;
    private TextPaint rowHeaderTextPaint;
    private TextPaint columnHeaderTextPaint;
    private TextPaint eventTextPaint;
    private Typeface defaultBoldTypeface;

    private enum ScrollDirection{
        RIGHT, LEFT, NONE
    }

    private class SimpleDateTimeFormatter implements  DateTimeFormatter{

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("", Locale.getDefault());

        @Override
        public String dateToString(Date date) {
            simpleDateFormat.applyPattern("EE dd.MM");
            return simpleDateFormat.format(date);
        }

        @Override
        public String timeToString(Date date) {
            simpleDateFormat.applyPattern("HH:mm");
            return simpleDateFormat.format(date);
        }

        @Override
        public String timeToString(int hour, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            simpleDateFormat.applyPattern("HH:mm");
            return simpleDateFormat.format(calendar.getTime());
        }
    }

    private class CustomOnScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(rowHeight*detector.getScaleFactor() >= minRowHeight){
                positionY *= detector.getScaleFactor();
                setRowHeight((int) (rowHeight * detector.getScaleFactor()));
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScale = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScale = false;
        }
    }

    private class CustomSimpleGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            mScroller.forceFinished(true);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(getWaitingState()){
                return true;
            }
            float eventX = e.getX();
            float eventY = e.getY();
            int columnIndex = (int) (currentLeftColumnIndex + Math.floor((eventX - rowHeaderWidth) / columnWidth));
            ArrayList<TableEvent> columnEvents = events.get(columnIndex);
            if(columnEvents!=null){

                float eventTop, eventBottom;
                for(TableEvent tableEvent:columnEvents){
                    eventTop = tableEvent.getTop(rowHeight) + columnHeaderHeight;
                    eventBottom = eventTop + tableEvent.getLength(rowHeight);
                    if(eventTop <= eventY + positionY && eventBottom >= eventY + positionY){
                        if(eventSingleClickListener!=null){
                            eventSingleClickListener.onClick(tableEvent.scheduleEvent);
                            return true;
                        }
                    }
                }
            }
            if(tableSingleClickListener!=null){
                //tableSingleClickListener.onClick(getTimeFromPoint(eventX + positionX - rowHeaderWidth, eventY + positionY - columnHeaderHeight));
                tableSingleClickListener.onClick(getTime(columnIndex, eventY + positionY - columnHeaderHeight));
                return true;
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(getWaitingState()){
                return true;
            }
            float eventX = e.getX();
            float eventY = e.getY();
            int columnIndex = (int) (currentLeftColumnIndex + Math.floor((eventX - rowHeaderWidth) / columnWidth));
            ArrayList<TableEvent> columnEvents = events.get(columnIndex);
            if(columnEvents!=null){

                float eventTop, eventBottom;
                for(TableEvent tableEvent:columnEvents){
                    eventTop = tableEvent.getTop(rowHeight) + columnHeaderHeight;
                    eventBottom = eventTop + tableEvent.getLength(rowHeight);
                    if(eventTop <= eventY + positionY && eventBottom >= eventY + positionY){
                        if(eventDoubleClickListener!=null){
                            eventDoubleClickListener.onDoubleClick(tableEvent.scheduleEvent);
                            return true;
                        }
                    }
                }
            }
            if(calendarDoubleClickListener !=null){
                //calendarDoubleClickListener.onDoubleClick(getTimeFromPoint(eventX + positionX - rowHeaderWidth, eventY + positionY - columnHeaderHeight));
                calendarDoubleClickListener.onDoubleClick(getTime(columnIndex, eventY + positionY - columnHeaderHeight));
                return true;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if(getWaitingState()){
                return;
            }
            float eventX = e.getX();
            float eventY = e.getY();
            int columnIndex = (int) (currentLeftColumnIndex + Math.floor((eventX - rowHeaderWidth) / columnWidth));

            ArrayList<TableEvent> columnEvents = events.get(columnIndex);
            if(columnEvents!=null){

                float eventTop, eventBottom;
                for(TableEvent tableEvent:columnEvents){
                    eventTop = tableEvent.getTop(rowHeight) + columnHeaderHeight;
                    eventBottom = eventTop + tableEvent.getLength(rowHeight);
                    if(eventTop <= eventY + positionY && eventBottom >= eventY + positionY){
                        CustomDragShadowBuilder shadowBuilder = new CustomDragShadowBuilder();
                        startDragX = (columnIndex - currentLeftColumnIndex) * columnWidth + rowHeaderWidth;
                        initialDragColumnIndex = columnIndex;
                        dragColumnIndex = columnIndex;
                        try {
                            initialDragEvent = tableEvent;
                            draggedEvent = (TableEvent) tableEvent.clone();
                        } catch (CloneNotSupportedException e1) {
                            e1.printStackTrace();
                            return;
                        }
                        isDragEvent = true;
                        initialDragEvent.setPicked(true);
                        dragTouchOffset = eventY - eventTop + positionY;
                        startDrag(null, shadowBuilder, null, 0);
                        return;
                    }
                }
            }
            if(tableLongPressListener!=null){
                //tableLongPressListener.onLongPress(getTimeFromPoint(eventX + positionX - rowHeaderWidth, eventY + positionY - columnHeaderHeight));
                tableLongPressListener.onLongPress(getTime(columnIndex, eventY + positionY - columnHeaderHeight));
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(getWaitingState()){
               return true;
            }
            if(Math.abs(distanceX) < Math.abs(distanceY)){
                mScroller.startScroll((int) positionX, (int) positionY, 0, (int) distanceY);
                scrollDirection = ScrollDirection.NONE;
            } else {
                mScroller.startScroll((int) positionX, (int) positionY, (int) distanceX, 0);
                if(distanceX > 0){
                    scrollDirection = ScrollDirection.RIGHT;
                } else if(distanceX < 0){
                    scrollDirection = ScrollDirection.LEFT;
                } else {
                    scrollDirection = ScrollDirection.NONE;
                }
            }
            isFited = false;
            ViewCompat.postInvalidateOnAnimation(ScrollCalendarView.this);
            return true;
        }

    }

    private class CustomDragShadowBuilder extends DragShadowBuilder{

        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
        }
    }

    private class CustomOnDragListener implements OnDragListener{

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch(event.getAction()){
                case DragEvent.ACTION_DRAG_STARTED:
                    invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    int columnsDrag = (int) Math.floor((event.getX() - startDragX) / columnWidth);
                    if(columnsDrag!=0) {
                        startDragX += columnsDrag * columnWidth;
                        dragColumnIndex += columnsDrag;
                    }
                    actualDragPosition = Math.max(0, Math.min(tableHeight - draggedEvent.getLength(rowHeight), positionY + event.getY() - dragTouchOffset - columnHeaderHeight));

                    Date actualDate = getTime(dragColumnIndex, actualDragPosition);
                    draggedEvent.setStartHour((byte) actualDate.getHours());
                    draggedEvent.setStartMinute((byte) actualDate.getMinutes());
                    invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    //initialDragEvent.setPicked(false);
                    if(eventDragListener!=null){
                        setWaitingState(true);
                        eventDragListener.onDrop(new EventMover(initialDragEvent, draggedEvent, initialDragColumnIndex, dragColumnIndex));
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    /*draggedEvent = null;
                    isDragEvent = false;
                    invalidate();*/
                    return true;
            };
            return false;
        }
    }

    public ScrollCalendarView(Context context){
        super(context);
        this.context = context;
        init();
    }

    public ScrollCalendarView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.ScrollCalendarView, 0, 0);
        try {
            rowHeight = a.getInt(R.styleable.ScrollCalendarView_rowHeight, CUSTOM_ROW_HEIGHT);
            timeStep = a.getInt(R.styleable.ScrollCalendarView_timeStep, CUSTOM_TIME_STEP);
            visibleColumnCount = a.getInt(R.styleable.ScrollCalendarView_visibleColumnCount, CUSTOM_VISIBLE_COLUMN_COUNT);
            todayBackgroundColor = a.getInt(R.styleable.ScrollCalendarView_todayBackgroundColor, CUSTOM_TODAY_BACKGROUND_COLOR);
            nowLineColor = a.getColor(R.styleable.ScrollCalendarView_nowLineColor, CUSTOM_NOWLINE_COLOR);
            nowLineWidth = a.getColor(R.styleable.ScrollCalendarView_nowLineWidth, CUSTOM_NOWLINE_WIDTH);
            eventPadding = a.getInt(R.styleable.ScrollCalendarView_eventPadding, CUSTOM_EVENT_PADDING);
            emptyEventBackgroundColor = a.getColor(R.styleable.ScrollCalendarView_emptyEventBackgroundColor, CUSTOM_EMPTY_EVENT_BACKGROUND_COLOR);
            eventTextPadding = a.getInt(R.styleable.ScrollCalendarView_eventTextPadding, CUSTOM_EVENT_TEXT_PADDING);
            eventTextColor = a.getColor(R.styleable.ScrollCalendarView_eventTextColor, CUSTOM_EVENT_TEXT_COLOR);
            eventTextPct = a.getColor(R.styleable.ScrollCalendarView_eventTextPct, CUSTOM_EVENT_TEXT_PCT);
            rowHeaderPadding = a.getInt(R.styleable.ScrollCalendarView_rowHeaderPadding, CUSTOM_ROW_HEADER_PADDING);
            rowHeaderColor = a.getColor(R.styleable.ScrollCalendarView_rowHeaderColor, CUSTOM_ROW_HEADER_COLOR);
            rowHeaderTextColor = a.getColor(R.styleable.ScrollCalendarView_eventTextColor, CUSTOM_ROW_HEADER_TEXT_COLOR);
            rowHeaderTextPct = a.getInt(R.styleable.ScrollCalendarView_rowHeaderTextPct, CUSTOM_ROW_HEADER_TEXT_PCT);
            columnHeaderPadding = a.getInt(R.styleable.ScrollCalendarView_columnHeaderPadding, CUSTOM_COLUMN_HEADER_PADDING);
            columnHeaderTextColor = a.getColor(R.styleable.ScrollCalendarView_columnHeaderTextColor, CUSTOM_COLUMN_HEADER_TEXT_COLOR);
            columnHeaderColor = a.getColor(R.styleable.ScrollCalendarView_columnHeaderColor, CUSTOM_COLUMN_HEADER_COLOR);
            columnHeaderTextPct = a.getInt(R.styleable.ScrollCalendarView_columnHeaderTextPct, CUSTOM_COLUMN_HEADER_TEXT_PCT);
        } finally {
            a.recycle();
        }
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        viewWidth = getWidth();
        viewHeight = getHeight();
        minRowHeight = (float)(viewHeight - columnHeaderHeight) / 24;

        calculateColumns();

        rowHeaderTextSize = viewHeight * rowHeaderTextPct / 100;
        rowHeaderTextPaint.setTextSize(rowHeaderTextSize);

        columnHeaderTextSize = viewHeight * columnHeaderTextPct / 100;
        columnHeaderTextPaint.setTextSize(columnHeaderTextSize);

        eventTextSize = viewHeight * eventTextPct / 100;
        eventTextPaint.setTextSize(eventTextSize);

        calculateRowHeader();
        calculateColumnHeader();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateToday();
        Calendar calendar = Calendar.getInstance();

        if (positionY > (tableHeight - viewHeight + columnHeaderHeight)) {
            positionY = tableHeight - viewHeight + columnHeaderHeight;
        }

        backgroundPaint.setColor(rowHeaderColor);
        canvas.drawRect(0, 0 , rowHeaderWidth, columnHeaderHeight, backgroundPaint);

        //draw row headers
        float rowOffset;
        if(positionY == 0) {
            rowOffset = 0;
        } else {
            rowOffset = positionY % rowHeight;
        }
        currentTopRowIndex = (int) Math.floor(positionY / rowHeight);
        calendar.set(Calendar.HOUR_OF_DAY, currentTopRowIndex);
        calendar.set(Calendar.MINUTE, 0);
        int visibleRowsCount = (int) Math.floor(((float) (viewHeight - columnHeaderHeight)) / rowHeight) + 1;
        float currentRowTop;
        int timeDividers = 60 / timeStep;
        float timeDividerHeight = rowHeight / timeDividers;
        canvas.clipRect(0, columnHeaderHeight, rowHeaderWidth, viewHeight, Region.Op.REPLACE);
        for(int rowIndex = 0; rowIndex < visibleRowsCount + 1; rowIndex++){
            for(int dividerIndex = 0; dividerIndex < timeDividers; dividerIndex++){
                currentRowTop = columnHeaderHeight + rowHeight * rowIndex - rowOffset + dividerIndex * timeDividerHeight;
                backgroundPaint.setColor(rowHeaderColor);
                canvas.drawRect(0, currentRowTop, rowHeaderWidth, currentRowTop + timeDividerHeight, backgroundPaint);

                if(dividerIndex == 0){
                    rowHeaderTextPaint.setTypeface(defaultBoldTypeface);
                } else{
                    rowHeaderTextPaint.setTypeface(null);
                }
                canvas.drawText(dateTimeFormatter.timeToString(calendar.getTime()), (float)rowHeaderWidth / 2, currentRowTop + rowHeaderPadding + rowHeaderTextHeight, rowHeaderTextPaint);
                calendar.add(Calendar.MINUTE, timeStep);
            }
        }

        //draw column headers
        float columnOffset;
        if(positionX == 0){
            columnOffset = 0;
        } else {
            columnOffset = positionX % columnWidth;
            if(columnOffset < 0){
                columnOffset = columnWidth + columnOffset;
            }
        }
        float currentColumnLeft;
        calendar.setTime(today);
        calendar.add(Calendar.DATE, scrollDaysDifference);
        canvas.clipRect(rowHeaderWidth, 0, viewWidth, columnHeaderHeight, Region.Op.REPLACE);
        for(int columnIndex = 0; columnIndex <= visibleColumnCount+1; columnIndex++){
            currentColumnLeft = rowHeaderWidth + columnWidth * columnIndex - columnOffset;
            backgroundPaint.setColor(columnHeaderColor);
            canvas.drawRect(currentColumnLeft, 0,currentColumnLeft + columnWidth, columnHeaderHeight, backgroundPaint);
            canvas.drawText(dateTimeFormatter.dateToString(calendar.getTime()), currentColumnLeft + columnWidth / 2, (float)(columnHeaderHeight + columnHeaderTextHeight) /2 , columnHeaderTextPaint);
            calendar.add(Calendar.DATE, 1);
        }

        //draw table cells
        linePaint.setColor(hourLineColor);
        linePaint.setStrokeWidth(hourLineWidth);
        canvas.clipRect(rowHeaderWidth, columnHeaderHeight, viewWidth, viewHeight, Region.Op.REPLACE);
        for(int rowIndex = 0; rowIndex < visibleRowsCount + 1; rowIndex++){
            currentRowTop = columnHeaderHeight + rowHeight * rowIndex - rowOffset;
            canvas.drawLine(rowHeaderWidth, currentRowTop, viewWidth, currentRowTop, linePaint);
            for(int dividerIndex = 0; dividerIndex < timeDividers; dividerIndex++) {
                for (int columnIndex = 0; columnIndex < visibleColumnCount + 1; columnIndex++) {
                    if (currentLeftColumnIndex + columnIndex == todayIndex) {
                        backgroundPaint.setColor(todayBackgroundColor);
                    } else {
                        backgroundPaint.setColor(emptyEventBackgroundColor);
                    }
                    canvas.drawRect(rowHeaderWidth + columnWidth * columnIndex - columnOffset + eventPadding, currentRowTop + eventPadding, rowHeaderWidth + columnWidth * (columnIndex + 1) - columnOffset - eventPadding, currentRowTop + timeDividerHeight - eventPadding, backgroundPaint);
                }
                currentRowTop += timeDividerHeight;
            }
        }

        //draw events
        int measuredChars = 0;
        float eventTop, eventBottom;
        String eventText;
        for(int columnIndex = 0; columnIndex < visibleColumnCount+1; columnIndex++){
            ArrayList<TableEvent> currentColumnEvents = events.get(currentLeftColumnIndex + columnIndex);
            if(currentColumnEvents!=null){
                currentColumnLeft = rowHeaderWidth + columnWidth * columnIndex - columnOffset;
                for(TableEvent tableEvent : currentColumnEvents){

                    if(tableEvent.isPicked()){
                        continue;
                    }

                    eventTop = tableEvent.getTop(rowHeight) + columnHeaderHeight;
                    eventBottom = eventTop + tableEvent.getLength(rowHeight);
                    if(!(((positionY + viewHeight) < eventTop) || (positionY + columnHeaderHeight) > eventBottom)){
                        backgroundPaint.setColor(tableEvent.scheduleEvent.getColor());
                        canvas.clipRect(Math.max(rowHeaderWidth, currentColumnLeft), Math.max(eventTop - positionY, columnHeaderHeight), currentColumnLeft + columnWidth, eventBottom - positionY, Region.Op.REPLACE);
                        canvas.drawRect(currentColumnLeft, Math.max(eventTop - positionY, columnHeaderHeight), currentColumnLeft + columnWidth, eventBottom - positionY, backgroundPaint);

                        // draw scheduleEvent text
                        eventText = tableEvent.getScheduleEvent().getName();
                        StaticLayout textLayout = new StaticLayout(eventText, eventTextPaint, (int) columnWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        canvas.save();

                        canvas.translate(currentColumnLeft + eventTextPadding, eventTop - positionY + eventTextPadding);
                        textLayout.draw(canvas);
                        canvas.restore();

                    }
                }
            }
        }

        canvas.clipRect(rowHeaderWidth, columnHeaderHeight, viewWidth, viewHeight, Region.Op.REPLACE);

        //draw nowline
        if(currentLeftColumnIndex <= todayIndex && todayIndex <= currentLeftColumnIndex + visibleColumnCount){
            if(todayTimePosition >= positionY && todayTimePosition <= positionY + viewHeight){
                linePaint.setColor(nowLineColor);
                linePaint.setStrokeWidth(nowLineWidth);
                canvas.drawLine(rowHeaderWidth - scrollDaysDifference * columnWidth - columnOffset, columnHeaderHeight + todayTimePosition - positionY, rowHeaderWidth - scrollDaysDifference * columnWidth + columnWidth - columnOffset, columnHeaderHeight + todayTimePosition - positionY, linePaint);
            }
        }

        //draw drag element
        if(isDragEvent && draggedEvent!=null) {
            eventTop = draggedEvent.getTop(rowHeight) + columnHeaderHeight;
            eventBottom = eventTop + draggedEvent.getLength(rowHeight);
            currentColumnLeft = rowHeaderWidth + columnWidth * (dragColumnIndex - currentLeftColumnIndex) - columnOffset;
            if (!(((positionY + viewHeight) < eventTop) || (positionY + columnHeaderHeight) > eventBottom)) {
                backgroundPaint.setColor(draggedEvent.scheduleEvent.getColor());
                //canvas.clipRect(Math.max(rowHeaderWidth, currentColumnLeft), Math.max(eventTop - positionY, columnHeaderHeight), currentColumnLeft + columnWidth, eventBottom - positionY, Region.Op.REPLACE);
                canvas.drawRect(currentColumnLeft, Math.max(eventTop - positionY, columnHeaderHeight), currentColumnLeft + columnWidth, eventBottom - positionY, backgroundPaint);

                // draw scheduleEvent text
                eventText = draggedEvent.getScheduleEvent().getName();
                StaticLayout textLayout = new StaticLayout(eventText, eventTextPaint, (int) columnWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                canvas.save();

                canvas.translate(currentColumnLeft + eventTextPadding, eventTop - positionY + eventTextPadding);
                textLayout.draw(canvas);
                canvas.restore();
            }

            //draw drag time line
            if(actualDragPosition > 0){
                canvas.drawText(dateTimeFormatter.timeToString(draggedEvent.getStartHour(), draggedEvent.getStartMinute()), startDragX + columnWidth / 2, actualDragPosition - positionY + columnHeaderHeight , rowHeaderTextPaint);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(getWaitingState()){
            return false;
        }
        scaleGestureDetector.onTouchEvent(event);
        if(!isScale) {
            mGestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                fitColumn();
            }
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if(isFited){
                positionX = mScroller.getCurrX();
                positionY = mScroller.getCurrY();
            } else {
                positionX = mScroller.getFinalX();
                positionY = mScroller.getFinalY();
            }
            if (positionY < 0) {
                positionY = 0;
            }
            checkCurrentPeriodIndex();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    // SERVICE METHODS

    private void init(){
        mScroller = new OverScroller(context);
        mGestureDetector = new GestureDetector(context, new CustomSimpleGestureListener());
        dateTimeFormatter = new SimpleDateTimeFormatter();
        scaleGestureDetector = new ScaleGestureDetector(context, new CustomOnScaleGestureListener());

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);

        rowHeaderTextPaint = new TextPaint();
        rowHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        rowHeaderTextPaint.setColor(rowHeaderTextColor);

        columnHeaderTextPaint = new TextPaint();
        columnHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        columnHeaderTextPaint.setColor(columnHeaderTextColor);

        eventTextPaint = new TextPaint();
        eventTextPaint.setColor(eventTextColor);
        eventTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        defaultBoldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);

        calculateRows();
        calculateRowHeader();
        calculateColumnHeader();

        updateToday();
        goToDate(today);
        currentPeriodIndex = todayIndex;

        setOnDragListener(new CustomOnDragListener());
    }

    private void checkCurrentPeriodIndex() {
        calculateColumnIndex();
        if((currentLeftColumnIndex + visibleColumnCount) >= (currentPeriodIndex + periodLength) || currentLeftColumnIndex <= (currentPeriodIndex - periodLength)){
            currentPeriodIndex = currentLeftColumnIndex;
            updateEvents();
        }
    }

    private void updateEvents() {
        if(eventsLoader!=null){
            setWaitingState(true);
            events.clear();
            Calendar startLoadDate = indexToDate(currentPeriodIndex - periodLength);
            Calendar endLoadDate = indexToDate(currentPeriodIndex + periodLength);
            eventsLoader.loadEvents(startLoadDate, endLoadDate);
        }
    }

    private void goToDate(Date date){
        currentLeftColumnIndex = dateToIndex(date);
        positionX = currentLeftColumnIndex * columnWidth;
        positionY = getTimePositionFromDate(date);
        invalidate();
    }

    private void updateToday(){
        today = Calendar.getInstance().getTime();
        todayIndex = dateToIndex(today);
        todayTimePosition = getTimePositionFromDate(today);
    }

    private void fitColumn(){
        if(!getWaitingState()) {
            mScroller.forceFinished(true);
            calculateColumnIndex();
            switch (scrollDirection) {
                case RIGHT:
                    scrollDaysDifference++;
                    break;
                case LEFT:
                    break;
                default:
                    break;
            }
            mScroller.startScroll((int) positionX, (int) positionY, (int) (Math.ceil(scrollDaysDifference * columnWidth)) - (int) Math.floor(positionX), 0, 250);
            isFited = true;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    //LOCAL CALCULATIONS

    private void calculateRows() {
        tableHeight = Math.max(24 * rowHeight, viewHeight - columnHeaderHeight);
    }

    private void calculateColumns(){
        columnWidth = (viewWidth - (float)rowHeaderWidth) / visibleColumnCount;
    }

    private void calculateRowHeader(){
        Rect rowHeaderBounds = new Rect();
        String timeString = dateTimeFormatter.timeToString(0,0);
        rowHeaderTextPaint.getTextBounds(timeString, 0, timeString.length(), rowHeaderBounds);
        rowHeaderWidth = rowHeaderBounds.right + rowHeaderPadding * 2;
        rowHeaderTextHeight = - rowHeaderBounds.top;
    }

    private void calculateColumnHeader(){
        Rect columnHeaderBounds = new Rect();
        String dateString = dateTimeFormatter.dateToString(Calendar.getInstance().getTime());
        columnHeaderTextPaint.getTextBounds(dateString, 0, dateString.length(), columnHeaderBounds);
        columnHeaderTextHeight = -columnHeaderBounds.top;
        columnHeaderHeight = columnHeaderTextHeight + columnHeaderPadding * 2;
    }

    private void calculateColumnIndex(){
        scrollDaysDifference = (int) Math.floor(positionX / columnWidth);
        currentLeftColumnIndex = todayIndex + scrollDaysDifference;
    }


    // DATE & TIME POSITION

    public Date getTimeFromPoint(float x, float y){
        int hours  = (int) Math.floor(y / rowHeight);
        int minutes = (int) ((y % rowHeight) * 60 / rowHeight);
        int daysLeft = (int) Math.floor(x / columnWidth);
        Calendar result = Calendar.getInstance();
        result.setTime(today);
        result.set(Calendar.HOUR_OF_DAY, hours);
        result.set(Calendar.MINUTE, minutes);
        result.add(Calendar.DATE, daysLeft);
        return result.getTime();
    }

    public Date getTime(int columnIndex, float timePosition){
        Calendar calendar = indexToDate(columnIndex);
        int hours  = (int) Math.floor(timePosition / rowHeight);
        int minutes = (int) ((timePosition % rowHeight) * 60 / rowHeight);
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    private int getDaysDifference(Date date1, Date date2){
        float msDifference  = date2.getTime() - date1.getTime();
        return (int) Math.floor(msDifference / (1000 * 60 * 60 * 24));
    }

    private int dateToIndex(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR);
    }

    private Calendar indexToDate(int index){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, index / 1000);
        calendar.set(Calendar.DAY_OF_YEAR, index % 1000);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    public int getTimePositionFromDate(Date date){
        return (int) (date.getHours() * rowHeight + date.getMinutes() * (rowHeight / 60) + date.getSeconds() * (rowHeight / (3600)));
    }


    // GETTERS & SETTERS

    private void setWaitingState(boolean state){
        waitingState = state;
        if(waitingStateListener !=null) {
            waitingStateListener.onWaitingStateChange(state);
        }
    }

    private boolean getWaitingState(){
        return waitingState;
    }

    public EventsLoader getEventsLoader() {
        return eventsLoader;
    }

    public void setEventsLoader(EventsLoader eventsLoader) {
        this.eventsLoader = eventsLoader;
        eventsLoader.setOnLoadListener(this);
        calculateColumnIndex();
        updateEvents();
    }

    public WaitingStateListener getWaitingStateListener() {
        return waitingStateListener;
    }

    public void setWaitingStateListener(WaitingStateListener waitingStateListener) {
        this.waitingStateListener = waitingStateListener;
    }

    public int getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(int timeStep) throws Exception {
        if(timeStep < 0 || timeStep > 60){
            throw new IllegalArgumentException("Time step must be beetwen 0 and 60");
        }
        if(60 % timeStep !=0){
            throw new IllegalArgumentException("Time step must be divider of 60");
        }
        if(timeStep % 5 != 0){
            throw new IllegalArgumentException("Time step must be multiplier of 5!");
        }
        this.timeStep = timeStep;
        calculateRows();
        invalidate();
    }

    public float getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(float rowHeight) {
        this.rowHeight = rowHeight;
        calculateRows();
        //updatTableEvents();
        invalidate();
    }

    public int getVisibleColumnCount() {
        return visibleColumnCount;
    }

    public void setVisibleColumnCount(int visibleColumnCount) {
        this.visibleColumnCount = visibleColumnCount;
        calculateColumns();
        fitColumn();
    }

    public EventSingleClickListener getEventSingleClickListener() {
        return eventSingleClickListener;
    }

    public void setEventSingleClickListener(EventSingleClickListener eventSingleClickListener) {
        this.eventSingleClickListener = eventSingleClickListener;
    }

    public TableSingleClickListener getTableSingleClickListener() {
        return tableSingleClickListener;
    }

    public void setTableSingleClickListener(TableSingleClickListener tableSingleClickListener) {
        this.tableSingleClickListener = tableSingleClickListener;
    }

    public EventDoubleClickListener getEventDoubleClickListener() {
        return eventDoubleClickListener;
    }

    public void setEventDoubleClickListener(EventDoubleClickListener eventDoubleClickListener) {
        this.eventDoubleClickListener = eventDoubleClickListener;
    }

    public CalendarDoubleClickListener getCalendarDoubleClickListener() {
        return calendarDoubleClickListener;
    }

    public void setCalendarDoubleClickListener(CalendarDoubleClickListener calendarDoubleClickListener) {
        this.calendarDoubleClickListener = calendarDoubleClickListener;
    }

    public TableLongPressListener getTableLongPressListener() {
        return tableLongPressListener;
    }

    public void setTableLongPressListener(TableLongPressListener tableLongPressListener) {
        this.tableLongPressListener = tableLongPressListener;
    }

    public void setEventDragListener(EventDragListener eventDragListener) {
        this.eventDragListener = eventDragListener;
    }


    // TABLE EVENT
    private class TableEvent implements Cloneable{

        private ScheduleEvent scheduleEvent;

        private byte startHour;
        private byte startMinute;

        private long length; //in minutes
        private boolean picked = false;

        TableEvent(ScheduleEvent scheduleEvent){
            if(scheduleEvent.getStart()==null || scheduleEvent.getEnd()==null){
                throw new IllegalArgumentException("ScheduleEvent must have start and end!");
            }
            setScheduleEvent(scheduleEvent);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(scheduleEvent.getStart());
            setStartHour((byte) calendar.get(Calendar.HOUR_OF_DAY));
            setStartMinute((byte) calendar.get(Calendar.MINUTE));
            long timeDiff = scheduleEvent.getEnd().getTime() - scheduleEvent.getStart().getTime();
            setLength(timeDiff / (60 * 1000));
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public byte getStartHour() {
            return startHour;
        }

        public void setStartHour(byte startHour) {
            this.startHour = startHour;
        }

        public byte getStartMinute() {
            return startMinute;
        }

        public void setStartMinute(byte startMinute) {
            this.startMinute = startMinute;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public float getTop(float hourHeight){
            return getStartHour() * hourHeight + getStartMinute() * hourHeight / 60;
        }

        public float getBottom(float hourHeight){
            return getStartHour() * hourHeight + getLength() * hourHeight / 60;
        }

        public float getLength(float hourHeight){
            return hourHeight * getLength() / 60;
        }

        public ScheduleEvent getScheduleEvent() {
            return scheduleEvent;
        }

        public void setScheduleEvent(ScheduleEvent scheduleEvent) {
            this.scheduleEvent = scheduleEvent;
        }

        public boolean isPicked() {
            return picked;
        }

        public void setPicked(boolean picked) {
            this.picked = picked;
        }
    }

    public class EventMover {

        private TableEvent oldEvent;
        private TableEvent newEvent;
        private int oldIndex;
        private int newIndex;


        protected EventMover(TableEvent oldEvent, TableEvent newEvent, int oldIndex, int newIndex){
            this.oldEvent = oldEvent;
            this.newEvent = newEvent;
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }

        public ScheduleEvent getEvent(){
            if(oldEvent!=null){
                return oldEvent.getScheduleEvent();
            } else return null;
        }

        public void decline(){
            //do nothing
            setWaitingState(false);
            oldEvent.setPicked(false);
            draggedEvent = null;
            isDragEvent = false;
            invalidate();
        }

        public void accept() throws ScheduleEvent.EventsIntersectionException {
            //move scheduleEvent
            setWaitingState(false);
            oldEvent.setPicked(false);
            draggedEvent = null;
            isDragEvent = false;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(indexToDate(newIndex).getTime());
            calendar.set(Calendar.HOUR_OF_DAY, newEvent.getStartHour());
            calendar.set(Calendar.MINUTE, newEvent.getStartMinute());
            calendar.set(Calendar.SECOND, 0);
            newEvent.getScheduleEvent().setStart(calendar.getTime());

            calendar.add(Calendar.MINUTE, (int) newEvent.getLength());
            newEvent.getScheduleEvent().setEnd(calendar.getTime());

            if(newEvent.getScheduleEvent().checkDates()){
                ArrayList<TableEvent> tableEvents = events.get(oldIndex);
                try {
                    addTableEvent(newEvent.getScheduleEvent());
                } catch (ScheduleEvent.EventsIntersectionException e) {
                    invalidate();
                    throw e;
                }
                tableEvents.remove(oldEvent);
            }

            invalidate();
        }
    }

    private void addTableEvent(ScheduleEvent scheduleEvent) throws ScheduleEvent.EventsIntersectionException {
        ArrayList<TableEvent> tableEvents = events.get(dateToIndex(scheduleEvent.getStart()));
        if(tableEvents==null){
            tableEvents = new ArrayList<>();
            events.put(dateToIndex(scheduleEvent.getStart()), tableEvents);
        }
        if(scheduleEvent.getStart()!=null || scheduleEvent.getEnd()!=null){
            //search intersections
            for(TableEvent curEvent: tableEvents){
                if(ScheduleEvent.eventsIntersect(curEvent.getScheduleEvent(), scheduleEvent)){
                    throw new ScheduleEvent.EventsIntersectionException();
                }
            }
            TableEvent tableEvent = new TableEvent(scheduleEvent);
            tableEvents.add(tableEvent);
        }
    }

    // INTERFACE IMPLEMENTATIONS

    @Override
    public void onLoad(ArrayList<ScheduleEvent> scheduleEvents) throws ScheduleEvent.EventsIntersectionException {
        try {
            for (ScheduleEvent scheduleEvent : scheduleEvents) {
                addTableEvent(scheduleEvent);
            }
        } catch (ScheduleEvent.EventsIntersectionException e) {
            throw e;
        } finally {
            setWaitingState(false);
            invalidate();
        }
    }

}
