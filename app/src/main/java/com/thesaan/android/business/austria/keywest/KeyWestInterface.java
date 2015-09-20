package com.thesaan.android.business.austria.keywest;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.PrintStream;

/**
 * Created by Michael Knöfler on 12.03.2015.
 */
public interface KeyWestInterface {

    //common
    String NEW_LINE = "\n";
    //References

    PrintStream c = System.out;

    int REQUEST_TAKE_PICTURE = 1;

    //folders
    String THUMBNAILS = "tmb";
    String IMAGES_LARGE = "img";

    //Image präfix
    String IMAGE_SPLIT_POINT = "IMG_";

    String NO_ENTRY = "KEIN EINTRAG";

    String UNKNOWN_BIRTHDATE = "01.01.1950";
    //Date format
    int DATE_UNFORMATTED = 8;
    int DATE_FORMATTED = 10;

    /*
    * Entry Activity
    *
    */
    //test details texts
    String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
    String TEST_DETAILS = "Ich programmiere diese Applikation";

    //selected Button identifier
    int DONE_BUTTON = 9983;
    String DONE_BUTTON_NAME = "DoneButtonID";
    int PHOTO_BUTTON = 9984;
    String PHOTO_BUTTON_NAME = "PhotoButtonID";

    String[] imagePathsKeys = {Database.COL_PROFILEPICTURE, Database.COL_PASSPORT};

    //take pictures
    Intent takePictureIntent = new Intent(
            MediaStore.ACTION_IMAGE_CAPTURE);

    /*
    * Database
    *
    */

    // database specific
    String QM = "=?";
    String AND = " AND ";

    //Application Package
    String PACKAGE = "com.thesaan.android.business.austria.keywest";

    //Database Backup Name

    String DATABASE_BACKUP_NAME = "db_bkp.db";
    String APP_EXT_STORAGE_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PersonenRegister";
    String APP_FILES_FOLDER = APP_EXT_STORAGE_FOLDER+"/files";
    String APP_CACHE_FOLDER = APP_EXT_STORAGE_FOLDER+"/cache/";

    String APP_DB_BACKUP_FOLDER = APP_FILES_FOLDER + "/backup/";
    String APP_ENTRIES_FOLDER = APP_FILES_FOLDER + "/entries/";
    String DATABASE_BACKUP_FILE = APP_DB_BACKUP_FOLDER + DATABASE_BACKUP_NAME;

    //Database Tables
    String DATABASE_TABLE_PERSONS = "personen";
    String DATABASE_TABLE_ACCOUNTS = "konten";

    // table values
    String PATH_MAX_VARCHAR_LENGTH = "500";
    String SELECT_ALL = "*";
    String COL_ID = "_id";
    String COL_AGE = "_alter";
    String COL_LASTNAME = "name";
    String COL_FIRSTNAME = "vorname";
    String COL_BIRTHDATE = "geburtsdatum";
    String COL_PROFILEPICTURE = "profilbild";
    String COL_PASSPORT = "ausweis";
    String COL_DETAILS = "details";
    String COL_BANNED = "lokalverbot";
    // COL_BANNED always has to be last index, otherwise change the loop in
    // addPerson()
    String[] COLUMNS = {COL_ID, COL_AGE, COL_FIRSTNAME,
            COL_LASTNAME, COL_BIRTHDATE, COL_PROFILEPICTURE, COL_PASSPORT, COL_DETAILS, COL_BANNED};

    String DATABASE_NAME = "KeyWestDatabase.db";
    String DATABASE_FILE = "//data//" + PACKAGE + "//databases//" + DATABASE_NAME;
    String TAG = "KeyWestDatabase";
    int DATABASE_VERSION = 1;

    String CREATE_PERSON_TABLE = "CREATE TABLE " + DATABASE_TABLE_PERSONS + "( "
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_AGE
            + " INTEGER," + COL_FIRSTNAME + " VARCHAR(20) NOT NULL,"
            + COL_LASTNAME + " VARCHAR(20) NOT NULL," + COL_BIRTHDATE
            + " DATE NOT NULL," + COL_PROFILEPICTURE + " VARCHAR("
            + PATH_MAX_VARCHAR_LENGTH + ") NOT NULL,"
            + COL_PASSPORT + " VARCHAR(" + PATH_MAX_VARCHAR_LENGTH + "),"
            + COL_DETAILS + " VARCHAR("
            + PATH_MAX_VARCHAR_LENGTH + ")," + COL_BANNED
            + " INTEGER  ) ";


    //Password table

     String RANK_STANDARD = "Standard";
     String RANK_STAFF= "Personal";
     String RANK_MASTER = "Chef";
     String RANK_ADMIN = "Administrator";

     int RANK_UNKNOWN_ID  = 5000;
     int RANK_STANDARD_ID = 5001;
     int RANK_STAFF_ID = 5002;
     int RANK_MASTER_ID  = 5003;
     int RANK_ADMIN_ID  = 5004;

    String COL_USERNAME = "username";
    String COL_PASSWORD = "key";
    String COL_RANK = "rank";

    String CREATE_ACCOUNT_TABLE = "CREATE TABLE " + DATABASE_TABLE_ACCOUNTS + "( "
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_USERNAME + " VARCHAR(100) NOT NULL UNIQUE,"
            + COL_PASSWORD + " VARCHAR(200) NOT NULL UNIQUE,"
            + COL_RANK + " VARCHAR(100) NOT NULL )";

    String COL_ = "";

    String DATABASE_TABLE_SETTINGS = "settings";

    String COL_LANGUAGE = "language";
    String COL_SUGGESTION_LIST_ITEMS = "suggestion_number";
    String COL_SUGGESTION_ORDER = "suggestion_order";

    String CREATE_SETTINGS_TABLE = "CREATE TABLE" + DATABASE_TABLE_SETTINGS + "( "
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_LANGUAGE + " VARCHAR NOT NULL,"
            + COL_SUGGESTION_LIST_ITEMS + " INTEGER NOT NULL,"
            + COL_SUGGESTION_ORDER + " VARCHAR8(100) NOT NULL )";


    String[] TABLES_TO_CREATE =
            {
                    CREATE_SETTINGS_TABLE,
                    CREATE_PERSON_TABLE,
                    CREATE_ACCOUNT_TABLE,
                    CREATE_SETTINGS_TABLE
            };
    /*
    * SearchActivity
    *
    * */
    int PERSON_INFO_REQUEST = 1;
    //ImageView Ids for the person info activity
    String PLUS_SIXTEEN = "unter 18";
    String PLUS_EIGHTEEN = "über 18";
    String BANNED = "Lokalerbot";
    String ALL = "Alle";

    /*
    * PersonInfoActivity
    *
    * */
    String[] orderedPathDescriptions = {
            "Profilepicture",
            "Passport"
    };

    /*
    * StartActivity
    *
    * */


}
