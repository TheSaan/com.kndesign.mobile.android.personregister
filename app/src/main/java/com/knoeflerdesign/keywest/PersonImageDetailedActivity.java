package com.knoeflerdesign.keywest;

import com.knoeflerdesign.keywest.Handler.BitmapHandler;
import com.knoeflerdesign.keywest.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ImageView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class PersonImageDetailedActivity extends Activity implements KeyWestInterface {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private ImageView image;
    Intent mIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_person_image_detailed);

        image = (ImageView) findViewById(R.id.zoomedImage);

        mIntent = getIntent();

        if (mIntent != null) {
            setupImage(mIntent);


        }
    }

    @Override
    public void onBackPressed(){

        mIntent = null;
        super.onDestroy();
        super.onBackPressed();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * Gets the bitmap path from intent extra and sets the created Bitmap in
     * the correct format to the fullscreen image view
     * @param intent
     * @return
     */
    private void setupImage(Intent intent) {
        //
        BitmapHandler bh = new BitmapHandler(getApplicationContext());

        //generate Bitmap
        Bitmap bitmap = null;

        //from intent extra
        String path;

        //get the path
        for (int i = 0; i < 7; i++) {
            if (orderedPathDescriptions[i].toString().equals(intent.getStringExtra("KEIN EINTRAG"))
                    || intent.getExtras().get(orderedPathDescriptions[i]) == null
                    || intent.getExtras().get(orderedPathDescriptions[i]) == "KEIN EINTRAG") {
                path = null;
                bitmap = null;
                //do nothing if theres no path in the extra
            } else {
                //get the path if thers some available
                path = intent.getStringExtra(orderedPathDescriptions[i]);
                bitmap = bh.getBitmap(path);
                break;
            }
        }

        if (bitmap != null) {
            bitmap = bh.rotateBitmap(bitmap,90);

            image.setImageBitmap(bitmap);

        }

    }

}
