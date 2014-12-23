package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {

	ListView personList;
	SearchView searchText;
	ArrayAdapter<String> adapter;
	CursorFactory cf;
	// search selection criteria

	final String PLUS_SIXTEEN = "16+";
	final String PLUS_EIGHTEEN = "18+";
	final String ALL = "all";
	final String BANNED = "hausverbot";

	protected Database db;

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
			Toast.makeText(SearchActivity.this,
					"Search [if] wurde aufgerufen!", Toast.LENGTH_SHORT).show();
			String query = intent.getStringExtra(SearchManager.QUERY);

			search(query);
		}
	}

	private void search(String query) {
		Toast.makeText(SearchActivity.this, "Query: " + query,
				Toast.LENGTH_SHORT).show();
		final String QUERY = query;
		// TODO check if first name or last name is on first place

		/*
		 * returns the correct cursor decided by a: + default name search +
		 * query equals '16+' + query equals '18+' + query equals 'hausverbot'
		 */
		Cursor c = getSearchCursor(query);
		CursorAdapter ca = new CursorAdapter(getApplicationContext(), c,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				int item_view_id = R.layout.search_list_item;

				// inflate item view to list view holder
				LinearLayout holderView = new LinearLayout(context);
				String inflaterName = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(inflaterName);
				inflater.inflate(item_view_id, holderView, true);

				return holderView;
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				// if(cursor != null){
				Person p = db.getPerson(db.findIdByQuery(QUERY));

				// list item children
				ImageView thumbnail = (ImageView) findViewById(R.id.thumbnail);
				TextView description = (TextView) findViewById(R.id.personDescription);
				ImageView banned = (ImageView) findViewById(R.id.bannedImage);
				ImageView moreDetails = (ImageView) findViewById(R.id.detailsImage);
				try{
				
					p.showInformationInList(thumbnail, description, banned,
							moreDetails);

					System.out.println("in bindView person is:\n");
					p.printAll();
				}catch(NullPointerException ex) {
					System.err.println("thumbnail:  " + thumbnail + "\n"
							+ "description:  " + description + "\n"
							+ "banned:  " + banned + "\n" + "moreDetails:  "
							+ moreDetails+"\n\n NPE!!");
				}
			}
		};
		personList.setAdapter(ca);
	}

	/**
	 * @see <a
	 *      href="http://developer.android.com/training/camera/photobasics.html#TaskScalePhoto">Android/Taking
	 *      Photos Simply/Scale Photos</a>
	 * */

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
		System.out.println("getSearchCursor startet");

		String[] columns = { Database.COL_ID, Database.COL_PROFILEPICTURE,
				Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME };
		String[] selection = {
				"_alter<18",
				"_alter>=18",
				"hausverbot=1",
				Database.COL_FIRSTNAME + "=? AND " + Database.COL_LASTNAME
						+ "=?" };
		db = new Database(this, Database.DATABASE_TABEL_PERSONS, cf, 1);

		Cursor c = db.getReadableDatabase().rawQuery(
				"SELECT * FROM " + Database.DATABASE_TABEL_PERSONS,
				new String[] {});
		// Show all
		if (command == ALL) {
			Log.v("getSearchCursor", "ALL, [" + command + "]");
			c.moveToFirst();
			c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
					columns, null, null, null, null, Database.COL_LASTNAME);
			return c;
		} else
		// show 16+
		if (command == PLUS_SIXTEEN) {
			Log.v("getSearchCursor", "PLUS_SIXTEEN, [" + command + "]");
			c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
					columns, selection[0], null, null, null,
					Database.COL_LASTNAME);
			return c;
		} else
		// show 18+
		if (command == PLUS_EIGHTEEN) {
			Log.v("getSearchCursor", "PLUS_EIGHTEEN, [" + command + "]");
			c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
					columns, selection[1], null, null, null,
					Database.COL_LASTNAME);
			return c;
		} else
		// show banned persons
		if (command == BANNED) {
			Log.v("getSearchCursor", "BANNED, [" + command + "]");
			c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
					columns, selection[2], null, null, null,
					Database.COL_LASTNAME);
			return c;
		} else {
			// show result of input text search (ordinary name search)

			Log.v("getSearchCursor", "ELSE, [" + command + "]");
			String[] query_parts = command.split(" ");
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

	private void addListeners() {
		MenuItem itemAll = (MenuItem) findViewById(R.id.icon_all);
		MenuItem item16plus = (MenuItem) findViewById(R.id.icon_plus16);
		MenuItem item18plus = (MenuItem) findViewById(R.id.icon_plus18);
		MenuItem itemBanned = (MenuItem) findViewById(R.id.icon_banned);

		final int allID, plus16ID, plus18ID, bannedID;
		allID = itemAll.getItemId();
		plus16ID = item16plus.getItemId();
		plus18ID = item18plus.getItemId();
		bannedID = itemBanned.getItemId();

		itemAll.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				search(ALL);
				return false;
			}
		});
		item16plus.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				search(PLUS_SIXTEEN);
				return false;
			}
		});
		item18plus.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				search(PLUS_EIGHTEEN);
				return false;
			}
		});
		itemBanned.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				search(BANNED);
				return false;
			}
		});

	}
}
