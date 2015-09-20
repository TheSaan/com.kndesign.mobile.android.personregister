package com.thesaan.android.business.austria.keywest;

import android.annotation.TargetApi;
import android.app.*;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import com.thesaan.android.business.austria.keywest.Handler.*;
import com.thesaan.android.business.austria.keywest.saandroid.ProActivity;

import java.io.File;
import java.io.IOException;
import java.util.Locale;


@TargetApi(19)
public class PersonInfoActivity extends ProActivity implements KeyWestInterface {


    PasswordDialog pd;

    //used to set the array numbers for the images and bitmaps
    private final int AMOUNT_OF_IMAGES = 2;

    //if delete process is to get signed
    private boolean isMaster = false;

    static boolean confirmDeleteProgress = false;
    //the search service
    private SearchResultService srs;
    private ServiceConnection SearchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            SearchResultService.MyBinder binder = (SearchResultService.MyBinder) service;
            srs = binder.getService();
            srs.binded = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            srs.binded = false;
            srs = null;
        }
    };
    final static int[] idCardImageIds = {R.id.personPPImage, R.id.personsPassport,};

    //the path chooser for the intent
    private String[] bitmapPaths = new String[AMOUNT_OF_IMAGES];

    //Zoom Activity intent path extras of the selected Bitmaps
    Intent FullscreenImageViewIntent;

    //Profile picture
    ImageView personImage;
    //The oder id images
    ImageView cicImage, dlImage, passImage, persofImage, persobImage, oebbImage;
    //ID image array
    ImageView[] idCardImages = {personImage, passImage};
    //to change the banned state
    Switch bannedSwitch;

    Button deleteButton;
    EditText detailsEditText;

    //person info textviews
    TextView personNameTextView, personAgeTextView, personBirthdateTextView, personBannedStateTextView;

    Cursor mCursor;

    Bundle extras;

    Intent srsintent = new Intent(this, SearchResultService.class);
    String personData;

    public void onResume() {
        super.onResume();
        //set the extras of the zoom image activity null
        //for being ready to get a new path (only one!)
        FullscreenImageViewIntent = null;

        srsintent = new Intent(this, SearchResultService.class);
        bindService(srsintent, SearchServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onBackPressed() {
        updatePersonsDetails(detailsEditText.getText().toString());
        getIntent().putExtra("backFromPersonInfoActivity", true);

        super.onBackPressed();
        onDestroy();
    }

    public void onPause() {
        super.onPause();
        System.gc();
    }

    public void onDestroy() {
        super.onDestroy();

        if (srs.binded) {
            unbindService(SearchServiceConnection);
        } else {
            srs.onUnbind(srsintent);
        }
        srs.restart();
        System.gc();
    }


    /*
    * initalize the Handlers for this activity
    * */
    Database db;
    DateHandler dh;
    FilesHandler fh;
    BitmapHandler bh;
    AndroidHandler ah;

    private final void init(Context c) {
        db = new Database(c);
        dh = new DateHandler();
        fh = new FilesHandler();
        bh = new BitmapHandler(c);
        ah = new AndroidHandler(c);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_person_info);


        init(this);
        extras = getIntent().getExtras();

        personData = extras.getString("personData");

        System.out.println("Show Person data of " + personData);
        mCursor = getSearchCursor(personData);

        if (mCursor == null) {
            Toast.makeText(PersonInfoActivity.this,
                    "Datenlade Fehler:" +
                            " Datatransfer failed because of empty cursor Cursor!",
                    Toast.LENGTH_LONG).show();
            onBackPressed();
        }
        createViews();
        addListeners(mCursor);
        addDataToViews(mCursor);

        pd = new PasswordDialog(PersonInfoActivity.this, true, PersonInfoActivity.this);
        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(dialog instanceof PasswordDialog)
                isMaster = ((PasswordDialog) dialog).hasPassed();
            }
        });

    }

    private void createViews() {
        //the id images
        for (int i = 0; i < idCardImages.length; i++) {
            idCardImages[i] = (ImageView) findViewById(idCardImageIds[i]);
        }

        //details
        detailsEditText = (EditText) findViewById(R.id.detailsText);

        //deleteButton
        deleteButton = (Button) findViewById(R.id.deletePersonButton);

        //banned switch
        bannedSwitch = (Switch) findViewById(R.id.bannedSwitch);

        //person information
        personNameTextView = (TextView) findViewById(R.id.personsName);
        personAgeTextView = (TextView) findViewById(R.id.personsAge);
        personBirthdateTextView = (TextView) findViewById(R.id.personsBirthdate);
        personBannedStateTextView = (TextView) findViewById(R.id.personsBannedState);

    }

    private void addListeners(Cursor cursor) {
        final Cursor c = cursor;

        bannedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    personBannedStateTextView.setText("Hausverbot");
                    db.updateBannedStatus(c.getInt(c.getColumnIndex(Database.COL_ID)), 1);
                } else {
                    personBannedStateTextView.setText("");
                    db.updateBannedStatus(c.getInt(c.getColumnIndex(Database.COL_ID)), 0);
                }

            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isMaster){
                    initDeleteDialog();
                }else {
                    pd.show();
                }
            }
        });


        for (int i = 0; i < idCardImages.length; i++) {
            final int k = i;
            idCardImages[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Intent Path: " + bitmapPaths[k]);

                    //the fullscreen imageview intent
                    FullscreenImageViewIntent = new Intent(PersonInfoActivity.this, PersonImageDetailedActivity.class);

                    FullscreenImageViewIntent.putExtra(orderedPathDescriptions[k], bitmapPaths[k]);
                    startActivity(FullscreenImageViewIntent);
                }
            });
        }
    }
    private void initDeleteDialog() {

        Button confirm,cancel;


        if (isMaster) {
            //if password is correct initialise delete process
            final Dialog dialog = new Dialog(this);

            dialog.setContentView(R.layout.dialog_confim_deleting);

            final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.confirmDeleteCheckbox);

            Button confirmButton = (Button) dialog.findViewById(R.id.confirmDeleteButton);
            Button cancelButton = (Button) dialog.findViewById(R.id.cancelDeleteButton);

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        delete();
                        dialog.cancel();
                    } else {
                        checkBox.setTextColor(Color.RED);
                        Toast.makeText(PersonInfoActivity.this,
                                "Zum Löschen bestätigen",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialog.setTitle("Person löschen?");
            try {
                dialog.show();
            } catch (Exception e) {
                System.err.println("Delete Dialog Show Exception\n" + e);
            }
        }
    }

    private Bitmap getBitmapToZoom(int identifier) {


        //generate Bitmaps
        Bitmap[] bitmaps = new Bitmap[7];
        //gets the number where to start looping through the paths in the cursor
        int cursorPathStartIndex = mCursor.getColumnIndex(Database.COL_PROFILEPICTURE);

        //create the bitmaps
        for (int i = 0; i < bitmaps.length; i++) {
            bitmaps[i] = bh.getBitmapWithOptions(mCursor.getString(cursorPathStartIndex));
            cursorPathStartIndex++;
        }
        if (!(identifier > bitmaps.length) && !(identifier < 0)) {
            bitmaps[identifier] = bh.rotateBitmap(bitmaps[identifier], 90);
            //if its the Profile picture turn 90 deg
            /*if (identifier == 0) {

            }else{
                //bitmaps[identifier] = bh.makeBitmapMutable(bitmaps[identifier]);
                System.out.println("Width:"+bitmaps[identifier].getWidth()+" Height:"+bitmaps[identifier].getHeight());
                bitmaps[identifier].setHeight((bitmaps[identifier].getWidth()-bitmaps[identifier].getHeight())/2);
            //}*/

            return bitmaps[identifier];
        }
        return null;
    }

    private synchronized void delete() {
        fh = new FilesHandler();
        String rootFolderPath = Environment.getExternalStorageDirectory() + "/KWIMG/";

        //the persons root folder
        File folderToDelete = new File(rootFolderPath + createFolderNameFromCursor(mCursor));

        //the thumbnail folder
        File tmbFolder = new File(folderToDelete.getPath() + "/" + EntryActivity.THUMBNAILS);

        //the full size images folder
        File imgFolder = new File(folderToDelete.getPath() + "/" + EntryActivity.IMAGES_LARGE);


       /* System.out.println("TMB:\t"+tmbFolder.getPath());
        System.out.println("IMG:\t"+imgFolder.getPath());
        System.out.println("ROOT:\t"+folderToDelete.getPath());*/

        try {
            if (folderToDelete.exists()) {

                //here only should be found the details file
                int numberOfFilesInRoot = fh.listFiles(folderToDelete.getPath(), false).length;
                File[] rootFiles = fh.listFiles(folderToDelete.getPath(), true);

                //files in tmb folder
                int numberOfFilesInTmb = fh.listFiles(tmbFolder.getPath(), false).length;
                File[] tmbFiles = fh.listFiles(tmbFolder.getPath(), true);

                int numberOfFilesInImg = fh.listFiles(imgFolder.getPath(), false).length;
                File[] imgFiles = fh.listFiles(imgFolder.getPath(), true);

                //delete tmb folder files
                //first all files inside have to be deleted
                if (tmbFolder.isDirectory())
                    //System.out.println("tmb Folder is a dir");

                    if (tmbFolder.exists()) {
                        //System.out.println("tmb Folder to delete exists");
                        for (int i = 0; i < numberOfFilesInTmb; i++) {

                            tmbFiles[i].delete();
                            if (tmbFiles[i].exists()) {
                                System.out.println(tmbFiles[i] + " wurde nicht gelöscht");
                            } else {
                                //System.out.println(tmbFiles[i] + " wurde gelöscht");
                            }
                        }

                        //now if the folder is empty delete it
                        if (tmbFolder.listFiles().length == 0)
                            tmbFolder.delete();

                    }

                //delete img folder files
                //first all files inside have to be deleted
                if (imgFolder.isDirectory())
                    //System.out.println("Img Folder is a dir");

                    if (imgFolder.exists()) {
                        //System.out.println("img Folder to delete exists");
                        for (int i = 0; i < numberOfFilesInImg; i++) {

                            imgFiles[i].delete();
                            if (imgFiles[i].exists()) {
                                System.out.println(imgFiles[i] + " wurde nicht gelöscht");
                            } else {
                                //System.out.println(imgFiles[i] + " wurde gelöscht");
                            }
                        }

                        //now if the folder is empty delete it
                        if (imgFolder.listFiles().length == 0)
                            imgFolder.delete();

                    }

                //delete root folder files
                //first all files inside have to be deleted

                for (int i = 0; i < numberOfFilesInRoot; i++) {

                    rootFiles[i].delete();
                    if (rootFiles[i].exists()) {
                        System.out.println(rootFiles[i] + " wurde nicht gelöscht");
                    } else {
                        //System.out.println(rootFiles[i]+ " wurde gelöscht");
                    }
                }
                //now if the folder is empty delete it
                if (folderToDelete.listFiles().length == 0)
                    folderToDelete.delete();
            } else {
                System.out.println("Folder to delete doesn't exist");
            }
        } catch (Exception e) {

        }
        int id = mCursor.getInt(0);
        db.removeData(id);
        db.exportDatabase();

        getIntent().putExtra("wasDeleted", true);

        srs.updateEntriesIndexesAfterRemovingOne(id);

        srs.mAdapter = srs.loadCostumerEntries();

        onBackPressed();
    }


    private String createFolderNameFromCursor(Cursor c) {
        final String underline = "_";
        String firstname = c.getString(c.getColumnIndex(Database.COL_FIRSTNAME));
        String lastname = c.getString(c.getColumnIndex(Database.COL_LASTNAME));
        String date = c.getString(c.getColumnIndex(Database.COL_BIRTHDATE));

        String[] dates = date.split("\\.");
        date = dates[0] + dates[1] + dates[2];

        String folderName = firstname.toUpperCase(Locale.GERMANY) + underline + lastname.toUpperCase(Locale.GERMANY) + underline + date + "/";

        return folderName;


    }

    protected void updatePersonsDetails(String details) {

        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_DETAILS, details);

            db.getWritableDatabase().update(DATABASE_TABLE_PERSONS, cv, COL_ID + "=" + mCursor.getInt(mCursor.getColumnIndex(COL_ID)), null);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void addDataToViews(Cursor c) {
        BitmapHandler bh = new BitmapHandler(getApplicationContext());
        c.moveToFirst();
        //the dimensions for a bigger (profile picture) and a smaller thumbnails (id cards)
        //bigger
        int heightBig = 512;
        double scaledWidthBig = heightBig * 0.5625;
        int widthBig = (int) scaledWidthBig;

        //smaller
        int heightSmall = 256;
        double scaledWidthSmall = heightSmall * 0.5625;
        int widthSmall = (int) scaledWidthSmall;

        String details, path;


        Bitmap[] bitmaps = new Bitmap[AMOUNT_OF_IMAGES];
        //gets the number where to start looping through the paths in the cursor
        int cursorPathStartIndex = c.getColumnIndex(Database.COL_PROFILEPICTURE);

        //create the bitmaps
        for (int i = 0; i < bitmaps.length; i++) {

            /*
            * To get the thumbnail, cut the path part /img/
            * out and replace it with /tmb/.
            * The rest of the path keeps the same
            * */
            path = c.getString(cursorPathStartIndex);
            if (path != null && !path.equals(NO_ENTRY)) {
                bitmapPaths[i] = path;
                System.out.println("Path: " + path);

                String[] pathCut = path.split(EntryActivity.IMAGES_LARGE);

                for (int u = 0; u < pathCut.length; u++) {
                    System.out.println("Pathcut[" + u + "]: " + pathCut[u]);
                }

                //now glue the path together again with the tmb value
                path = pathCut[0] + EntryActivity.THUMBNAILS + pathCut[1];

                bitmaps[i] = bh.getBitmap(path);
            }
            cursorPathStartIndex++;

        }


        //setup the imageviews

        //person.printData();
        for (int i = 0; i < bitmaps.length; i++) {
            Bitmap bm = bitmaps[i];
            if (bitmaps[i] != null) {
                if (idCardImages[i] != null) {
                    {
                        if (i == 0) {
                            bm = bh.scaleBitmap(bm, heightBig, widthBig);
                            bm = bh.rotateBitmap(bm, 90);
                            //set profile picture

                            idCardImages[i].setImageBitmap(bm);
                            //System.out.println("Profile Picture:"+ personImage);
                        } else {
                            bm = bh.scaleBitmap(bm, heightSmall, widthSmall);
                            bm = bh.rotateBitmap(bm, 0);
                            idCardImages[i].setImageBitmap(bm);
                            //System.out.println("Picture[Image]: "+ idCardImages[i-1]);
                        }
                    }
                } else {
                    Log.e("Image Null", "Image: " + idCardImages[i]);
                }

            } else {
                Log.e("Bitmap Null", "Bitmap: " + bitmaps[i]);

            }
        }


        fh = new FilesHandler();
        try {
            details = c.getString(c.getColumnIndex(Database.COL_DETAILS));
        } catch (Exception fnf) {
            //System.out.println("Detailsfile of " + person.firstname + " " + person.lastname + " not found!");
            details = "";
        }

        String firstname = c.getString(c.getColumnIndex(Database.COL_FIRSTNAME));
        String lastname = c.getString(c.getColumnIndex(Database.COL_LASTNAME));
        String age = Integer.toString(c.getInt(c.getColumnIndex(Database.COL_AGE)));
        String date = c.getString(c.getColumnIndex(Database.COL_BIRTHDATE));

        detailsEditText.setText(details);
        personNameTextView.setText(firstname + " " + lastname);
        personAgeTextView.setText(age);
        personBirthdateTextView.setText(date);

        if (c.getInt(c.getColumnIndex(Database.COL_BANNED)) == 1) {
            /*Toast.makeText(PersonInfoActivity.this,
                    "HAUSVERBOT!",
                    Toast.LENGTH_SHORT).show();*/
            personBannedStateTextView.setText("Hausverbot");
            bannedSwitch.setChecked(true);
        } else {
            /*Toast.makeText(PersonInfoActivity.this,
                    "KEIN HAUSVERBOT!",
                    Toast.LENGTH_SHORT).show();*/
            personBannedStateTextView.setText("");
            bannedSwitch.setChecked(false);
        }
    }


    //MENUBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_person_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    private Cursor getSearchCursor(String command) {
        //System.out.println("getSearchCursor startet mit: " + command);
        //the coumns to select
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
        Cursor c;
        try {
            // show result of input text search (ordinary date  search)
            String[] query_parts = command.split(" ");
            c = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                    null, selection[7], query_parts, null, null,
                    Database.COL_LASTNAME);

            return c;

        } catch (Exception ex) {
            Log.v("GetSearchCursor EX", "In getSearchCursor(" + command + " Exception thrown: " + ex);
            return null;
        }
    }


}
