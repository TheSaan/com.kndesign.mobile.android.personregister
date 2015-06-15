package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.knoeflerdesign.keywest.Handler.AndroidHandler;
import com.knoeflerdesign.keywest.Handler.BitmapHandler;
import com.knoeflerdesign.keywest.Handler.DateHandler;
import com.knoeflerdesign.keywest.Handler.FilesHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;

public class Database extends SQLiteOpenHelper implements PatternCollection, KeyWestInterface{

    private final SQLiteDatabase db;
    Context context;

    TextView bdayEvent;

    public Database(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        init(context);
        db = this.getWritableDatabase();
    }

    /*
        * initalize the Handlers for this activity
        * */
    DateHandler dh;
    FilesHandler fh;
    BitmapHandler bh;
    AndroidHandler ah;

    private final void init(Context c){
        dh = new DateHandler();
        fh = new FilesHandler();
        bh = new BitmapHandler(c);
        ah = new AndroidHandler(c);
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            // create table 'personen'
            db.execSQL(CREATE_PERSON_TABLE_STRING);
            //db.execSQL(sql3);

            System.out.println("Database Path:\t" + db.getPath());

            setHashMapForSearchSuggestions();
        } catch (Exception e) {
            Log.e("Error message", e.getMessage());
        }
    }

    // TODO Its only upgrade the persons side, but not the password table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PERSONS);
        onCreate(db);
    }

    private HashMap<String,String> mAliasMap;

    private void setHashMapForSearchSuggestions(){

        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<String, String>();

        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", COL_ID + " as " + "_id" );

        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, /*COL_AGE+ " " + */COL_FIRSTNAME /*+" "+ COL_LASTNAME */+ " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);

        // Icon for Suggestions ( Optional )
        // mAliasMap.put( SearchManager.SUGGEST_COLUMN_ICON_1, FIELD_FLAG + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);

        // This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
        mAliasMap.put( SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, COL_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID );

    }

    /** Returns Persons */
    public Cursor getPersons(String[] selectionArgs){

        String selection =
                COL_FIRSTNAME   + " or " +
                COL_LASTNAME    + " or " +
                COL_AGE         + " or " +
                COL_BIRTHDATE   +
                " like ? ";

        if(selectionArgs!=null){
            selectionArgs[0] = "%"+selectionArgs[0] + "%";


        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);

        queryBuilder.setTables(Database.DATABASE_TABLE_PERSONS);

        Cursor c = queryBuilder.query(getReadableDatabase(),
                new String[] { "_ID",
                        COL_FIRSTNAME  } ,
                selection,
                selectionArgs,
                null,
                null,
                COL_AGE + " asc ","5"
        );
            return c;
        }else{
        System.err.println("Selection Args are null in getPersons()");
            return null;
    }

    }

    /** Return Person corresponding to the id */
    public Cursor getPerson(String id){

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(DATABASE_TABLE_PERSONS);

        Cursor c = queryBuilder.query(getReadableDatabase(),
                new String[]{COL_ID, COL_FIRSTNAME, COL_LASTNAME, COL_BIRTHDATE},
                "_id = ?", new String[]{id}, null, null, null, "1"
        );

        return c;
    }
    public Cursor getPersonForSuggestion(String query){
        return getReadableDatabase().query(
          DATABASE_TABLE_PERSONS,
                new String[]{COL_ID,COL_FIRSTNAME,COL_LASTNAME,COL_AGE, COL_BANNED},
                COL_FIRSTNAME+"=?"+" OR "+COL_LASTNAME+"=?"+" OR "+COL_AGE+"=?",new String[]{query},null,null,COL_AGE
        );
    }
    /**
     * @param name      The last name
     * @param firstname The first name
     */
    /*
	 * the rest of the arguments have to be the paths of the card images
	 */
    public void addPerson(int age, String firstname, String name, String date,
                          String profilepicture, String driverslicence, String checkitcard,
                          String passport, String oebbcard, String personalidcardfront,
                          String personalidcardback, String details, int banned) {

        String[] arguments = {firstname, name, date, profilepicture,
                driverslicence, checkitcard, passport, oebbcard,
                personalidcardfront, personalidcardback, details};

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues data = new ContentValues();

        final int columnsSize = COLUMNS.length;
        int k = 0;
        // add to database
        if (data != null) {
           /* for (int i = 0; i < columnsSize; i++) {
                System.out.println(i + ". Column:\t" + COLUMNS[i]);
            }*/
            for (int i = 0; i < columnsSize; i++) {
                /*if (i == 0) {
                    data.put(COLUMNS[i], (this.countPersons() + 1));
                }*/
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
            db.insert(DATABASE_TABLE_PERSONS, null, data);
            db.close();
        }
    }

    public int countPersons() {
        String countQuery = "SELECT  * FROM " + DATABASE_TABLE_PERSONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /*
    * Get the amount of all Entries
    *
    *
    *
    * */
    public Cursor readData() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(DATABASE_TABLE_PERSONS, COLUMNS, null, null, null, null, COL_AGE);

        if (c != null)
            c.moveToFirst();
        return c;
    }
    /*
        * Get the amount of all Entries
        *
        *
        *
        * */
    public Cursor readUnder18() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(DATABASE_TABLE_PERSONS, COLUMNS, COL_AGE+"<=?",new String[]{"17"}, null, null, COL_AGE);

        if (c != null)
            c.moveToFirst();
        return c;
    }/*
    * Get the amount of all Entries
    *
    *
    *
    * */
    public Cursor readOver18() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(DATABASE_TABLE_PERSONS, COLUMNS, COL_AGE+">=?",new String[]{"18"}, null, null, COL_AGE);

        if (c != null)
            c.moveToFirst();
        return c;
    }/*
    * Get the amount of all Entries
    *
    *
    *
    * */
    public Cursor readBanned() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(DATABASE_TABLE_PERSONS, COLUMNS, COL_BANNED+"=?",new String[]{"1"}, null, null, COL_AGE);

        if (c != null)
            c.moveToFirst();
        return c;
    }
    /*
    * Delete Database Entries
    * TODO Benutze die Pfad angaben gleich, um die Daten im Ordner zu löschen
    *
    *
    * */
    public void removeData(int member_id) {
        db.delete(DATABASE_TABLE_PERSONS, COL_ID + " = " + member_id, null);
    }

    /**
     * arg format: Fistname+" "+Lastname+" "+Age
     *
     * @param query
     * @return
     *  Index when entry was found. Otherwise return -1
     */
    public int getId(String query) {
        int id;
        String[] queries = query.split(" ");

        String WHERE = COL_FIRSTNAME + QM + AND + COL_LASTNAME + QM + AND + COL_AGE + QM;

        Cursor c = db.query(DATABASE_TABLE_PERSONS, null, WHERE, queries,
                null, null, null);
        if(c.getCount() > 0) {
            c.moveToFirst();
            id = c.getInt(0);
        }else{
            id = -1;
        }
        return id;
    }

    protected Cursor getSuggestionCursor(String[] columns,String selection, String[] selectionArgs){

        Cursor c = getReadableDatabase().query(DATABASE_TABLE_PERSONS,columns,selection,selectionArgs,null,null,null);
        return c;
    }
    protected Cursor getCursor(int index) {

        String WHERE = COL_ID + QM;
        String[] id = {index + ""};
        String[] columns = {COL_ID,COL_PROFILEPICTURE};
        Cursor c = db.query(DATABASE_TABLE_PERSONS, null, WHERE, id,
                null, null, null);
        return c;
    }
    protected Cursor getCursorFromSearchQuery(String QUERY) {

        //set also date and age as possible searchresults
        final String[] DATE_CONVENTIONS = {STANDARD_DATE_PATTERN};
        final String[] AGE_CONVENTIONS = {STANDARD_AGE_PATTERN};
        final String[] SINGLE_NAME_CONVENTIONS = {STANDARD_SINGLE_WORD_PATTERN};

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
        if (SearchActivity.PLUS_SIXTEEN.equals(QUERY)) {
            Log.v("getSearchCursor", "PLUS_SIXTEEN, [" + QUERY + "]\n" + "\n");
            cursor = getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                    columns, selection[0], null, null, null,
                    Database.COL_LASTNAME);
            return cursor;
        }
        // show 18+
        if (SearchActivity.PLUS_EIGHTEEN.equals(QUERY)) {
            Log.v("getSearchCursor", "PLUS_EIGHTEEN, [" + QUERY + "]\n" + "\n");
            cursor = getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                    columns, selection[1], null, null, null,
                    Database.COL_LASTNAME);
            return cursor;
        }
        // show banned persons
        if (SearchActivity.BANNED.equals(QUERY)) {
            Log.v("getSearchCursor", "BANNED, [" + QUERY + "]\n" + "\n");
            cursor = getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                    columns, selection[2], null, null, null,
                    Database.COL_LASTNAME);
            return cursor;
        }
        //if ask for name
        //this order is important
        // search for name with single word (first or last name
        if (ah.checkMultiplePatterns(SINGLE_NAME_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 4);
        }
        //search for multiple name formats
        if (ah.checkMultiplePatterns(NAME_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 1);
        }
        //search for age
        if (ah.checkMultiplePatterns(AGE_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 2);
        }
        //search for date
        if (ah.checkMultiplePatterns(DATE_CONVENTIONS, QUERY)) {
            cursor = getSearchCursor(QUERY, 3);
        } else {
            /*Toast.makeText(SearchActivity,
                    "'" + QUERY + "' nicht erkannt!",
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(SearchActivity,
                    "Eingabe nicht erkannt!",
                    Toast.LENGTH_SHORT).show();*/
           return null;
        }
        return null;
    }
    private Cursor getSearchCursor(String command, int identifier) {
        //System.out.println("getSearchCursor startet mit: " + command);
        //the coumns to select
        String[] columns = {Database.COL_ID, Database.COL_PROFILEPICTURE, Database.COL_BIRTHDATE, Database.COL_DETAILS,
                Database.COL_AGE, Database.COL_FIRSTNAME, Database.COL_LASTNAME, Database.COL_BANNED};

        String[] selection = {
                Database.COL_AGE + "<18",
                Database.COL_FIRSTNAME + "=? AND " + Database.COL_LASTNAME
                        + "=?",
                Database.COL_AGE + ">=18",
                Database.COL_BANNED + "=1",
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
                c = readData();
                c.moveToFirst();
                return c;
            } else
                //identifier == 1 -> its a name
                if (identifier == 1) {
                    // show result of input text search (ordinary name search)

                    String[] query_parts = command.split(" ");
                    //Log.v("getSearchCursor", "Name recognized , [" + command + "]\n\n");
                    c = getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                            columns, selection[3], query_parts, null, null,
                            Database.COL_LASTNAME);

                    return c;
                } else
                    //identifier == 2 -> its a age
                    if (identifier == 2) {
                        // show result of input text search (ordinary age search)
                        String[] age = {command};
                        c = getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                                columns, selection[4], age, null, null,
                                Database.COL_LASTNAME);

                        return c;
                    } else
                        //identifier == 3 -> its a date
                        if (identifier == 3) {
                            // show result of input text search (ordinary date  search)
                            String[] date = {command};
                            c = getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                                    columns, selection[5], date, null, null,
                                    Database.COL_LASTNAME);

                            return c;
                        } else
                            //identifier == 3 -> its a date
                            if (identifier == 4) {
                                // show result of input text search (ordinary date  search)
                                String[] word = {command, command};
                                c = getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                                        columns, selection[6], word, null, null,
                                        Database.COL_LASTNAME);

                                return c;
                            } else if (identifier == 5) {
                                // show result of input text search (ordinary date  search)
                                String[] query_parts = command.split(" ");
                                c = getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
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
    protected void update(int id, int bannedStatus,String[] textColumnEntries) {
        ContentValues cv = new ContentValues();

        int colums_last = COLUMNS.length - 1;
        int k = 0;
        for (int i = 0; i < COLUMNS.length; i++) {
            if (i == 0)
                cv.put(COL_ID, id);
            if (i == colums_last)
                cv.put(COL_BANNED, bannedStatus);
            if (i >= 2 && i < colums_last - 1) {
                System.out.println("Set "+textColumnEntries[k]+" to Column("+COLUMNS[i]+")");
                cv.put(COLUMNS[i], textColumnEntries[k]);
                k++;
            }
        }

        SQLiteDatabase database = getWritableDatabase();

        if (id != 0)
            database.update(DATABASE_TABLE_PERSONS, cv, COL_ID + "=" + id, null);

    }
    protected void updateBannedStatus(int index,int status){

        ContentValues cv = new ContentValues();
        cv.put(COL_BANNED, status);

        if (index != 0)
            db.update(DATABASE_TABLE_PERSONS, cv, COL_ID + "=" + index, null);

    }

    protected void updateIndex(int index){

        ContentValues cv = new ContentValues();
        cv.put(COL_ID, index-1);

        if (index != 0)
            db.update(DATABASE_TABLE_PERSONS, cv, COL_ID + "=" + index, null);
    }

    /**
     * Make the database entries(persons) older if the have their birthday.
     * If a contact has its birthday. Print it on the homescreen.
     *
     * @param activity
     */
    protected void checkPersonsAgeInDatabase(Activity activity) {

        boolean hasBDay = false;

        Cursor cursor = readData();

        cursor.moveToFirst();
        /*check every entry of its age. If the person got older
        * set the new age to it
        * */

         for (int i = 0; i < cursor.getCount(); i++) {

            int currentAgeInDatabase = cursor.getInt(cursor.getColumnIndex(COL_AGE));
            String dateOfBirth = cursor.getString(cursor.getColumnIndex(COL_BIRTHDATE));
            String firstname = cursor.getString(cursor.getColumnIndex(COL_FIRSTNAME));
            String lastname = cursor.getString(cursor.getColumnIndex(COL_LASTNAME));

            /*A new person calculates its age by birthdate. So I just have to create a new
            * person with the cursor: first name, last name, date of birth to get the actual
            * age of the person.
            *
            * Then check the persons age with the age integer in the database and change it if
            * required
            * */

             //if the person has become older, update its age
             if (compareAgeStatus(currentAgeInDatabase, dateOfBirth, cursor)) {
                 hasBDay = true;
             }
             //the textview to show the birthday event
             if(hasBDay) {
                 String notificationText =
                         firstname + " " + lastname + " ist heute " + (currentAgeInDatabase + 1) + " Jahre alt geworden";
                    /*
                 c.println(notificationText);

                 if (activity instanceof StartActivity) {
                     bdayEvent = (TextView) activity.findViewById(R.id.bdayInfo);

                     bdayEvent.setText(bdayEvent.getText() + NEW_LINE + notificationText);
                     hasBDay = false;
                 }else{
                     c.println("StartActivity not detected");
                 }*/
             }else{
                 //System.out.println("No birth day for "+firstname+" "+lastname);
             }
            cursor.moveToNext();
        }
        cursor.close();
    }
    /*
    * If a person has its birthday, update its age
    *
    *
    *
    * */
    protected void updateAgeInEntry(String[] columns, int age, int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        for (int i = 0; i < columns.length; i++) {
            cv.put(columns[i], age);
        }

        db.update(DATABASE_TABLE_PERSONS, cv, COL_ID + "=" + id, null);

    }

    public void checkColumnState(Cursor c) {

        c.moveToFirst();

        for (int i = 0; i < COLUMNS.length; i++) {
            System.out.println("Check current column[" + COLUMNS[i] + "]\n");

            System.out.println(c.getColumnIndex(COLUMNS[i]));

            c.moveToNext();
        }
    }


    private boolean compareAgeStatus(int databaseAge, String databaseDate,Cursor cursor) {
        String[] columns = {COL_AGE};
        DateHandler dh = new DateHandler();

        String[] date = databaseDate.split("\\.");

        int day = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]);
        int year = Integer.parseInt(date[2]);

        int age = dh.getAgeInYears(day, month, year);
        //System.out.println("Database Date: "+databaseDate+"\tCalc. Age: "+age+"\tDatabase Age: "+databaseAge);
        if (age > databaseAge) {
            updateAgeInEntry(columns, age, cursor.getInt(cursor.getColumnIndex(COL_ID)));
            return true;
        }
        return false;
    }
    /*
    * Backup Database to external Folder to have access from Computer
    *
    */
    public void exportDatabase(String databaseName) {
        try {
            File sd = new File(Environment.getExternalStorageDirectory()+"/KWIMG/", "BACKUP");
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {

                String currentDBPath = "//data//"+PACKAGE+"//databases//"+databaseName+"";
                String backupDBPath = DATABASE_BACKUP_NAME+".db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }else{
                    System.out.println("Datenbank existiert nicht");
                }
            }
        } catch (Exception e) {

        }
    }


}
