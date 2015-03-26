package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.view.Display;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.knoeflerdesign.keywest.Handler.AndroidHandler;
import com.knoeflerdesign.keywest.Handler.BitmapHandler;
import com.knoeflerdesign.keywest.Handler.FilesHandler;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;


public class SearchResultService extends Service{

    private String lastSearchQuery;

    private final IBinder mBinder = new MyBinder();
    protected Database db;
    protected AndroidHandler ah;
    SQLiteDatabase.CursorFactory cf;
    Cursor cursor;
    boolean binded;
    SimpleCursorAdapter mAdapter;

    public SearchResultService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        binded = true;
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //access the database
        db = new Database(this);

        /*Thread adapterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mAdapter = loadCostumerEntries();
            }
        });


        adapterThread.start();*/


        //System.out.println("SearchService runs");
        new AdapterLoadTask().execute(loadCostumerEntries());


        //Does not depend on the intent
        //because this service runs anyway
        return Service.START_STICKY;
    }

    protected SimpleCursorAdapter loadCostumerEntries() {

        ah = new AndroidHandler(getApplicationContext());
        cursor = db.readData();

        String[] select_from = {Database.COL_ID, Database.COL_PROFILEPICTURE, Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME, Database.COL_BANNED};

        //view to add the data in each list item
        int[] add_to = new int[]{
                R.id.personListIndex,
                R.id.thumbnail,
                R.id.personAge,
                R.id.personFirstname,
                R.id.personLastname
        };


        final SimpleCursorAdapter ca = new SimpleCursorAdapter(SearchResultService.this, R.layout.search_list_item, cursor, select_from, add_to);

        ca.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            //calculates the length of the full name and
            //if its bigger than 18 print "Max Musterm..."


            int first_name_length
                    ,
                    last_name_length;
            int getIndex;


            String firstName
                    ,
                    lastName
                    ;
            int bannedState;


            //Bitmap bitmap;
            BitmapHandler bh = new BitmapHandler(getApplicationContext());

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {


                getIndex = cursor.getColumnIndex(Database.COL_BANNED);
                bannedState = cursor.getInt(getIndex);

                if(view instanceof ImageView){
                    if (view.getId() == R.id.thumbnail) {
                        try{
                            ImageView image = (ImageView) view;
                            String path = cursor.getString(columnIndex);

                            /*
                            * To get the thumbnail, cut the path part /img/
                            * out and replace it with /tmb/.
                            * The rest of the path keeps the same
                            * */
                            String[] pathCut = path.split(EntryActivity.IMAGES_LARGE);

                            //now glue the path together again with the tmb value
                            path = pathCut[0]+EntryActivity.THUMBNAILS+pathCut[1];

                            Bitmap bitmap = bh.getBitmap(path);

                            if (bitmap != null) {
                                int height = 128;
                                double scaledWidth = height * 0.5625;
                                int width = (int) scaledWidth;

                                bitmap = bh.scaleBitmap(bitmap, height, width);
                                bitmap = bh.rotateBitmap(bitmap, 90);

                                image.setImageBitmap(bitmap);
                                return true;
                            }
                        }catch(Resources.NotFoundException nfe){
                            System.out.println("ImageView Resource couldn't be found in ViewBinder");

                        }
                    }
                    return false;
                }
                if (view instanceof TextView) {

                    if (view.getId() == R.id.personListIndex) {

                        ((TextView) view).setTextColor(Color.BLACK);
                        return false;
                    }
                    if (view.getId() == R.id.personAge) {
                        ((TextView) view).setTextColor(getResources().getColor(R.color.yellow));
                        return false;
                    }

                    if (view.getId() == R.id.personFirstname) {


                        firstName = cursor.getString(cursor.getColumnIndex(Database.COL_FIRSTNAME));
                        first_name_length = firstName.toCharArray().length;

                        if (bannedState == 1) {
                            ((TextView) view).setTextColor(Color.RED);
                        } else {
                            ((TextView) view).setTextColor(Color.WHITE);
                        }
                        return false;
                    }

                    if (view.getId() == R.id.personLastname) {


                        lastName = cursor.getString(cursor.getColumnIndex(Database.COL_LASTNAME));
                        last_name_length = lastName.toCharArray().length;
                        //System.out.println(text.getText().toString()+"("+thisLength+")");

                        if (first_name_length + last_name_length + 1 > 17) {

                            //prepare full name
                            String[] cuts = ah.cutStringIfTooLongAndAddDots(firstName + " " + lastName, 20).split(" ");

                            // select last name
                            String cutted_last_name = cuts[1];

                            //set new lastname to view
                            ((TextView) view).setText(" " + cutted_last_name);


                            //colorize text to red if the person is banned
                            if (bannedState == 1) {
                                ((TextView) view).setTextColor(Color.RED);
                            } else {
                                ((TextView) view).setTextColor(Color.WHITE);
                            }
                            return true;

                        } else {

                            //colorize text to red if the person is banned
                            if (bannedState == 1) {
                                ((TextView) view).setTextColor(Color.RED);
                            } else {
                                ((TextView) view).setTextColor(Color.WHITE);
                            }
                            return false;

                        }
                    }
                }
//                bitmap.recycle();
                return false;
            }

        });

        //tone.startTone(ToneGenerator.TONE_CDMA_CONFIRM);
        //cursor.close();
/*
        Toast.makeText(SearchResultService.this,
                "Datenbank geladen",
                Toast.LENGTH_LONG).show();*/
        return ca;
    }

    protected void updateEntriesIndexesAfterRemovingOne(int startIndex) {
        int entriesNumber;
        if (cursor != null) {
            entriesNumber = cursor.getCount();
        } else {
            cursor = db.readData();
            entriesNumber = cursor.getCount();
        }
        for (int i = 0; i < entriesNumber; i++) {
            db.updateIndex(startIndex + 1);
        }
    }

    protected void restart() {
        stopSelf();
        startService(new Intent(this, SearchResultService.class));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        binded = false;
        super.onUnbind(intent);

        return true;
    }

    private class AdapterLoadTask extends AsyncTask<SimpleCursorAdapter, Integer, Boolean> {


        @Override
        protected Boolean doInBackground(SimpleCursorAdapter... adapter) {

            int count = adapter[0].getCount();

            for (int i = 0; i < count; i++) {
                publishProgress((int) ((i / (float) count) * 100));
                if (isCancelled()) break;
            }
            mAdapter = adapter[0];
            System.out.println("New adapter set");
            return true;
        }

       /* protected void onProgressUpdate(Integer... progress){
            setProgressPercent(progress[0]);
        }*/

       /*protected void onPostExecute(Long result) {
           showDialog("Downloaded " + result + " bytes");
       }*/

    }

    class MyBinder extends Binder {
        SearchResultService getService() {
            return SearchResultService.this;
        }
    }

    public void reloadAdapter(){
        mAdapter = loadCostumerEntries();
    }

    protected void setLastSearch(String query){
        this.lastSearchQuery = query;
    }
    protected String getLastSearchQuery(){
        return lastSearchQuery;
    }
}

