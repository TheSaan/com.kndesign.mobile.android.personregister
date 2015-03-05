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

public class SearchActivity extends Activity implements PatternCollection {

    public final static int PERSON_INFO_REQUEST = 1;
    //ImageView Ids for the person info activity
    final static int[] idCardImageIds = {R.id.personsCheckItCard, R.id.personsDriversLicence, R.id.personsPassport, R.id.personsPassFront, R.id.personsPassBack, R.id.personsOebbb};
    protected final static String PLUS_SIXTEEN = "unter 18";
    protected final static String PLUS_EIGHTEEN = "über 18";
    protected final static String BANNED = "Verbot";
    protected final static String ALL = "Alle";
    private static SearchActivity myClass;
    private static boolean isThumbnailShown = false;

    // search selection criteria
    protected Database db;
    ListView personList;
    CursorFactory cf;
    View[] list_items;
    //include self written functions for path splitting, etc
    AndroidHandler ff;
    Bundle extras;
    //the last search query before pausing activity
    String lastQuery;
    Intent srsintent;


    //the search service
    private SearchResultService srs;
    private ServiceConnection SearchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(SearchActivity.this,
                    "Verbinde SRS...",
                    Toast.LENGTH_SHORT).show();


            SearchResultService.MyBinder binder = (SearchResultService.MyBinder) service;
            srs = binder.getService();
            srs.binded = true;
            Toast.makeText(SearchActivity.this,
                    "SRS Verbunden",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            srs.binded = false;
            srs = null;
        }
    };


    private SharedPreferences memory;
    private SharedPreferences.Editor editor;

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

        // get the existing database
        db = new Database(this);
        // The list of the results
        personList = (ListView) findViewById(R.id.resultList);


        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            System.out.println("go search "+query);

            search(query);
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String value = intent.getStringExtra(SearchManager.QUERY);
        search(value);
    }

    protected void search(String query) {
        ff = new AndroidHandler(getApplicationContext());
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

            personList.setOnItemClickListener(new AdapterView.OnItemClickListener() {


                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    //selection test
                    //view.setBackgroundColor(Color.YELLOW);


                    ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
                    if (!isThumbnailShown) {
                        //select cursor from list items data
                        Cursor c = getPersonCursor(view);
                        if (c.getCount() > 0) {
                            c.moveToFirst();

                            Bitmap bm = ff.getBitmap(c.getString(c.getColumnIndex(Database.COL_PROFILEPICTURE)));

                            //set dimension of the bitmap
                            int height = 128;
                            double scaledWidth = height * 0.5625;
                            int width = (int) scaledWidth;
                            bm = ff.rotateBitmap(bm, 90);
                            bm = ff.scaleBitmap(bm, width, height);
                            thumbnail.setImageBitmap(bm);
                            isThumbnailShown = true;
                        } else {
                            System.out.println("Cursor is null in ClickListener");
                        }

                    } else {
                        isThumbnailShown = false;
                        thumbnail.setImageBitmap(null);
                    }

                }
            });
            personList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

                    showPersonInfo(view);
                    return false;
                }
            });
            return;
        } else {
            System.out.println("Connection: " + SearchServiceConnection + "\nService: " + srs);
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
        //the adapter which adds the data to the views
        SimpleCursorAdapter ca2 = new SimpleCursorAdapter(SearchActivity.this, R.layout.search_list_item, getCursorFromSearchQuery(QUERY), select_from, add_to);

        ca2.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            //calculates the length of the full name and
            //if its bigger than 18 print "Max Musterm..."

            boolean isNameTooLong = false;
            int first_name_length
                    ,
                    last_name_length;
            int getIndex;
            String firstName
                    ,
                    lastName;
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


                    /*if (view instanceof ImageView) {
                        if (view.getId() == R.id.thumbnail) {
                            try {
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
                            } catch (Resources.NotFoundException nfe) {
                                //System.out.println("ImageView Resource couldn't be found in ViewBinder");

                            }
                        } else {
                            //System.out.println("Bitmap was empty or null");
                        }

                    } else*/
                if (view instanceof TextView) {


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
                            String[] cuts = ff.cutStringIfTooLongAndAddDots(firstName + " " + lastName, 20).split(" ");

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
                //bitmap.recycle();
                return false;
            }

        });

        ca2.notifyDataSetChanged();

        personList.setAdapter(ca2);

        //set items selectable
        personList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        personList.setClickable(true);

        // Log.v("Test", "Setting listener");
        personList.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
                if (!isThumbnailShown) {
                    //select cursor from list items data
                    Cursor c = getPersonCursor(view);
                    if (c.getCount() > 0) {
                        c.moveToFirst();

                        Bitmap bm = ff.getBitmap(c.getString(c.getColumnIndex(Database.COL_PROFILEPICTURE)));

                        //set dimension of the bitmap
                        int height = 128;
                        double scaledWidth = height * 0.5625;
                        int width = (int) scaledWidth;
                        bm = ff.rotateBitmap(bm, 90);
                        bm = ff.scaleBitmap(bm, width, height);
                        thumbnail.setImageBitmap(bm);
                        isThumbnailShown = true;
                    } else {
                        System.out.println("Cursor is null in ClickListener");
                    }

                } else {
                    isThumbnailShown = false;
                    thumbnail.setImageBitmap(null);
                }

            }
        });
        personList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

                showPersonInfo(view);
                return false;
            }
        });
        createPersonItemArray(personList);
    }

    protected Cursor getCursorFromSearchQuery(String QUERY) {





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
            cursor = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                    columns, selection[0], null, null, null,
                    Database.COL_LASTNAME);
            return cursor;
        }else
        // show 18+
        if (PLUS_EIGHTEEN.equals(QUERY)) {
            Log.v("getSearchCursor", "PLUS_EIGHTEEN, [" + QUERY + "]\n" + "\n");
            cursor = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                    columns, selection[1], null, null, null,
                    Database.COL_LASTNAME);
            return cursor;
        }else
        // show banned persons
        if (BANNED.equals(QUERY)) {
            Log.v("getSearchCursor", "BANNED, [" + QUERY + "]\n" + "\n");
            cursor = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                    columns, selection[2], null, null, null,
                    Database.COL_LASTNAME);
            return cursor;
        }else
        //if ask for name
        //this order is important
        // search for name with single word (first or last name
        if (ff.checkMultiplePatterns(SINGLE_NAME_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 4);
            return cursor;
        }
        //search for multiple name formats
        if (ff.checkMultiplePatterns(NAME_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 1);
            return cursor;
        }else
        //search for age
        if (ff.checkMultiplePatterns(AGE_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 2);
            return cursor;
        }else
        //search for date
        if (ff.checkMultiplePatterns(DATE_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 3);
            return cursor;
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

    private void createPersonItemArray(ListView lv) {
        list_items = new View[lv.getChildCount()];

        for (int i = 0; i < lv.getChildCount(); i++) {
            list_items[i] = lv.getChildAt(i);
        }
    }

    private Cursor getSearchCursor(String command, int identifier) {
        //System.out.println("getSearchCursor startet mit: " + command);
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
                if (identifier == 1) {
                    // show result of input text search (ordinary name search)

                    String[] query_parts = command.split(" ");
                    //Log.v("getSearchCursor", "Name recognized , [" + command + "]\n\n");
                    c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                            columns, selection[3], query_parts, null, null,
                            Database.COL_LASTNAME);

                    return c;
                } else
                    //identifier == 2 -> its a age
                    if (identifier == 2) {
                        // show result of input text search (ordinary age search)
                        String[] age = {command};
                        c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                                columns, selection[4], age, null, null,
                                Database.COL_LASTNAME);

                        return c;
                    } else
                        //identifier == 3 -> its a date
                        if (identifier == 3) {
                            // show result of input text search (ordinary date  search)
                            String[] date = {command};
                            c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                                    columns, selection[5], date, null, null,
                                    Database.COL_LASTNAME);

                            return c;
                        } else
                            //identifier == 3 -> its a date
                            if (identifier == 4) {
                                // show result of input text search (ordinary date  search)
                                String[] word = {command, command};
                                c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                                        columns, selection[6], word, null, null,
                                        Database.COL_LASTNAME);

                                return c;
                            } else if (identifier == 5) {
                                // show result of input text search (ordinary date  search)
                                String[] query_parts = command.split(" ");
                                c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                                        null, selection[7], query_parts, null, null,
                                        Database.COL_LASTNAME);

                                return c;
                            } else {
                                return null;
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

            String query = firstname.getText().toString() + " " + lastname.getText().toString() + " " + ageTextview.getText().toString();

            /*query data to create a new cursor in the PersonInfoActivity*/
            i.putExtra("personData", query);
            i.putExtra("lastSearchQuery", lastQuery);

            //if i delete this person in the person activity i will get the start index from where to change all further indexes
            i.putExtra("personId", Integer.parseInt(index.getText().toString()));
            ageTextview = null;
            firstname = null;
            lastname = null;

            startActivityForResult(i, PERSON_INFO_REQUEST);
            onStop();
            query = null;
            System.out.println();
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
                            if (srs != null)
                                srs.restart();
                            //after restarting the service call search to show
                            //the last search again
                            try {
                                search(extras.get("lastSearchQuery").toString());
                            } catch (Exception e) {
                                System.err.println("Search could not get started after resuming\nfrom PersonInfoActivity!");
                            }
                        }
                    }

                } else {
                    System.out.println("Bundle 'extras' is null");
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
