package com.example.smith.attendenceportal;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;

public class MyOutlineProvider extends ViewOutlineProvider {
    private float radius;
    private Resources mResources;
    public MyOutlineProvider(float rad, Resources resources) {
        radius = rad;
        mResources = resources;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        Path path;
        view.setElevation(mResources.getDimensionPixelSize(R.dimen.elevation));
        path = getPath(view,mResources.getDimensionPixelSize(R.dimen.radius),true,true,true,true);
        outline.setConvexPath(path);

    }

    private Path getPath(View v,float radius, boolean topLeft, boolean topRight,
                         boolean bottomRight, boolean bottomLeft) {

        final Path path = new Path();
        final float[] radii = new float[8];

        if (topLeft) {
            radii[0] = radius;
            radii[1] = radius;
        }

        if (topRight) {
            radii[2] = radius;
            radii[3] = radius;
        }

        if (bottomRight) {
            radii[4] = 0;
            radii[5] = 0;
        }

        if (bottomLeft) {
            radii[6] = 0;
            radii[7] = 0;
        }

        path.addRoundRect(new RectF(0, 0, v.getWidth(), v.getHeight()),
                radii, Path.Direction.CW);
        return path;
    }
}
