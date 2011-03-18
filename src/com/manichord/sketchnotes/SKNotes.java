package com.manichord.sketchnotes;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SKNotes extends Activity {

	private static final String TAG = "SKNotes";

	/** Menu ID for the command to clear the window. */
	private static final int CLEAR_ID = Menu.FIRST;

	/** Menu ID for the command to list pages */
	private static final int PAGELIST_ID = Menu.FIRST + 1;

	/** Menu ID for the command to Save current page */
	private static final int SETTINGS_ID = Menu.FIRST + 3;

	protected static final String LOAD_FILENAME = "com.manichord.sketchnotes.load_filename";

	/** The view responsible for drawing the window. */
	SketchView sView;
	
	private String mCurrentFileName;
	
	private long lastSaveTime;
	
	private int SAVE_DELAY = 30 * 1000; //30sec

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		sView = new SketchView(this);
		setContentView(sView);
		Date now = new Date();
		
		String fileToLoad = getIntent().getStringExtra(LOAD_FILENAME);
		if (fileToLoad != null && !"".equals(fileToLoad)) {
			mCurrentFileName = fileToLoad;
		} else {				
			mCurrentFileName = "skpage"+now.getTime()+".png";
		}
		
		lastSaveTime = now.getTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CLEAR_ID, 0, "New");
		menu.add(0, PAGELIST_ID, 0, "Pages");
		menu.add(0, SETTINGS_ID, 0, "Settings");
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CLEAR_ID:
			if (sView != null) {
				Date now = new Date();
				mCurrentFileName = "skpage"+now.getTime()+".png";
				sView.clear();
			} else {
				Log.e(TAG, "NO Sketch VIEW!!!");
			}
			return true;
		case PAGELIST_ID:
			Intent intent = new Intent(this, PagesList.class);
			startActivity(intent);
			return true;
		case SETTINGS_ID:
			//TODO
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sView.saveCurrentBitMap();
	}
	

	public class SketchView extends View implements OnTouchListener {

		private static final String TAG = "SketchView";

		List<Point> points = new ArrayList<Point>();

		private Bitmap mBitmap;
		private Canvas mCanvas;
		
		private Bitmap mBackgroundBitmap;
		private Canvas mBackgroundCanvas;
		
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

		private String FILENAME = "saved_bitmap";
		
		private boolean unsaved = false;

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
			penPainter.setColor(getResources().getColor(
					R.color.pen_colour_bluepen));

			// square graph paper:
			Resources res = getResources();
			xpos = Math.round(res.getDimension(R.dimen.grid_size));
			ypos = Math.round(res.getDimension(R.dimen.grid_size));

			penWidth = res.getDimension(R.dimen.pen_size);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mBackgroundCanvas != null) {
				canvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
			} else {
				Log.e(TAG, "NO BACKGROUND BITMAP!");
			}
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, 0, 0, null);
			} else {
				Log.e(TAG, "NO BITMAP!");
			}
			Date now = new Date();
			if (now.getTime() > (lastSaveTime + SAVE_DELAY) ) {
				saveCurrentBitMap();
				lastSaveTime = now.getTime();
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
			unsaved = true;
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

			Bitmap newBackgroundBitmap = Bitmap.createBitmap(curW, curH,
					Bitmap.Config.RGB_565);
			Canvas newBackgroundCanvas = new Canvas();
			newBackgroundCanvas.setBitmap(newBackgroundBitmap);
			
			Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
					Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			
			
			if (mBitmap != null) {
				newCanvas.drawBitmap(mBitmap, 0, 0, null);
			}
			if (mBackgroundBitmap != null) {
				newBackgroundCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
			}
			mBitmap = newBitmap;
			mCanvas = newCanvas;
			
			mBackgroundBitmap = newBackgroundBitmap;
			mBackgroundCanvas = newBackgroundCanvas;

			drawPageGrid();
			
			loadBitMap();
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
			mBackgroundCanvas.drawPaint(pagePainter);

			// Draw Background Grid
			Display display = getWindowManager().getDefaultDisplay();
			width = display.getWidth();// start
			height = display.getHeight();// end

			for (int i = 0; i < width; i += xpos) {
				mBackgroundCanvas.drawLine(i, 0, i, height, gridPainter);
			}
			for (int i = 0; i < height; i += ypos) {
				mBackgroundCanvas.drawLine(0, i, width, i, gridPainter);
			}
		}

		private void drawPoint(float x, float y, float pressure, float width) {
//			Log.i("TouchPaint", "Drawing: " + x + "x" + y + " p=" + pressure
//					+ " width=" + width);
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

		public void saveCurrentBitMap() {
			if (!unsaved) {
				return; //do nothing if no unsaved changes pending
			}
			try {
				FileOutputStream out = openFileOutput(mCurrentFileName,
						Context.MODE_PRIVATE);
				mBitmap.compress(Bitmap.CompressFormat.PNG, 99, out); //note PNG lossless
				unsaved = false;
				Log.i(TAG, "saved page:"+mCurrentFileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void loadBitMap() {
			Bitmap loadedBM = BitmapFactory.decodeFile(getFilesDir()+File.separator+mCurrentFileName);
			if (loadedBM != null) {
				Log.i(TAG, "decoded:"
						+ loadedBM.getHeight());		
				mCanvas.drawBitmap(loadedBM, 0, 0, null);
				unsaved = false;
				invalidate();
			} else {
				Log.e(TAG, "bitmap file not found!");
			}
		}
	}
}
