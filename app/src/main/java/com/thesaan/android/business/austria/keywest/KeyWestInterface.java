package com.thesaan.android.business.austria.keywest;

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
      String APP_EXT_STORAGE_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()+"/KWIMG/files/";
      String DATABASE_BACKUP_FOLDER = APP_EXT_STORAGE_FOLDER+"/backup/";
      String DATABASE_BACKUP_FILE = DATABASE_BACKUP_FOLDER+DATABASE_BACKUP_NAME;

    //Database Tables
      String DATABASE_TABLE_PERSONS = "personen";
      String DATABASE_TABLE_SUGGESTIONS = "vorschläge";
      String DATABASE_TABEL_PASSWORDS = "passwörter";

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
      String DATABASE_FILE = "//data//"+PACKAGE+"//databases//"+DATABASE_NAME;
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

      String COL_DATABASE_WAS_CHANGED = "dbHasChanged";
      String COL_ = "";

      String DATABASE_TABLE_APPINFO = "app_info";

      String CREATE_APP_INFO_TABLE = "CREATE TABLE" + DATABASE_TABLE_APPINFO +"( "
            + COL_DATABASE_WAS_CHANGED + " INTEGER )";

    String[] TABLES_TO_CREATE =
            {
                CREATE_APP_INFO_TABLE,
                CREATE_PERSON_TABLE
            };
    /*
    * SearchActivity
    *
    * */
      int PERSON_INFO_REQUEST = 1;
    //ImageView Ids for the person info activity
      int[] idCardImageIds = {R.id.personsPassport};
      String PLUS_SIXTEEN = "unter 18";
      String PLUS_EIGHTEEN = "über 18";
      String BANNED = "Verbot";
      String ALL = "Alle";

    /*
    * PersonInfoActivity
    *
    * */
      String[] orderedPathDescriptions = {
            "Profilepicture",
            "Driverslicence"
    };

    /*
    * StartActivity
    *
    * */


   }
