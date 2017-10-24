package com.manichord.sketchnotes;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * View Model for the currently selected pen type
 */
public class PenSelectionSpinnerModel extends BaseObservable {

    private final SketchView mView;

    public PenSelectionSpinnerModel(SketchView view) {
        mView = view;
    }

    public void setPenPos(int pos) {
        mView.setCurrentPenIndex(pos);
        notifyPropertyChanged(BR.spinModel);
    }

    @Bindable
    public int getPenPos() {
        return mView.getCurrentPenIndex();
    }

}
