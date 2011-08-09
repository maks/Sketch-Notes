package com.manichord.sketchnotes;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SKNotes extends Activity {

	private static final String TAG = "SKNotes";

	/** Menu ID for the command to clear the window. */
	private static final int CLEAR_ID = Menu.FIRST;

	/** Menu ID for the command to list pages */
	private static final int PAGELIST_ID = Menu.FIRST + 1;

	/** Menu ID for the command to Save current page */
	private static final int SETTINGS_ID = Menu.FIRST + 2;

	protected static final String LOAD_FILENAME = "com.manichord.sketchnotes.load_filename";

	/** The view responsible for drawing the window. */
	SketchView sView;
	
	private String mCurrentFileName;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setupViews();
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
				sView.saveCurrentBitMap(mCurrentFileName);
				
				Date now = new Date();
				mCurrentFileName = "skpage"+now.getTime()+".png";
				sView.clear();
			} else {
				Log.e(TAG, "NO Sketch VIEW!!!");
			}
			return true;
		case PAGELIST_ID:
			Intent intent = new Intent(this, PagesList.class);
			sView.saveCurrentBitMap(mCurrentFileName);
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
		sView.saveCurrentBitMap(mCurrentFileName);
		sView.nullBitmaps();
		System.gc();
		Log.d(TAG, "SKNotes onPause:"+getMemUsageString());
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();		
		setupViews();
	}

	public String getCurrentFileName() {
		return mCurrentFileName;
	}

	public static String getMemUsageString() {
		int usedNativeKbs = (int)(Debug.getNativeHeapAllocatedSize() / 1024L);
		
    	return String.format(" SKNotes Memory Used: %d KB", usedNativeKbs);
	}
	
	private void setupViews() {
		
		setContentView(R.layout.main);
		sView = (SketchView)findViewById(R.id.skview);			
		
		findViewById(R.id.eraserButton).setOnClickListener(sView);
		findViewById(R.id.penButton).setOnClickListener(sView);
				
		Date now = new Date();
		
		String fileToLoad = getIntent().getStringExtra(LOAD_FILENAME);
		if (fileToLoad != null && !"".equals(fileToLoad)) {
			mCurrentFileName = fileToLoad;
		} else {				
			mCurrentFileName = "skpage"+now.getTime()+".png";
		}
		
    	Log.d(TAG," SKNotes - "+SKNotes.getMemUsageString());
	}
}
