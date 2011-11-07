package com.manichord.sketchnotes;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class SketchView extends View implements OnTouchListener, OnClickListener {

		private static final String TAG = "SketchView";
					
		List<Point> points = new ArrayList<Point>();
		
		private long mLastSaveTime;
		
		private int SAVE_DELAY = 30 * 1000; //30sec
		
		private String mPageBackgroundType;
		
		private Bitmap mBitmap;
		private Canvas mCanvas;
		
		private Bitmap mBackgroundBitmap;
		private Canvas mBackgroundCanvas;
		
		Path mCurrentPath;
		
		private final Rect mRect = new Rect();
		private final Paint mPagePainter;
		private final Paint mGridPainter;
		private final Paint mPenPainter;
		private final Paint mEraserPainter;
		
		int width = 0;
		int height = 0;
		int pass = 0;
		
		private boolean mUnsaved = false;
		private boolean mEraserMode = false;
		private float mEraserWidth;
		
		
		public SketchView(Context context) {
			this(context, null);
		}
		
		public SketchView(Context context, AttributeSet attrs) {
			super(context, attrs);
			
			setFocusable(true);
			
			String penSizePref = PreferenceManager.getDefaultSharedPreferences(context).getString("penSizePref", "medium");
			Log.d(TAG,"sview init using pen size: "+penSizePref);
			
			Integer penColour = PreferenceManager.getDefaultSharedPreferences(context).getInt("penColour", R.color.blue_pen);
			Log.d(TAG,"sview init using pen colour: "+penColour);
			
			mPagePainter = new Paint();
			mPagePainter.setAntiAlias(true);
			mPagePainter.setColor(getResources().getColor(R.color.page_colour));

			mGridPainter = new Paint();
			mGridPainter.setColor(getResources().getColor(R.color.grid_colour));
			mGridPainter.setStyle(Style.STROKE);

			mPenPainter = new Paint();
			mPenPainter.setColor(penColour);		
			
			int penSizeIdentifier = getResources().getIdentifier(penSizePref, 
					"dimen", 
					getContext().getPackageName());
			
			Log.d(TAG, penSizePref+"-> got IDENT:"+penSizeIdentifier);
			
			float penSize = getResources().getDimension(R.dimen.pen_medium);
			if (penSizeIdentifier != 0) {
				penSize = getResources().getDimension(penSizeIdentifier);
			}
						
			mPenPainter.setStrokeWidth(penSize);
			mPenPainter.setStyle(Style.STROKE);

			mEraserPainter = new Paint();
			mEraserPainter.setColor(Color.TRANSPARENT);
			mEraserPainter.setStrokeWidth(getResources().getDimension(R.dimen.eraser_size));
			mEraserPainter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			
			mEraserWidth = getResources().getDimension(R.dimen.eraser_size);			
			mLastSaveTime = (new Date()).getTime();
		}

		public String getCurrentPenColour() {
			return "TODO";
		}

		public void setCurrentPenColour(String penColourname) {
			String colourIdentName = penColourname.toLowerCase().replace(" ", "_");
			int colourIdentifier = getResources().getIdentifier(colourIdentName, 
					"color", 
					getContext().getPackageName());
			
			Log.d(TAG, colourIdentName+"-> got IDENT:"+colourIdentifier);
			if (colourIdentifier != 0) {
				int penColour = getResources().getColor(colourIdentifier);
				Editor ed = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
				ed.putInt("penColour", new Integer(penColour));
				ed.commit();
				mPenPainter.setColor(penColour);
			} else {
				Log.d(TAG, "INVALID colourname:"+ colourIdentName+"-> got IDENT:"+colourIdentifier);
			}
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
			if (now.getTime() > (mLastSaveTime + SAVE_DELAY) ) {
				saveCurrentBitMap(((SKNotes)getContext()).getCurrentFileName());
				mLastSaveTime = now.getTime();
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float mCurX;
			float mCurY;
			
			ArrayList<Point> eraserpoints = new ArrayList<Point>();

			int action = event.getAction();
			if (action != MotionEvent.ACTION_UP
					&& action != MotionEvent.ACTION_CANCEL) {
				//Log.d(TAG, "PEN UP");
			}
			if (action == MotionEvent.ACTION_DOWN) {
				//start recording points
				mCurrentPath = new Path();
				mCurrentPath.moveTo(event.getX(), event.getY());
			}
			if (action == MotionEvent.ACTION_MOVE) {
				//start recording points
				int N = event.getHistorySize();
				int P = event.getPointerCount(); 
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < P; j++) { 
						mCurX = event.getHistoricalX(j, i);
						mCurY = event.getHistoricalY(j, i);	
						
						if (mEraserMode == true) {
							if (eraserpoints != null) {
								eraserpoints.add(new Point(Math.round(mCurX), Math.round(mCurY)));
							} else {
								Log.e(TAG, "no eraserpoints array defined, skipping adding erase point");
							}
						} else {												
							if (mCurrentPath != null) {
								mCurrentPath.lineTo(mCurX, mCurY);							
							} else {
								Log.e(TAG, "NO PATH TO ADD POINT"+mCurX+","+mCurY);							
							}
						}
					}
				}
								
				if (mCurrentPath != null) {
					mCurrentPath.lineTo(event.getX(), event.getY());	
					mCanvas.drawPath(mCurrentPath, (mEraserMode == true ? mEraserPainter : mPenPainter));
					invalidate();
				} else {
					Log.e(TAG, "Missing CurrentPath object");
				}							
			}
			mUnsaved = true;
			return true;
		}
		

		
		public boolean onTouch(View v, MotionEvent event) {
			return false;
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {

			Log.i(TAG, "Size changed w:" + w + " h:" + h);

//			int curW = mBitmap != null ? mBitmap.getWidth() : 0;
//			int curH = mBitmap != null ? mBitmap.getHeight() : 0;
//			if (curW >= w && curH >= h) {
//				return;
//			}
//
//			if (curW < w)
//				curW = w;
//			if (curH < h)
//				curH = h;
			
			createNewDrawingCanvasAndBitMap(w,h);
			
			String[] pageRulingList = getResources().getStringArray(R.array.pageRulingList);
			drawPageGridOrLines(w,h); 
			loadBitMap(((SKNotes)getContext()).getCurrentFileName());
		}

		public void clear() {
			int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			int curH = mBitmap != null ? mBitmap.getHeight() : 0;
			
			createNewDrawingCanvasAndBitMap(curW, curH);		
			drawPageGridOrLines(curW, curH);
			invalidate();			
		}
		
		private void createNewDrawingCanvasAndBitMap(int w, int h) {
			Bitmap newBitmap = Bitmap.createBitmap(w, h,
					Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			
			if (mBitmap !=  null) {
				mBitmap.recycle(); //tell android to clear up prev bitmap	
			}
			
			mBitmap = newBitmap;
			mCanvas = newCanvas;
		}

		private void drawPageGridOrLines(int w, int  h) {
			
			Resources res = getResources();
			int xpos = Math.round(res.getDimension(R.dimen.grid_size));
			int ypos = Math.round(res.getDimension(R.dimen.grid_size));
			
			Bitmap newBackgroundBitmap = Bitmap.createBitmap(w, h,
					Bitmap.Config.RGB_565);
			Canvas newBackgroundCanvas = new Canvas();
			newBackgroundCanvas.setBitmap(newBackgroundBitmap);
					
					
			if (mBackgroundBitmap != null) {
				newBackgroundCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
			}
			mBackgroundBitmap = newBackgroundBitmap;
			mBackgroundCanvas = newBackgroundCanvas;
			
			// make the entire canvas page colour
			mBackgroundCanvas.drawPaint(mPagePainter);

			// Draw Background Grid
			Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
			width = display.getWidth();// start
			height = display.getHeight();// end
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			String pageBackgroundPref = prefs.getString("pageBackgroundPref", "Grid");
			
			//lines only
			if (!pageBackgroundPref.equals("None")) {
				for (int i = 0; i < height; i += ypos) {
					mBackgroundCanvas.drawLine(0, i, width, i, mGridPainter);
				}			
			}
			
			if (pageBackgroundPref.equals("Grid")) {
				// add for square graph paper:				
				for (int i = 0; i < width; i += xpos) {
					mBackgroundCanvas.drawLine(i, 0, i, height, mGridPainter);
				}
			}
		}
		
		

		public void saveCurrentBitMap(String filename) {
			if (!mUnsaved) {
				return; //do nothing if no unsaved changes pending
			}
			try {
				FileOutputStream out = ((Activity)getContext()).openFileOutput(filename,
						Context.MODE_PRIVATE);
				mBitmap.compress(Bitmap.CompressFormat.PNG, 99, out); //note PNG lossless
				mUnsaved = false;
				Log.i(TAG, "saved page:"+filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void loadBitMap(String filename) {
			Bitmap loadedBM = BitmapFactory.decodeFile(((Activity)getContext()).getFilesDir()+File.separator+filename);
			if (loadedBM != null) {
				Log.i(TAG, "decoded:"
						+ loadedBM.getHeight());		
				mCanvas.drawBitmap(loadedBM, 0, 0, null);
				mUnsaved = false;
				invalidate();
			} else {
				Log.e(TAG, "bitmap file not found!");
			}
		}

		
		public void onClick(View v) {
			//handle button clicks for pen/pencolour/eraser
			int selectedBg = getResources().getColor(R.color.selected_button_background);
			int unselectedBg = getResources().getColor(R.color.unselected_button_background);
			
			switch(v.getId()) {
				case R.id.eraserButton: 
					mEraserMode = true;
					v.setBackgroundColor(selectedBg);
					((View)v.getParent()).findViewById(R.id.penButton).setBackgroundColor(unselectedBg);
					break;
				
				case R.id.penButton:
					mEraserMode = false;
					v.setBackgroundColor(selectedBg);
					((View)v.getParent()).findViewById(R.id.eraserButton).setBackgroundColor(unselectedBg);
					break;
					
				case R.id.penColourButton:
					
					break;
			}
		}

		public void nullBitmaps() {
			mCanvas = null;
			if (mBitmap != null) {
				mBitmap.recycle();
			}
			mBitmap = null;
			mBackgroundCanvas = null;
			if (mBackgroundBitmap != null) {
				mBackgroundBitmap.recycle();
			}
			mBackgroundBitmap = null;
		}
		
	}