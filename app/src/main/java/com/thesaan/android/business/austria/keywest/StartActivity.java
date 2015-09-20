package com.thesaan.android.business.austria.keywest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;


import com.thesaan.android.business.austria.keywest.Handler.AndroidHandler;
import com.thesaan.android.business.austria.keywest.Handler.BitmapHandler;
import com.thesaan.android.business.austria.keywest.Handler.DateHandler;
import com.thesaan.android.business.austria.keywest.Handler.FilesHandler;
import com.thesaan.android.business.austria.keywest.Service.TestEntryCreationService;
import com.thesaan.android.business.austria.keywest.saandroid.ProActivity;

import java.io.File;


public class StartActivity extends ProActivity implements KeyWestInterface {
    TextView startInfoText;
    private ViewSwitcher viewSwitcher;
    Button newEntryButton, listButton, searchButton;

    private SearchResultService srs;
    private ServiceConnection SearchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(StartActivity.this,
                    "Verbinde SRS...",
                    Toast.LENGTH_SHORT).show();


            SearchResultService.MyBinder binder = (SearchResultService.MyBinder) service;
            srs = binder.getService();

            Toast.makeText(StartActivity.this,
                    "SRS Verbunden",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            srs = null;
        }
    };

    private AgeControlService acs;
    private ServiceConnection AgeControlServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(StartActivity.this,
                    "Verbinde ACS...",
                    Toast.LENGTH_SHORT).show();


            AgeControlService.MyBinder binder = (AgeControlService.MyBinder) service;
            acs = binder.getService();

            Toast.makeText(StartActivity.this,
                    "ACS Verbunden",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            acs = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setMinAgeDate();

//        Toast.makeText(getApplicationContext(), DATABASE_BACKUP_FILE, Toast.LENGTH_LONG).show();
        init(this);

        File sd = new File(APP_DB_BACKUP_FOLDER);
        sd.mkdir();


        startInfoText = (TextView) findViewById(R.id.infoTextview);
        searchButton = (Button) findViewById(R.id.searchButton);
        newEntryButton = (Button) findViewById(R.id.newEntryButton);
        listButton = (Button) findViewById(R.id.listButton);

        showAppInfos(startInfoText);

        checkDatabaseStatus();
        viewSwitcher = (ViewSwitcher) findViewById(R.id.startViewSwitcher);
        /*
        * Starts to load all Entries from the database in an SimpleCursorAdapter. So
        * if I wanna see them, they don't have to be loaded at first.
        * */


        Intent SearchServiceIntent = new Intent(this, SearchResultService.class);
        Intent AgeServiceIntent = new Intent(this, AgeControlService.class);
        Intent TestEntryIntent = new Intent(this, TestEntryCreationService.class);

        //start
        try {
            getApplicationContext().startService(SearchServiceIntent);
            getApplicationContext().startService(AgeServiceIntent);

            //getApplicationContext().startService(TestEntryIntent);
            acs.myActivity = this;


        } catch (NullPointerException npe) {
            Log.e("Service Intent", "Service intent throws " + npe);
        }
        setListeners();
        createAgbSwitches();

        PasswordDialog pd = new PasswordDialog(StartActivity.this,false, StartActivity.this);

//        db.addUser("admin","latinrce44",RANK_ADMIN);
//        db.addUser("dieter","dieterp",RANK_MASTER);
//        db.addUser("security","seckw",RANK_STAFF);
//
//        db.getUsers();
        pd.show();
    }

    private void showAppInfos(TextView v) {
        String infoText =
                "Einträge gesamt: " + db.readData().getCount() +
                        NEW_LINE +
                        "Einträge < 18: " + db.readUnder18().getCount() +
                        NEW_LINE +
                        "Einträge > 18: " + db.readOver18().getCount() +
                        NEW_LINE +
                        "Personen mit Hausverbot: " + db.readBanned().getCount();

        v.setText(infoText);
    }

    /*
        * initalize the Handlers for this activity
        * */
    Database db;
    DateHandler dh;
    FilesHandler fh;
    BitmapHandler bh;
    AndroidHandler ah;

    private final void init(Context c) {
        db = new Database(c);
        dh = new DateHandler();
        fh = new FilesHandler();
        bh = new BitmapHandler(c);
        ah = new AndroidHandler(c);
    }

    private void createAgbSwitches() {
        final Switch agbSwitch = (Switch) findViewById(R.id.agbSwitch);
        final Switch agbSwitch2 = (Switch) findViewById(R.id.agbSwitch2);

        agbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    viewSwitcher.showNext();
                    agbSwitch2.setChecked(true);
                }
            }
        });
        agbSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    viewSwitcher.showPrevious();
                    agbSwitch.setChecked(false);
                }
            }
        });
    }

    private void checkDatabaseStatus() {
        String name = null, lastname = null, date = null, pp = null, pass = null, details = null;
        int age = 0, banned = 0;

        //if the app gets re-installed, check if a backup is available
        //and copy its data into the app database
        if (db.countPersons() == 0) {
            SQLiteDatabase backup = db.getBackupDatabase();
            if (backup != null) {
                Cursor entries = backup.query(DATABASE_TABLE_PERSONS, null, null, null, null, null, null);
                if (entries != null) {
                    int numColumns = entries.getColumnCount();
                    if (numColumns > 0) {
                        Toast.makeText(getApplicationContext(), "Stelle Sicherung wieder her...", Toast.LENGTH_SHORT).show();
                        entries.moveToFirst();
                        for (int i = 0; i < entries.getCount(); i++) {
                            age = entries.getInt(entries.getColumnIndex(COL_AGE));
                            name = entries.getString(entries.getColumnIndex(COL_FIRSTNAME));
                            lastname = entries.getString(entries.getColumnIndex(COL_LASTNAME));
                            date = entries.getString(entries.getColumnIndex(COL_BIRTHDATE));
                            pp = entries.getString(entries.getColumnIndex(COL_PROFILEPICTURE));
                            pass = entries.getString(entries.getColumnIndex(COL_PASSPORT));
                            details = entries.getString(entries.getColumnIndex(COL_DETAILS));
                            banned = entries.getInt(entries.getColumnIndex(COL_BANNED));

                            db.addPerson(age, name, lastname, date, pp, pass, details, banned);
                            entries.moveToNext();
                        }

                        Toast.makeText(getApplicationContext(), "App muss geschlossen werden um Änderungen wirksam zu machen!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Starten Sie die App einfach erneut um fortfahren zu können.", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Datenbank Backup enthält keine Einträge!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Zeiger für Datenbank Backup leer", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Datenbank Backup nicht lesbar/vorhanden,\n" +
                        "stelle Datenbank durch Dateien wieder her...", Toast.LENGTH_LONG).show();
                boolean restored = db.restoreDatabaseFromFiles();

                if(restored){
                    Toast.makeText(getApplicationContext(), "Datenbank aus Dateien wiederhergestellt.", Toast.LENGTH_SHORT).show();

                    onDestroy();
                }else{
                    Toast.makeText(getApplicationContext(), "Datenbank konnte nicht wiederhergestellt werden!", Toast.LENGTH_LONG).show();
                }
            }
        } else {
                Toast.makeText(getApplicationContext(), "Sichere Datenbank...", Toast.LENGTH_SHORT).show();
                try {
                    db.exportDatabase();
                    Toast.makeText(getApplicationContext(), "Datenbank gesichert.", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Datenbank Sicherung fehlgeschlagen aufgrund von:\n"+e, Toast.LENGTH_LONG).show();
                }

        }

    }

    protected void onResume() {
        setMinAgeDate();
        if (srs != null) {
            srs.restart();
        }
        super.onResume();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();


        // Assumes current activity is the searchable activity
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
            searchView.setSubmitButtonEnabled(true);
            return true;
        } else {

            Log.v("SearchView not found",
                    "The search button is not linked to a SearchView!\n" +
                            "StartActivity onCreateOptionsMenu(Menu)");
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_search: {
                onSearchRequested();
                return true;
            }
            case R.id.action_user_add: {
                addPerson();
                return true;
            }
            case R.id.action_action_show_all_entries: {
                showAllEntries();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
*/
    private void setListeners() {
        newEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPerson();
            }
        });
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllEntries();
            }
        });
        searchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSearchRequested();
                    }
                }
        );


    }

    private void setMinAgeDate() {
        DatePicker d = new DatePicker(getApplicationContext());
        DateHandler date = new DateHandler();
        /*
         * minAge is calculated by the default start date of the TimeZone class
		 * (1.1.1970 00:00:00) minus the age of 16 in milliseconds at the
		 * current date
		 */
        long minAge = date.getAgeInMilliseconds(1, 1, 1970)
                - date.getAgeInMilliseconds(date.currDay,
                date.currMonth, date.currYear - 16);
        long maxAge =  date.getAgeInMilliseconds(1, 1, 1970)
                -  date.getAgeInMilliseconds(date.currDay,
                date.currMonth, date.currYear - 16);
        d.setMaxDate(maxAge);
        d.setMinDate(minAge);

        TextView tfMinAge = (TextView) findViewById(R.id.textfield_MIN_AGE_16);
        TextView tfMinAge18 = (TextView) findViewById(R.id.textfield_MIN_AGE_18);
        //add "0" if number is less than 10
        String dayString, monthString;
        int day = d.getDayOfMonth();
        int month = d.getMonth() + 1;

        if (day < 10) {
            dayString = "0" + day;
        } else {
            dayString = "" + day;
        }
        if (month < 10) {
            monthString = "0" + month;
        } else {
            monthString = "" + month;
        }
        tfMinAge.setText(dayString + "." + monthString + "."
                + d.getYear());

        tfMinAge18.setText(dayString + "." + monthString + "."
                + (d.getYear()-2));

        // if(d.getMinDate() != d.getMaxDate()){
        // d.setMinDate(d.getMaxDate());
        // }
    }

    private void addPerson() {
        Intent i = new Intent(getApplicationContext(), EntryActivity.class);
        startActivity(i);
        onPause();

    }

    private void showAllEntries() {
        Intent i = new Intent(getApplicationContext(), SearchActivity.class);
        startActivity(i);

        onPause();
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        appData.putBoolean(SearchActivity.SEARCH_SERVICE, true);
        startSearch(null, false, appData, false);
        return true;
    }

}
