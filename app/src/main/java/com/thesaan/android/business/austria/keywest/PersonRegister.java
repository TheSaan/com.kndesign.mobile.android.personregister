package com.thesaan.android.business.austria.keywest;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import com.thesaan.android.business.austria.keywest.Handler.*;

import java.io.File;
import java.util.Locale;

/**
 * Written to get common functions like Getters for entry folders,...
 */
public class PersonRegister implements KeyWestInterface {

    Database db;

    public PersonRegister(Context c) {
        db = new Database(c);
    }

    public File getPersonFolder(int id) {

        Cursor c = db.getCursor(id);
        c.moveToFirst();

        String first, last, date;

        //building the folder name
        first = c.getString(c.getColumnIndex(COL_FIRSTNAME));
        last = c.getString(c.getColumnIndex(COL_LASTNAME));
        date = c.getString(c.getColumnIndex(COL_BIRTHDATE));

        String[] dates = date.split("\\.");

        date = dates[0] + dates[1] + dates[2];

        String folderName = first.toUpperCase() + "_" + last.toUpperCase() + "_" + date;

        File folder = new File(KeyWestInterface.APP_EXT_STORAGE_FOLDER + "/" + folderName + "/");

        System.out.println("Folder: " + folder.getAbsolutePath());
        return folder;
    }

    public File[] getEntryFolders() {
        File f = new File(APP_ENTRIES_FOLDER);
        return f.listFiles();
    }

    /**
     * Creates a String array with the data needed for restore the Database
     *
     * @return
     */
    public String[] getFolderData(File folder) {
        String name = folder.getName();
        String firstname = name.split("_")[0];
        String lastname = name.split("_")[1];
        String date = name.split("_")[2];

        //image file paths
        String pp = "";
        String passport = "";

        String day = date.substring(0, 2);
        String month = date.substring(2, 4);
        String year = date.substring(4, 8);

        date = day + "." + month + "." + year;

        File imgFolder = new File(folder.getPath(), "img");
        if(imgFolder.list().length > 0) {
            File image = imgFolder.listFiles()[0];


            if (isProfilePicture(image)) {
                pp = image.getAbsolutePath().toString();
            } else {
                passport = image.getAbsolutePath().toString();
            }
        }

    String isBanned = "0";

    DateHandler dh = new DateHandler();
    int age = dh.createAgeFromFromFormattedDate(date);

    File lvBackup = new File("LV.txt");
    if(lvBackup.exists()){
        isBanned = "1";
    }

    String[] data = new String[]{
            Integer.toString(age),
            firstname,
            lastname,
            date,
            pp,
            passport,
            "",
            isBanned
    };

    return data;
}

    public boolean isProfilePicture(File image) {
        String name = image.getName();
        String[] parts = name.split("_");

        if (parts[parts.length - 1].equals("PROFILBILD.jpg")) {
            return true;
        } else
            return false;
    }

    /**
     * @param id
     * @param changedColumnValue set the column as value which was changed. The method sets all required changes
     *                           automatically
     *                           <p/>
     *                           But only use COL_FIRSTNAME, COL_LASTNAME, COL_BIRTHDATE. Here is no check for
     *                           the column implemented if this method is used for another purpose.
     */
    public void changePersonFolderData(int id, String changedColumnValue, String changeTo) {


        changeTo = changeTo.toUpperCase(Locale.GERMAN);

        int identifier = -1;

        Cursor c = db.getCursor(id);

        c.moveToFirst();

        File folder = getPersonFolder(id);
        System.out.println("PersonFolder: " + folder.getName());

        FilesHandler fh = new FilesHandler();

        File[] imageFolders = fh.listFiles(folder.getPath(), true);

        File img, tmb, details = null;
        //check if no more folders are added
        if (imageFolders.length < 4) {
            //always check that the details file is not
            //set as one of the directories
            if (imageFolders[0].isDirectory())
                img = imageFolders[0];
            else {
                details = imageFolders[0];
                img = imageFolders[2];
            }
            if (imageFolders[1].isDirectory())
                tmb = imageFolders[1];
            else {
                details = imageFolders[1];
                tmb = imageFolders[0];
            }
            if (!imageFolders[2].isDirectory())
                details = imageFolders[2];

            if (tmb != null && img != null) {
                File tmbImage = tmb.listFiles()[0];
                File imgImage = img.listFiles()[0];

                //the file for renaming the old file
                File newFile;
                //
                String folderName = folder.getName();

                String[] folderData = folderName.split("_");

                ArrayHandler.printAllValues(folderData);

                final int FIRSTNAME = 0;
                final int LASTNAME = 1;
                final int BIRTHDATE = 2;

                //set the identifier
                for (int i = 0; i < COLUMNS.length; i++) {
                    if (COLUMNS[i].equals(changedColumnValue)) {
                        identifier = i;
                        System.out.println("Identifier: " + i);
                        System.out.println("Column: " + COLUMNS[i]);
                        break;
                    } else {
                        identifier = -1;
                    }
                }

                //
                switch (identifier) {
                    //first name
                    case 2: {
                        //Split the file name at the first name position
                        String[] parts = tmbImage.getAbsolutePath().split(folderData[FIRSTNAME]);

                        ArrayHandler.printAllValues(parts);

                        newFile = new File(parts[0] + changeTo + parts[1]);

                        //rename all files inside the folders
                        tmbImage.renameTo(newFile);
                        imgImage.renameTo(newFile);


                        //rename the actual person folder
                        parts = folder.getAbsolutePath().split(folderData[FIRSTNAME]);

                        newFile = new File(parts[0] + changeTo + parts[1]);

                        folder.renameTo(newFile);
                        break;

                    }
                    //last name
                    case 3: {
                        //Split the file name at the first name position
                        String[] parts = tmbImage.getName().split(folderData[LASTNAME]);

                        newFile = new File(parts[0] + changeTo + parts[1]);

                        //rename all files inside the folders
                        tmbImage.renameTo(newFile);
                        imgImage.renameTo(newFile);

                        //rename the actual person folder
                        parts = folder.getName().split(folderData[LASTNAME]);

                        newFile = new File(parts[0] + changeTo + parts[1]);

                        folder.renameTo(newFile);
                        break;
                    }
                    //date of birth
                    case 4: {
                        //Split the file name at the first name position
                        String[] parts = tmbImage.getName().split(folderData[BIRTHDATE]);

                        newFile = new File(parts[0] + changeTo + parts[1]);

                        //rename all files inside the folders
                        tmbImage.renameTo(newFile);
                        imgImage.renameTo(newFile);

                        //rename the actual person folder
                        parts = folder.getName().split(folderData[BIRTHDATE]);

                        newFile = new File(parts[0] + changeTo + parts[1]);

                        folder.renameTo(newFile);
                        break;
                    }
                    default: {
                        System.err.println("Nichts wurde geändert");
                        break;
                    }
                }
            }
        }
    }

    public static int getMinimumAccountRank(Activity activity) {
        if(activity instanceof PersonInfoActivity){
            return RANK_MASTER_ID;
        }else if(activity instanceof StartActivity){
            return RANK_STAFF_ID;
        }else{
            return -1;
        }
    }
}
