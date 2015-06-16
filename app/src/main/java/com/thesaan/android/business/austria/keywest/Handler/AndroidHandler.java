package com.thesaan.android.business.austria.keywest.Handler;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.thesaan.android.business.austria.keywest.Database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michael on 12.02.2015.
 */
public class AndroidHandler {


    Context context;
    SQLiteDatabase db;
    SQLiteDatabase.CursorFactory cf;

    //Bitmap size default value
    final static int DEFAULT_SIZE = 128;

    //with percentage multiplier from height
    final static double DEFAULT_WIDTH_PERCENTAGE = 0.5625;
    //Bitmap sizes
    final static int THUMBNAIL_SIZE = 1;
    final static int PROFILE_PICTURE_SIZE = 2;
    final static int SMALL_SIZE = 3;
    final static int BIG_SIZE = 4;

    final String BIG_MARK_LINE =
            "---------------------------------------------------------\n" +
                    "---------------------------------------------------------";

    public AndroidHandler(Context context){
        this.context = context;
    }


    public void logAllDataFromCursor(Cursor c) {
        //show all entries for check
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {

            //System.out.println("Eintrag(" + i + ")\n");
            //System.out.println("---------------------------------------------------------------");
            for (int j = 0; j < c.getColumnCount(); j++) {

                //check the data type of the current column
                switch (c.getType(j)) {
                    case Cursor.FIELD_TYPE_NULL: {
                        Log.i("(" + j + ")" + c.getColumnName(j), null);
                        break;
                    }
                    case Cursor.FIELD_TYPE_INTEGER: {
                        Log.i("(" + j + ")" + c.getColumnName(j), c.getInt(j) + "");
                        break;
                    }
                    case Cursor.FIELD_TYPE_FLOAT: {
                        Log.i("(" + j + ")" + c.getColumnName(j), c.getFloat(j) + "");
                        break;
                    }
                    case Cursor.FIELD_TYPE_STRING: {
                        Log.i("(" + j + ")" + c.getColumnName(j), c.getString(j) + "");
                        break;
                    }
                    default: {
                        Log.i("(" + j + ")" + c.getColumnName(j), "Other");
                        break;
                    }
                }
            }
            //System.out.println("---------------------------------------------------------------");
            c.moveToNext();
        }
    }


    public boolean isNumeric(String str){
        try {
            double d = Double.parseDouble(str);
        }catch (NumberFormatException nfe){
            return false;
        }
        return true;
    }
    public static String[] splitIntoFILEPATHAndFILENAME(String path, String splitpoint, boolean printIt) {
        /**
         * Splits the path @see java.io.String into the directory path and its filename. But
         * the split point will be added to the second part, the file name.
         *
         *
         *@param path
         *  The file path
         *@param splitpoint
         *  The String expression where the path will be split
         *@param printIt
         *  The filepath and filename get printed in the console if this parameter is true.
         *
         *  */
        String[] cuts = path.split(splitpoint);

        //add the  prefix to the filename again
        cuts[1] = splitpoint + cuts[1];

        if (printIt) {
            //System.out.println(BIG_MARK_LINE + "\n\t\t\tFilepath & Filename");
            for (int i = 0; i < cuts.length; i++) {
                //System.out.println("cuts[" + i + "]:" + cuts[i]);
            }
        }

        return cuts;
    }


 /*   public Bitmap cutBitmap(Bitmap src){


        Matrix m = new Matrix();

        *//*First I have to Mirror the Image so it can cut the bottom
        m.preScale(-1, 1);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);

    }*/


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    /*
    public void loadImages() {
        List<ImageView> list = getImageList();
        for (int i = 0; i < getImageCount(getCursor()); i++) {
            addToLayout(list.get(i));
        }
    }

    public void addToLayout(ImageView iv) {
        TableLayout table = (TableLayout) findViewById(R.id.imageTable);
        table.addView(iv);
    }*/

    public int getImageCount(Cursor c) {
        int size = c.getCount();
        return size;
    }

    public Cursor getCursor(Database db) {
        String[] columns = {Database.COL_PROFILEPICTURE, Database.COL_FIRSTNAME, Database.COL_LASTNAME};

        Cursor c = db.getReadableDatabase().query(Database.DATABASE_TABLE_PERSONS,
                columns, null, null, null, null,
                Database.COL_LASTNAME);
        return c;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)





    /**
     * E.g. you want to create a ListView of names
     * and every list element can have only a
     * max length of 20 letters.
     *
     * If a name has more than 20 letters, take the
     * whole length of the full name and cut it off
     * at letter 17 and add three dots '...' to it.
     *
     * Than you have always maximal 20 letters/digits.
     *
     * @param src
     * @param wanted_length
     * @return
     */
    public String cutStringIfTooLongAndAddDots(String src, int wanted_length){
        String cut = src.subSequence(0, wanted_length-3)+"...";
        return cut;
    }

    public boolean checkMultiplePatterns(String[] patterns,String stringToCheck) {

        boolean isMatch = false;
        Pattern p;
        Matcher m;
        for (int i = 0; i < patterns.length; i++) {
            p = Pattern.compile(patterns[i]);
            m = p.matcher(stringToCheck);

            if (m.matches()) {
                return true;
            }else{
                continue;
            }
        }
        //TODO hier kann es passieren dass die methode immer false ausgibt AUFPASSEN!
        return false;
    }

    private String readTextFile(String path) {
        String details, line;
        File f = new File(path);

        try {

            BufferedReader file = new BufferedReader(new FileReader(f));

            try {
                details = "";
                while ((line = file.readLine()) != null) {
                    details += line + "\n";
                }
                return details;
            } catch (IOException e) {
                //System.out.println("could not read line...");
                return null;
            }

        } catch (FileNotFoundException ex) {
            //System.out.println("details file not found!");
            return null;
        }

    }

    public List<ImageView> getImageList() {
        List<ImageView> list = new ArrayList<ImageView>();
        BitmapHandler bh = new BitmapHandler(context);

        //add images to list
        //count amount of entries in cursor
        Cursor c = getCursor(new Database(context));
        c.moveToFirst();

        for (int i = 0; i < getImageCount(c); i++) {
            Log.i("Image " + i + " " + c.getColumnName(0), c.getString(0));
            list.add(bh.getImage(bh.getBitmap(c.getString(0))));
            c.moveToNext();
        }
        bh = null;
        c = null;
        return list;

    }
}
