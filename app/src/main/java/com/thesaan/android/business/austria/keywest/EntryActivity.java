package com.thesaan.android.business.austria.keywest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.thesaan.android.business.austria.keywest.Handler.*;

import java.io.File;
import java.util.Locale;

public class EntryActivity extends Activity implements PatternCollection,KeyWestInterface{

    private boolean isCameraActivated = false;
    final private static String[] pathNamesInOrder = {
            "Profilbild", "Führerschein", "CheckItCard", "Pass", "ÖBB", "PersoF", "PersoB"
    };

    protected static boolean isPersonCreatedOrUpdated = false;

    //the factor how small the thumbnail is compares to the original
    private static final double THUMBNAIL_SCALE_FACTOR = 0.16;

    // banned from company is guest (Hausverbot 0 = no, 1 = yes)
    protected int isBanned = 0;

    //Views
    private ImageView CurrentViewForImage;
    private Button currentPhotoButton;
    private Switch bannedSwitch;
    private Button profilePictureButton,passportButton, saveButton;
    private TextView text;
    private EditText newNameText, dateText;

    //the final folder for this person to save the files
    private File personDIR;

    //the thumbnail folder of this person
    private File personThumbnails;

    //the person folder with the full sized images
    private File personFullSizedImages;

    //the root folder of all entries
    private File KWIMG;

    //the folder identifier as first name, last name and date
    private String PERSON_FOLDER;

    //the final name of the current file
    private String fileName;

    //the calculated age
    //this value is only for the Database.addPerson Method as argument
    private int age;

    //persons id from database when updating entry
    private int mId;

    //checks if the activity has just started or is returned from camera after
    //taking a picture
    private boolean isNewStart = true;

    //Dialog elements
    boolean isUpdatable = false;//if the new entry can be updated in database
    private TextView tvFirst, tvLast, tvAge;
    private ImageView picture;
    private Button overwrite, keep;

    //the file where to write the additional details of the person
    private File detailsFile;

    //the actual photo
    private File photo;

    //the photo URI
    private Uri imageUri;



    /*This cursor reads all entries of the person database and
    * looks for an entry which has the given data from the views.
    *
    * If it contains at least one match, the next cursor opens with
    * the required data (Image source, first & last name
    * to show these data inside a Dialog for asking
    * the User to overwrite or not
    * */
    private Cursor personDuplicateCursor;
    //
    private String datestring, sMonth, sYear, sDay, formattedDate, firstname, lastname, imageKind, profilePicturePath, driverslicencePath, checkitcardPath, oebbPath,
            passportPath, idcardPath_front, idcardPath_back, detailspath;

    // Database array to recieve the selected cards as booleans
    private String[] imagePaths = {
            profilePicturePath, passportPath
    };

