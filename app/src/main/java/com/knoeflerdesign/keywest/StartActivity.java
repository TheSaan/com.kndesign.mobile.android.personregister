package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.knoeflerdesign.keywest.Handler.AndroidHandler;
import com.knoeflerdesign.keywest.Handler.BitmapHandler;
import com.knoeflerdesign.keywest.Handler.DateHandler;
import com.knoeflerdesign.keywest.Handler.FilesHandler;
import com.knoeflerdesign.keywest.Service.TestEntryCreationService;

import java.io.File;


public class StartActivity extends Activity implements KeyWestInterface{

    private ViewSwitcher viewSwitcher;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setMinAgeDate();

       init(this);

        File sd = new File(Environment.getExternalStorageDirectory()+"/KWIMG/", "BACKUP");
        sd.mkdir();

        db.exportDatabase(Database.DATABASE_NAME);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.startViewSwitcher);
        /*
        * Starts to load all Entries from the database in an SimpleCursorAdapter. So
        * if I wanna see them, they don't have to be loaded at first.
        * */


        Intent SearchServiceIntent = new Intent(this, SearchResultService.class);
        Intent AgeServiceIntent = new Intent(this, AgeControlService.class);
        Intent TestEntriyIntent = new Intent(this, TestEntryCreationService.class);

        //start
        try {
            getApplicationContext().startService(SearchServiceIntent);
            getApplicationContext().startService(AgeServiceIntent);
            getApplicationContext().startService(TestEntriyIntent);
        } catch (NullPointerException npe) {
            Log.e("Service Intent", "Service intent throws " + npe);
        }

        createAgbSwitches();

    }
    /*
        * initalize the Handlers for this activity
        * */
    Database db;
    DateHandler dh;
    FilesHandler fh;
    BitmapHandler bh;
    AndroidHandler ah;

    private final void init(Context c){
        db = new Database(c);
        dh = new DateHandler();
        fh = new FilesHandler();
        bh = new BitmapHandler(c);
        ah = new AndroidHandler(c);
    }
    private void createAgbSwitches() {
        final Switch agbSwitch = (Switch)findViewById(R.id.agbSwitch);
        final Switch agbSwitch2 = (Switch)findViewById(R.id.agbSwitch2);

        agbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    viewSwitcher.showNext();
                    agbSwitch2.setChecked(true);
                }
            }
        });
        agbSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    viewSwitcher.showPrevious();
                    agbSwitch.setChecked(false);
                }
            }
        });
    }

    protected void onResume() {
        setMinAgeDate();
        if(srs != null){
            srs.restart();
        }
        super.onResume();
    }

    @Override
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


    private void setMinAgeDate() {
        DatePicker d = new DatePicker(getApplicationContext());
        DateHandler date = new DateHandler();
        /*
         * minAge is calculated by the default start date of the TimeZone class
		 * (1.1.1970 00:00:00) minus the age of 16 in milliseconds at the
		 * current date
		 */
        long minAge = (long) date.getAgeInMilliseconds(1, 1, 1970)
                - (long) date.getAgeInMilliseconds(date.currDay,
                date.currMonth, date.currYear - 16);
        long maxAge = (long) date.getAgeInMilliseconds(1, 1, 1970)
                - (long) date.getAgeInMilliseconds(date.currDay,
                date.currMonth, date.currYear - 16);
        d.setMaxDate(maxAge);
        d.setMinDate(minAge);

        TextView tfMinAge = (TextView) findViewById(R.id.textfield_MIN_AGE);
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
