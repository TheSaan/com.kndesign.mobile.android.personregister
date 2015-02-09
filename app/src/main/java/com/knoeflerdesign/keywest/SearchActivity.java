package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.LauncherActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ListIterator;
import java.util.Vector;

public class SearchActivity extends Activity {

    final String PLUS_SIXTEEN = "16+";
    final String PLUS_EIGHTEEN = "18+";
    final String BANNED = "hausverbot";
    // search selection criteria
    protected Database db;
    ListView personList;
    SearchView searchText;
    ArrayAdapter<String> adapter;
    CursorFactory cf;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_results);

        // get the existing database
        db = new Database(this, Database.DATABASE_TABEL_PERSONS, cf, 1);
        // The list of the results
        personList = (ListView) findViewById(R.id.resultList);



        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }

    }

    protected void search(String query) {

        final String QUERY = query;
        // TODO check if first name or last name is on first place

		/*
         * returns the correct cursor decided by a: + default name search +
		 * query equals '16+' + query equals '18+' + query equals 'hausverbot'
		 */
        Cursor cursor;

        if(QUERY == Database.ALL ||QUERY == PLUS_SIXTEEN ||QUERY == PLUS_EIGHTEEN ||QUERY == BANNED){
            cursor = getSearchCursor(QUERY);
        }else {
            cursor = db.readData();
        }
        //columns from database
        final String[] select_from = {Database.COL_ID,Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME};

        //view to add the data in each list item
        int[] add_to = new int[]{
                R.id.personListIndex,
                R.id.personAge,
                R.id.personFirstname,
                R.id.personLastname
        };
        //the adapter which adds the data to the views
        SimpleCursorAdapter ca = new SimpleCursorAdapter(SearchActivity.this,R.layout.search_list_item,cursor,select_from,add_to);

        ca.notifyDataSetChanged();
        //check for banned icon to show or not
        setBannedIconState(personList, QUERY);
        personList.setAdapter(ca);


    }
    private void setBannedIconState(ListView lv, String query){
        try {
            Cursor c = getSearchCursor(query);
            for (int i = 0; i < lv.getCount(); i++) {

                lv.getChildAt(i).setVisibility(View.VISIBLE);

            }
        }catch (NullPointerException npe){

        }

    }
    private void setPicAsThumbnail(ImageView mImageView,
                                   String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private Cursor getSearchCursor(String command) {
        System.out.println("getSearchCursor startet mit: " + command);
        //the coumns to select
        String[] columns = {Database.COL_ID, Database.COL_PROFILEPICTURE,
                Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME};

        String[] selection = {
                Database.COL_AGE + "<18",
                Database.COL_AGE + ">=18",
                Database.COL_BANNED + "=1",
                Database.COL_FIRSTNAME + "=? AND " + Database.COL_LASTNAME
                        + "=?"};


        db = new Database(this, Database.DATABASE_TABEL_PERSONS, cf, 1);

        Cursor c;

        // Show all
        if (command == Database.ALL) {
            Log.v("getSearchCursor", "ALL, [" + command + "]\n" +
                    "\n");
            c = db.readData();
            c.moveToFirst();
            return c;
        } else
            // show 16+
            if (command == PLUS_SIXTEEN) {
                Log.v("getSearchCursor", "PLUS_SIXTEEN, [" + command + "]\n" +
                        "\n");
                c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                        columns, selection[0], null, null, null,
                        Database.COL_LASTNAME);
                return c;
            } else
                // show 18+
                if (command == PLUS_EIGHTEEN) {
                    Log.v("getSearchCursor", "PLUS_EIGHTEEN, [" + command + "]\n" +
                            "\n");
                    c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                            columns, selection[1], null, null, null,
                            Database.COL_LASTNAME);
                    return c;
                } else
                    // show banned persons
                    if (command == BANNED) {
                        Log.v("getSearchCursor", "BANNED, [" + command + "]\n" +
                                "\n");
                        c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                                columns, selection[2], null, null, null,
                                Database.COL_LASTNAME);
                        return c;
                    } else {
                        // show result of input text search (ordinary name search)

                        String[] query_parts = command.split(" ");
                        Log.v("getSearchCursor", "Name recognized , [" + command + "]\n\n");
                        c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                                columns, selection[3], query_parts, null, null,
                                Database.COL_LASTNAME);

                        return c;
                    }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        // Assumes current activity is the searchable activity
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
            searchView.setSubmitButtonEnabled(true);
            return true;
        } else {

            Log.v("SearchView not found",
                    "The search button is not linked to a SearchView!");
            return true;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.icon_all:{
                search(Database.ALL);
                return false;
            }
            case R.id.icon_plus16:{
                search(PLUS_SIXTEEN);
                return false;
            }
            case R.id.icon_plus18:{
                search(PLUS_EIGHTEEN);
                return false;
            }
            case R.id.icon_banned:{
                search(BANNED);
                return false;
            }
            default:
                return false;
        }
    }
}
