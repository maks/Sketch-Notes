package com.manichord.sketchnotes;

import java.io.File;
import java.io.FilePermission;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PageImageAdapter extends BaseAdapter {

	private final String TAG = "PageImageAdapter";
	
	private final static int THUMB_SCALE_FACTOR = 4;
	
	private Context mContext;
	private File[] mFileList;
	private int mBackgroundColor;
	private Paint mPainter;
	
	HashMap<String, Bitmap> mThumbs = new HashMap<String, Bitmap>();

    public PageImageAdapter(Context c) {
        mContext = c;
        mBackgroundColor = mContext.getResources().getColor(R.color.page_colour);       
    	getFilesList();
    }

    public int getCount() {    	
    	return getFilesList();
    }

    public Object getItem(int position) {
    	return null;
    }

    public long getItemId(int position) {
        return mFileList[position].hashCode();
    }
    
    @Override
	public boolean hasStableIds() {
		return true;
	}

	// create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	Log.d(TAG,"Page Adapter - "+SKNotes.getMemUsageString());
    	
    	if (position == 0) {
    		getFilesList();
    	}
    	Bitmap thumbBM = mkThumb(position);    	
        ImageView imageView;
                  
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
        	Log.d(TAG, "NEW IMG VIEW");
            imageView = (ImageView) convertView;
        }
        
        imageView.setImageBitmap(thumbBM);
        return imageView;
    }

    private int getFilesList() {
    	File storeDir = mContext.getFilesDir();    	
    	
    	Log.d(TAG, "getting page files list...");
    	
    	if (storeDir != null) {
    		mFileList = storeDir.listFiles();    	
    	} else {
    		Log.e(TAG, "invalid store dir:"+storeDir);
    	}
    	return mFileList.length;
    }
    
    private Bitmap mkThumb(int position) {
    	Log.e(TAG, "img file:"+getFileName(position));    	
    	//for explanation of why to use BMFactory options see 
    	//http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
    	
    	String filePath = mFileList[position].getAbsolutePath();
    	
        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = THUMB_SCALE_FACTOR;
        Bitmap loadedBM = BitmapFactory.decodeFile(filePath, o2);
        		
		Bitmap thumbBitMap = Bitmap.createBitmap(loadedBM.getWidth(), loadedBM.getHeight(),
				Bitmap.Config.RGB_565);
		
		Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(thumbBitMap);		
		thumbBitMap.eraseColor(mBackgroundColor);
		newCanvas.drawBitmap(loadedBM, 0, 0, mPainter);

		loadedBM.recycle();
		
		return thumbBitMap;
    }
    

	void deletePage(int index) {
		mFileList[index].delete();
		Log.i(TAG, "Deleted Page:" + mFileList[index].getName());
	}
	
	void renamePage(int index, String nuName) {
		//TODO
	}

	public String getFileName(int position) {
		return mFileList[position].getName();
	}
}
