package com.manichord.sketchnotes;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PageImageAdapter extends BaseAdapter {

	private final String TAG = "PageImageAdapter";
	private Context mContext;
	private File[] mFileList;
	private int mBackgroundColor;
	private Paint mPainter;
	
	HashMap<String, Bitmap> mThumbs = new HashMap<String, Bitmap>();

    public PageImageAdapter(Context c) {
        mContext = c;
        mBackgroundColor = mContext.getResources().getColor(R.color.page_colour);
        Paint mPainter = new Paint();
    	int bgColor = mContext.getResources().getColor(R.color.page_colour);
        
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

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	if (position == 0) {
    		getFilesList();
    	}
    	Bitmap thumbBM = mkThumb(position);
    	
        ImageView imageView;
          
        int THUMB_WIDTH = thumbBM.getWidth() / 4;
        int THUMB_HEIGHT = thumbBM.getHeight() / 4;
        
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(THUMB_WIDTH, THUMB_HEIGHT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(8, 8, 8, 8);
        } else {
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
    	Log.e(TAG, "img file:"+mFileList[position].getName());    	
    	
    	Canvas newCanvas = new Canvas();
		Bitmap loadedBM = BitmapFactory.decodeFile(mFileList[position].getAbsolutePath());
		
		Bitmap thumbBitMap = Bitmap.createBitmap(loadedBM.getWidth(), loadedBM.getHeight(),
				Bitmap.Config.RGB_565);
		
		newCanvas.setBitmap(thumbBitMap);
		
		thumbBitMap.eraseColor(mBackgroundColor);
		newCanvas.drawBitmap(loadedBM, 0, 0, mPainter);
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
