package com.manichord.sketchnotes;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SKNotes extends Activity {

	/** The view responsible for drawing the window. */
	SketchView sView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(new SketchView(this));

	}

	public class SketchView extends View implements OnTouchListener {

		private static final String TAG = "SketchView";
		
		List<Point> points = new ArrayList<Point>();
		
		private Path circle;

		private Path grid;

//		private final Paint cPaint;
//		private final Paint tPaint;
		
		private static final int FADE_ALPHA = 0x06;
        private static final int MAX_FADE_STEPS = 256/FADE_ALPHA + 4;
        private static final int TRACKBALL_SCALE = 10;

        private Bitmap mBitmap;
        private Canvas mCanvas;
        private final Rect mRect = new Rect();
        private final Paint mPaint;
        private final Paint mFadePaint;
        private float mCurX;
        private float mCurY;
        private int mFadeSteps = MAX_FADE_STEPS;

		int width = 0;
		int height = 0;
		int pass = 0;
		int xpos = 0;
		int ypos = 0;

		public SketchView(Context context) {
			super(context);
			
//			setFocusable(true);
//	        setFocusableInTouchMode(true);
//			this.setOnTouchListener(this);
//
//			int color = Color.BLUE; // solid blue
//
//			cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//			cPaint.setStyle(Paint.Style.STROKE);
//			cPaint.setColor(color);
//			cPaint.setStrokeWidth(1);
//
//			tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			
			setFocusable(true);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setARGB(255, 255, 255, 255);
            mFadePaint = new Paint();
            mFadePaint.setDither(true);
            mFadePaint.setARGB(FADE_ALPHA, 0, 0, 0);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
            } else {
            	Log.e(TAG, "NO BITMAP!");
            }
			
			// Drawing commands go here

			// custom drawing code here
			// remember: y increases from top to bottom
			// x increases from left to right

//			Paint paint = new Paint();
//			paint.setStyle(Paint.Style.FILL);
//
//			// make the entire canvas white
//			paint.setColor(Color.WHITE);
//			canvas.drawPaint(paint);
//
//			Display display = getWindowManager().getDefaultDisplay();
//			width = display.getWidth();// start
//			height = display.getHeight();// end
//
//			int NUM_OF_LINES = 30;
//
//			xpos = width / NUM_OF_LINES;
//			ypos = height / NUM_OF_LINES;
//
//			for (int i = 0; i < NUM_OF_LINES; i++) {
//
//				paint.setColor(Color.BLUE);
//				canvas.drawLine(xpos + (xpos * i), 0, xpos + (xpos * i),
//						height, paint);
//
//			}
//			paint.setStyle(Style.STROKE);
//			for (int i = 0; i < NUM_OF_LINES; i++) {
//				paint.setColor(Color.BLUE);
//				canvas.drawLine(0, (ypos * pass) + 5, width, (ypos * pass) + 5,
//						paint);
//				pass++;
//			}
//
//			for (Point point : points) {
//				canvas.drawCircle(point.x, point.y, 1, paint);
//				Log.d(TAG, "Painting: "+point);
//			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int action = event.getAction();
            if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
                int N = event.getHistorySize();
                int P = event.getPointerCount();
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < P; j++) {
                        mCurX = event.getHistoricalX(j, i);
                        mCurY = event.getHistoricalY(j, i);
                        drawPoint(mCurX, mCurY,
                                event.getHistoricalPressure(j, i),
                                1);
                    }
                }
                for (int j = 0; j < P; j++) {
                    mCurX = event.getX(j);
                    mCurY = event.getY(j);
                    drawPoint(mCurX, mCurY, event.getPressure(j), 1);
                }
            }
            return true;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			int N = event.getHistorySize();
			
	        Point point = new Point();
	        point.x = (int) event.getX();
	        point.y = (int) event.getY();
	        points.add(point);
	        invalidate();
	        Log.d(TAG, "point: " + point);
	        return true;
		}
		
		@Override protected void onSizeChanged(int w, int h, int oldw,
                int oldh) {
			
			Log.i(TAG, "Size changed");
			
            int curW = mBitmap != null ? mBitmap.getWidth() : 0;
            int curH = mBitmap != null ? mBitmap.getHeight() : 0;
            if (curW >= w && curH >= h) {
                return;
            }

            if (curW < w) curW = w;
            if (curH < h) curH = h;

            Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
                                                   Bitmap.Config.RGB_565);
            Canvas newCanvas = new Canvas();
            newCanvas.setBitmap(newBitmap);
            if (mBitmap != null) {
                newCanvas.drawBitmap(mBitmap, 0, 0, null);
            }
            mBitmap = newBitmap;
            mCanvas = newCanvas;
            
            // make the entire canvas white
			mPaint.setColor(Color.WHITE);
			mCanvas.drawPaint(mPaint);
			mPaint.setStyle(Style.STROKE);
			
			//Draw Background Grid
			Display display = getWindowManager().getDefaultDisplay();
			width = display.getWidth();// start
			height = display.getHeight();// end

			int NUM_OF_LINES = width / 10;

			xpos = width / NUM_OF_LINES;
			ypos = height / NUM_OF_LINES;

			for (int i = 0; i < NUM_OF_LINES; i++) {
				mPaint.setColor(Color.BLUE);
				mCanvas.drawLine(xpos + (xpos * i), 0, xpos + (xpos * i),
						height, mPaint);
			}
			
			for (int i = 0; i < NUM_OF_LINES; i++) {
				mPaint.setColor(Color.BLUE);
				mCanvas.drawLine(0, (ypos * pass) + 5, width, (ypos * pass) + 5,
						mPaint);
				pass++;
			}
            
        }
		
		private void drawPoint(float x, float y, float pressure, float width) {
            Log.i("TouchPaint", "Drawing: " + x + "x" + y + " p="
                    + pressure + " width=" + width);
            if (width < 1) width = 1;
            if (mBitmap != null) {
                float radius = width / 2;
                int pressureLevel = (pressure == 0) ? 255 : (int)(pressure * 255);
                mPaint.setARGB(pressureLevel, 255, 0, 0);
                
                mCanvas.drawCircle(x, y, radius, mPaint);
                mRect.set((int) (x - radius - 2), (int) (y - radius - 2),
                        (int) (x + radius + 2), (int) (y + radius + 2));
                invalidate(mRect);
            }
        }

	}

}
