package com.knoeflerdesign.keywest;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class SearchResultService extends Service {

    private final IBinder mBinder = new MyBinder();
    protected Database db;
    protected FunctionalityFactory ff;
    ToneGenerator tone;
    int volume = 30;
    int streamType = AudioManager.STREAM_SYSTEM;
    SimpleCursorAdapter adapter;
    SQLiteDatabase.CursorFactory cf;

    public SearchResultService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        //access the database
        db = new Database(this, Database.DATABASE_TABEL_PERSONS, cf, 1);

        //create the adapter which contains every database entry
        adapter = loadCostumerEntries();

        //Does not depend on the intent
        //because this service runs anyway
        return Service.START_STICKY;
    }

    private SimpleCursorAdapter loadCostumerEntries() {

        tone = new ToneGenerator(streamType, volume);
        tone.startTone(ToneGenerator.TONE_CDMA_ANSWER);
        Toast.makeText(SearchResultService.this,
                "Lade Datenbank...",
                Toast.LENGTH_LONG).show();
        ff = new FunctionalityFactory();

        Cursor cursor = db.readData();

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

            boolean isNameTooLong = false;
            int first_name_length,last_name_length;
            int getIndex;
            String firstName,lastName;
            int bannedState;



            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                /*DE INFO:
                * Wenn ich am Ende einer if Anweisung true übergebe, dann lässt der ViewBinder
                * mich die Arbeit übernehmen.
                *
                * Übergebe ich false nimmt er die Daten vom Cursor
                * */

                getIndex = cursor.getColumnIndex(Database.COL_BANNED);
                bannedState = (int) cursor.getInt(getIndex);


                if (view instanceof ImageView) {
                    if (view.getId() == R.id.thumbnail) {
                        try{
                            ImageView image = (ImageView) view;
                            String path = cursor.getString(columnIndex);

                            Bitmap bitmap = ff.getBitmap(path);

                            if (bitmap != null) {
                                int height = 128;
                                double scaledWidth = height * 0.5625;
                                int width = (int) scaledWidth;

                                bitmap = ff.scaleBitmap(bitmap, height, width);
                                bitmap = ff.rotateBitmap(bitmap, 90);

                                image.setImageBitmap(bitmap);
                                return true;
                            }
                        }catch(Resources.NotFoundException nfe){
                            System.out.println("ImageView Resource couldn't be found in ViewBinder");

                        }
                    }else {
                        System.out.println("Bitmap was empty or null");
                    }

                } else if (view instanceof TextView) {

                    if (view.getId() == R.id.personListIndex){

                        ((TextView) view).setTextColor(Color.BLACK);
                        return false;
                    }
                    if (view.getId() == R.id.personAge) {
                        ((TextView) view).setTextColor(Color.GREEN);
                        return false;
                    }

                    if (view.getId() == R.id.personFirstname) {


                        firstName = cursor.getString(cursor.getColumnIndex(Database.COL_FIRSTNAME));
                        first_name_length = firstName.toCharArray().length;

                        if (bannedState == 1) {
                            ((TextView) view).setTextColor(Color.RED);
                        } else {
                            ((TextView) view).setTextColor(Color.BLACK);
                        }
                        return false;
                    }

                    if (view.getId() == R.id.personLastname) {


                        lastName = cursor.getString(cursor.getColumnIndex(Database.COL_LASTNAME));
                        last_name_length = lastName.toCharArray().length;
                        //System.out.println(text.getText().toString()+"("+thisLength+")");



                        if (first_name_length + last_name_length + 1 > 17) {

                            //prepare full name
                            String[] cuts = ff.cutStringIfTooLongAndAddDots(firstName+" "+lastName,20).split(" ");

                            // select last name
                            String cutted_last_name = cuts[1];

                            //set new lastname to view
                            ((TextView) view).setText(cutted_last_name);


                            //colorize text to red if the person is banned
                            if (bannedState == 1) {
                                ((TextView) view).setTextColor(Color.RED);
                            } else {
                                ((TextView) view).setTextColor(Color.BLACK);
                            }
                            return true;

                        } else {
                            //colorize text to red if the person is banned
                            if (bannedState == 1) {
                                ((TextView) view).setTextColor(Color.RED);
                            } else {
                                ((TextView) view).setTextColor(Color.BLACK);
                            }
                            return false;

                        }
                    }
                }
                return false;
            }

        });

        tone.startTone(ToneGenerator.TONE_CDMA_CONFIRM);
        Toast.makeText(SearchResultService.this,
                "Datenbank geladen",
                Toast.LENGTH_LONG).show();
        return ca;
    }

    public SimpleCursorAdapter getEntryListAdapter() {
        return adapter;
    }

    class MyBinder extends Binder {
        SearchResultService getService() {
            return SearchResultService.this;
        }
    }


}

