package com.manichord.sketchnotes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.Toast;

public class SKNotes extends Activity {

    private static final String TAG = "SKNotes";

    /** Menu ID for the command to clear the window. */
    private static final int CLEAR_ID = Menu.FIRST;

    /** Menu ID for the command to list pages */
    private static final int PAGELIST_ID = Menu.FIRST + 1;

    /** Menu ID for the command to Save current page */
    private static final int SETTINGS_ID = Menu.FIRST + 2;

    /** Menu ID for the command to Save current page */
    private static final int SHARE_ID = Menu.FIRST + 3;

    protected static final String LOAD_FILENAME = "com.manichord.sketchnotes.load_filename";

    /** The view responsible for drawing the window. */
    SketchView sView;

    private ImageButton colourMenuButton;


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
        menu.add(0, SHARE_ID, 0, "Share");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CLEAR_ID:
            if (sView != null) {
                sView.saveCurrentBitMap(mCurrentFileName);

                Date now = new Date();
                mCurrentFileName = "skpage" + now.getTime() + ".png";
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
            startActivity(new Intent(this, SKNPrefs.class));
            return true;
        case SHARE_ID:
            shareSketch();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void shareSketch() {
        // make sure current version is saved
        sView.saveCurrentBitMap(mCurrentFileName);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        File extFile = new File(getShareDir(), mCurrentFileName);
        FileInputStream internalFile = null;
        try {
            internalFile = getApplicationContext().openFileInput(
                    mCurrentFileName);
            copyToFile(internalFile, extFile);
            Uri sketchUri = Uri.fromFile(extFile);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, sketchUri);
            startActivity(Intent.createChooser(sharingIntent,
                    "Share Sketch Using..."));
        } catch (IOException e) {
            Log.e(TAG, "error sharing " + mCurrentFileName, e);
        } finally {
            if (internalFile != null) {
                try {
                    internalFile.close();
                } catch (IOException e) {
                    // can't do anything about this
                }
            }
        }
    }

    private File getShareDir() {
        File dir = new File(getExternalCacheDir(), "share");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Copies src file to dst file. If the dst file does not exist, it will be
     * created.
     * 
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copyToFile(InputStream src, File dst)
            throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = src.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // can't do anything about this
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sView.saveCurrentBitMap(mCurrentFileName);
        sView.nullBitmaps();
        System.gc();
        Log.d(TAG, "SKNotes onPause:"+mCurrentFileName+" mem:" + getMemUsageString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "SKNotes onResume:"+mCurrentFileName+" mem:" + getMemUsageString());
        setupViews();
    }

    public String getCurrentFileName() {
        return mCurrentFileName;
    }

    public static String getMemUsageString() {
        int usedNativeKbs = (int) (Debug.getNativeHeapAllocatedSize() / 1024L);

        return String.format(" SKNotes Memory Used: %d KB", usedNativeKbs);
    }

    private void setupViews() {

        setContentView(R.layout.main);
        sView = (SketchView) findViewById(R.id.skview);

        findViewById(R.id.eraserButton).setOnClickListener(sView);
        findViewById(R.id.penButton).setOnClickListener(sView);

        Date now = new Date();

        String fileToLoad = getIntent().getStringExtra(LOAD_FILENAME);
        getIntent().removeExtra(LOAD_FILENAME);//clear intent
        if (fileToLoad != null && !"".equals(fileToLoad)) {
        	Log.d(TAG, "file from Intent filename:"+fileToLoad);
            mCurrentFileName = fileToLoad;
        } else {
        	if (mCurrentFileName != null) {
        		Log.d(TAG, "have current filename:"+mCurrentFileName);
        	} else {        		
        		mCurrentFileName = "skpage" + now.getTime() + ".png";
        		Log.d(TAG, "NEW filename:"+mCurrentFileName);
        	}
        }

        this.colourMenuButton = (ImageButton) this.findViewById(R.id.penColourButton);
        this.colourMenuButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                ColoursPopupWindow dw = new ColoursPopupWindow(v, sView);
                dw.showLikePopDownMenu();
            }
        });

        Log.d(TAG, " SKNotes - " + SKNotes.getMemUsageString());
    }

    /**
     * Extends {@link BetterPopupWindow}
     * <p>
     * Overrides onCreate to create the view and register the button listeners
     *
     * @author qbert
     *
     */
    private static class ColoursPopupWindow extends BetterPopupWindow implements
            OnClickListener {

        //ref to view used to set current pen/pencil drawing colour
        private SketchView sView;

        public ColoursPopupWindow(View anchor, SketchView view) {
            super(anchor);
            this.sView = view;
        }

        @Override
        protected void onCreate() {
            // inflate layout
            LayoutInflater inflater = (LayoutInflater) this.anchor.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            ViewGroup root = (ViewGroup) inflater.inflate(
                    R.layout.colours_popup, null);

            // setup button events
            for (int i = 0, icount = root.getChildCount(); i < icount; i++) {
                View v = root.getChildAt(i);

                if (v instanceof TableRow) {
                    TableRow row = (TableRow) v;

                    for (int j = 0, jcount = row.getChildCount(); j < jcount; j++) {
                        View item = row.getChildAt(j);
                        if (item instanceof Button) {
                            Button b = (Button) item;
                            b.setOnClickListener(this);
                        }
                    }
                }
            }

            // set the inflated view as what we want to display
            this.setContentView(root);
        }


        public void onClick(View v) {
            Button b = (Button) v;

            sView.setCurrentPenColour(b.getText().toString());

            //display a simple toast as a confirmation to user
            Toast.makeText(this.anchor.getContext(), b.getText(),
                    Toast.LENGTH_SHORT).show();

            this.dismiss();
        }
    }
}
