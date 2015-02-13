package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    //include self written functions for path splitting, etc
    FunctionalityFactory ff;

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

        final String NAME_PATTERN = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}";

        Pattern p = Pattern.compile(NAME_PATTERN);
        Matcher m = p.matcher(QUERY);

        Cursor cursor;

        String[] select_from = {Database.COL_ID, Database.COL_PROFILEPICTURE, Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME, Database.COL_BANNED};

        //view to add the data in each list item
        int[] add_to = new int[]{
                R.id.personListIndex,
                R.id.thumbnail,
                R.id.personAge,
                R.id.personFirstname,
                R.id.personLastname,
                R.id.bannedImage
        };


        if (QUERY == PLUS_SIXTEEN || QUERY == PLUS_EIGHTEEN || QUERY == Database.COL_BANNED) {
            cursor = getSearchCursor(QUERY);


        }else
        if(QUERY == Database.ALL){
            cursor = db.readData();
        }


        //if ask for name
        //this order is important
        else if(m.matches()){
            //search for name
            cursor = getSearchCursor(QUERY);
        }else{
            Toast.makeText(SearchActivity.this,
                    "Eingabe nicht erkannt!",
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(SearchActivity.this,
                    "Es werden alle Einträge angezeigt!",
                    Toast.LENGTH_LONG).show();
            cursor = db.readData();
        }


        //the adapter which adds the data to the views
        SimpleCursorAdapter ca = new SimpleCursorAdapter(SearchActivity.this, R.layout.search_list_item, cursor, select_from, add_to);

        ca.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            //calculates the length of the full name and
            //if its bigger than 18 print "Max Musterm..."
            int name_length;
            int db_id,id;
            String firstName,lastName;
            //for checking if its the right entry to change the name
            Cursor c;
            String[] columns = {Database.COL_ID,Database.COL_FIRSTNAME,Database.COL_LASTNAME};


            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                db_id = cursor.getInt(0)+1;

                if (!(view instanceof ImageView)) {
                    if (view instanceof TextView) {

                        if (view.getId() == R.id.personFirstname) {
                            firstName = null;
                            TextView text = (TextView) view;
                            name_length = text.getText().toString().toCharArray().length;
                            firstName = text.getText().toString();
                            //System.out.println(text.getText().toString()+"("+name_length+")");
                        }
                        if (view.getId() == R.id.personLastname) {
                            TextView text = (TextView) view;
                            lastName = text.getText().toString();

                            //cursor has to search for the name
                            String[] args = {firstName, lastName};

                            //+1 for the space character
                            int thisLength = text.getText().toString().toCharArray().length;

                            //System.out.println(text.getText().toString()+"("+thisLength+")");

                            int end = thisLength - ((name_length + 1 + thisLength) - 17);
                            if (name_length + thisLength + 1 > 17) {

                                //testing if the String gets edited correctly
                                String cut = (String) text.getText().subSequence(0, end);
                                /*System.err.println("["+firstName+" "+text.getText().toString()+"]Text is too long!");
                                System.err.println("["+firstName+" "+cut+"] lastname as cut");*/
                                cut += "...";
                                //System.err.println("["+firstName+" "+cut+"] lastname as cut(edited)");

                                //cursor has to get the correct and current entry
                                //which is in the ViewBinder loop at the moment
                                c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS, columns, Database.COL_FIRSTNAME + "=? AND " + Database.COL_LASTNAME + "=?", args, null, null, null);
                                c.moveToFirst();
                                //c.moveToPosition();
                                //get the (index=id) of the database entry
                                id = c.getInt(0);
                                System.err.println("ID: "+id);
                                System.err.println("DB ID: "+db_id);
                                //checks if the list_item is the correct one from the id
                                if (id == db_id) {
                                    System.err.println("\n\nSet "+text.getText().toString()+" to "+cut);
                                    text.setText(cut);
                                    System.err.println("Last name should be "+cut+" and is "+text.getText().toString()+"\n\n");
                                }
                            }
                        }
                    }
                }
                if (view instanceof ImageView) {
                    if (view.getId() == R.id.thumbnail) {

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
                        } else {
                            System.out.println("Bitmap was empty or null");
                        }
                    }
                    if (view.getId() == R.id.bannedImage) {
                        ImageView image = (ImageView) view;
                        int bannedState = (int) cursor.getInt(columnIndex);
                        if (bannedState == 1) {
                            image.setBackgroundResource(R.drawable.ic_house_banned_red);
                        } else {
                            image.setBackgroundResource(0);
                        }
                    }
                    return true; //true because the data was bound to the view
                }
                return false;
            }
        });

        ca.notifyDataSetChanged();

        personList.setAdapter(ca);
    }



    private Cursor getListItemCursor() {
        /*
        * Returns the Cursor which is setup to get the list item settings
        * */

        //all columns for the settings
        String[] column_banned = {Database.COL_ID, Database.COL_PROFILEPICTURE, Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME, Database.COL_BANNED};

        Cursor c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                column_banned, null, null, null, null,
                Database.COL_LASTNAME);

        //test log method
        //ff.logAllDataFromCursor(c);
        return c;
    }

    private Map<String, String> getProfilePictureFilePathCacheMap() {
        /*
        * Saves every Bitmap of every entry with the index id as key
        * */
        final int CURSOR_ID_INDEX = 0;
        Map map = new HashMap();
        Cursor c = getListItemCursor();
        String path;
        int id;
        if (c.getCount() > 0) {
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {

                path = c.getString(c.getColumnIndex("profilbild"));

                id = c.getInt(CURSOR_ID_INDEX);

                map.put(Integer.toString(id), path);
            }
        } else {
            System.err.println("Cursor is empty in [getBitmapCacheList()].");
        }
        return map;
    }


    private void setIconAndImageStates(ListView lv) {
        /*
        * if the ListView gets created by the database data,
        * run through all items and set thier Profile Picture
        * thumbnails and check the banned status
        * */

        //all columns for the settings
        String[] column_banned = {Database.COL_ID, Database.COL_BANNED, Database.COL_PROFILEPICTURE};


        try {

            for (int i = 0; i < lv.getCount(); i++) {
                //the current list item
                View v = lv.getChildAt(i);

                //get all children of the current list item
                TextView firstname = (TextView) v.findViewById(R.id.personFirstname);
                TextView lastname = (TextView) v.findViewById(R.id.personLastname);
                ImageView banned_image = (ImageView) v.findViewById(R.id.bannedImage);
                ImageView img = (ImageView) v.findViewById(R.id.thumbnail);
                TextView id = (TextView) v.findViewById(R.id.personListIndex);
                int mID = Integer.parseInt(id.getText().toString());
                //options for selecting
                String[] selection = {
                        Database.COL_FIRSTNAME + "=?" + " AND " +
                                Database.COL_LASTNAME + "=?" + " AND " +
                                Database.COL_BANNED + "=" + 1 + " AND " +
                                Database.COL_ID + "=" + mID
                };

                //selection args
                String[] args = {firstname.getText().toString(), lastname.getText().toString()};

                //setup the cursor
                Cursor c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                        column_banned, selection[0], args, null, null,
                        Database.COL_LASTNAME);

                //check stats
                if (c.getCount() > 0) {
                    for (int it = 1; it < c.getCount(); it++) {
                        if (c.getInt(c.getColumnIndex("hausverbot")) == 1) {
                            img.setImageResource(R.drawable.ic_house_banned_black);
                        }
                    }
                } else {
                    System.out.println("Cursor is empty");
                }
            }
        } catch (NullPointerException npe) {

        }

    }


    private Cursor getSearchCursor(String command) {
        //System.out.println("getSearchCursor startet mit: " + command);
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
