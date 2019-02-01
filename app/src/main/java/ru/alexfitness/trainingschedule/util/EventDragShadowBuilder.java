package ru.alexfitness.trainingschedule.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.View;

import com.alamkanak.weekview.WeekViewEvent;

public class EventDragShadowBuilder extends View.DragShadowBuilder {

    private RectF rectF;
    private Paint paint;

    public EventDragShadowBuilder(WeekViewEvent event, RectF rectF){
        super();
        this.rectF = new RectF(0, 0, rectF.width(), rectF.height());
        paint = new Paint();
        paint.setColor(event.getColor());
    }

    @Override
    public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
        int height, width;
        width = (int) rectF.width();
        height = (int) rectF.height();

        outShadowSize.set(width, height);
        outShadowTouchPoint.set(width/2, height/2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        canvas.drawRoundRect(rectF, 10, 10, paint);
    }
}
