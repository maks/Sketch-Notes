package com.manichord.sketchnotes;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class PagesList extends Activity  {
	
	private static final String TAG = "PagesList";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.pages);

	    GridView gridview = (GridView) findViewById(R.id.gridview);
	    if (gridview == null) {
	    	Log.e(TAG, "NO GRIDVIEW!!");
	    } else {
	    	gridview.setAdapter(new PageImageAdapter(this));
	    	
	    	 gridview.setOnItemClickListener(new OnItemClickListener() {
	 	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	 	            Log.e(TAG, "pos:"+position);
	 	        }
	 	    });
	    }	   
	}	
}
