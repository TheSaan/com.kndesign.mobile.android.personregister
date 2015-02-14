package com.knoeflerdesign.keywest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michael on 12.02.2015.
 */
public class FunctionalityFactory {


    Context context;
    SQLiteDatabase db;
    SQLiteDatabase.CursorFactory cf;



    final String BIG_MARK_LINE =
            "---------------------------------------------------------\n" +
                    "---------------------------------------------------------";

    public Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void logAllDataFromCursor(Cursor c) {
        //show all entries for check
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {

            System.out.println("Eintrag(" + i + ")\n");
            System.out.println("---------------------------------------------------------------");
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
            System.out.println("---------------------------------------------------------------");
            c.moveToNext();
        }
    }

    public File[] listFiles(String path, boolean printIt) {
        File user_dir = new File(path);
        File[] files = user_dir.listFiles();

        if (printIt) {
            for (int i = 0; i < files.length; i++) {
                Log.v("File:", files[i].toString());
            }
        }

        return files;
    }

    public String[] splitIntoFILEPATHAndFILENAME(String path, String splitpoint, boolean printIt) {
        /**
         * Splits the path @see java.io.String into the directory path and its filename
         *
         *@param path
         *  The file path
         *@param splitpoint
         *  The String expression where the path will be splittet
         *@param printIt
         *  The filepath and filename get printet in the console if this parameter is true.
         *
         *  */
        String[] cuts = path.split(splitpoint);

        //add the JPEG_ prefix to the filename again
        cuts[1] = splitpoint + cuts[1];
        if (printIt) {
            System.out.println(BIG_MARK_LINE + "\n\t\t\tFilepath & Filename");
            for (int i = 0; i < cuts.length; i++) {
                System.out.println("cuts[" + i + "]:" + cuts[i]);
            }
        }

        return cuts;
    }

    public Bitmap scaleBitmap(Bitmap src,int width, int height){

        Bitmap scaled = Bitmap.createScaledBitmap(src,width,height,true);
        return scaled;
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

        Cursor c = db.getReadableDatabase().query(Database.DATABASE_TABEL_PERSONS,
                columns, null, null, null, null,
                Database.COL_LASTNAME);
        return c;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public Bitmap getBitmap(String path) {
        //System.out.println("Testing the Path:\t" + path);
        //System.out.println(Environment.getExternalStorageDirectory());
        //System.out.println(Environment.getRootDirectory());

        try {
            String filepath = splitIntoFILEPATHAndFILENAME(path, "IMG_", false)[0];
            String filename = splitIntoFILEPATHAndFILENAME(path, "IMG_", false)[1];

            File file = new File(filepath, filename);

            if (file.exists()) {

                file.setReadable(true);
                file.setExecutable(true);
                System.out.println("File exists");

                FileInputStream fis = new FileInputStream(file);

                Bitmap bitmap = BitmapFactory.decodeStream(fis);

                if (bitmap != null) {
                    System.out.println("Bitmap returned.");
                    fis.close();
                    return bitmap;

                } else {
                    fis.close();
                    System.out.println("Bitmap not created.");
                    return null;
                }
            } else {
                System.out.println("File don't exist");
                return null;
            }
        } catch (Exception ex) {
            System.out.println("IOE " + ex);
            return null;
        }
    }

    public ImageView getImage(Bitmap bmp) {
        ImageView image = new ImageView(context);
        image.setImageBitmap(bmp);
        return image;
    }

    public List<ImageView> getImageList() {
        List<ImageView> list = new ArrayList<ImageView>();

        //add images to list
        //count amount of entries in cursor
        Cursor c = getCursor(new Database(context,Database.DATABASE_TABEL_PERSONS,cf,1));
        c.moveToFirst();

        for (int i = 0; i < getImageCount(c); i++) {
            Log.i("Image " + i + " " + c.getColumnName(0), c.getString(0));
            list.add(getImage(getBitmap(c.getString(0))));
            c.moveToNext();
        }
        return list;

    }

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


}
