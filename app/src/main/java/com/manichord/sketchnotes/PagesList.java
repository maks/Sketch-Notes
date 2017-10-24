package com.manichord.sketchnotes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class PagesList extends Activity implements OnItemClickListener {

	private static final String TAG = "PagesList";
	
	//==Pop up menu ==
	/** Menu ID for the command to clear the window. */
	private static final int RENAME_ID = Menu.FIRST;

	/** Menu ID for the command to clear the window. */
	private static final int DELETE_ID = Menu.FIRST + 1;
	
	/** Menu ID for the command to clear the window. */
	private static final int TAG_ID = Menu.FIRST + 2;
	//=====
		
	/** Menu ID for the command to create new sketch page. */
	private static final int NEW_ID = Menu.FIRST + 3;

	private PageImageAdapter mAdapter;
	
	private GridView mView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "Create PAGESLIST - "+ SKNotesActivity.getMemUsageString());
		
		setContentView(R.layout.pages);
		
		mView = (GridView) findViewById(R.id.gridview);
		if (mView == null) {
			Log.e(TAG, "NO GRIDVIEW!!");
		} else {						
			mAdapter = new PageImageAdapter(this);
			mView.setAdapter(mAdapter);
			mView.setOnItemClickListener(this);
			registerForContextMenu(mView);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, NEW_ID, 0, "New");
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case NEW_ID:
			Intent intent = new Intent(this, SKNotesActivity.class);
			startActivity(intent);			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "Paused PAGESLIST"+ SKNotesActivity.getMemUsageString());
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		populateMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int index = info.position;

		switch (item.getItemId()) {
		case RENAME_ID:
			Log.i(TAG, "reName Page" + index);
			// TODO
			return true;
		case TAG_ID:
			Log.i(TAG, "Tag Page" + index);
			// TODO
			return true;			
		case DELETE_ID:
			//TODO: add confirmation dialog
			mAdapter.deletePage(index);
			mView.invalidateViews();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void populateMenu(Menu menu) {
		//TODO: implement rename function 
		//menu.add(Menu.NONE, RENAME_ID, Menu.NONE, "Rename");
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Delete");
		//TODO: implement tagging function 
		//menu.add(Menu.NONE, TAG_ID, Menu.NONE, "Tag");
		
	}

	
	public void onItemClick(AdapterView<?> parent, View v,
			int position, long id) {
		Log.e(TAG, "pos:" + position);
		Intent intent = new Intent(this, SKNotesActivity.class);
		intent.putExtra(SKNotesActivity.LOAD_FILENAME, mAdapter.getFileName(position));
		startActivity(intent);
	}

}
