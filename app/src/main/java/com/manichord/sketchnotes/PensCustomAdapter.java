package com.manichord.sketchnotes;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom adapter for Coloured Pens in Sketchnotes
 */

public class PensCustomAdapter extends ArrayAdapter<PenModel> {

    public PensCustomAdapter(@NonNull Context context, ArrayList<PenModel> pens) {
        super(context, R.layout.support_simple_spinner_dropdown_item, pens);
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        PenModel pen = getItem(position);
        row.setBackgroundColor(pen.getPenColour());
        ((TextView)row.findViewById(android.R.id.text1)).setText(pen.getPenName());
        ((TextView)row.findViewById(android.R.id.text1)).setTextColor(getContrastColor(pen.getPenColour()));
        return row;
    }

    // https://stackoverflow.com/a/41335343/85472
    private static int getContrastColor(int color) {
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return a < 0.5 ? Color.BLACK : Color.WHITE;
    }
}

