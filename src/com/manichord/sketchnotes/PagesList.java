package com.manichord.sketchnotes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
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

public class PagesList extends Activity {

	private static final String TAG = "PagesList";

	/** Menu ID for the command to clear the window. */
	private static final int RENAME_ID = Menu.FIRST;

	/** Menu ID for the command to clear the window. */
	private static final int DELETE_ID = Menu.FIRST + 1;
	
	/** Menu ID for the command to clear the window. */
	private static final int TAG_ID = Menu.FIRST + 2;

	private PageImageAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pages);
		
		final PagesList self = this;

		GridView gridview = (GridView) findViewById(R.id.gridview);
		if (gridview == null) {
			Log.e(TAG, "NO GRIDVIEW!!");
		} else {
						
			mAdapter = new PageImageAdapter(this);
			gridview.setAdapter(mAdapter);

			gridview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					Log.e(TAG, "pos:" + position);
					Intent intent = new Intent(self, SKNotes.class);
					intent.putExtra(SKNotes.LOAD_FILENAME, mAdapter.getFileName(position));
					startActivity(intent);
				}
			});
			registerForContextMenu(gridview);
		}
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
			Log.i(TAG, "reName Page" + index);
			// TODO
			return true;			
		case DELETE_ID:
			//TODO: add confirmation dialog
			mAdapter.deletePage(index);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void populateMenu(Menu menu) {
		menu.add(Menu.NONE, RENAME_ID, Menu.NONE, "Rename");
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Delete");
		//TODO: menu.add(Menu.NONE, TAG_ID, Menu.NONE, "Tag");
		
	}

}
