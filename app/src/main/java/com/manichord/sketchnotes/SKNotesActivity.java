package com.manichord.sketchnotes;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.Toast;

import com.manichord.sketchnotes.databinding.SknotesActivityBinding;

import java.io.File;
import java.util.Date;

import timber.log.Timber;

public class SKNotesActivity extends AppCompatActivity {

    private static final String TAG = "SKNotesActivity";

    /** Menu ID for the command to clear the window. */
    private static final int CLEAR_ID = Menu.FIRST;

    /** Menu ID for the command to list pages */
    private static final int PAGELIST_ID = Menu.FIRST + 1;

    /** Menu ID for the command to Save current page */
    private static final int SETTINGS_ID = Menu.FIRST + 2;

    /** Menu ID for the command to Save current page */
    private static final int SHARE_ID = Menu.FIRST + 3;

    protected static final String LOAD_FILENAME = "com.manichord.sketchnotes.load_filename";

    private ImageButton colourMenuButton;

    private String mCurrentFileName;
    private SknotesActivityBinding mBinding;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new Timber.DebugTree());
        Timber.d("TIMBER ready");

        setupViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_draw:
                mBinding.skview.setEraserMode(false);
                return true;
            case R.id.menu_eraser:
                mBinding.skview.setEraserMode(true);
                return true;
            case R.id.menu_new_page:
                if (mBinding.skview != null) {
                    mBinding.skview.saveCurrentBitMap(mCurrentFileName);

                    Date now = new Date();
                    mCurrentFileName = "skpage" + now.getTime() + ".png";
                    mBinding.skview.clear();
                } else {
                    Log.e(TAG, "NO Sketch VIEW!!!");
                }
                return true;
            case R.id.menu_pages:
                Intent intent = new Intent(this, PagesList.class);
                mBinding.skview.saveCurrentBitMap(mCurrentFileName);
                startActivity(intent);
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, SKNPrefs.class));
                return true;
            case R.id.menu_share:
                shareSketch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareSketch() {
        // make sure current version is saved
        mBinding.skview.saveCurrentBitMap(mCurrentFileName);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        Uri sketchUri = FileProvider.getUriForFile(getApplicationContext(),
                "com.manichord.sketchnotes.fileprovider",
                new File(getApplicationContext().getFilesDir(),mCurrentFileName));
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, sketchUri);
        startActivity(Intent.createChooser(sharingIntent,
                "Share Sketch Using..."));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBinding.skview.saveCurrentBitMap(mCurrentFileName);
        mBinding.skview.nullBitmaps();
        System.gc();
        Log.d(TAG, "SKNotesActivity onPause:"+mCurrentFileName+" mem:" + getMemUsageString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "SKNotesActivity onResume:"+mCurrentFileName+" mem:" + getMemUsageString());
        setupViews();
    }

    public String getCurrentFileName() {
        return mCurrentFileName;
    }

    public static String getMemUsageString() {
        int usedNativeKbs = (int) (Debug.getNativeHeapAllocatedSize() / 1024L);

        return String.format(" SKNotesActivity Memory Used: %d KB", usedNativeKbs);
    }

    private void setupViews() {

        mBinding = DataBindingUtil.setContentView(this, R.layout.sknotes_activity);

        mBinding.penColourSpinner.setAdapter(new PensCustomAdapter(this, PenModel.getPens(this.getApplicationContext())));

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

        Toolbar toolbar = ((Toolbar) findViewById(R.id.toolbar));
        setSupportActionBar(toolbar);

        Log.d(TAG, " SKNotesActivity - " + SKNotesActivity.getMemUsageString());
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
