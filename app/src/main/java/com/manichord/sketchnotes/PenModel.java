package com.manichord.sketchnotes;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by maks on 14/09/17.
 */

class PenModel {

    private int colour;
    private String name;

    PenModel(int colour, String name) {
        this.colour = colour;
        this.name = name;
    }

    public int getPenColour() {
        return colour;
    }

    public String getPenName() {
        return name;
    }

    public static ArrayList<PenModel> getPens(Context context) {
        int[] penColours = context.getResources().getIntArray(R.array.penColourList);
        String[] penNames = context.getResources().getStringArray(R.array.penNameList);
        ArrayList<PenModel> pensList = new ArrayList<>(penColours.length);
        for (int i=0; i < penColours.length; i++) {
            pensList.add(new PenModel(penColours[i], penNames[i]));
        }
        return pensList;
    }

}