    //to save the input values of the views for returning from camera
    //because this shots a screen orientation change-> activity restart
    private SharedPreferences memory;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newentry);
        age = 0;

        init(this);
        findAllViewsByID();

        // confirmation to save
        saveButton = (Button) findViewById(R.id.saveButton);

        addListeners();

        memory = getApplicationContext().getSharedPreferences("temp", 0);
        editor = memory.edit();

        //recreateData();

        // DEVELOPERS MODE
        /*
         * the values are only settet to test ordinary input values without
		 * having to type them in all the time
		 */
        //fillActivityWithTestData("Michael Knöfler","08111990",TEST_DETAILS);

        // DEVELOPERS MODE END
        isNewStart = true;
    }
    /*
        * initalize the Handlers for this activity
        * */
    Database db;
    DateHandler dh;
    FilesHandler fh;
    BitmapHandler bh;
    AndroidHandler ah;

    private final void init(Context c){
        db = new Database(c);
        dh = new DateHandler();
        fh = new FilesHandler();
        bh = new BitmapHandler(c);
        ah = new AndroidHandler(c);
    }

    private synchronized void createLocation(View v, int id) {
        /*
         * If View v is the horizontal LinearLayout which includes the Button
		 * and the ImageView than define as the ImageView of the touched element
		 * parent
		 */
        LinearLayout LayoutContainer = (LinearLayout) findViewById(id);
        try {
            /*
             * id�s with ending "LL" are the horizontal LinearLayout Elements
			 * which including the ImageView and the Button
			 */


            ImageView imageView = (ImageView) findViewById(LayoutContainer
                    .getChildAt(0).getId());
            Button button = (Button) findViewById(LayoutContainer.getChildAt(1).getId());

            setCurrentViewForImage(imageView);
            setCurrentPhotoButton(button);


            //saves the image view besides the selected photo button
            saveButtonIdToMemory(getCurrentViewForImage().getId(),DONE_BUTTON);

            //saves the actual photo button
            saveButtonIdToMemory(getCurrentPhotoButton().getId(),PHOTO_BUTTON);

        } catch (Exception e) {
            c.println("Exception:\t" + e);
        }


        // chooses the kind of card which has been taken as image
        String IDCARD = checkKindOfImage(getCurrentViewForImage());

        // get selected date
        EditText dateEditText = (EditText) findViewById(R.id.dateText);

        String inputText = dateEditText.getText().toString();

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

        //format date from DDMMYYYY to DD.MM.YYYY
        formattedDate = sDay + "." + sMonth + "." + sYear;

        inputText = newNameText.getText().toString();

        try {

            String[] firstAndLastName = inputText.split(" ");

            //if the name has two first names
            //create format firstname1-firstname2 Lastname
            if (firstAndLastName.length > 2) {
                Toast.makeText(getApplicationContext(),"Enthält doppelten Vor/Nachname!\nBitte mit Bindestrich schreiben!",Toast.LENGTH_LONG).show();
            }else {

                if (firstAndLastName[0] == "") {

                    for (int i = 0; i < firstAndLastName.length; i++) {
                        firstAndLastName[i].replaceAll("\\s", "Unbekannt");
                    /*c.println("Start replacing Array Content " + i
                            + " to 'Unbekannt'.\n" + "Content of Index " + i
                            + ": " + firstAndLastName[i]);*/
                    }
                }

                // root directory of the app data
                KWIMG = new File(Environment.getExternalStorageDirectory(), "KWIMG");
                //c.println("KWIMG path: " + KWIMG.getPath());
                KWIMG.mkdir();
                KWIMG.setWritable(true);

			/*
             * create individualized folder with named by the persons full name
			 * and its birthday
			 */
                PERSON_FOLDER = firstAndLastName[0].toUpperCase(Locale.GERMAN) + "_"
                        + firstAndLastName[1].toUpperCase(Locale.GERMAN) + "_"
                        + datestring;
                //c.println("PERSON_FOLDER: " + PERSON_FOLDER);

                // assumption of all firstname and lastname
                String fullName = firstAndLastName[0] + " " + firstAndLastName[1];

                personDIR = new File(KWIMG.getPath() + "/" + PERSON_FOLDER);

                personDIR.canWrite();
                personDIR.mkdirs();

                //create the person thumbnail folder
                personThumbnails = new File(personDIR.getPath(), THUMBNAILS);
                personThumbnails.canWrite();
                personThumbnails.mkdir();

                //create the person full sized images folder
                personFullSizedImages = new File(personDIR.getPath(), IMAGES_LARGE);
                personFullSizedImages.canWrite();
                personFullSizedImages.mkdir();

                c.println("Print input name >>>>>>>>>>");
                printForTest(firstAndLastName);

                if (ah.checkMultiplePatterns(ACCEPTED_NAME_CONVENTIONS, fullName)) {
                    newNameText.setHintTextColor(Color.parseColor("#F5F6CE"));

                    String path = firstAndLastName[0].toUpperCase(Locale.GERMANY)
                            + "_"
                            + firstAndLastName[1].toUpperCase(Locale.GERMANY);

                    fileName = "IMG" + "_" + path + "_" + datestring + IDCARD + FilesHandler.JPEG;

                    // save filepath and its name
                    String filePath = personFullSizedImages.getPath() + fileName;

                    // database dispatches
                    firstname = firstAndLastName[0];
                    lastname = firstAndLastName[1];

                    // Button IDs
                    final int[] PHOTO_BUTTON_IDS = {
                            profilePictureButton.getId(),
                            passportButton.getId()
                    };


                    // set the file path to array for database
                    for (int i = 0; i < PHOTO_BUTTON_IDS.length; i++) {
                        //c.println("MAX: " + PHOTO_BUTTON_IDS.length);
                        //c.println("i: " + i);
                        if (PHOTO_BUTTON_IDS[i] == LayoutContainer.getChildAt(1)
                                .getId()) {
                            imagePaths[i] = filePath;
                            //c.println("1 Set " + filePath + " to " + imagePaths[i] + "\n");

                        }
                        if (imagePaths[i] == "" || imagePaths[i] == null) {

                            imagePaths[i] = "KEIN EINTRAG";
                        }
                        //c.println("Add [" + imagePathsKeys[i] + "]" + imagePaths[i] + " to memory" + "\n\n\n");
                        editor.putString(imagePathsKeys[i], imagePaths[i]);
                    }


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
                   /* Toast.makeText(EntryActivity.this, "Öffne Kamera...",
                            Toast.LENGTH_SHORT).show();*/
                        // clear first
                        photo = null;


                        photo = new File(personFullSizedImages, fileName);

                        // declare imageUri as the Uri of photo
                        imageUri = Uri.fromFile(photo);
                        //saves the uri path temporary
                        saveToMemory(imageUri);


                        detailsFile = new File(personDIR.getPath(), path + "_" + datestring + "_" + "DETAILS.txt");
                        detailspath = detailsFile.getAbsolutePath();

                        takePicture(v);
                    }

                } else {
                    Toast.makeText(EntryActivity.this,
                            "Name enthält falsches Format. (Doppelnamen mit Bindestrich)",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(EntryActivity.this,
                    "Kein Name erkannt oder Nachname fehlt!",
                    Toast.LENGTH_SHORT).show();
            System.err.println("CacheString Index Out Of Bounds: " + ex);

            personDIR = null;
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
        int[] date = dh.getDateInSplittedFormatAsInteger(cursor.getString(cursor.getColumnIndex(Database.COL_BIRTHDATE)));
        int age = dh.getAgeInYears(date[0], date[1], date[2]);

        tvAge.setText(age + " Jahre");

        final int id = cursor.getInt(0);

        BitmapHandler bh = new BitmapHandler(getApplicationContext());

        final Bitmap bm = bh.getBitmap(cursor.getString(cursor.getColumnIndex(Database.COL_PROFILEPICTURE)));
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


    private synchronized void takePicture(View v) {
        isNewStart = false;

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //save the id of the current camera image view

        boolean isDuplicate = checkPersonForOverwrite(v);
        if (takePictureIntent != null) {

            if (isDuplicate) {
                if (isUpdatable) {
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        System.out.println("in true if");
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);

                        onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
                    }
                } else {
                    if (personDuplicateCursor != null)
                        initDialog(personDuplicateCursor);
                }
            } else if (!isDuplicate) {
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    System.out.println("in false if");
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
                    onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
                }
            }


        }
        //deletes all temporary data
        editor.clear();

    }


    /*
    * For Listeners and other objects where the dialog element
    * would have to be final to access it inside an inner class
    * */
    Dialog mDialog;

    private void setDialogAsset(Dialog d) {
        mDialog = d;
    }

    private Dialog getmDialog() {
        return mDialog;
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
                    } else {
                        System.out.println("is Updatable, return true");

                    }
                }
            }
        }
        System.out.println("return false");
        return false;
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


                        recreateData(resultCode);

                        //add new Image Paths
                        notifyAsSynchronized();

                    } else {

                        recreateData(resultCode);
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
    private void setCurrentPhotoButton(Button button){
        currentPhotoButton = button;
    }
    private Button getCurrentPhotoButton(){
        return currentPhotoButton;
    }
    private ImageView getCurrentViewForImage() {

        return CurrentViewForImage;
    }

    private void setCurrentViewForImage(ImageView i) {
        CurrentViewForImage = i;
    }


    private void createThumbnails() {

        //temporary file array
        File[] origFiles = personFullSizedImages.listFiles();

        //The amount of files
        int fileAmount = origFiles.length;

        System.out.println("Anzahl der Dateien im img Ordner: "+fileAmount);
        //temporary bitmap object
        Bitmap bitmap;

        /*
        * Run through the file array and check wether it's a image or textfile.
        *
        * Then if its an image, resize it
        * */
        for(int i = 0;i < fileAmount;i++){
            //the path of the current file
            String path = origFiles[i].getPath();
            String fileEnd = fh.checkFileType(origFiles[i]);

            System.out.println("Dateiname: "+ path);

            //check if the file is a jpeg
            if(FilesHandler.JPEG.equals(fileEnd)){
                bitmap = bh.getBitmap(path);

                System.out.println("Width: "+ bitmap.getWidth()+" Height: "+bitmap.getHeight());

                bitmap = bh.resizeBitmap(bitmap,THUMBNAIL_SCALE_FACTOR);

                File thumbnail = new File(personThumbnails,ah.splitIntoFILEPATHAndFILENAME(path,IMAGE_SPLIT_POINT,false)[1]);
                System.out.println("Vorschau Dateipfad: "+thumbnail.getAbsolutePath());
                bh.saveBitmapToFile(thumbnail,bitmap);

                System.out.println("Bilddatei wurde bearbeitet: "+path);
                System.out.println("Und verkleinert nach "+thumbnail.getAbsolutePath()+" verschoben.");

            }else{
                System.out.println("Datei ist keine Bilddatei: "+path);
            }
        }
    }

    private void printForTest(String[] str){
        for(int i= 0;i<str.length;i++){
            c.println("Test Print "+i+":\t"+str[i]);
        }
    }

    protected void save() {
        Toast.makeText(
                EntryActivity.this, "Speichere...", Toast.LENGTH_SHORT).show();
        db.getWritableDatabase();

        try{
            createThumbnails();
        }catch(Exception tmbEx){
            System.out.println("Error while creating thumbnails "+tmbEx);
        }
        if (db != null) {

            // add details to textfile
            EditText detailsText = (EditText)findViewById(R.id.detailsText);
            String details = detailsText.getText().toString();

            isNewStart = true;

            if (!isUpdatable) {
                //if no person with these data exists create one
                db.addPerson(age, firstname, lastname, formattedDate,
                        imagePaths[0], imagePaths[1],
                        details, isBanned);
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
        passportButton = (Button) findViewById(R.id.passportButton);
        /*// confirmation to save
        saveButton = (Button) findViewById(R.id.saveButton);*/

        text = (TextView) findViewById(R.id.currAgeTextView);

        bannedSwitch = (Switch) findViewById(R.id.isBannedSwitch);
        newNameText = (EditText) findViewById(R.id.newName);
        dateText = (EditText) findViewById(R.id.dateText);

    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void addListeners() {


        // View Arrays
        Button[] buttons = {
                profilePictureButton,
                passportButton,
                };
        int[] linearlayouts = {R.id.miniProfilePictureLL,R.id.miniPassportLL,};

        for (int i = 0; i < buttons.length; i++) {

            int id= 0;
            if (i > linearlayouts.length - 1) {

                id = linearlayouts[i];
            }

            final int id2 = id;
            buttons[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    calculateAge();
                    createLocation(v, id2);
                }
            });

        }
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>SAVE TEST<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                save();
            }
        });


        bannedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isBanned = 1;
                    Toast.makeText(
                            EntryActivity.this, "HV "+isBanned, Toast.LENGTH_SHORT).show();
                    buttonView.setBackgroundResource(R.drawable.banned_state_on_background);
                } else {
                    isBanned = 0;
                    Toast.makeText(
                            EntryActivity.this, "HV "+isBanned, Toast.LENGTH_SHORT).show();
                    buttonView.setBackgroundResource(R.drawable.banned_state_off_background);
                }
            }
        });
    }

    private boolean checkIfEnoughDataIsAvailable(){
        int counter = 0;
        /*check that at least on path is available, means
        * at least one id card.
        *
        * Starting from index 1 because 0 is the Profile picture which
        * isn't required primary
        * */
        for(int i = 1;i<imagePaths.length;i++) {
            if (imagePaths[i] != null && !(imagePaths[i].toString().equals(NO_ENTRY))) {
                counter++;
            }
            if(counter >= 1)
                return true;

        }

        return false;
    }

    private void fillActivityWithTestData(String name, String date, String details) {

        newNameText.setText(name);
        // date

        dateText.setText(date);

        EditText detailsView = (EditText)findViewById(R.id.personDetails);
        detailsView.setText(details);

        //profilePictureButton.callOnClick();

    }

    public void onStop() {
        onTrimMemory(TRIM_MEMORY_COMPLETE);

        super.onStop();
    }

    public void onPause(){
        if(!isNewStart)
            isCameraActivated = true;


        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    public void onBackPressed() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_cancel);
        EntryActivity.this.setDialogAsset(d);
        final Button ok, cancel;
        String text = "Möchten Sie wirklich beenden?";
        ok = (Button) d.findViewById(R.id.okButton);
        cancel = (Button) d.findViewById(R.id.cancelButton);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ok.setOnClickListener(null);
                cancel.setOnClickListener(null);
                EntryActivity.this.setDialogAsset(null);
                EntryActivity.this.finish();


            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EntryActivity.this.getmDialog().dismiss();
                ok.setOnClickListener(null);
                cancel.setOnClickListener(null);
                EntryActivity.this.setDialogAsset(null);

            }
        });

        d.setTitle(text);
        d.show();
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
        if (view == findViewById(R.id.miniPassportLL)) {
            text = "_AUSWEIS";
            setKindOfImage(text);
            return text;
        } else {

            c.println("checkKindOfImage() else wurde aufgerufen");
            setKindOfImage("");
            return "";
        }

    }

    private int loadButtonIdFromMemory(int identifier) {
        switch (identifier){
            case DONE_BUTTON:{
                return memory.getInt(DONE_BUTTON_NAME, -1);
            }
            case PHOTO_BUTTON:{
                return memory.getInt(PHOTO_BUTTON_NAME, -1);
            }
            default:
                return -1;
        }

    }

    private void saveButtonIdToMemory(int id,int identifier) {

        if (id != 0) {
            switch (identifier){
                case DONE_BUTTON:{
                    editor.putInt(DONE_BUTTON_NAME, id);
                    editor.commit();
                }
                case PHOTO_BUTTON:{
                    editor.putInt(PHOTO_BUTTON_NAME, id);
                    editor.commit();
                }
            }
        }
    }

    private String loadUriFromMemory() {
        String p = memory.getString("Path", null);
        if (p != null) {
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

    private synchronized void recreateData(int resultCode) {
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

            ImageView i = (ImageView) findViewById(loadButtonIdFromMemory(DONE_BUTTON));
            Button b = (Button) findViewById(loadButtonIdFromMemory(PHOTO_BUTTON));

            if (getCurrentViewForImage() == null) {
                setCurrentViewForImage(i);
            } else {
                do {
                    newNameText.setText(loadNameTextFromMemory());
                    dateText.setText(loadDateTextFromMemory());

                    if(resultCode == Activity.RESULT_OK) {
                        i.setBackgroundResource(R.drawable.ic_action_done);
                        b.setBackgroundResource(R.drawable.button_yellow_confirmed);

                        //make saveButton green if the user can save
                        //because he has put in enough data
                        checkIfSavingIsPossible(checkIfEnoughDataIsAvailable());

                        File picture = new File(loadUriFromMemory());
                    }
                    isNewStart = false;
                    isCameraActivated = false;
                    k++;
                }
                while (currAge.getText().toString().isEmpty() && i.getBackground() == getCurrentViewForImage().getResources().getDrawable(R.drawable.ic_action_photo));
            }
        }
    }
    private void checkIfSavingIsPossible(boolean checkPaths){
        if(checkPaths)
            saveButton.setBackgroundResource(R.drawable.save);
        else
            saveButton.setBackgroundResource(R.drawable.save_not_enabled);
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
