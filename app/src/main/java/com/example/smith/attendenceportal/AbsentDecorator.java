package com.example.smith.attendenceportal;

import android.content.Context;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

public class AbsentDecorator implements DayViewDecorator {

    private final int color;
    private final HashSet<CalendarDay> dates;
    private final Context context;
    private  boolean selection;


    AbsentDecorator(int color, Collection<CalendarDay> dates, Context context) {
        this.color = color;
        this.dates = new HashSet<>(dates);
        this.context = context;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
            view.addSpan(new TriangleSpan(this.color));
    }
}