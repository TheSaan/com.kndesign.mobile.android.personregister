package com.knoeflerdesign.keywest;

import com.knoeflerdesign.keywest.R.color;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.SearchView;
import android.widget.TextView;


public class StartActivity extends Activity {

	CursorFactory cf;
	Database db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		setMinAgeDate();

		db = new Database(this, Database.DATABASE_TABEL_PERSONS, cf, 1);
        //TODO Diese Funktion wird später in den "Informationen" aufgelistet und ist nur dem Entwickler und dem Chef zugängig

	}

	protected void onResume() {
		setMinAgeDate();
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
       /* case R.id.action_entry_count:{
            showMoreInformation();
            return true;
        }*/
        case R.id.action_action_show_all_entries:{
            //showAllEntries();
            db.getAllEntries();
        }
        case R.id.action_back:{
            setContentView(R.layout.activity_start);
        }
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setMinAgeDate() {
		DatePicker d = new DatePicker(getApplicationContext());
		DateCalculator date = new DateCalculator();
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
        int month = d.getMonth()+1;

        if(day<10){
            dayString = "0"+day;
        }else{
            dayString = ""+day;
        }
        if(month<10){
            monthString = "0"+month;
        }else{
            monthString= ""+month;
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

    private void showAllEntries(){
        Intent i = new Intent(getApplicationContext(), SearchActivity.class);
        i.putExtra("query",Database.ALL);
        startActivity(i);
        onPause();
    }

	@Override
	public boolean onSearchRequested(){
		Bundle appData = new Bundle();
		appData.putBoolean(SearchActivity.SEARCH_SERVICE, true);
		startSearch(null,false,appData,false);
		return true;
	}

  /*  private int showMoreInformation(){
        setContentView(R.layout.activity_info);
        checkForHomeToActivateBackButton(R.layout.activity_info);
        db.getReadableDatabase();
        TextView amountText = (TextView)findViewById(R.id.textviewAmountOfEntriesNumber);
        amountText.setText(""+db.countPersons());
        return R.layout.activity_info;
    }*/
	/*private boolean checkForHomeToActivateBackButton(int cv_id){
        int id = R.id.startContentView;
        MenuItem back = (MenuItem)findViewById(R.id.action_back);

        if(cv_id != id){
            //back.setVisible(true);
            return false;
        }else{
            //back.setVisible(false);
            return true;
        }
    }*/
}
