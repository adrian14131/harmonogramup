package pl.ozog.harmonogramup.listeners;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeListener implements View.OnTouchListener{

    private final GestureDetector gestureDetector;
    private static final String TAG = "OnSwipeListener";
    protected OnSwipeListener(Context context){
        this.gestureDetector = new GestureDetector(context, new GestureListener());

    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener{

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_SPEED = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float dX = e2.getX() - e1.getX();
            float dY = e2.getY() - e1.getY();

            if(Math.abs(dX) > Math.abs(dY)){
                if(Math.abs(dX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_SPEED){
                    if(dX > 0)
                        onSwipeRight();
                    else if(dX <0)
                        onSwipeLeft();
                }
            }
            return false;
        }

    }
    public void onSwipeRight(){

    }
    public void onSwipeLeft(){

    }
}
