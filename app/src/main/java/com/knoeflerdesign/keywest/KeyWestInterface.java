package com.knoeflerdesign.keywest;

import android.content.Intent;
import android.provider.MediaStore;
import android.widget.TextView;

import java.io.PrintStream;

/**
 * Created by Michael Knöfler on 12.03.2015.
 */
public interface KeyWestInterface {

    //common
    final String NEW_LINE = "\n";
    //References

    PrintStream c = System.out;

    static final int REQUEST_TAKE_PICTURE = 1;

    //folders
    final static String THUMBNAILS = "tmb";
    final static String IMAGES_LARGE = "img";

    //Image präfix
    final static String IMAGE_SPLIT_POINT = "IMG_";

    final static String NO_ENTRY = "KEIN EINTRAG";


    //Date format
    final int DATE_UNFORMATTED = 8;
    final int DATE_FORMATTED = 10;

    /*
    * Entry Activity
    *
    */
    //test details texts
    final static String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
    final static String TEST_DETAILS = "Ich programmiere diese Applikation";

    //selected Button identifier
    final static int DONE_BUTTON = 9983;
    final static String DONE_BUTTON_NAME = "DoneButtonID";
    final static int PHOTO_BUTTON = 9984;
    final static String PHOTO_BUTTON_NAME = "PhotoButtonID";

    final static String[] imagePathsKeys = {Database.COL_PROFILEPICTURE, Database.COL_DRIVERSLICENCE, Database.COL_CHECKITCARD, Database.COL_PASSPORT, Database.COL_OEBBCARD, Database.COL_PERSO_FRONT/*, Database.COL_PERSO_BACK*/};

    //take pictures
    final static Intent takePictureIntent = new Intent(
            MediaStore.ACTION_IMAGE_CAPTURE);

    /*
    * Database
    *
    */

    // database specific
    final static String QM = "=?";
    final static String AND = " AND ";

    //Application Package
    final static String PACKAGE = "com.knoeflerdesign.keywest";

    //Database Backup Name
    final static String DATABASE_BACKUP_NAME = "key_west_pers_reg_backup";

    //Database Tables
    final static String DATABASE_TABEL_PERSONS = "personen";
    final static String DATABASE_TABEL_SUGGESTIONS = "vorschläge";
    final static String DATABASE_TABEL_PASSWORDS = "passwörter";

    // table values
    final static String PATH_MAX_VARCHAR_LENGTH = "500";
    final static String SELECT_ALL = "*";
    final static String COL_ID = "_id";
    final static String COL_AGE = "_alter";
    final static String COL_LASTNAME = "name";
    final static String COL_FIRSTNAME = "vorname";
    final static String COL_BIRTHDATE = "geburtsdatum";
    final static String COL_PROFILEPICTURE = "profilbild";
    final static String COL_DRIVERSLICENCE = "führerschein";
    final static String COL_CHECKITCARD = "checkitcard";
    final static String COL_PASSPORT = "reisepass";
    final static String COL_OEBBCARD = "öbbcard";
    final static String COL_PERSO_FRONT = "personalausweis_vs";
    final static String COL_PERSO_BACK = "personalausweis_rs";
    final static String COL_DETAILS = "details";
    final static String COL_BANNED = "hausverbot";
    // COL_BANNED always has to be last index, otherwise change the loop in
    // addPerson()
    final static String[] COLUMNS = {COL_ID, COL_AGE, COL_FIRSTNAME,
            COL_LASTNAME, COL_BIRTHDATE, COL_PROFILEPICTURE,
            COL_DRIVERSLICENCE, COL_CHECKITCARD, COL_PASSPORT, COL_OEBBCARD,
            COL_PERSO_FRONT, COL_PERSO_BACK, COL_DETAILS, COL_BANNED};

    final static String DATABASE_NAME = "KeyWestDatabase.db";
    final static String TAG = "KeyWestDatabase";
    final static int DATABASE_VERSION = 1;

    final static String CREATE_PERSON_TABEL_STRING = "CREATE TABLE " + DATABASE_TABEL_PERSONS + "( "
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
            + ") NOT NULL," + COL_DETAILS + " VARCHAR("
            + PATH_MAX_VARCHAR_LENGTH + ") NOT NULL," + COL_BANNED
            + " INTEGER  ) ";

    /*
    * SearchActivity
    *
    * */
    final static int PERSON_INFO_REQUEST = 1;
    //ImageView Ids for the person info activity
    final static int[] idCardImageIds = {R.id.personsCheckItCard, R.id.personsDriversLicence, R.id.personsPassport, R.id.personsPassFront, R.id.personsPassBack, R.id.personsOebbb};
    final static String PLUS_SIXTEEN = "unter 18";
    final static String PLUS_EIGHTEEN = "über 18";
    final static String BANNED = "Verbot";
    final static String ALL = "Alle";

    /*
    * PersonInfoActivity
    *
    * */
    final static String[] orderedPathDescriptions = {
            "Profilepicture",
            "Driverslicence",
            "CheckItCard",
            "Passport",
            "OebbCard",
            "PersoFront",
            "PersonBack"
    };

    /*
    * StartActivity
    *
    * */


   }
