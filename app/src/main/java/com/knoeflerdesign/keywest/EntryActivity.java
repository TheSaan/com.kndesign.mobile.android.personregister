package com.knoeflerdesign.keywest;

import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Color;
import android.hardware.Camera;
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

public class EntryActivity extends Activity {

    public static final int REQUEST_TAKE_PICTURE = 1;
    PrintStream c = System.out;


    ImageView CurrentViewForImage;
    Database db;
    CursorFactory cf;
    DateCalculator datecalc;
    Camera camera;
    ImageButton bannedImageButton;
    Button profilePictureButton, driversLicenceButton, checkitcardButton,
            oebbCardButton, passportButton, idFrontButton, idBackButton,
            saveButton, calcButton;
    TextView text;
    EditText newNameText, dateText;
    private Uri imageUri, temporary;
    int currYear;
    int age;
    final int DATE_UNFORMATTED = 8;
    final int DATE_FORMATTED = 10;
    // banned from company is guest (Hausverbot 0 = no, 1 = yes)
    protected int isBanned = 0;
    String datestring, sMonth, sYear, sDay, personName;
    String formattedDate;
    String firstname, lastname;
    String imageKind;
    String profilePicturePath, driverslicencePath, checkitcardPath, oebbPath,
            passportPath, idcardPath_front, idcardPath_back;
    boolean dateIsCurrentDate = true;
    // Database array to recieve the selected cards as booleans
    protected String[] imagePaths = {profilePicturePath, driverslicencePath,
            checkitcardPath, oebbPath, passportPath, idcardPath_front,
            idcardPath_back};
    File detailsFile, photo;
    int testCounter;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newentry);
        age = 0;
        testCounter = 0; //counter to count how often the takePicture method gets called
        findAllViewsByID();
        // add listeners
        addListeners();

        // DEVELOPERS MODE
        /*
		 * the values are only settet to test ordinary input values without
		 * having to type them in all the time
		 */
        // name
        // fillActivityWithTestData("Michael Kn�fler", "08111990");
        // DEVELOPERS MODE END
    }

    private void createLocation(View v, int id) {

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
        } catch (Exception e) {
            c.println("Exception:\t" + e);
        }
        // chooses the kind of card which has been taken as image
        String IDCARD = checkKindOfImage(getCurrentViewForImage());
        // add the path of the image to the database object

        // returns the persons name which is filled in
        newNameText = (EditText) findViewById(R.id.newName);
        datecalc = new DateCalculator();

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

		/*
		 * TODO activating the timestamp funcionality to create a unique folder
		 * name
		 */
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new
        // Date());

        // regular expression settings
        NAME_PATTERN = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}";
		/*
		 * Split the name into first & last name and send it to a vector (if a
		 * second first name is available)
		 */
        inputText = newNameText.getText().toString();
        // String[] firstAndLastName = new String[inputText.split(" ").length];
        String[] firstAndLastName = new String[3];
        try {

            c.println("Array Lenght: " + firstAndLastName.length);
            firstAndLastName = inputText.split(" ");
            c.println("Firstname: " + firstAndLastName[0] + ", Lastname: " + firstAndLastName[1]);

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

            File KWIMG = new File(getApplication().getApplicationContext()
                    .getFilesDir() + "/KWIMG");
            KWIMG.mkdir();
            KWIMG.setWritable(true);
            String kwPath = KWIMG.getPath().toString()+"/";

            if (KWIMG.exists())
                System.err.println("KWIMG created.");
			/*
			 * create individualized folder with named by the persons full name
			 * and its birthday
			 */
            PERSON_FOLDER = firstAndLastName[0].toUpperCase(Locale.GERMAN) + "_"
                            + firstAndLastName[1].toUpperCase(Locale.GERMAN) + "_"
                            + datestring;

            // assumption of all firstname1,firstname2(if available) and
            // lastname
            fullName = firstAndLastName[0] + " " + firstAndLastName[1];

            personDIR = new File(Environment.getExternalStorageDirectory()
                    .toString() + kwPath + PERSON_FOLDER + "/");

            personDIR.mkdirs();
            personDIR.canWrite();
            System.err.println("Creating locations successfully.");

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

        c.println("Check FULLNAME: " + fullName);

        if (m.matches()) {
            c.println("Matcher startet with " + fullName);

            newNameText.setHintTextColor(Color.parseColor("#F5F6CE"));
            c.println("Picture Path:\t" + personDIR.getPath());
            // save path to array to send it to database
            String filePath = personDIR.getPath()
                    + firstAndLastName[0].toUpperCase(Locale.GERMANY)
                    + firstAndLastName[1].toUpperCase(Locale.GERMANY) + IDCARD
                    + ".jpg";

            // database dispatches
            firstname = firstAndLastName[0];
            lastname = firstAndLastName[1];

            // Button IDs
            final int[] PHOTO_BUTTON_IDS = {profilePictureButton.getId(),
                    driversLicenceButton.getId(), checkitcardButton.getId(),
                    oebbCardButton.getId(), passportButton.getId(),
                    idFrontButton.getId(), idBackButton.getId()};
            // set the file path to array for database
            for (int i = 0; i < 7; i++) {
                if (PHOTO_BUTTON_IDS[i] == LayoutContainer.getChildAt(1)
                        .getId()) {
                    Log.v("PHOTO BUTTON", "PHOTO_BUTTON_ID[" + i + "] is: "
                            + PHOTO_BUTTON_IDS[i]);

                    imagePaths[i] = filePath;

                    Log.v("Photo Path (imagePaths[" + i + "])",
                            imagePaths[i].toString());

                } else {
                    Log.v("else for (imagePaths[" + i + "])",
                            "Else wurde aufgerufen");
                    imagePaths[i] = "KEIN EINTRAG";
                }
                Log.v("ImagePath Index", "" + i);
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
                    + firstAndLastName[1].toUpperCase(Locale.GERMANY);

            // clear first
            photo = null;
            photo = new File(personDIR.getPath(), path + IDCARD + ".jpg");

            // clear first
            detailsFile = null;
            detailsFile = new File(personDIR.getPath(), path + "_DETAILS.txt");

            // declare imageUri as the Uri of photo
            temporary = Uri.fromFile(photo);
            try {
                testCounter++;
                Toast.makeText(EntryActivity.this,
                        "Test Counter = "+testCounter,
                        Toast.LENGTH_SHORT).show();
                String date = dateText.getText().toString();
                String name = newNameText.getText().toString();
                // age output
                String output = text.getText().toString();
                int dateLength = dateText.getText().toString().toCharArray().length;
                c.println("TRY block");
                if (name == "" || date == "" || output == "" || dateLength == 8) {
                    c.println("first if");
                    if (name == "") {
                        Toast.makeText(EntryActivity.this, "Name ist leer!",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (date == "") {
                        Toast.makeText(EntryActivity.this, "Datum ist leer!",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (dateLength == 8 && output == "") {
                        calcButton.callOnClick();
                    }

                }
                if (name != "" && date != "" && output != "" && dateLength == 10) {
                    Toast.makeText(EntryActivity.this,
                            "Taking picture if[data is fine] entered...",
                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(EntryActivity.this,
                            "Photo Uri: "+photo.toURI().toString(),
                            Toast.LENGTH_LONG).show();
                    takePicture(Uri.fromFile(photo));

                } else {
                    Toast.makeText(EntryActivity.this,
                            "Taking picture if[data is wrong] entered...",
                            Toast.LENGTH_SHORT).show();
                    onRestart();
                }
            } catch (Exception ex) {
                Toast.makeText(EntryActivity.this,
                        "DEV: Taking " + imageKind + " failed!",
                        Toast.LENGTH_SHORT).show();
                System.err.println("TAKE PICTURE EXCEPTION: " + ex);
            }
        } else {
            c.println("Full Name Variable doesn't match: " + fullName);
        }

        // KWIMG.setWritable(false);
        // KWIMG.setReadable(false);
    }

    private void takePicture(Uri uri) {
        Log.v("start TAKEPICTURE", "takePicture() started");
        // call the device camera
        final Intent takePictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);

        if (uri != null) {
            if (takePictureIntent != null) {
                Toast.makeText(EntryActivity.this,
                        "Start taking picture...",
                        Toast.LENGTH_SHORT).show();

                Log.v("intent not null in [takePicture]", "Bundle: "
                        + takePictureIntent.getExtras());
                Log.v("intent not null in [takePicture]", "intent: "
                        + takePictureIntent);
                // Test
                /*
                Thread camShot = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(EntryActivity.this,
                                "CamShot thread runs...",
                                Toast.LENGTH_SHORT).show();
                        try {
                            startActivityForResult(takePictureIntent,
                                    REQUEST_TAKE_PICTURE);
                            Toast.makeText(EntryActivity.this,
                                    "Taking picture successfully...",
                                    Toast.LENGTH_SHORT).show();
                        } catch (ActivityNotFoundException ex) {
                            Log.v("ActivityNotFoundEX", "" + ex);
                            Toast.makeText(EntryActivity.this,
                                    "Taking picture failed. Please check LOG!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                // Test End
                camShot.run();
                Toast.makeText(EntryActivity.this,
                        "Stop thread...",
                        Toast.LENGTH_SHORT).show();
                camShot.stop();
                */
                startActivityForResult(takePictureIntent,REQUEST_TAKE_PICTURE);
                onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
                Toast.makeText(EntryActivity.this,
                        "Taking picture method ran through...",
                        Toast.LENGTH_SHORT).show();
                c.println("EntryActivity paused");

            } else {
                Log.v("Uri in [takePicture]", "uri: " + uri);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*
		 * take the picture with the device camera and its software
		 */
        try {
            super.onActivityResult(requestCode, resultCode, data);
            c.println("307");
            if (data == null) {
                Log.v("data is null in [onActivityResult]", "data: " + data);
            } else {
                Log.v("data not null in [onActivityResult]", "data: " + data);
                c.println("337");
                if (requestCode == REQUEST_TAKE_PICTURE
                        && resultCode == Activity.RESULT_OK) {

                    Uri tmp = temporary;
                    // notify everything that the image is about to change
                    getContentResolver().notifyChange(tmp, null);

                    c.println("345");
                    ImageView imageView = getCurrentViewForImage();
                    c.println("347");
                    // Toast "Photo taken"
                    Toast.makeText(EntryActivity.this, "Photo taken",
                            Toast.LENGTH_SHORT).show();

                    // set the picture to the ImageView
                    imageView.setBackgroundResource(R.drawable.ic_action_done);
                    c.println("354");
                }
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

    private void setCurrentViewForImage(ImageView i) {
        CurrentViewForImage = i;
    }

    private ImageView getCurrentViewForImage() {

        return CurrentViewForImage;
    }

    private void addDetailsToFile(File f, int id) {
        f = detailsFile;
        EditText e = (EditText) findViewById(id);
        FileWriter w;
        String details = e.getText().toString();
        try {
            w = new FileWriter(f.getName());
            w.write(details);
            w.close();
        } catch (IOException ex) {
            Log.v("Write Detailsx Error", ex + "");
        }
    }

    protected void save(View v) {

        db = new Database(this, Database.DATABASE_TABEL_PERSONS, cf, 1);
        db.getWritableDatabase();

        if (db != null) {
            if (firstname != null)
                c.println("firstname is valid = " + firstname);
            if (lastname != null)
                c.println("lastname is valid = " + lastname);
            if (formattedDate != null)
                c.println("datestring is valid = " + formattedDate);

            // add details to textfile
            addDetailsToFile(detailsFile, R.id.personDetails);

            db.addPerson(age, firstname, lastname, formattedDate,
                    imagePaths[0], imagePaths[1], imagePaths[2], imagePaths[3],
                    imagePaths[4], imagePaths[5], imagePaths[6],
                    detailsFile.getPath(), isBanned);

            // TODO change text to smaller user information
            Toast.makeText(
                    EntryActivity.this,

                    lastname + "," + firstname + "," + formattedDate + ",\n"
                            + imagePaths[0] + ",\n" + imagePaths[1] + ",\n"
                            + imagePaths[2] + ",\n" + imagePaths[3] + ",\n"
                            + imagePaths[4] + ",\n" + imagePaths[5] + ",\n"
                            + imagePaths[6], Toast.LENGTH_LONG).show();
        } else {
            c.println("Database is was not found.");
        }

        Intent i = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(i);
        onDestroy();
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
        // for calculating the age of the new person
        calcButton = (Button) findViewById(R.id.calcAgeButton);
        text = (TextView) findViewById(R.id.currAgeTextView);

        bannedImageButton = (ImageButton) findViewById(R.id.isBannedButton);
        newNameText = (EditText) findViewById(R.id.newName);
        dateText = (EditText) findViewById(R.id.dateText);

    }

    private void addListeners() {

        // View Arrays
        Button[] buttons = {profilePictureButton, driversLicenceButton,
                checkitcardButton, oebbCardButton, passportButton,
                idFrontButton, idBackButton};
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

        calcButton.setOnClickListener(new View.OnClickListener() {

            DateCalculator date = new DateCalculator();

            public void onClick(View v) {
                EditText dateEditText = (EditText) findViewById(R.id.dateText);
                String inputText = dateEditText.getText().toString();
                String day, month, year;

                day = inputText.substring(0, 2);
                month = inputText.substring(2, 4);
                year = inputText.substring(4, 8);

                int d = Integer.parseInt(day);
                int m = Integer.parseInt(month);
                int y = Integer.parseInt(year);

                if (inputText.toCharArray().length == 8) {
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
                            "Bitte halten Sie das Datums Format ein (TTMMYYY)!",
                            Toast.LENGTH_SHORT).show();
                }
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

        // Calculate age button

        calcButton.callOnClick();

        profilePictureButton.callOnClick();

    }

    public void onResume() {
        super.onResume();
    }

    public void onPause(Intent intent) {
        super.onPause();
    }

    public void onStop() {
        onTrimMemory(TRIM_MEMORY_COMPLETE);
        c.println("EntryActivity stopped");
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

}