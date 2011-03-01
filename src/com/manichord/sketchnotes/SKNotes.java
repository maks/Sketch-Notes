package com.manichord.sketchnotes;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SKNotes extends Activity {

	/** Menu ID for the command to clear the window. */
	private static final int CLEAR_ID = Menu.FIRST;
	/** Menu ID for the command to toggle fading. */
	private static final int PAGELIST_ID = Menu.FIRST + 1;
	private static final String TAG = "SKNotes";

	/** The view responsible for drawing the window. */
	SketchView sView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		sView = new SketchView(this);
		setContentView(sView);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CLEAR_ID, 0, "Clear");
		menu.add(0, PAGELIST_ID, 0, "Pages");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CLEAR_ID:
			if (sView != null) {
				sView.clear();
			} else {
				Log.e(TAG, "NO Sketch VIEW!!!");
			}
			return true;
		case PAGELIST_ID:
			// TODO: show list of saved notes
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class SketchView extends View implements OnTouchListener {

		private static final String TAG = "SketchView";

		List<Point> points = new ArrayList<Point>();

		private static final int FADE_ALPHA = 0x06;

		private Bitmap mBitmap;
		private Canvas mCanvas;
		private final Rect mRect = new Rect();
		private final Paint pagePainter;
		private final Paint gridPainter;
		private final Paint penPainter;

	
		int width = 0;
		int height = 0;
		int pass = 0;
		int xpos = 0;
		int ypos = 0;
		float penWidth = 0;

		public SketchView(Context context) {
			super(context);

			setFocusable(true);
			pagePainter = new Paint();
			pagePainter.setAntiAlias(true);
			pagePainter.setColor(getResources().getColor(R.color.page_colour));

			gridPainter = new Paint();
			gridPainter.setColor(getResources().getColor(R.color.grid_colour));
			gridPainter.setStyle(Style.STROKE);

			penPainter = new Paint();
			penPainter.setColor(getResources().getColor(R.color.pen_colour));
			// penPainter.setStyle(Style.STROKE);

			// square graph paper:
			Resources res = getResources();
			xpos = Math.round(res.getDimension(R.dimen.grid_size));
			ypos = Math.round(res.getDimension(R.dimen.grid_size));

			penWidth = res.getDimension(R.dimen.pen_size);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, 0, 0, null);
			} else {
				Log.e(TAG, "NO BITMAP!");
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float mCurX;
			float mCurY;

			// API level 9 and above supports "Major" property on event which
			// gives
			// the size of the touch area at the point of contact
			// so for now we just hard code
			float TOUCH_AREA_SIZE = penWidth;

			int action = event.getAction();
			if (action != MotionEvent.ACTION_UP
					&& action != MotionEvent.ACTION_CANCEL) {
				int N = event.getHistorySize();
				int P = event.getPointerCount();
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < P; j++) {
						mCurX = event.getHistoricalX(j, i);
						mCurY = event.getHistoricalY(j, i);
						drawPoint(mCurX, mCurY,
								event.getHistoricalPressure(j, i),
								TOUCH_AREA_SIZE);
					}
				}
				for (int j = 0; j < P; j++) {
					mCurX = event.getX(j);
					mCurY = event.getY(j);
					drawPoint(mCurX, mCurY, event.getPressure(j),
							TOUCH_AREA_SIZE);
				}
			}
			return true;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return false;
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {

			Log.i(TAG, "Size changed w:" + w + " h:" + h);

			int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			int curH = mBitmap != null ? mBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;

			Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
					Bitmap.Config.RGB_565);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (mBitmap != null) {
				newCanvas.drawBitmap(mBitmap, 0, 0, null);
			}
			mBitmap = newBitmap;
			mCanvas = newCanvas;

			drawPageGrid();
		}

		public void clear() {
			if (mCanvas != null) {
				Paint clearer = new Paint();
				clearer.setARGB(0xff, 0, 0, 0);
				mCanvas.drawPaint(clearer);
				drawPageGrid();
				invalidate();
			}
		}

		private void drawPageGrid() {
			// make the entire canvas page colour
			mCanvas.drawPaint(pagePainter);

			// Draw Background Grid
			Display display = getWindowManager().getDefaultDisplay();
			width = display.getWidth();// start
			height = display.getHeight();// end

			for (int i = 0; i < width; i += xpos) {
				mCanvas.drawLine(i, 0, i, height, gridPainter);
			}

			for (int i = 0; i < height; i += ypos) {
				mCanvas.drawLine(0, i, width, i, gridPainter);
			}
		}

		private void drawPoint(float x, float y, float pressure, float width) {
			Log.i("TouchPaint", "Drawing: " + x + "x" + y + " p=" + pressure
					+ " width=" + width);
			if (width < 1)
				width = 1;
			if (mBitmap != null) {
				float radius = width / 2;

				// TODO: need to test on a device that supports pressure
				// sensitive input
				// int pressureLevel = (pressure == 0) ? 255 : (int)(pressure *
				// 255);
				// penPainter.setARGB(pressureLevel, 255, 0, 0);

				mCanvas.drawCircle(x, y, radius, penPainter);
				mRect.set((int) (x - radius - 2), (int) (y - radius - 2),
						(int) (x + radius + 2), (int) (y + radius + 2));
				invalidate(mRect);
			}
		}

	}

}
