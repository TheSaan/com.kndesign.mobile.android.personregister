package com.thesaan.android.business.austria.keywest.Handler;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Michael Knöfler on 02.03.2015.
 */
public class BitmapHandler {
    Context context;
    ContentResolver mContentResolver;

    public BitmapHandler(Context context){

        this.context = context;
    }

    public Bitmap resizeBitmap(Bitmap bm, double scaleFactor){
        if(bm.getHeight() > 0 && bm.getWidth() > 0) {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() * scaleFactor), (int) (bm.getHeight() * scaleFactor), true);
            return resizedBitmap;
        }else{
            return null;
        }
    }
    public Bitmap makeBitmapMutable(Bitmap bitmap){
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap.Config type = bitmap.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, bitmap.getRowBytes()*height);
            bitmap.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            bitmap.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            bitmap = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            bitmap.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
    public Bitmap getBitmap(String path) {
        AndroidHandler ah = new AndroidHandler(context);


        try {
            String filepath = ah.splitIntoFILEPATHAndFILENAME(path, "IMG_", false)[0];
            String filename = ah.splitIntoFILEPATHAndFILENAME(path, "IMG_", false)[1];

            File file = new File(filepath, filename);

            if (file.exists()) {

                file.setReadable(true);
                file.setExecutable(true);
                //System.out.println("File exists");

                FileInputStream fis = new FileInputStream(file);


                Bitmap bitmap = BitmapFactory.decodeStream(fis);

                if (bitmap != null) {
                    //System.out.println("Bitmap returned.");
                    fis.close();
                    return bitmap;

                } else {
                    fis.close();
                    //System.out.println("Bitmap not created.");
                    return null;
                }
            } else {
                //System.out.println("File don't exist");
                return null;
            }
        } catch (Exception ex) {
            //System.out.println("IOE " + ex);
            return null;
        }
    }
    public Bitmap getBitmapWithOptions(String path) {

        Bitmap b;
        BitmapFactory.Options options = new BitmapFactory.Options();

        //set configuration
        //Each pixel is stored on 4 bytes.
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        options.inMutable = true;

        b = BitmapFactory.decodeFile(path,options);
        return b;

    }
    public ImageView getImage(Bitmap bmp) {

        try {
            ImageView image = new ImageView(context);
            image.setImageBitmap(bmp);
            return image;
        }catch (NullPointerException npe){
            System.err.println("NPE in getImage("+bmp+","+context+")\n\n"+npe);
            return null;
        }
    }
    public Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    public Bitmap scaleBitmap(Bitmap src,int width, int height){

        Bitmap scaled = Bitmap.createScaledBitmap(src,width,height,true);
        return scaled;
    }
    public void saveBitmapToFile(File file, Bitmap bm){
        System.out.println("Width: "+ bm.getWidth()+" Height: "+bm.getHeight());
        try
        {
            FileOutputStream out = new FileOutputStream(file.getPath());
            bm.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            Log.e("File Compress Error", "ERROR:" + e.toString());
        }
    }


}
