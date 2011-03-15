package com.manichord.sketchnotes;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
	ArrayList<Bitmap> mThumbs = new ArrayList<Bitmap>();

    public PageImageAdapter(Context c) {
        mContext = c;        
        makeThumbs();
    }

    public int getCount() {
        return mThumbs.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        
        int THUMB_WIDTH = mThumbs.get(position).getWidth() / 4;
        int THUMB_HEIGHT = mThumbs.get(position).getHeight() / 4;
        
        
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(THUMB_WIDTH, THUMB_HEIGHT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        
        imageView.setImageBitmap(mThumbs.get(position));
        return imageView;
    }

    private void makeThumbs() {
    	File storeDir = mContext.getFilesDir();
    	if (storeDir != null) {
    		mFileList = storeDir.listFiles();    	
    	} else {
    		Log.e(TAG, "invalid store dir:"+storeDir);
    	}
    	    	
    	Paint painter = new Paint();
    	int bgColor = mContext.getResources().getColor(R.color.page_colour);
    	    	
    	Canvas newCanvas = new Canvas();
		
    	
    	for(File iFile:mFileList) {
    		Log.e(TAG, "img files:"+iFile.getName());
    		Bitmap loadedBM = BitmapFactory.decodeFile(iFile.getAbsolutePath());
    		
    		Bitmap thumbBitMap = Bitmap.createBitmap(loadedBM.getWidth(), loadedBM.getHeight(),
    				Bitmap.Config.RGB_565);
    		
    		newCanvas.setBitmap(thumbBitMap);
    		
    		thumbBitMap.eraseColor(bgColor);
    		newCanvas.drawBitmap(loadedBM, 0, 0, painter);
    		mThumbs.add(thumbBitMap);
    	}		
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
