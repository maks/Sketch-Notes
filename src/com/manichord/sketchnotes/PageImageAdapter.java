package com.manichord.sketchnotes;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class PageImageAdapter extends BaseAdapter {

	private final String TAG = "PageImageAdapter";
	
	private final static int THUMB_SCALE_FACTOR = 4;
	
	private Context mContext;
	private File[] mFileList;
	private int mBackgroundColor;
	private Paint mPainter;

	private Bitmap mDefaultBitMap;

    public PageImageAdapter(Context c) {
        mContext = c;
        mBackgroundColor = mContext.getResources().getColor(R.color.page_colour);       
    	getFilesList();
    	mDefaultBitMap = ((BitmapDrawable)mContext.getResources().getDrawable(R.drawable.default_thumb)).getBitmap();    	
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
    	    	
    	if (convertView == null) {  // if it's not recycled, initialize some attributes
        	Log.d(TAG, "NEW IMG VIEW");
        	convertView = new ImageView(mContext);
        }
    	
    	Object currThumbTask = convertView.getTag();
    	if (currThumbTask != null) {
    		((MkThumbsAsync)currThumbTask).cancel(true);
    	}
    	
    	MkThumbsAsync thumbLoader = new MkThumbsAsync((ImageView)convertView);
    	convertView.setTag(thumbLoader);
    	thumbLoader.execute(position);    	
    	
    	((ImageView)convertView).setImageBitmap(mDefaultBitMap);
    	
        return convertView;
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
	
	class MkThumbsAsync extends AsyncTask<Integer, Void, Bitmap>
	{
		private final String TAG = "MkThumbsAsync";
		
		int mPosition;
		
		WeakReference<ImageView> mImgView;
		
		public MkThumbsAsync(ImageView imgView) {
			mImgView = new WeakReference<ImageView>(imgView);
		}

		@Override
		protected Bitmap doInBackground(Integer... params) {
			mPosition = params[0];
			File currentfile = mFileList[mPosition];
			Log.e(TAG, "img file:"+currentfile);    	
	    	//for explanation of why to use BMFactory options see 
	    	//http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
	    	
	    	String filePath = currentfile.getAbsolutePath();
	    	
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

		@Override
		protected void onPostExecute(Bitmap thumb) {
			if (mImgView != null) {
				ImageView iView = mImgView.get();
				if (iView != null) {
					iView.setImageBitmap(thumb);
				}
			}			
		}
	}
}


