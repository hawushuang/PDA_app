package lecho.lib.hellocharts.listener;


import lecho.lib.hellocharts.model.PointValue;

public interface OnTouchPointListener {

    /**
     * Called only in chart selection mode when user touch empty space causing value deselection.
     * Note: this method is not called when selection mode is disabled.
     */
    public void onPointTouch(PointValue point);
}
