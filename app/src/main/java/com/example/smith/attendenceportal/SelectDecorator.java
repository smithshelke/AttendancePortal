package com.example.smith.attendenceportal;

import android.content.Context;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class SelectDecorator implements DayViewDecorator {

    private final Context context;
    private  boolean selection;


    SelectDecorator(Context context) {
        this.context = context;
    }


    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true;
    }

    @Override
    public void decorate(DayViewFacade view) {
            view.setSelectionDrawable(Objects.requireNonNull(context.getDrawable(R.drawable.my_selector)));
    }
}