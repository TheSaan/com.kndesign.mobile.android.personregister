package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchActivity extends Activity implements PatternCollection {

    final String PLUS_SIXTEEN = "16+";
    final String PLUS_EIGHTEEN = "18+";
    final String BANNED = "hausverbot";
    // search selection criteria
    protected Database db;
    ListView personList;
    SearchView searchText;
    ArrayAdapter<String> adapter;
    CursorFactory cf;

    //include self written functions for path splitting, etc
    FunctionalityFactory ff;
    //the actual service
    private SearchResultService srs;
    private ServiceConnection searchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(SearchActivity.this,
                    "Verbinde...",
                    Toast.LENGTH_SHORT).show();


            SearchResultService.MyBinder binder  = (SearchResultService.MyBinder) service;
            srs = binder.getService();

            Toast.makeText(SearchActivity.this,
                    "Verbunden",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            srs = null;
        }
    };
    public void onResume(){
        super.onResume();
        Intent intent = new Intent(this, SearchResultService.class);
        bindService(intent,searchServiceConnection,Context.BIND_AUTO_CREATE);
    }
    public void onPause(){
        super.onPause();
        unbindService(searchServiceConnection);
    }

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
        ff = new FunctionalityFactory();
        final String QUERY = query;
        // TODO check if first name or last name is on first place

		/*
         * returns the correct cursor decided by a: + default name search +
		 * query equals '16+' + query equals '18+' + query equals 'hausverbot'
		 */

        final String NAME_PATTERN = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}\\s";

        Pattern p = Pattern.compile(NAME_PATTERN);
        Matcher m = p.matcher(QUERY);
        SimpleCursorAdapter ca;
        Cursor cursor;

        String[] select_from = {Database.COL_ID, Database.COL_PROFILEPICTURE, Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME, Database.COL_BANNED};

        //view to add the data in each list item
        int[] add_to = new int[]{
                R.id.personListIndex,
                R.id.thumbnail,
                R.id.personAge,
                R.id.personFirstname,
                R.id.personLastname
        };

        if (QUERY == Database.ALL) {
            if (searchServiceConnection != null) {
                Toast.makeText(SearchActivity.this,
                        "Daten transfer gelungen",
                        Toast.LENGTH_SHORT).show();

                //the adapter which adds the data to the views
                ca = srs.adapter;
                ca.notifyDataSetChanged();
                personList.setAdapter(ca);
            }
        } else {
            if (QUERY == PLUS_SIXTEEN || QUERY == PLUS_EIGHTEEN || QUERY == Database.COL_BANNED) {
                cursor = getSearchCursor(QUERY);


            } else if (QUERY == Database.ALL) {
                cursor = db.readData();
            }


            //if ask for name
            //this order is important
            else if (ff.checkMultiplePatterns(NAME_CONVENTIONS,QUERY)) {
                //search for name
                cursor = getSearchCursor(QUERY);
            } else {
                Toast.makeText(SearchActivity.this,
                        "'"+QUERY+"' nicht erkannt!",
                        Toast.LENGTH_SHORT).show();
                Toast.makeText(SearchActivity.this,
                        "Eingabe nicht erkannt!",
                        Toast.LENGTH_SHORT).show();
                Toast.makeText(SearchActivity.this,
                        "Es werden alle Einträge angezeigt!",
                        Toast.LENGTH_LONG).show();

                cursor = null;

                //call search again to show the data from service
                onDestroy();


            }

            //the adapter which adds the data to the views
            ca = new SimpleCursorAdapter(SearchActivity.this, R.layout.search_list_item, cursor, select_from, add_to);


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


            ca.notifyDataSetChanged();

            personList.setAdapter(ca);

        }
    }

    private Cursor getSearchCursor(String command) {
        //System.out.println("getSearchCursor startet mit: " + command);
        //the coumns to select
        String[] columns = {Database.COL_ID, Database.COL_PROFILEPICTURE,
                Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME,Database.COL_BANNED};

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
            //Log.v("getSearchCursor", "ALL, [" + command + "]\n" +"\n");
            c = db.readData();
            c.moveToFirst();
            return c;
        } else
            // show 16+
            if (command == PLUS_SIXTEEN) {
                //Log.v("getSearchCursor", "PLUS_SIXTEEN, [" + command + "]\n" +"\n");
                c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                        columns, selection[0], null, null, null,
                        Database.COL_LASTNAME);
                return c;
            } else
                // show 18+
                if (command == PLUS_EIGHTEEN) {
                    // Log.v("getSearchCursor", "PLUS_EIGHTEEN, [" + command + "]\n" +"\n");
                    c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                            columns, selection[1], null, null, null,
                            Database.COL_LASTNAME);
                    return c;
                } else
                    // show banned persons
                    if (command == Database.COL_BANNED) {
                        // Log.v("getSearchCursor", "BANNED, [" + command + "]\n" +"\n");
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

    /*
    *
    * Service handling
    *
    * */

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icon_all: {
                search(Database.ALL);
                return false;
            }
            case R.id.icon_plus16: {
                search(PLUS_SIXTEEN);
                return false;
            }
            case R.id.icon_plus18: {
                search(PLUS_EIGHTEEN);
                return false;
            }
            case R.id.icon_banned: {
                search(Database.COL_BANNED);
                return false;
            }
            default:
                return false;
        }
    }
}
