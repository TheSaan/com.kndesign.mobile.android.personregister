package com.knoeflerdesign.keywest;

import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class Database extends SQLiteOpenHelper {

	private SQLiteDatabase db;

	// database specific
	public static final String QM = "=?";
	public static final String AND = " AND ";

	// local values
	public static final String PLUS_SIXTEEN = "16+";
	public static final String PLUS_EIGHTEEN = "18+";
	public static final String ALL = "all";

	// table values
    //TODO umlaute in utf schreiben
	public static final String PATH_MAX_VARCHAR_LENGTH = "250";
	public static final String SELECT_ALL = "*";
	public static final String COL_ID = "_id";
	public static final String COL_AGE = "_alter";
	public static final String COL_LASTNAME = "name";
	public static final String COL_FIRSTNAME = "vorname";
	public static final String COL_BIRTHDATE = "geburtsdatum";
	public static final String COL_PROFILEPICTURE = "profilbild";
	public static final String COL_DRIVERSLICENCE = "f�hrerschein";
	public static final String COL_CHECKITCARD = "checkitcard";
	public static final String COL_PASSPORT = "reisepass";
	public static final String COL_OEBBCARD = "�bbcard";
	public static final String COL_PERSO_FRONT = "personalausweis_vs";
	public static final String COL_PERSO_BACK = "personalausweis_rs";
	public static final String COL_DETAILS = "details";
	public static final String COL_BANNED = "hausverbot";

	// COL_BANNED always has to be last index, otherwise change the loop in
	// addPerson()
	private static final String[] COLUMNS = { COL_ID, COL_AGE, COL_FIRSTNAME,
			COL_LASTNAME, COL_BIRTHDATE, COL_PROFILEPICTURE,
			COL_DRIVERSLICENCE, COL_CHECKITCARD, COL_PASSPORT, COL_OEBBCARD,
			COL_PERSO_FRONT, COL_PERSO_BACK, COL_DETAILS, COL_BANNED };

	protected static final String DATABASE_NAME = "KEY_WEST_DATABASE";
	private static final String DATABASE_TABEL_PASSWORDS = "passw�rter";
	protected static final String DATABASE_TABEL_PERSONS = "personen";

	private static final String TAG = "KeyWestDatabase";
	private static final int DATABASE_VERSION = 1;

	public Database(Context activity, String name, CursorFactory factory,
			int version) {
		super(activity, name, factory, version);

		db = getWritableDatabase();
	}

	public void onCreate(SQLiteDatabase db) {
		try {
			// create table 'personen'
			String sql1 = "CREATE TABLE " + DATABASE_TABEL_PERSONS + "( "
					+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_AGE
					+ " INTEGER," + COL_FIRSTNAME + " VARCHAR(20) NOT NULL,"
					+ COL_LASTNAME + " VARCHAR(20) NOT NULL," + COL_BIRTHDATE
					+ " DATE NOT NULL," + COL_PROFILEPICTURE + " VARCHAR("
					+ PATH_MAX_VARCHAR_LENGTH + ") NOT NULL,"
					+ COL_DRIVERSLICENCE + " VARCHAR("
					+ PATH_MAX_VARCHAR_LENGTH + ") NOT NULL," + COL_CHECKITCARD
					+ " VARCHAR(" + PATH_MAX_VARCHAR_LENGTH + ") NOT NULL,"
					+ COL_PASSPORT + " VARCHAR(" + PATH_MAX_VARCHAR_LENGTH
					+ ") NOT NULL," + COL_OEBBCARD + " VARCHAR("
					+ PATH_MAX_VARCHAR_LENGTH + ") NOT NULL," + COL_PERSO_FRONT
					+ " VARCHAR(" + PATH_MAX_VARCHAR_LENGTH + ") NOT NULL,"
					+ COL_PERSO_BACK + " VARCHAR(" + PATH_MAX_VARCHAR_LENGTH
					+ ") NOT NULL, " + COL_DETAILS + " VARCHAR("
					+ PATH_MAX_VARCHAR_LENGTH + ") NOT NULL, " + COL_BANNED
					+ " INTEGER  ) ";
			db.execSQL(sql1);

			// create table 'passw�rter'

			String sql2 = "CREATE TABLE " + DATABASE_TABEL_PASSWORDS
					+ "( _id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "username VARCHAR(20) NOT NULL,"
					+ "password VARCHAR(20) NOT NULL,"
					+ "kontotyp VARCHAR(25) NOT NULL)";
			db.execSQL(sql2);

			System.out.println("Database Path:\t" + db.getPath());

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Error message", e.getMessage());
		}
	}

	// TODO Its only upgrade the persons side, but not the password table
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABEL_PERSONS);
		onCreate(db);
	}

	public synchronized void onClose() {

	}

	/**
	 * @param name
	 *            The last name
	 * @param firstname
	 *            The first name
	 * 
	 * */
	/*
	 * the rest of the arguments have to be the paths of the card images
	 */
	public void addPerson(int age, String firstname, String name, String date,
			String profilepicture, String driverslicence, String checkitcard,
			String passport, String oebbcard, String personalidcardfront,
			String personalidcardback, String details, int banned) {

		String[] arguments = { firstname, name, date, profilepicture,
				driverslicence, checkitcard, passport, oebbcard,
				personalidcardfront, personalidcardback, details };

		//
		ContentValues data = new ContentValues();
		final int columnsSize = COLUMNS.length;
		int k = 0;
		// add to database
		if (data != null) {
			for (int i = 1; i < columnsSize; i++) {
				if (i == 1) {
					// add age at correct position
					data.put(COLUMNS[i], age);
				}
				if (i == columnsSize - 1) {
					// add banned (DE: "Hausverbot") at correct (last) position
					// (as boolean integer)
					data.put(COLUMNS[i], banned);
				}
				if (i >= 2 && i < columnsSize - 1) {
					data.put(COLUMNS[i], arguments[k]);
					k++;
				}
			}
			Log.v("data not NULL", data.toString());
			db.insert(DATABASE_TABEL_PERSONS, null, data);
			db.close();
		}
	}

	public Person getPerson(int id) {
		// TODO the method doesn't look for the id
		// get the db reference
		SQLiteDatabase db = this.getReadableDatabase();
		Person person;
		// create query
		Cursor c = db.query(DATABASE_TABEL_PERSONS, COLUMNS, "_id = ?",
				new String[] { String.valueOf(id) }, null, null, null, null);

		// if a result get the first one
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();

			person = new Person(Integer.parseInt(c.getString(0)),// id
					c.getString(2),// firstname
					c.getString(3),// lastname
					c.getString(4),// date
					c.getString(5),// profilepicture path
					c.getString(6),// driverslicence path
					c.getString(7),// checkitcard path
					c.getString(8),// oebb path
					c.getString(9),// passport path
					c.getString(10),// id front path
					c.getString(11),// id back path
					c.getString(12),// details path)
					Integer.parseInt(c.getString(13)));// isBanned

			System.out.println("person:\n");
			person.printAll();
			c.close();
			return person;
		} else {
			Log.v("Tabel EMPTY", "Table " + DATABASE_TABEL_PERSONS
					+ " is empty");
			person = null;
			return person;
		}

	}

	public Person getPerson(Cursor cursor) {
		Person person;
		Cursor c = cursor;
		// if a result get the first one
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();

			person = new Person(Integer.parseInt(c.getString(0)),// id
					c.getString(2),// firstname
					c.getString(3),// lastname
					c.getString(4),// date
					c.getString(5),// profilepicture path
					c.getString(6),// driverslicence path
					c.getString(7),// checkitcard path
					c.getString(8),// oebb path
					c.getString(9),// passport path
					c.getString(10),// id front path
					c.getString(11),// id back path
					c.getString(12),// details path)
					Integer.parseInt(c.getString(13)));// isBanned
			c.close();
			return person;

		} else {
			return null;
		}
	}

	public int countPersons() {
		String countQuery = "SELECT  * FROM " + DATABASE_TABEL_PERSONS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();

		// return count
		return count;
	}

	public Vector<Person> getAllEntries() {
		Vector<Person> persons = new Vector<Person>();

		// 1. build the query with order rules
		String orderByDesc = " ORDER BY name DESC";
		String query = "SELECT  * FROM " + DATABASE_TABEL_PERSONS + orderByDesc;

		// 2. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery(query, null);

		// 3. go over each row, build book and add it to list
		Person person = null;

		if (c.getCount() > 0 && c != null) {
			if (c.moveToFirst()) {
				do {

					person = new Person(Integer.parseInt(c.getString(0)),// id
							c.getString(2),// firstname
							c.getString(3),// lastname
							c.getString(4),// date
							c.getString(5),// profilepicture path
							c.getString(6),// driverslicence path
							c.getString(7),// checkitcard path
							c.getString(8),// oebb path
							c.getString(9),// passport path
							c.getString(10),// id front path
							c.getString(11),// id back path
							c.getString(12),// details path)
							Integer.parseInt(c.getString(13)));// isBanned
                    person.printAll();
					persons.add(person);

				} while (c.moveToNext());
			}
		} else {
			System.out.println("Cursor id empty or 0 and moveToFirst() is "
					+ c.moveToFirst());
		}

		Log.d("get all Persons", persons.toString());

		// return books
		return persons;
	}

	public Cursor getCursor(int button_id) {
		String group_by, having, order_by;
		Cursor c;
		String[] list_input = { COLUMNS[1], COLUMNS[2], COLUMNS[3] };

		switch (button_id) {
		case R.id.icon_all: {
			group_by = COL_AGE;
			order_by = COL_LASTNAME;
			having = null;
			c = db.query(DATABASE_TABEL_PERSONS, list_input, null, null,
					group_by, having, order_by);
			return c;
		}
		case R.id.icon_plus18: {
			group_by = COL_AGE;
			order_by = COL_LASTNAME;
			having = " alter >=18";
			c = db.query(DATABASE_TABEL_PERSONS, list_input, null, null,
					group_by, having, order_by);
			return c;
		}
		case R.id.icon_plus16: {
			group_by = COL_AGE;
			order_by = COL_LASTNAME;
			having = " alter >=16";
			c = db.query(DATABASE_TABEL_PERSONS, list_input, null, null,
					group_by, having, order_by);
			return c;
		}
		case R.id.icon_banned: {
			group_by = COL_AGE;
			order_by = COL_LASTNAME;
			having = " hausverbot = 1";
			c = db.query(DATABASE_TABEL_PERSONS, list_input, null, null,
					group_by, having, order_by);
			return c;
		}
		default: {
			group_by = COL_AGE;
			order_by = COL_LASTNAME;
			having = null;
			c = db.query(DATABASE_TABEL_PERSONS, list_input, null, null,
					group_by, having, order_by);
			return null;
		}
		}

	}

	public int findIdByQuery(String query) {
		int id = 0;
		String WHERE;
		String[] selectionArgs = query.split(" ");
		String[] columns = { COL_ID };
		if (selectionArgs.length > 1) {
			WHERE = COL_FIRSTNAME + QM + AND + COL_LASTNAME + QM;
		} else {
			if (query == PLUS_SIXTEEN || query == PLUS_EIGHTEEN)
				WHERE = COL_AGE + ">=16" + AND + COL_AGE + "<=18";
			if (query == COL_BANNED)
				WHERE = COL_BANNED + "=1";
			else
				WHERE = null;// otherwise show all
		}
		Cursor c = db.query(DATABASE_TABEL_PERSONS, columns, WHERE,
				selectionArgs, null, null, null);
		//TODO Hier muss ich testen ob der index gleich des funktions indexes ist
		id = c.getColumnIndex(COL_ID);
		return id;
	}
}
