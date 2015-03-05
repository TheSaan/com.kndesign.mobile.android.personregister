package com.knoeflerdesign.keywest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knoeflerdesign.keywest.Handler.AndroidHandler;
import com.knoeflerdesign.keywest.Handler.DateHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntryActivity extends Activity {

    public static final int REQUEST_TAKE_PICTURE = 1;
    final static String[] pathNamesInOrder = {
            "Profilbild", "Führerschein", "CheckItCard", "Pass", "ÖBB", "PersoF", "PersoB"
    };
    protected static boolean isPersonCreatedOrUpdated = false;
    final int DATE_UNFORMATTED = 8;
    final int DATE_FORMATTED = 10;
    final String[] imagePathsKeys = {Database.COL_PROFILEPICTURE, Database.COL_DRIVERSLICENCE, Database.COL_CHECKITCARD, Database.COL_PASSPORT, Database.COL_OEBBCARD, Database.COL_PERSO_FRONT/*, Database.COL_PERSO_BACK*/};
    // banned from company is guest (Hausverbot 0 = no, 1 = yes)
    protected int isBanned = 0;
    PrintStream c = System.out;
    Database db;
    CursorFactory cf;
    DateHandler datecalc;
    //Views
    ImageView CurrentViewForImage;
    ImageButton bannedImageButton;
    Button profilePictureButton, driversLicenceButton, checkitcardButton,
            oebbCardButton, passportButton, idFrontButton, idBackButton,
            saveButton;
    TextView text;
    EditText newNameText, dateText;
    int age, testCounter;
    //persons id from database when updating entry
    int mId;
    boolean isNewStart = true;
    //Dialog elements
    boolean isUpdatable = false;//if the new entry can be updated in database
    TextView tvFirst, tvLast, tvAge;
    ImageView picture;
    Button overwrite, keep;
    File detailsFile, photo, photo_tmp, absolutPath;
    Uri imageUri;
    Intent takePictureIntent = new Intent(
            MediaStore.ACTION_IMAGE_CAPTURE);
    //test counter
    int counter = 0;
    //
    private String datestring, sMonth, sYear, sDay, formattedDate, firstname, lastname, imageKind, profilePicturePath, driverslicencePath, checkitcardPath, oebbPath,
            passportPath, idcardPath_front, idcardPath_back, detailspath;
    // Database array to recieve the selected cards as booleans
    private String[] imagePaths = {
            profilePicturePath, driverslicencePath,
            checkitcardPath, passportPath, oebbPath,
            idcardPath_front/*, idcardPath_back*/
    };
    private SharedPreferences memory;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        db = new Database(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newentry);
        age = 0;
        testCounter = 0; //counter to count how often the takePicture method gets called
        findAllViewsByID();
        // add listeners
        addListeners();

        memory = getApplicationContext().getSharedPreferences("temp", 0);
        editor = memory.edit();
        // DEVELOPERS MODE
        c.println("\n\nonCreate()\n\n");
        recreateData();
        /*
         * the values are only settet to test ordinary input values without
		 * having to type them in all the time
		 */
        //fillActivityWithTestData("Michael Knöfler","08111990");
        isNewStart = true;
        //fillActivityWithTestData("Michael Knöfler","08111990");
        // DEVELOPERS MODE END
    }

    private synchronized void createLocation(View v, int id) {

        String PERSON_FOLDER;
        String inputText;
        String NAME_PATTERN;
        LinearLayout LayoutContainer;
        File personDIR;
        String fullName;

		/*
         * If View v is the horizontal LinearLayout which includes the Button
		 * and the ImageView than define as the ImageView of the touched element
		 * parent
		 */

        LayoutContainer = (LinearLayout) findViewById(id);
        try {
            /*
             * id�s with ending "LL" are the horizontal LinearLayout Elements
			 * which including the ImageView and the Button
			 */
            if (id != R.id.miniPersonalidLL) {

                ImageView imageView = (ImageView) findViewById(LayoutContainer
                        .getChildAt(0).getId());

                setCurrentViewForImage(imageView);
            } else {
                if (v.getId() == R.id.idFrontsideButton) {
                    ImageView imageView = (ImageView) findViewById(LayoutContainer
                            .getChildAt(0).getId());
                    setCurrentViewForImage(imageView);
                }
                if (v.getId() == R.id.idBacksideButton) {
                    ImageView imageView = (ImageView) findViewById(LayoutContainer
                            .getChildAt(2).getId());
                    setCurrentViewForImage(imageView);
                }

            }
            saveButtonIdToMemory(getCurrentViewForImage().getId());
        } catch (Exception e) {
            c.println("Exception:\t" + e);
        }


        // chooses the kind of card which has been taken as image
        String IDCARD = checkKindOfImage(getCurrentViewForImage());
        // add the path of the image to the database object

        // returns the persons name which is filled in
        newNameText = (EditText) findViewById(R.id.newName);
        datecalc = new DateHandler();


        // get selected date
        EditText dateEditText = (EditText) findViewById(R.id.dateText);
        inputText = dateEditText.getText().toString();
        int inputTextLength = inputText.toCharArray().length;


        // count with or without splitting dots
        switch (inputTextLength) {
            case DATE_UNFORMATTED: {
                dateEditText.setHintTextColor(Color.parseColor("#F5F6CE"));
                sDay = inputText.substring(0, 2);
                sMonth = inputText.substring(2, 4);
                sYear = inputText.substring(4, 8);
                break;
            }
            case DATE_FORMATTED: {
                dateEditText.setHintTextColor(Color.parseColor("#F5F6CE"));
                sDay = inputText.substring(0, 2);
                sMonth = inputText.substring(3, 5);
                sYear = inputText.substring(6, 10);
                break;
            }
            default: {
                Toast.makeText(EntryActivity.this, "Kein Datum erkannt!",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }


        // merge
        datestring = sDay + sMonth + sYear;
        formattedDate = sDay + "." + sMonth + "." + sYear;

        NAME_PATTERN = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}";

        inputText = newNameText.getText().toString();

        String[] firstAndLastName = new String[3];

        try {

            firstAndLastName = inputText.split(" ");

            if (firstAndLastName[0] == "") {

                for (int i = 0; i < firstAndLastName.length; i++) {
                    firstAndLastName[i].replaceAll("\\s", "Unbekannt");
                    c.println("Start replacing Array Content " + i
                            + " to 'Unbekannt'.\n" + "Content of Index " + i
                            + ": " + firstAndLastName[i]);
                }
            } else {
                c.println(firstAndLastName[0] + ", " + firstAndLastName[1]
                        + " set as Name.");
            }

            // root directory of the app data
            File KWIMG = new File(Environment.getExternalStorageDirectory(), "KWIMG");
            c.println("KWIMG path: " + KWIMG.getPath());
            KWIMG.mkdir();
            KWIMG.setWritable(true);

			/*
             * create individualized folder with named by the persons full name
			 * and its birthday
			 */
            PERSON_FOLDER = firstAndLastName[0].toUpperCase(Locale.GERMAN) + "_"
                    + firstAndLastName[1].toUpperCase(Locale.GERMAN) + "_"
                    + datestring;
            c.println("PERSON_FOLDER: " + PERSON_FOLDER);

            // assumption of all firstname1,firstname2(if available) and
            // lastname
            fullName = firstAndLastName[0] + " " + firstAndLastName[1];

            personDIR = new File(KWIMG.getPath() + "/" + PERSON_FOLDER);

            //c.println("personDIR(& absolutPath): "+ personDIR.getPath());
            absolutPath = personDIR;

            personDIR.mkdirs();
            personDIR.canWrite();

        } catch (IndexOutOfBoundsException ex) {
            Toast.makeText(EntryActivity.this,
                    "Kein Name erkannt oder Nachname fehlt!",
                    Toast.LENGTH_SHORT).show();
            System.err.println("CacheString Index Out Of Bounds: " + ex);
            if (firstAndLastName != null) {
                System.err.println("String length is less than 2, length:\t"
                        + firstAndLastName.length
                        + " Name was set to firstname, lastname = UNKNOWN ");
            }
            fullName = "";
            personDIR = null;
        }
        Pattern p = Pattern.compile(NAME_PATTERN);
        Matcher m = p.matcher(fullName);


        if (m.matches()) {
            newNameText.setHintTextColor(Color.parseColor("#F5F6CE"));


            // save filepath and its name
            String filePath = personDIR.getPath()
                    + "IMG_"
                    + firstAndLastName[0].toUpperCase(Locale.GERMANY)
                    + "_"
                    + firstAndLastName[1].toUpperCase(Locale.GERMANY) + IDCARD
                    + ".jpg";

            // database dispatches
            firstname = firstAndLastName[0];
            lastname = firstAndLastName[1];

            // Button IDs
            final int[] PHOTO_BUTTON_IDS = {profilePictureButton.getId(),
                    driversLicenceButton.getId(), checkitcardButton.getId(),
                    passportButton.getId(), oebbCardButton.getId(),
                    idFrontButton.getId()/*, idBackButton.getId()*/};


            testCounter++;
            c.println(testCounter);

            // set the file path to array for database
            for (int i = 0; i < PHOTO_BUTTON_IDS.length; i++) {
                //c.println("MAX: " + PHOTO_BUTTON_IDS.length);
                //c.println("i: " + i);
                if (PHOTO_BUTTON_IDS[i] == LayoutContainer.getChildAt(1)
                        .getId()) {
                    imagePaths[i] = filePath;
                    //c.println("1 Set " + filePath + " to " + imagePaths[i] + "\n");

                } else
                /*if(PHOTO_BUTTON_IDS[i] == R.id.idBacksideButton){

                    imagePaths[i] = filePath;
                    c.println("2 Set "+filePath+" to "+imagePaths[i]+"\n");
                }*/
                    if (imagePaths[i] == "" || imagePaths[i] == null) {

                        imagePaths[i] = "KEIN EINTRAG";
                    }
                //c.println("Add [" + imagePathsKeys[i] + "]" + imagePaths[i] + " to memory" + "\n\n\n");
                editor.putString(imagePathsKeys[i], imagePaths[i]);
            }

            /*
             * The actual photo object
			 *
			 * includes directory and the file name
			 *
			 * the file name is based on the first & last name and the birthday
			 * which was given by the users input
			 */
            String path = firstAndLastName[0].toUpperCase(Locale.GERMANY)
                    + "_"
                    + firstAndLastName[1].toUpperCase(Locale.GERMANY);

            String date = dateText.getText().toString();
            String name = newNameText.getText().toString();

            // age output
            String output = text.getText().toString();
            int dateLength = dateText.getText().toString().toCharArray().length;

            if (name == "" || date == "" || output == "" || dateLength == 8) {

                if (name == "")
                    Toast.makeText(EntryActivity.this, "Bitte Namen angeben!",
                            Toast.LENGTH_SHORT).show();
                if (date == "")
                    Toast.makeText(EntryActivity.this, "Bitte Datum angeben!",
                            Toast.LENGTH_SHORT).show();

                if (dateLength == 8) {
                    //and press the take picture button again
                    calculateAge();
                    v.callOnClick();
                }
            }
            if (date != "" && output != "" && dateLength == 10) {
                Toast.makeText(EntryActivity.this, "Öffne Kamera...",
                        Toast.LENGTH_SHORT).show();
                // clear first
                photo = null;

                photo = new File(personDIR, "IMG_" + path + IDCARD + ".jpg");
                photo_tmp = photo;

                // declare imageUri as the Uri of photo
                imageUri = Uri.fromFile(photo);
                //saves the uri path temporary
                saveToMemory(imageUri);


                detailsFile = new File(personDIR.getPath(), path + "_DETAILS.txt");
                detailspath = detailsFile.getAbsolutePath();

                takePicture(v);
            }

        }

    }


    private void initDialog(Cursor cursor) {

        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog_person_info);

        tvFirst = (TextView) dialog.findViewById(R.id.dialogFirstname);
        tvLast = (TextView) dialog.findViewById(R.id.dialogLastname);
        tvAge = (TextView) dialog.findViewById(R.id.dialogAge);
        picture = (ImageView) dialog.findViewById(R.id.dialogImage);

        //Fill dialog with cursor data
        tvFirst.setText(cursor.getString(cursor.getColumnIndex(Database.COL_FIRSTNAME)));
        tvLast.setText(cursor.getString(cursor.getColumnIndex(Database.COL_LASTNAME)));

        //calculate the age out of the database
        int[] date = datecalc.getDateInSplittedFormatAsInteger(cursor.getString(cursor.getColumnIndex(Database.COL_BIRTHDATE)));
        int age = datecalc.getAgeInYears(date[0], date[1], date[2]);

        tvAge.setText(age + " Jahre");

        final int id = cursor.getInt(0);

        AndroidHandler ah = new AndroidHandler(getApplicationContext());

        final Bitmap bm = ah.getBitmap(cursor.getString(cursor.getColumnIndex(Database.COL_PROFILEPICTURE)));
        picture.setImageBitmap(bm);

        overwrite = (Button) dialog.findViewById(R.id.dialogTakeThisButton);
        keep = (Button) dialog.findViewById(R.id.dialogKeepThisData);


        overwrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete at first the found entry
                mId = id;
                isUpdatable = true;
                bm.recycle();
                dialog.dismiss();
                EntryActivity.isPersonCreatedOrUpdated = true;

            }
        });
        keep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bm.recycle();
                Intent i = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(i);
                onDestroy();
            }
        });

        dialog.setTitle("Eintrag überscheiben?");
        dialog.show();

    }

    private Cursor personDuplicateCursor;
    private synchronized void takePicture(View v) {
        isNewStart = false;

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //save the id of the current camera image view

        boolean isDuplicate = checkPersonForOverwrite(v);
        if (takePictureIntent != null) {

            if(isDuplicate){
                if(isUpdatable) {
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        System.out.println("in true if");
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
                        onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
                    }
                }else{
                    if(personDuplicateCursor != null)
                        initDialog(personDuplicateCursor);
                }
            }else
            if(!isDuplicate){
                if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    System.out.println("in false if");
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
                    onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
                }
            }


        }
            //deletes all temporary data
            editor.clear();

    }



    private boolean checkPersonForOverwrite(View v) {

        //Test cursor for the first entry
        Cursor checkCursor = db.readData();
        //ensure that at least one entry is available to check
        if (checkCursor.getCount() > 0) {

            Cursor c = db.getCursor(db.getId(firstname + " " + lastname + " " + age));

            if (c != null) {
                if (c.getCount() > 0) {
                    if (!isUpdatable) {
                        c.moveToFirst();
                        personDuplicateCursor = c;
                        return true;
                    }else{
                        System.out.println("is Updatable, return true");

                    }
                }
            }
        }
        System.out.println("return false");
        return false;
    }
    public void onResume() {
        recreateData();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
         * take the picture with the device camera and its software
		 */

        try {
            super.onActivityResult(requestCode, resultCode, data);
            Log.v("Passed", "OnActivityResult");
            if (data == null) {

                if (requestCode == REQUEST_TAKE_PICTURE) {
                    c.println("Request Code OK\n");

                    if (resultCode == Activity.RESULT_OK) {
                        //add new Image Paths

                        notifyAsSynchronized();

                    } else {
                        c.println("Result Code FAILED\n");
                    }
                } else {
                    c.println("Request Code FAILED\n");
                    Toast.makeText(EntryActivity.this, "Fehlgeschlagen! Bitte erneut versuchen.",
                            Toast.LENGTH_SHORT).show();
                    Log.v("Passed", "Restart OnActivityResult");
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
                }
            } else {
                /*
				 * if you throw the picture away which was taken to make a new
				 * and receive the saved View
				 */
                if (resultCode == Activity.RESULT_CANCELED) {
                    c.println("Nehme neues Foto auf.");
                    createLocation(getCurrentViewForImage(), 1);
                }
            }

        } catch (Exception ex) {
            System.err.println("ON ATIVITY RESULT EXCEPTION: " + ex);
        }
    }

    private synchronized void notifyAsSynchronized() {
        notifyAll();
    }

    private ImageView getCurrentViewForImage() {

        return CurrentViewForImage;
    }

    private void setCurrentViewForImage(ImageView i) {
        CurrentViewForImage = i;
    }

    private void addDetailsToFile(File file, int id) {
        file = detailsFile;
        EditText e = (EditText) findViewById(id);
        FileWriter w;
        String details = e.getText().toString();
        try {
            w = new FileWriter(file);
            w.write(details);
            w.close();
        } catch (IOException ex) {
            Log.v("Write Details Error", ex + "");
        }
    }

    protected void save(View v) {

        db.getWritableDatabase();

        if (db != null) {

            // add details to textfile
            addDetailsToFile(detailsFile, R.id.personDetails);
            isNewStart = true;

            for (int i = 0; i < imagePaths.length; i++) {
                //c.println(pathNamesInOrder[i]+": "+imagePaths[i]);
            }
            if (!isUpdatable) {
                //if no person with these data exists create one
                db.addPerson(age, firstname, lastname, formattedDate,
                        imagePaths[0], imagePaths[1], imagePaths[2], imagePaths[3],
                        imagePaths[4], imagePaths[5], "KEIN EINTRAG",
                        detailsFile.getPath(), isBanned);
            } else {
                String[] textBasedColumns = {firstname, lastname, formattedDate,
                        imagePaths[0], imagePaths[1], imagePaths[2], imagePaths[3],
                        imagePaths[4], imagePaths[5], "KEIN EINTRAG",
                        detailsFile.getPath()
                };
                c.println("Firstname in array:  " + textBasedColumns[2]);
                //update
                db.update(mId, isBanned, textBasedColumns);

                Toast.makeText(
                        EntryActivity.this, "Überschrieben!", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(
                    EntryActivity.this, "Gespeichert", Toast.LENGTH_SHORT).show();


            Intent i = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(i);
            onDestroy();


        } else {
            Toast.makeText(
                    EntryActivity.this, "Datenbankfehler: Es kann keine Datenbank gefunden werden.",
                    Toast.LENGTH_LONG).show();
        }


    }

    private void findAllViewsByID() {
        // button for activating the camera
        profilePictureButton = (Button) findViewById(R.id.profilePictureButton);
        driversLicenceButton = (Button) findViewById(R.id.driversLicenceButton);
        checkitcardButton = (Button) findViewById(R.id.checkitcardButton);
        oebbCardButton = (Button) findViewById(R.id.oebbcardButton);
        passportButton = (Button) findViewById(R.id.passportButton);
        idFrontButton = (Button) findViewById(R.id.idFrontsideButton);
        idBackButton = (Button) findViewById(R.id.idBacksideButton);
        // confirmation to save
        saveButton = (Button) findViewById(R.id.saveButton);
        text = (TextView) findViewById(R.id.currAgeTextView);

        bannedImageButton = (ImageButton) findViewById(R.id.isBannedButton);
        newNameText = (EditText) findViewById(R.id.newName);
        dateText = (EditText) findViewById(R.id.dateText);

    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void addListeners() {


        // View Arrays
        Button[] buttons = {profilePictureButton, driversLicenceButton,
                checkitcardButton, oebbCardButton, passportButton,
                idFrontButton, /*idBackButton*/};
        int[] linearlayouts = {R.id.miniProfilePictureLL,
                R.id.miniDriversLicenceLL, R.id.miniCheckitCardLL,
                R.id.miniOebbCardLL, R.id.miniPassportLL, R.id.miniPersonalidLL};

        Log.v("buttons length", "" + buttons.length);
        Log.v("layouts length", "" + linearlayouts.length);

        for (int i = 0; i < buttons.length; i++) {

            final int id;
            if (i > linearlayouts.length - 1) {
                id = linearlayouts[5];
            } else {
                id = linearlayouts[i];
            }

            buttons[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    calculateAge();
                    createLocation(v, id);
                }
            });

        }
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                save(v);
            }
        });


        bannedImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isBanned == 0) {
                    isBanned = 1;
                    bannedImageButton
                            .setImageResource(R.drawable.ic_house_banned_red);
                } else {
                    isBanned = 0;
                    bannedImageButton
                            .setImageResource(R.drawable.ic_house_banned_grey);
                }
            }
        });
    }

    private void fillActivityWithTestData(String name, String date) {

        newNameText.setText(name);
        // date

        dateText.setText(date);


        //profilePictureButton.callOnClick();

    }

    public void onStop() {
        onTrimMemory(TRIM_MEMORY_COMPLETE);

        super.onStop();
    }

    private void setKindOfImage(String string) {
        this.imageKind = string;
    }

    private String checkKindOfImage(ImageView view) {

        String text;
        if (view == findViewById(R.id.miniProfilePicture)) {
            text = "_PROFILBILD";
            // c.println("+_PROFILBILD");
            setKindOfImage(text);
            return text;
        }
        if (view == findViewById(R.id.miniDriversLicence)) {
            text = "_FUEHRERSCHEIN";
            // c.println("+_FUEHRERSCHEIN");
            setKindOfImage(text);
            return text;
        }
        if (view == findViewById(R.id.miniCheckitCard)) {
            text = "_CHECKITCARD";
            // c.println("+_CHECKITCARD");
            setKindOfImage(text);
            return text;
        }
        if (view == findViewById(R.id.miniOebbCard)) {
            text = "_OEBB";
            // c.println("+_OEBB");
            setKindOfImage(text);
            return text;
        }
        if (view == findViewById(R.id.miniPassport)) {
            text = "_REISEPASS";
            // c.println("+_REISEPASS");
            setKindOfImage(text);
            return text;
        }
        if (view == findViewById(R.id.miniPersonalIDfront)) {
            text = "_PERSONALAUSWEIS_VS";
            // c.println("+_PERSONALAUSWEIS_VS");
            setKindOfImage(text);
            return text;
        }
        if (view == findViewById(R.id.miniPersonalIDback)) {
            text = "_PERSONALAUSWEIS_RS";
            // c.println("+_PERSONALAUSWEIS_RS");
            setKindOfImage(text);
            return text;
        } else {

            c.println("checkKindOfImage() else wurde aufgerufen");
            setKindOfImage("");
            return "";
        }

    }

    private int loadButtonIdFromMemory() {
        int id = memory.getInt("ButtonID", -1);
        //c.println("ID out: " + id);
        if (id != 0) {
            //Toast.makeText(EntryActivity.this, "ButtonID(" + id + ") wird geladen...", Toast.LENGTH_SHORT).show();
            return memory.getInt("ButtonID", -1);
        } else {
            return -1;
        }
    }

    private void saveButtonIdToMemory(int id) {
        if (id != 0) {
            editor.putInt("ButtonID", id);
            //c.println("ID in: " + id);
            //Toast.makeText(EntryActivity.this, "ButtonID(" + id + ") wurde gespeichert", Toast.LENGTH_SHORT).show();
            editor.commit();

        }
    }

    private String loadUriFromMemory() {
        String p = memory.getString("Path", null);
        //c.println("Uri Path out: " + p);
        if (p != null) {
            //Toast.makeText(EntryActivity.this, "Path (" + p + ") wird geladen...", Toast.LENGTH_SHORT).show();
            return memory.getString("Path", p);
        } else {
            return null;
        }
    }

    private void saveToMemory(Uri uri) {
        /**
         * Saves the path of the Uri to memory
         */
        String p = uri.getPath();
        if (uri != null) {
            editor.putString("Path", p);
            //c.println("Uri Path in: " + p);
            //Toast.makeText(EntryActivity.this, "Path(" + p + ") wurde gespeichert", Toast.LENGTH_SHORT).show();
            editor.commit();

        }
    }

    private String loadNameTextFromMemory() {
        String name = memory.getString("Text_Name", null);
        //c.println("Name out: " + name);
        if (name != null) {
            //Toast.makeText(EntryActivity.this, "Name(" + name + ") wird geladen...", Toast.LENGTH_SHORT).show();
            return memory.getString("Name", name);
        } else {
            return null;
        }
    }

    private String loadDateTextFromMemory() {
        String date = memory.getString("Text_Date", null);
        //c.println("Name out: " + date);
        if (date != null) {
            //Toast.makeText(EntryActivity.this, "Name(" + date + ") wird geladen...", Toast.LENGTH_SHORT).show();
            return memory.getString("Name", date);
        } else {
            return null;
        }
    }

    private void saveTextToMemory(String name, String date) {
        /**
         * Saves the path of the Uri to memory
         */

        if (name != null) {
            editor.putString("Text_Name", name);
            c.println("Name in: " + name);
            //Toast.makeText(EntryActivity.this, "Name(" + name + ") wurde gespeichert.", Toast.LENGTH_SHORT).show();
            editor.commit();
        } else {
            //Toast.makeText(EntryActivity.this, "Name(" + name + ") wurde nicht erkannt oder ist leer!", Toast.LENGTH_SHORT).show();
        }
        if (name != null) {
            editor.putString("Text_Date", date);
            c.println("Date in: " + date);
            // Toast.makeText(EntryActivity.this, "Datum(" + date + ") wurde gespeichert", Toast.LENGTH_SHORT).show();
            editor.commit();
        } else {
            //Toast.makeText(EntryActivity.this, "Datum(" + date + ") wurde nicht erkannt oder ist leer!", Toast.LENGTH_SHORT).show();
        }
    }

    private synchronized void recreateData() {
        c.println("recreateData()");
          /*
              * 1. After returning from camera, the app rotates its orientation and loose
              * the data from the EditTexts.
              * I saved them as SharedPreferences to load them after changing
              * the screen orientation
              *
              * 2.also simulate the click on the calculation button to show the age again
              * 3.set the background image to the "häckchen"
              * */
        TextView currAge = (TextView) findViewById(R.id.currAgeTextView);
        int k = 0;
        if (!isNewStart) {

            ImageView i = (ImageView) findViewById(loadButtonIdFromMemory());
            if (getCurrentViewForImage() == null) {
                setCurrentViewForImage(i);
            } else {
                do {
                    newNameText.setText(loadNameTextFromMemory());
                    dateText.setText(loadDateTextFromMemory());


                    i.setBackgroundResource(R.drawable.ic_action_done);
                    Log.v("Passed", "Häckchen set");
                    File picture = new File(loadUriFromMemory());
                    Log.v("Passed", "Image created");
                    isNewStart = false;
                    k++;
                    c.println("recreateData() Durchlauf(" + k + ")");
                }
                while (currAge.getText().toString().isEmpty() && i.getBackground() == getCurrentViewForImage().getResources().getDrawable(R.drawable.ic_action_photo));
            }
            c.println("getCurrentViewForImage() = null");
        }
    }

    private boolean checkIfExistsInDatabase() {
        return false;
    }

    private void calculateAge() {
        DateHandler date = new DateHandler();
        isNewStart = false;

        EditText dateEditText = (EditText) findViewById(R.id.dateText);
        String inputText = dateEditText.getText().toString();
        String day, month, year;
        try {
            if (inputText.toCharArray().length == 8 || inputText.toCharArray().length == 10) {

                if (inputText.toCharArray().length == 8) {
                    day = inputText.substring(0, 2);
                    month = inputText.substring(2, 4);
                    year = inputText.substring(4, 8);
                } else if (inputText.toCharArray().length == 10) {
                    day = inputText.substring(0, 2);
                    month = inputText.substring(3, 5);
                    year = inputText.substring(6, 10);
                } else {
                    day = null;
                    month = null;
                    year = null;
                    inputText = null;
                }
                int d = Integer.parseInt(day);
                int m = Integer.parseInt(month);
                int y = Integer.parseInt(year);
                // check that the user cannot input invalid numbers for day
                // in month
                if (d <= date.getDaysOfMonth(m, y) && d > 0) {
                    // check the valid input
                    if (day != null && month != null && year != null) {
                        if (day != "" && month != "" && year != "") {
                            Toast.makeText(EntryActivity.this,
                                    day + "." + month + "." + year,
                                    Toast.LENGTH_SHORT).show();
                            text.setText("" + date.getAgeInYears(d, m, y)
                                    + " Jahre alt.");
                            age = date.getAgeInYears(d, m, y);
                            dateEditText.setText(day + "." + month + "."
                                    + year);
                            inputText = dateEditText.getText().toString();
                        } else {
                            Toast.makeText(EntryActivity.this,
                                    "Some is empty", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else {
                        Toast.makeText(EntryActivity.this, "some is null",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(
                            EntryActivity.this,
                            "Der " + date.getMonthName(m, "de")
                                    + " hat nur "
                                    + date.getDaysOfMonth(m, y) + " Tage!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(
                        EntryActivity.this,
                        "Datumsformat einhalten (TTMMYYY)!",
                        Toast.LENGTH_SHORT).show();
                day = month = year = "";
            }

            EditText nameView = (EditText) findViewById(R.id.newName);
            String nameText = nameView.getText().toString();
            saveTextToMemory(nameText, inputText);
        } catch (NullPointerException npe) {

        }
    }
}
