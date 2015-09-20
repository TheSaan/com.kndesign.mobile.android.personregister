package com.thesaan.android.business.austria.keywest;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.thesaan.android.business.austria.keywest.Handler.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;

public class Database extends SQLiteOpenHelper implements PatternCollection, KeyWestInterface {

    private final SQLiteDatabase db;
    Context context;

    public static Drawable[] profile_icon_drawables;
    /*
    this value contains the highest id value of the entries in database
     to create the max value of possible profile_icon_drawables
     to make them available by index
    */
    public static int drawable_id_max;

    private HashMap<String, String> mAliasMap;

    TextView bdayEvent;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        init(context);
        db = this.getWritableDatabase();

/*
        setupDrawableMaxId();

        //create all profile picture icon drawables
        setupDrawables();*/
    }

    public Database(Context context, String args) {
        super(context, DATABASE_BACKUP_NAME, null, DATABASE_VERSION);
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

    private final void init(Context c) {
        dh = new DateHandler();
        fh = new FilesHandler();
        bh = new BitmapHandler(c);
        ah = new AndroidHandler(c);
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            createTables(db, TABLES_TO_CREATE);


            setHashMapForSearchSuggestions();
        } catch (Exception e) {
            Log.e("Error message", e.getMessage());
        }
    }

    /**
     * @param db
     * @param creationStrings An array of the strings to create all tables.
     */
    private void createTables(SQLiteDatabase db, String[] creationStrings) {
        for (int i = 0; i < creationStrings.length; i++) {
            try {
                db.execSQL(creationStrings[i]);
            } catch (Exception e) {
                Log.e("Error message", e + "");
            }
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


    private void setHashMapForSearchSuggestions() {

        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<String, String>();

        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", COL_ID + " as " + "_id");

        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, /*COL_AGE+ " " + */COL_FIRSTNAME /*+" "+ COL_LASTNAME */ + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        //mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, /*COL_AGE+ " " + */COL_DETAILS /*+" "+ COL_LASTNAME */ + " as " + SearchManager.SUGGEST_COLUMN_TEXT_2);

        // Icon for Suggestions ( Optional )
//        mAliasMap.put( SearchManager.SUGGEST_COLUMN_ICON_1, context.getResources().getDrawable(R.drawable.ic_person) + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);

        // This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, COL_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);

    }

/*    protected Drawable getIconDrawable(int id){
        return profile_icon_drawables[id];
    }
    *//**
     * Prepares the profile pictures of all entries as drawables to
     * make them usable for the icon function in the search suggestion
     *//*
    protected void setupDrawables(){
        //get the
        profile_icon_drawables = createProfilePictureThumbnailsFromDatabase();
        System.err.println("");
    }

    private void setupDrawableMaxId() {
        Cursor c = readData();
        int num = c.getCount();
        int max = 0;
        c.moveToFirst();
        for(int i = 0; i < num; i++) {
            int id = c.getInt(c.getColumnIndex(COL_ID));

            if(id > max)
                max = id;

            c.moveToNext();
        }

        drawable_id_max = max;
    }
    private Drawable[] createProfilePictureThumbnailsFromDatabase() {
        //get all entries
        Cursor c = readData();

        //final bitmaps
        BitmapDrawable[] drawables = new BitmapDrawable[drawable_id_max+1];

        FileInputStream fis;
        File file;

        int entryId = 0;

        //the paths of each entry
        String[] paths = getProfilePictureFilePathFromDatabase(c);
        try {
            c.moveToFirst();

            for (int i = 0; i < drawable_id_max;i++) {
                //the first is always null, but in this way the idexes are always equal
                //even if some entry gets deleted
                int index = c.getInt(c.getColumnIndex(COL_ID));

                String filename = paths[i].split(THUMBNAILS)[1];
                String filepath = paths[i].split(THUMBNAILS)[0]+THUMBNAILS;

                file = new File(filepath, filename);



                if(file.exists()) {
                    fis = new FileInputStream(file);
                    drawables[index] = (BitmapDrawable) Drawable.createFromStream(fis,paths[i]);
                }else {
                    drawables[index] = (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_person);
                    System.err.println("File does not exist!(" + paths[i] + ")");
                }
                c.moveToNext();
            }

            return drawables;
        }catch (Exception e){
            System.err.println("Drawable could not be loaded correctly\n"+e);
            e.printStackTrace();
            return null;
        }
    }
    protected String getPersonDirectoryPath(String s) {
        *//*
        Up to index 4 is the root path of each entry

        /storage/emulated/0/MAX_MUSTERMANN_01011950
         *//*
        String[] parts = s.split("/");
        String dir = "/"+parts[1]+"/"+parts[2]+"/"+parts[3]+"/"+parts[4]+"/"+parts[5];
        return dir;
    }
    protected String[] getProfilePictureFilePathFromDatabase(Cursor c){
        int num = c.getCount();

        String[] paths = new String[num];

        c.moveToFirst();

        //choose the thumbnail path of the image
        for(int i = 0; i < num; i++){
            paths[i] = c.getString(c.getColumnIndex(COL_PROFILEPICTURE));

            //split the path to get the file name which is equal in both folders
            String filename = ah.splitFileNameAndPath(paths[i], IMAGE_SPLIT_POINT, false)[1];

            //go back to root of entry
            paths[i] = getPersonDirectoryPath(paths[i]);

            //now go into the thumbnail folder and choose the profile picture
            paths[i] += "/"+THUMBNAILS+filename;

            c.moveToNext();
        }
        return paths;
    }*/


    /**
     *
     * @param username
     * @param password
     * @return
     */
    public boolean isPasswordCorrect(String username,String password){

        //add selection information to the username for database
        String[] args = {username};
        Cursor c = db.query(DATABASE_TABLE_ACCOUNTS, new String[]{COL_PASSWORD}, COL_USERNAME+"=?", args, null, null, null);
        c.moveToFirst();
        String dbPWD = c.getString(c.getColumnIndex(COL_PASSWORD));

        if(dbPWD.equals(password))
            return true;
        else
            return false;
    }

    public synchronized boolean isUserAssigned(String username){
        String[] args = {username};
        //check uf username is already assigned
        Cursor c;

        if(db.isOpen()) {
            c = db.query(DATABASE_TABLE_ACCOUNTS, new String[]{COL_USERNAME}, COL_USERNAME + "=?", args, null, null, null);
            c.moveToFirst();
        }else{
            System.err.println("[isUserAssigned()]Database is closed. Unable to read Usernames");
            return false;
        }

        if(c.getCount() > 0 )
            return true;
        else
            return false;
    }
    /**
     *
     * @return Cursor
     */
    public Cursor readKey(String username){

        //add selection information to the username for database
        String[] args = {username};

        Cursor c = db.query(DATABASE_TABLE_ACCOUNTS, new String[]{COL_PASSWORD}, COL_USERNAME+"=?", args, null, null, null);

        if (c != null)
            c.moveToFirst();
        return c;
    }

    public String changeIntoRankName(int rank_id){
        switch (rank_id){
            case RANK_STANDARD_ID: return RANK_STANDARD;
            case RANK_STAFF_ID: return RANK_STAFF;
            case RANK_MASTER_ID: return RANK_MASTER;
            case RANK_ADMIN_ID: return RANK_ADMIN;
            default:return null;
        }
    }

    /**
     *
     * @param rank
     * @return
     */
    public int changeIntoRankId(String rank){
        System.out.println("Rank to change: "+rank);
        if(RANK_STANDARD.equals(rank)){
            return RANK_STANDARD_ID;
        }
        if(RANK_STAFF.equals(rank))   {
            return RANK_STAFF_ID;
        }
        if(RANK_MASTER.equals(rank))  {
            return RANK_MASTER_ID;
        }
        if(RANK_ADMIN.equals(rank))   {
            return RANK_ADMIN_ID;
        }else {
            return RANK_UNKNOWN_ID;
        }
    }

    public void changeUserPermission(String username, String newPermission){
        String[] args = {username};
        ContentValues cv = new ContentValues();

        Cursor c = db.query(DATABASE_TABLE_ACCOUNTS, new String[]{COL_ID}, COL_USERNAME+"=?", args, null, null, null);
        if (c != null)
            c.moveToFirst();

        if(newPermission == RANK_STAFF ||
                newPermission == RANK_STANDARD ||
                newPermission == RANK_ADMIN ||
                newPermission == RANK_MASTER ) {
            int id = c.getInt(c.getColumnIndex(COL_ID));

            cv.put(COL_RANK,newPermission);



            db.update(DATABASE_TABLE_ACCOUNTS, cv, COL_ID + "=" + id, null);

            System.err.println("Berechtigung von "+ username + " auf "+ newPermission+" geändert.");
        }
    }

    public void changeUserName(String oldUsername, String newUsername) {
        String[] args = {oldUsername};
        ContentValues cv = new ContentValues();

        Cursor c = db.query(DATABASE_TABLE_ACCOUNTS, new String[]{COL_ID}, COL_USERNAME + "=?", args, null, null, null);
        if (c != null){
            c.moveToFirst();

            int id = c.getInt(c.getColumnIndex(COL_ID));

            cv.put(COL_USERNAME,newUsername);

            db.update(DATABASE_TABLE_ACCOUNTS, cv, COL_ID + "=" + id, null);

            System.err.println("Username von "+ oldUsername + " auf "+ newUsername+" geändert.");
        }
    }

    public void changeUserPassword(String username, String newPassword) {
        String[] args = {username};
        ContentValues cv = new ContentValues();

        Cursor c = db.query(DATABASE_TABLE_ACCOUNTS, new String[]{COL_ID}, COL_USERNAME + "=?", args, null, null, null);
        if (c != null){
            c.moveToFirst();

            int id = c.getInt(c.getColumnIndex(COL_ID));

            cv.put(COL_PASSWORD,newPassword);

            db.update(DATABASE_TABLE_ACCOUNTS, cv, COL_ID + "=" + id, null);

            System.err.println("Passwort von "+ username + " auf "+ newPassword+" geändert.");
        }
    }
    /**
     *
     * @return Rank of the User
     */
    public String readRank(String username){

        //add selection information to the username for database
        String[] arg = {username};

        Cursor c = db.query(DATABASE_TABLE_ACCOUNTS, new String[]{COL_RANK}, COL_USERNAME+"=?", arg, null, null, null);

        if (c != null)
            c.moveToFirst();
        String rank = c.getString(c.getColumnIndex(COL_RANK));
        return rank;
    }

    /**
     *
     * @return Rank of the User
     */
    public int readRankID(String username){
        Cursor c = db.query(DATABASE_TABLE_ACCOUNTS,new String[]{COL_RANK},COL_USERNAME+QM,new String[]{username},null,null,null);
        c.moveToFirst();
        return changeIntoRankId(c.getString(c.getColumnIndex(COL_RANK)));
    }
    /**
     *
     * @param rank_id
     *  RANK_STANDARD_ID - shouldn't be set
     *  RANK_STAFF_ID
     *  RANK_MASTER_ID - primary for the owner of the company
     *  RANK_ADMIN_ID - Only for the developer
     *  RANK_UNKNOWN_ID
     * @return Cursor
     *  Cursor with the group of this rank.
     *  Returns null if no group member was found for this rank;
     */
    public Cursor readUsersWithRank(int rank_id){
        String rank = "";

        Cursor c;
        String[] arg= {rank};

        String selection = COL_RANK+"=?";

        switch (rank_id){
            case RANK_STANDARD_ID:{
                rank = RANK_STANDARD;
                break;
            }
            case RANK_STAFF_ID:{
                rank = RANK_STAFF;
                break;
            }
            case RANK_MASTER_ID:{
                rank = RANK_MASTER;
                break;
            }
            case RANK_ADMIN_ID:{
                rank = RANK_ADMIN;
                break;
            }
            default: {
                rank = null;
                rank_id = RANK_UNKNOWN_ID;
                break;
            }

        }

        if(rank_id != RANK_UNKNOWN_ID && rank != null) {
            c = db.query(DATABASE_TABLE_ACCOUNTS, new String[]{COL_USERNAME}, selection, arg, null, null, null);
            if(c.getCount() > 0) {
                return c;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    public String[] getUsers(){
        Cursor c = db.query(DATABASE_TABLE_ACCOUNTS, null, null, null, null, null, null);
        int count = c.getCount();

        if(count > 0) {
            c.moveToFirst();
            String[] users = new String[count];
            for( int i = 0; i<count; i++){
                users[i] = c.getString(c.getColumnIndex(COL_USERNAME));
                System.out.println(users[i]);
                c.moveToNext();
            }
            return users;
        }else{
            System.err.println("No users registered!");
            return null;
        }


    }
    /**Add a new  User to the database.
     * Is checking also if the username is aleady assigned.
     *
     * @param username The username
     * @param password The password
     * @param rank
     *  RANK_STANDARD   -   Standard
     *  RANK_STAFF      -   Personal
     *  RANK_MASTER     -   Chef
     *  RANK_ADMIN      -   Administrator
     */
    protected synchronized void addUser(String username,String password, String rank){
        ContentValues data = new ContentValues();
        getWritableDatabase();
        data.put(COL_USERNAME,username);
        data.put(COL_PASSWORD,password);
        data.put(COL_RANK,rank);



        //if this username is not assigned
        if(!isUserAssigned(username)) {
            db.insert(DATABASE_TABLE_ACCOUNTS, null, data);
            System.err.println("Benutzername gespeichert");
        }else{
            //TODO diese Aussage als Textview hinzufügen
            System.err.println("Benutzername ist bereits vergeben");
        }
    }

    /**
     * Returns Persons
     */
    public Cursor getPersons(String query) {

        String selection =
                COL_FIRSTNAME + " LIKE ? OR " +
                COL_LASTNAME + " LIKE ? OR " +
                COL_AGE + " LIKE ? OR " +
                COL_DETAILS + " LIKE ? OR " +
                COL_BIRTHDATE + " LIKE ?";


        if (query != null) {
            query = "%" + query + "%";


            String[] selValues = new String[]{
                    query,
                    query,
                    query,
                    query,
                    query,
            };

            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setProjectionMap(mAliasMap);

            queryBuilder.setTables(Database.DATABASE_TABLE_PERSONS);

            int amountOfEntriesDisplayed = 9;//todo Einstellung

            Cursor c = queryBuilder.query(getReadableDatabase(),
                    new String[]{
                            COL_ID,
                            COL_AGE,
                            COL_FIRSTNAME,
                            COL_LASTNAME,
                            COL_BIRTHDATE,
                            COL_BANNED,
                            COL_DETAILS
                    },
                    selection,
                    selValues,
                    null,
                    null,
                    COL_LASTNAME + " asc ",/*TODO Einstellung*/
                    amountOfEntriesDisplayed + ""
            );
            if (c.getCount() > 0)
                return c;
            else {
                MatrixCursor mc = new MatrixCursor(new String[]{"_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2});

                mc.moveToFirst();
                return mc;
            }
        } else {
            System.err.println("Selection Args are null in getPersons()");
            return null;
        }

    }

    /**
     * Return Person corresponding to the id
     */
    public Cursor getPerson(String id) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(DATABASE_TABLE_PERSONS);

        Cursor c = queryBuilder.query(
                getReadableDatabase(),
                null,
                COL_ID + " = ?", new String[]{id}, null, null, null, "1"
        );

        return c;
    }

    /**
     * @param name      The last name
     * @param firstname The first name
     */
    /*
     * the rest of the arguments have to be the paths of the card images
	 */
    public void addPerson(int age, String firstname, String name, String date,
                          String profilepicture, String passport, String details, int banned) {

        String[] arguments = {firstname, name, date, profilepicture, passport, details};

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
            db.insert(DATABASE_TABLE_PERSONS, null, data);
            db.close();
        }
    }

    /**
     * If no data is available in the app database, transfer the available
     * data from the database backup
     *
     * @param backup
     */
    protected void transferDataFromBackup(Database backup) {
        ContentValues cv = new ContentValues();
        int age, banned;
        String firstname, name, date,
                profilepicture, passport, details;
        Cursor c = backup.readData();
        int num = c.getCount();
        System.out.println(num + " Einträge gefunden");

        if (c != null && num > 0) {
            for (int i = 0; i < num; i++) {
                age = c.getInt(c.getColumnIndex(COL_AGE));
                banned = c.getInt(c.getColumnIndex(COL_BANNED));
                firstname = c.getString(c.getColumnIndex(COL_FIRSTNAME));
                name = c.getString(c.getColumnIndex(COL_LASTNAME));
                date = c.getString(c.getColumnIndex(COL_BIRTHDATE));
                profilepicture = c.getString(c.getColumnIndex(COL_PROFILEPICTURE));
                passport = c.getString(c.getColumnIndex(COL_PASSPORT));
                details = c.getString(c.getColumnIndex(COL_DETAILS));

                cv.put(COL_AGE, age);
                cv.put(COL_BANNED, banned);
                cv.put(COL_FIRSTNAME, firstname);
                cv.put(COL_LASTNAME, name);
                cv.put(COL_BIRTHDATE, date);
                cv.put(COL_PROFILEPICTURE, profilepicture);
                cv.put(COL_PASSPORT, passport);
                cv.put(COL_DETAILS, details);
            }
        }

        db.insert(DATABASE_TABLE_PERSONS, null, cv);
        db.close();
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
        Cursor c = db.query(DATABASE_TABLE_PERSONS, COLUMNS, COL_AGE + "<=?", new String[]{"17"}, null, null, COL_AGE);

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
        Cursor c = db.query(DATABASE_TABLE_PERSONS, COLUMNS, COL_AGE + ">=?", new String[]{"18"}, null, null, COL_AGE);

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
        Cursor c = db.query(DATABASE_TABLE_PERSONS, COLUMNS, COL_BANNED + "=?", new String[]{"1"}, null, null, COL_AGE);

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
     * @return Index when entry was found. Otherwise return -1
     */
    public int getId(String query) {
        int id;
        String[] queries = query.split(" ");

        String WHERE = COL_FIRSTNAME + QM + AND + COL_LASTNAME + QM + AND + COL_AGE + QM;

        Cursor c = db.query(DATABASE_TABLE_PERSONS, null, WHERE, queries,
                null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            id = c.getInt(0);
        } else {
            id = -1;
        }
        return id;
    }

    protected Cursor getSuggestionCursor(String[] columns, String selection, String[] selectionArgs) {

        Cursor c = getReadableDatabase().query(DATABASE_TABLE_PERSONS, columns, selection, selectionArgs, null, null, null);
        return c;
    }

    protected Cursor getCursor(int index) {

        String WHERE = COL_ID + QM;
        String[] id = {index + ""};
        String[] columns = {COL_ID, COL_PROFILEPICTURE};
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

    protected void update(int id, int bannedStatus, String[] textColumnEntries) {
        ContentValues cv = new ContentValues();

        int colums_last = COLUMNS.length - 1;
        int k = 0;
        for (int i = 0; i < COLUMNS.length; i++) {
            if (i == 0)
                cv.put(COL_ID, id);
            if (i == colums_last)
                cv.put(COL_BANNED, bannedStatus);
            if (i >= 2 && i < colums_last - 1) {
                System.out.println("Set " + textColumnEntries[k] + " to Column(" + COLUMNS[i] + ")");
                cv.put(COLUMNS[i], textColumnEntries[k]);
                k++;
            }
        }

        SQLiteDatabase database = getWritableDatabase();

        if (id != 0)
            database.update(DATABASE_TABLE_PERSONS, cv, COL_ID + "=" + id, null);

    }

    protected void updateBannedStatus(int index, int status) {

        ContentValues cv = new ContentValues();
        cv.put(COL_BANNED, status);

        if (index != 0)
            db.update(DATABASE_TABLE_PERSONS, cv, COL_ID + "=" + index, null);

    }

    protected void updateIndex(int index) {

        ContentValues cv = new ContentValues();
        cv.put(COL_ID, index - 1);

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
            if (hasBDay) {
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
            } else {
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


    private boolean compareAgeStatus(int databaseAge, String databaseDate, Cursor cursor) {
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
    public void exportDatabase() {
        try {
            File sd = new File(APP_DB_BACKUP_FOLDER);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {

                File currentDB = new File(data, DATABASE_FILE);
                File backupDB = new File(sd, DATABASE_BACKUP_NAME);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } else {
                    System.out.println("Datenbank existiert nicht");
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * Restores the database from the existing files. Except for the details.
     */
    public boolean restoreDatabaseFromFiles(){

        PersonRegister pr = new PersonRegister(context);

        File[] f = pr.getEntryFolders();

        try {
            for (int i = 0; i < f.length; i++) {
                String[] data = pr.getFolderData(f[i]);

                System.err.println("i:"+i);
                addPerson(
                        Integer.getInteger(data[0]),
                        data[1],
                        data[2],
                        data[3],
                        data[4],
                        data[5],
                        data[6],
                        Integer.decode(data[7]));
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * @return
     */
    public SQLiteDatabase getBackupDatabase() {
        try {
            File dbfile = new File(DATABASE_BACKUP_FILE);

            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);

            if (db != null) {
                return db;
            } else {
                return null;
            }
        } catch (Exception e) {
            Toast.makeText(context, "No backup db available\n(" + e + ")", Toast.LENGTH_LONG);
            System.err.println("No backup db available");
            return null;
        }
    }

//    private Cursor getAppInfo(){
//        return db.query(DATABASE_TABLE_APPINFO, null,null,null,null,null,null);
//    }
//    protected boolean databaseChanged(){
//        Cursor c = getAppInfo();
//
//        if(c.getInt(c.getColumnIndex(COL_DATABASE_WAS_CHANGED)) == 1)return true;
//        else return false;
//    }
}
