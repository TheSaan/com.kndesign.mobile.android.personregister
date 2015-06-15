package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.SearchManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.knoeflerdesign.keywest.Handler.AndroidHandler;
import com.knoeflerdesign.keywest.Handler.BitmapHandler;
import com.knoeflerdesign.keywest.Handler.DateHandler;
import com.knoeflerdesign.keywest.Handler.FilesHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class SearchActivity extends Activity implements PatternCollection, KeyWestInterface {


    // search selection criteria
    ListView personList;
    View[] list_items;
    //include self written functions for path splitting, etc
    Bundle extras;
    //the last search query before pausing activity
    String lastQuery;
    Intent srsintent;
    /*
        * initalize the Handlers for this activity
        * */
    Database db;
    DateHandler dh;
    FilesHandler fh;
    BitmapHandler bh;
    AndroidHandler ah;
    private boolean isThumbnailShown = false;
    //the search service
    private SearchResultService srs;
    private ServiceConnection SearchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /*Toast.makeText(SearchActivity.this,
                    "Verbinde SRS...",
                    Toast.LENGTH_SHORT).show();*/


            SearchResultService.MyBinder binder = (SearchResultService.MyBinder) service;
            srs = binder.getService();
            srs.binded = true;
            /*Toast.makeText(SearchActivity.this,
                    "SRS Verbunden",
                    Toast.LENGTH_SHORT).show();*/
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            srs.binded = false;
            srs = null;
        }
    };

    public void onResume() {
        super.onResume();
        srsintent = new Intent(this, SearchResultService.class);
        bindService(srsintent, SearchServiceConnection, Context.BIND_AUTO_CREATE);


    }

    public void onRestart() {
        super.onRestart();

        srsintent = new Intent(this, SearchResultService.class);
        bindService(srsintent, SearchServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onBackPressed() {
        super.onBackPressed();
        onDestroy();
    }

    public void onDestroy() {
        super.onDestroy();
        if (srs != null) {
            if (srs.binded) {
                unbindService(SearchServiceConnection);
            } else {
                srs.onUnbind(srsintent);
            }
            srs.restart();
        }
    }

    public void onStop() {
        super.onStop();
        if (srs.binded) {
            unbindService(SearchServiceConnection);
            srs.onUnbind(srsintent);
            srs.restart();
        } else {
            srs.onUnbind(srsintent);
            srs.restart();
        }
    }

    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        init(this);
        // The list of the results
        personList = (ListView) findViewById(R.id.resultList);


        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            c.println("go search " + query);

            //check if the query is just a number
            if (!ah.isNumeric(query)) {
                //set search value in the correct form
                query = correctLowerAndUppercase(query);
                query = removeSpacesFromLineEnd(query);
            }

            search(query);
        }


    }

    private String correctLowerAndUppercase(String editString) {
        String[] words = editString.split(" ");

        //set editstring at first to null
        //otherwise the first added word is a twin of it
        editString = null;

        //words
        if (words.length > 1) {
            for (int i = 0; i < words.length; i++) {

                //split the word into first letter and rest
                //
                //the second letter
                char cutLetter = words[i].charAt(1);

                //split the word at the second letter
                String firstLetter = words[i].substring(0, 1);
                String rest = words[i].substring(1);
                //make first letter uppercase
                firstLetter = firstLetter.toUpperCase(Locale.GERMANY);

                //make the rest lowercase
                rest = rest.toLowerCase(Locale.GERMANY);

                //now glue the word togheter
                words[i] = firstLetter + rest;
            }

            editString = words[0];

            for (int k = 1; k < words.length; k++) {
                editString += " " + words[k];
            }
        } else {
            //split the word into first letter and rest
            //
            //the second letter
            //split the word at the second letter
            String firstLetter = words[0].substring(0, 1);
            String rest = words[0].substring(1);

            //make first letter uppercase
            firstLetter = firstLetter.toUpperCase(Locale.GERMAN);

            //make the rest lowercase
            rest = rest.toLowerCase(Locale.GERMAN);

            editString = firstLetter + rest;
        }

        return editString;
    }

    private String removeSpacesFromLineEnd(String editString) {

        String[] words;

        if (editString.split(" ").length > 1) {
            words = editString.split(" ");

            for (int i = 1; i < words.length; i++) {
                editString = words[0] + " " + words[i];
            }
            c.println(editString + "was edited");
        } else {
            editString = editString.split(" ")[0];
        }
        return editString;
    }

    private final void init(Context c) {
        db = new Database(c);
        dh = new DateHandler();
        fh = new FilesHandler();
        bh = new BitmapHandler(c);
        ah = new AndroidHandler(c);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String value = intent.getStringExtra(SearchManager.QUERY);

        //set search value in the correct form
        value = correctLowerAndUppercase(value);
        value = removeSpacesFromLineEnd(value);

        search(value);
    }

    protected void search(final String query) {
        bh = new BitmapHandler(getApplicationContext());
        final String QUERY = query;
        lastQuery = query;


		/*
         * If the Service provides the loaded Adapter take the service
          * ohterwise create the search cursor from the query as a
          * new cursor and create the adapter again
		 */


        if (QUERY == ALL && SearchServiceConnection != null && srs != null) {

            //the adapter which adds the data to the views
            final SimpleCursorAdapter cursorAdapter = srs.mAdapter;

            cursorAdapter.notifyDataSetChanged();


            Thread adapterThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            personList.setAdapter(cursorAdapter);
                        }
                    });
                }
            });
            adapterThread.start();

            Log.v("Test", "Setting listener");


            personList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
                    srs.setLastSearch(QUERY);
                    showPersonInfo(view);
                    return false;
                }
            });
            return;
        } else {
            c.println("Connection: " + SearchServiceConnection + "\nService: " + srs);
        }
        /*
        * run below code if no adapter providing service is available
        * */

        String[] select_from = {Database.COL_ID, Database.COL_PROFILEPICTURE, Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME, Database.COL_BANNED};
        //view to add the data in each list item
        int[] add_to = {
                R.id.personListIndex,
                R.id.thumbnail,
                R.id.personAge,
                R.id.personFirstname,
                R.id.personLastname
        };

        //the cursor for the current list
        Cursor resultCursor = getCursorFromSearchQuery(QUERY);
        if (resultCursor != null) {
            //the adapter which adds the data to the views
            SimpleCursorAdapter ca2 = new SimpleCursorAdapter(SearchActivity.this, R.layout.search_list_item, resultCursor, select_from, add_to);

            ca2.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                //calculates the length of the full name and
                //if its bigger than 18 print "Max Musterm..."

                boolean isNameTooLong = false;
                int first_name_length
                        ,
                        last_name_length;
                int getIndex
                        ,
                        bannedState;
                String firstName
                        ,
                        lastName
                        ,
                        fileText;


                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {


                    getIndex = cursor.getColumnIndex(Database.COL_BANNED);
                    bannedState = cursor.getInt(getIndex);

                    if (view instanceof ImageView) {
                        if (view.getId() == R.id.thumbnail) {
                            try {
                                ImageView image = (ImageView) view;
                                String path = cursor.getString(columnIndex);

                            /*
                            * To get the thumbnail, cut the path part /img/
                            * out and replace it with /tmb/.
                            * The rest of the path keeps the same
                            * */
                                String[] pathCut = path.split(EntryActivity.IMAGES_LARGE);

                                //now glue the path together again with the tmb value
                                path = pathCut[0] + EntryActivity.THUMBNAILS + pathCut[1];

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
                            } catch (Resources.NotFoundException nfe) {
                                c.println("ImageView Resource couldn't be found in ViewBinder");

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
                            //c.println(text.getText().toString()+"("+thisLength+")");

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

            ca2.notifyDataSetChanged();

            personList.setAdapter(ca2);

            //set items selectable
            personList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            personList.setClickable(true);

            // Log.v("Test", "Setting listener");

            personList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    showPersonInfo(view);
                    return false;
                }
            });
        } else {
            Toast.makeText(SearchActivity.this, "Kein Eintrag gefunden. Überprüfen Sie die Eingabe!",
                    Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    protected Cursor getCursorFromSearchQuery(String QUERY) {

        AndroidHandler ah = new AndroidHandler(getApplicationContext());


        String[] columns = {Database.COL_ID, Database.COL_PROFILEPICTURE, Database.COL_BIRTHDATE, Database.COL_DETAILS,
                Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME, Database.COL_BANNED};

        String[] selection = {
                Database.COL_AGE + "<18",
                Database.COL_AGE + ">=18",
                Database.COL_BANNED + "=1",
                Database.COL_FIRSTNAME + "=? AND " + Database.COL_LASTNAME
                        + "=?",
                Database.COL_AGE + "=?",
                Database.COL_BIRTHDATE + "=?",
                Database.COL_FIRSTNAME + "=? OR " + Database.COL_LASTNAME
                        + "=?",
                Database.COL_FIRSTNAME + "=? AND " + Database.COL_LASTNAME
                        + "=? AND " + Database.COL_AGE + "=?"
        };

        Cursor cursor;


        // show 16+
        if (PLUS_SIXTEEN.equals(QUERY)) {
            Log.v("getSearchCursor", "PLUS_SIXTEEN, [" + QUERY + "]\n" + "\n");
            cursor = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                    columns, selection[0], null, null, null,
                    Database.COL_AGE);
            if (cursor.getCount() > 0)
                return cursor;
            else
                return null;
        } else
            // show 18+
            if (PLUS_EIGHTEEN.equals(QUERY)) {
                Log.v("getSearchCursor", "PLUS_EIGHTEEN, [" + QUERY + "]\n" + "\n");
                cursor = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                        columns, selection[1], null, null, null,
                        Database.COL_AGE);
                if (cursor.getCount() > 0)
                    return cursor;
                else
                    return null;
            } else
                // show banned persons
                if (BANNED.equals(QUERY)) {
                    Log.v("getSearchCursor", "BANNED, [" + QUERY + "]\n" + "\n");
                    cursor = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                            columns, selection[2], null, null, null,
                            Database.COL_AGE);
                    return cursor;
                } else if (ah.checkMultiplePatterns(SINGLE_NAME_CONVENTIONS, QUERY)) {
                    cursor = getSearchCursor(QUERY, 4);
                    if (cursor.getCount() > 0)
                        return cursor;
                    else
                        return null;
                }
        //search for multiple name formats
        if (ah.checkMultiplePatterns(SEARCH_NAME_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 1);

            //if cursor has no matches change the switch the first and lastname
            //and check again otherwise return null
            if (cursor.getCount() == 0) {
                c.println(QUERY + " not found. Retry with " + changeFirstAndLastName(QUERY) + "...");
                cursor = getSearchCursor(changeFirstAndLastName(QUERY), 1);
                if (cursor.getCount() > 0) {
                    return cursor;
                } else {
                    return null;
                }

            } else {
                return cursor;
            }
        } else
            //search for age
            if (ah.checkMultiplePatterns(AGE_CONVENTIONS, QUERY)) {
                cursor = getSearchCursor(QUERY, 2);
                return cursor;
            } else
                //search for date
                if (ah.checkMultiplePatterns(DATE_CONVENTIONS, QUERY)) {
                    if (QUERY.toCharArray().length == 8)
                        QUERY = formatDateInput(QUERY);

                    cursor = getSearchCursor(QUERY, 3);
                    if (cursor.getCount() > 0)
                        return cursor;
                    else
                        return null;
                } else {
                    Toast.makeText(SearchActivity.this,
                            "'" + QUERY + "' nicht erkannt!",
                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(SearchActivity.this,
                            "Eingabe nicht erkannt!",
                            Toast.LENGTH_SHORT).show();


                    //call search again to show the data from service
                    onDestroy();
                    return null;
                }
    }

    private String formatDateInput(String input) {
        String sDay, sMonth, sYear;


        sDay = input.substring(0, 2);
        sMonth = input.substring(2, 4);
        sYear = input.substring(4, 8);

        return sDay + "." + sMonth + "." + sYear;
    }

    private String changeFirstAndLastName(String name) {
        String[] names = name.split(" ");

        name = "";
        //put all the firstnames together
        for (int i = 0; i < names.length - 1; i++) {
            c.println("firstnames[" + i + "]: " + names[i]);
            if (names.length > 2)
                names[0] += " " + names[i];
            c.println("firstnames complete : " + name);
        }

        //now write the name with lastname at first place and return

        return names[names.length - 1] + " " + names[0];
    }

    private Cursor getSearchCursor(String command, int identifier) {
        //c.println("getSearchCursor startet mit: " + command);
        //the coumns to select
        String[] columns = {Database.COL_ID, Database.COL_PROFILEPICTURE, Database.COL_BIRTHDATE, Database.COL_DETAILS,
                Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME, Database.COL_BANNED};

        String[] selection = {
                Database.COL_AGE + "<18",
                Database.COL_AGE + ">=18",
                Database.COL_BANNED + "=1",
                Database.COL_FIRSTNAME + "=? AND " + Database.COL_LASTNAME
                        + "=?",
                Database.COL_AGE + "=?",
                Database.COL_BIRTHDATE + "=?",
                Database.COL_FIRSTNAME + "=? OR " + Database.COL_LASTNAME
                        + "=?",
                Database.COL_FIRSTNAME + "=? AND " + Database.COL_LASTNAME
                        + "=? AND " + Database.COL_AGE + "=?"
        };

        Cursor c;
        try {
            // Show all
            if (command == ALL) {
                //Log.v("getSearchCursor", "ALL, [" + command + "]\n" +"\n");
                c = db.readData();
                c.moveToFirst();
                return c;
            } else
            //identifier == 1 -> its a name
            {
                if (identifier == 1) {
                    // show result of input text search (ordinary name search)

                    String[] query_parts = command.split(" ");
                    String[] tmp;
                    //if the name has two first names
                    if (query_parts.length == 3) {

                        tmp = new String[query_parts.length - 1];

                        //glue the two first names in one object
                        tmp[0] = query_parts[0] + "-" + query_parts[1];
                        tmp[1] = query_parts[2];
                    } else {
                        tmp = query_parts;
                    }
                    query_parts = null;

                    //Log.v("getSearchCursor", "Name recognized , [" + command + "]\n\n");
                    c = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                            columns, selection[3], tmp, null, null,
                            Database.COL_LASTNAME);

                    return c;
                } else
                    //identifier == 2 -> its a age
                    if (identifier == 2) {
                        // show result of input text search (ordinary age search)
                        String[] age = {command};
                        c = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                                columns, selection[4], age, null, null,
                                Database.COL_LASTNAME);

                        return c;
                    } else
                        //identifier == 3 -> its a date
                        if (identifier == 3) {
                            // show result of input text search (ordinary date  search)
                            String[] date = {command};
                            c = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                                    columns, selection[5], date, null, null,
                                    Database.COL_LASTNAME);

                            return c;
                        } else
                            //identifier == 3 -> its a date
                            if (identifier == 4) {
                                // show result of input text search (ordinary date  search)
                                String[] word = {command, command};
                                c = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                                        columns, selection[6], word, null, null,
                                        Database.COL_LASTNAME);

                                return c;
                            } else if (identifier == 5) {
                                // show result of input text search (ordinary date  search)
                                String[] query_parts = command.split(" ");

                                String[] tmp;
                                //if the name has two first names
                                if (query_parts.length == 3) {

                                    tmp = new String[query_parts.length - 1];

                                    //glue the two first names in one object
                                    tmp[0] = query_parts[0] + SPACE + query_parts[1];
                                    tmp[1] = query_parts[2];
                                } else {
                                    tmp = query_parts;
                                }
                                query_parts = null;

                                c = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                                        null, selection[7], query_parts, null, null,
                                        Database.COL_LASTNAME);

                                return c;
                            } else {
                                return null;
                            }
            }
        } catch (Exception ex) {
            Log.v("GetSearchCursor EX", "In getSearchCursor(" + command + ") Exception thrown: " + ex);
            return null;
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
                search(ALL);
                return false;
            }
            case R.id.action_search: {
                onSearchRequested();
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
                search(BANNED);
                return false;
            }
            default:
                return false;
        }
    }

    private String getNameIfTrimmedWithDots(int age, String firstname, String lastnameTrimmed) {
        //get all entries
        Cursor c = db.readData();
        c.moveToFirst();

        String selection = Database.COL_LASTNAME + "=?";
        String[] searchFor = new String[1];

        //the cursor which searches for the firstname of the current lastname query
        Cursor tmpC;

        //Get the letters of the lastname and its digit amount to seach inside the db lastname for it
        String[] trimmed = lastnameTrimmed.split("\\.");
        lastnameTrimmed = trimmed[0];
        trimmed = null;

        int letterAmount = lastnameTrimmed.toCharArray().length;

        String dbLastname;
        //
        for (int i = 0; i < c.getCount(); i++) {
            dbLastname = c.getString(c.getColumnIndex(Database.COL_LASTNAME));
            System.out.println("Last name " + dbLastname);

            //look if the lastname parts equal the lastname in database
            if (dbLastname.toCharArray().length >= letterAmount) {

                String lastNameEnd = dbLastname.substring(letterAmount - 1);

                System.out.println("Last name end " + lastNameEnd);

                searchFor[0] = lastnameTrimmed + lastNameEnd;
                searchFor[0] = searchFor[0].trim();

                System.out.println("Search for " + searchFor[0]);

                tmpC = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                        null, selection, searchFor, null, null,
                        null);


            /*
            * If the first name and the age of the entry matching the arguments then return
            * the full last name
            * */


                if (tmpC != null) {
                    System.out.println("tmpC:\t" + tmpC);
                    if (tmpC.getCount() > 0) {

                        tmpC.moveToFirst();
                        System.out.println("Entries found");

                        if (firstname.equals(tmpC.getString(tmpC.getColumnIndex(Database.COL_FIRSTNAME)))) {
                            if (age == tmpC.getInt(tmpC.getColumnIndex(Database.COL_AGE))) {
                                System.out.println("MATCH--> Age: " + age + "\t" + firstname + "\t -->Returning " + tmpC.getString(tmpC.getColumnIndex(Database.COL_LASTNAME)) + " as last name");
                                return tmpC.getString(tmpC.getColumnIndex(Database.COL_LASTNAME));
                            }
                        }
                    } else {
                        System.out.println("No data found for " + searchFor[0]);
                    }

                }
            }
            c.moveToNext();
        }
        return null;
    }

    public void showPersonInfo(View item) {
        //create Person object and save it to memory
        if (item != null) {
            /*
            * 1. views bestimmen und deren daten übergeben
            * 2. search cursor nach name suchen lassen + alter 'identifier = 5'
            * */
            Intent i = new Intent(getApplicationContext(), PersonInfoActivity.class);

            TextView index = (TextView) item.findViewById(R.id.personListIndex);
            TextView firstname = (TextView) item.findViewById(R.id.personFirstname);
            TextView lastname = (TextView) item.findViewById(R.id.personLastname);
            TextView ageTextview = (TextView) item.findViewById(R.id.personAge);

            String query;

            if (lastname.getText().toString().contains("...")) {
                c.println("DOTS FOUND<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                String ln = getNameIfTrimmedWithDots(
                                Integer.parseInt(ageTextview.getText().toString()),
                                firstname.getText().toString(),lastname.getText().toString());
                if(ln != null) {
                    query =
                            firstname.getText().toString() + " " +
                                    ln +
                                    " " + ageTextview.getText().toString();
                }else{
                    query = null;
                }
            } else {
                /*
                query data to create a new cursor in the PersonInfoActivity
                */
                query = firstname.getText().toString() + " " + lastname.getText().toString() + " " + ageTextview.getText().toString();


            }

            if (query != null){
                i.putExtra("personData", query);

                i.putExtra("lastSearchQuery", lastQuery);

                //if i delete this person in the person activity i will get the start index from where to change all further indexes
                i.putExtra("personId", Integer.parseInt(index.getText().toString()));
                ageTextview = null;
                firstname = null;
                lastname = null;

                startActivityForResult(i, PERSON_INFO_REQUEST);
                onStop();
            }else {
                Toast.makeText(SearchActivity.this,
                        "Data cannot be created from this source",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SearchActivity.PERSON_INFO_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

        /*If resume was called after returning from an person info activity
        * restart the service so that  a new adapter gets created
        * */
                extras = data.getExtras();
                if (extras != null) {
                    if (!extras.isEmpty()) {
                        boolean b = ((Boolean) extras.get("backFromPersonInfoActivity")).booleanValue();

                        if (b) {

                            try {
                                if (srs != null) {
                                    c.println("restart srs...");

                                    srs.restart();

                                    c.println("...restarted");

                                    c.println("Search again for " + srs.getLastSearchQuery() + "...");

                                    search(srs.getLastSearchQuery());
                                    c.println("searched.");
                                } else {
                                    c.println("SRS is null in onActivityResult()");
                                }
                            } catch (Exception e) {
                                System.err.println("Search could not get started after resuming\nfrom PersonInfoActivity!");
                            }
                        }
                    }

                } else {
                    c.println("Bundle 'extras' is null");
                }
            }
        }
    }


    private Cursor getPersonCursor(View item) {
        TextView firstname = (TextView) item.findViewById(R.id.personFirstname);
        TextView lastname = (TextView) item.findViewById(R.id.personLastname);
        TextView ageTextview = (TextView) item.findViewById(R.id.personAge);


        String query = firstname.getText().toString() + " " + lastname.getText().toString() + " " + ageTextview.getText().toString();
        Cursor c = db.getCursor(db.getId(query));

        return c;
    }
}
