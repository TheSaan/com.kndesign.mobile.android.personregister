package com.thesaan.android.business.austria.keywest.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import com.thesaan.android.business.austria.keywest.Database;
import com.thesaan.android.business.austria.keywest.EntryActivity;
import com.thesaan.android.business.austria.keywest.Handler.DateHandler;
import com.thesaan.android.business.austria.keywest.Handler.FilesHandler;
import com.thesaan.android.business.austria.keywest.Handler.RandomHandler;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class TestEntryCreationService extends Service {

    protected Database db;
    FilesHandler fh;
    DateHandler dateHandler;

    File KWIMG = new File(Environment.getExternalStorageDirectory(), "KWIMG");
    File BACKUP = new File(KWIMG.getPath(), "BACKUP");
    File TEST = new File(KWIMG.getPath(), "TEST/TESTPERSON");
    File TEST_ROOT = new File(KWIMG.getPath(), "TEST");

    File TEST_ENTRY_FOLDER = new File(KWIMG.getPath(),"TEST_TEST_01011900");

    File TEST_TMB = new File(TEST.getAbsolutePath()+"/"+EntryActivity.THUMBNAILS);
    File TEST_IMG = new File(TEST.getAbsolutePath()+"/"+EntryActivity.IMAGES_LARGE);

    private final String TMB_FOLDER = "/"+EntryActivity.THUMBNAILS;
    private final String IMG_FOLDER = "/"+EntryActivity.IMAGES_LARGE;

    
    private final static String PROFILEPICTURE_FILE_NAME = "PROFILBILD" + FilesHandler.JPEG;
    private final static String DRIVERSLICENCE_FILE_NAME = "FUEHRERSCHEIN" + FilesHandler.JPEG;
    private final static String CHECKITCARD_FILE_NAME = "CHECKITCARD" + FilesHandler.JPEG;
    private final static String OEBBCARD_FILE_NAME = "OEBB" + FilesHandler.JPEG;
    private final static String PASSPORT_FILE_NAME = "REISEPASS" + FilesHandler.JPEG;
    private final static String PERONSALFRONT_FILE_NAME = "PERSONALAUSWEIS-VS" + FilesHandler.JPEG;
    private final static String PERSONALBACK_FILE_NAME = "PERSONALAUSWEIS-RS" + FilesHandler.JPEG;
    public final static String TEXTFILE = ".txt";
    private final static String DETAILS_FILE_NAME = "DETAILS" + FilesHandler.TXT;

    //declares the maximum of an entries' amount of files
    private final static int PERSON_MAX_FILES = 7;

    private final static int IDENTIFIER_PROFILEPICTURE = 0;
    private final static int IDENTIFIER_DRIVERSLICENCE = 1;
    private final static int IDENTIFIER_CHECKITCARD = 2;
    private final static int IDENTIFIER_PASSPORT = 3;
    private final static int IDENTIFIER_OEBBCARD = 4;
    private final static int IDENTIFIER_PERONSALFRONT = 5;
    private final static int IDENTIFIER_PERSONALBACK = 6;
    private final static int IDENTIFIER_DETAILS = 7;
    private final static int[] PATH_IDENTIFIERS = {
            IDENTIFIER_PROFILEPICTURE,
            IDENTIFIER_DRIVERSLICENCE,
            IDENTIFIER_CHECKITCARD,
            IDENTIFIER_OEBBCARD,
            IDENTIFIER_PASSPORT,
            IDENTIFIER_PERONSALFRONT,
            IDENTIFIER_PERSONALBACK,
            IDENTIFIER_DETAILS
    };
    private final IBinder mBinder = new MyBinder();


    private final String BACKUPPATH = BACKUP.getAbsolutePath();
    private final String ROOTPATH = KWIMG.getAbsolutePath();
    private final String TESTPATH = TEST.getAbsolutePath();
    private final String TMBPATH = TEST_TMB.getAbsolutePath();
    private final String IMGPATH = TEST_IMG.getAbsolutePath();

    private final String[] FIRSTNAMES = {
            "Michael",
            "Johannes",
            "Tom",
            "Steve",
            "Mark",
            "Sebastian",
            "Erich",
            "Manuel",
            "Stefan",
            "Matthias",
            "Robert",
            "Tino",
            "Max",
            "Peter",
            "Fabian",
            "Daniel",
            "Klaus",
            "Dieter",
            "Alois",
            "Dominik",
            "Paul",
            "Wolfgang",

            "Peter-Hans",
            "Fabian-Johann",
            "Daniel-Erich",
            "Klaus-Moritz",


            "Lisa",
            "Marianne",
            "Laura",
            "Sabine",
            "Stefanie",
            "Katharina",
            "Claudia",
            "Carina",
            "Luise",
            "Vanessa",
            "Anna",
            "Maria",
            "Gabi",
            "Petra",
            "Anna-Maria",
            "Maria-Magdalena",
            "Gabi-Stefanie",
            "Petra-Claudia",
            "Antje",
            "Brigitte",
            "Alina",
            "Beate",
            "Chiara",
            "Corinna",
            "Elisabeth",
            "Viktoria"
    };
    private final String[] LASTNAMES = {
            "Knöfler",
            "Quester",
            "Haas",
            "Baumgartner",
            "Steiner-Feldwurz",
            "Hammer-Kohler",
            "Wartner-Perl",
            "Stifter-Pertersen",
            "Lorenz",
            "Lekar",
            "Maier",
            "Mustermann",
            "Tester",
            "Steiner",
            "Baumer",
            "Thaler",
            "Pock",
            "Maitz",
            "Schiller",
            "Lenz",
            "Gärtner",
            "Fuchs",
            "Bauer"
    };

    private String personpath;


    public TestEntryCreationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        dateHandler = new DateHandler();
        fh = new FilesHandler();
        //access the database
        db = new Database(this);

        BACKUP.setReadOnly();
        TEST_ROOT.setReadOnly();
        TEST_ENTRY_FOLDER.setReadOnly();


        try {
            createEntries(50);
        } catch (NullPointerException npe) {
            System.out.println("Test Entry Exception & " + npe);
        }


        //Does not depend on the intent
        //because this service runs anyway
        return Service.START_STICKY;
    }

    private void createEntries(final int amount) {

        Thread creator = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= amount - 1; i++) {
                    System.out.println("Create entry "+i);
                    newEntry();
                }
            }
        });

        creator.start();

    }

    private void newEntry() {
        //first name random
        Random rand = new Random();

        //get the formatted date for saving
        String formatedDate = dateHandler.createRandomBirthdays(1965, 1999, 1)[0];

        //get only one birhtdate, remove dots and set to upper case to get the right form
        String date = dateHandler.unformatDate(formatedDate);


        //temporary declared to get fully Uppercase
        //and normal first letter uppercase format
        String tmpFirstname = FIRSTNAMES[rand.nextInt(29)];
        String tmpLastname = LASTNAMES[rand.nextInt(22)];

        //for folder and file name
        //form: MAX_MUSTERMANN_DDMMYYYY
        String personFilePräfix = tmpFirstname.toUpperCase(Locale.GERMANY) + "_" + tmpLastname.toUpperCase(Locale.GERMANY) + "_" + date;

        //for database entry
        String firstname = tmpFirstname;
        String lastname = tmpLastname;

        //copy the test data into the new person folder
        File[] rootFiles = TEST.listFiles();

        //create person folder
        File personFolder = new File(ROOTPATH, personFilePräfix);
        personFolder.mkdir();

        //create subfolders
        File personTmbFolder = new File(personFolder+"/"+EntryActivity.THUMBNAILS);
        personTmbFolder.mkdir();

        File personImgFolder = new File(personFolder+"/"+EntryActivity.IMAGES_LARGE);
        personImgFolder.mkdir();

        //paths of the new entry, with non-correct order
        String[] paths = new String[PERSON_MAX_FILES];

        int fileLooper = 0;

        String fileEnd;
        String[] fileParts;

        for (File file : rootFiles) {
            //System.out.println(">>>>>>>>> Aktuelle Datei: " + file.getName());

            if (!file.isDirectory()) {

                //System.out.println("<<<<<<<<< TEXT DATEI: " + file.getName());
                //get the filename end of the current loop file
                fileParts = file.getAbsolutePath().toString().split("_");

                //System.out.println("TEXT DATEI Aufteilung: " + file.getName());
                //detailstext = 4 parts, images have 5 parts
                if (fileParts.length == 4) {
                    fileEnd = fileParts[3];

                    String fileName = personFilePräfix + "_" + fileEnd;

                    File newFile = new File(personFolder, fileName);

                    //set path
                    paths[fileLooper] = newFile.getAbsolutePath();
                    try {
                        fh.copy(file, newFile);
                        fileLooper++;
                    } catch (IOException ex) {
                        System.err.println("Copying files failed. " + ex);
                    }
                } else {
                    System.out.println("Cannot copy file: Wrong name format!\nFile: " + file.getName());
                }
            } else {
                //System.err.println(file.getName()+" als Verzeichnis erkannt.");
                //these loop only runs if there are images as files
                //because of the parts length questioning
                File[] subFiles = file.listFiles();


                String subFolderPath;
                boolean isOriginalSize = false;

                //System.err.println("File Path " +file.getAbsolutePath().toString());
                //System.err.println("TMB Path " + TMBPATH);
                //System.err.println("IMG Path " + IMGPATH);

                if (TMBPATH.equals(file.getAbsolutePath().toString())) {
                    subFolderPath = TMB_FOLDER;
                }else
                if (IMGPATH.equals(file.getAbsolutePath().toString())) {
                    subFolderPath = IMG_FOLDER;
                    isOriginalSize = true;
                } else {
                    subFolderPath = null;
                }
                for (File subFile : subFiles) {

                    //get the filename end of the current loop file
                    fileParts = subFile.getAbsolutePath().toString().split("/");

                    //split the file name
                    fileParts = fileParts[fileParts.length - 1].split("_");

                /*In this folder are only images with a special name format
                * contained. If a file with a different format is inside,
                * print it with an error message
                * */
                    if (fileParts.length == 5) {
                        //get the file end
                        fileEnd = fileParts[fileParts.length - 1];

                        String fileName = "IMG_" + personFilePräfix + "_" + fileEnd;


                        //System.err.println("Creating the new File " + personFolder+subFolderPath+fileName);
                        File newFile = new File(personFolder+subFolderPath, fileName);

                        //set path, but only if its the original size image
                        if (isOriginalSize) {
                            paths[fileLooper] = newFile.getAbsolutePath();
                            fileLooper++;
                        }
                        try {
                            fh.copy(subFile, newFile);

                        } catch (IOException ex) {
                            System.err.println("Copying files failed. " + ex);
                        }
                    } else {
                        System.out.println("Cannot copy file in directory "+file.getName()+". Wrong name format!\nFile: " + subFile.getName());
                    }
                }

            }
        }

            //paths in correct order
            String[] finalPaths = sortPathsForDatabase(paths);

            //create banned status
            int banned = RandomHandler.createIntegerFromRange(0, 1, rand);

            //calculate age
            int age = createAge(formatedDate);

            //System.out.println("Create person:\n" + firstname + " " + lastname + " " + age + " Hausverbot " + banned + " ");
//          TODO change this method for new addPerson method
//            db.addPerson(age, firstname, lastname, formatedDate,
//                    finalPaths[0], finalPaths[1], finalPaths[2], finalPaths[3],
//                    finalPaths[4], finalPaths[5], finalPaths[6],
//                    finalPaths[7], banned);
    }


    private String[] sortPathsForDatabase(String[] paths){
        String[] sorted = new String[PATH_IDENTIFIERS.length];
        String[] parts;
        String identifier;

        //fill sorted array first with "KEIN EINTRAG" entry
        //to avoid null
        for (int j = 0; j < paths.length; j++) {
            sorted[j] = "KEIN EINTRAG";
        }

        //now fill the sorted array with the available Paths on
        //the correct position
        for (int i = 0; i < paths.length; i++) {
            parts = paths[i].split("/");
            parts = parts[parts.length - 1].split("_");
            /*for(int k = 0;k<parts.length;k++){
                System.out.println("Parts "+parts[k]);
            }*/

            identifier = parts[parts.length-1];

            int pathPosition = checkIdentifierPosition(identifier);

            //put the current path on the correct position for the database entry
            sorted[pathPosition] = paths[i];
        }

        return sorted;
    }

    private int checkIdentifierPosition(String identifier) {
        if (identifier.equals(PROFILEPICTURE_FILE_NAME)) {
            return IDENTIFIER_PROFILEPICTURE;
        }
        if (identifier.equals(DRIVERSLICENCE_FILE_NAME)) {
            return IDENTIFIER_DRIVERSLICENCE;
        }
        if (identifier.equals(CHECKITCARD_FILE_NAME)) {
            return IDENTIFIER_CHECKITCARD;
        }
        if (identifier.equals(OEBBCARD_FILE_NAME)) {
            return IDENTIFIER_OEBBCARD;
        }
        if (identifier.equals(PASSPORT_FILE_NAME)) {
            return IDENTIFIER_PASSPORT;
        }
        if (identifier.equals(PERONSALFRONT_FILE_NAME)) {
            return IDENTIFIER_PERONSALFRONT;
        }
        if (identifier.equals(PERSONALBACK_FILE_NAME)) {
            return IDENTIFIER_PERSONALBACK;
        }
        if (identifier.equals(DETAILS_FILE_NAME)) {
            return IDENTIFIER_DETAILS;
        } else {
            System.err.println(" else wurde aufgerufen >>EntryCreator->checkIdentifierPosition(" + identifier + ")");
            return -1;
        }

    }

    private String setIdentifierName(int identifier) {
        if (identifier == IDENTIFIER_PROFILEPICTURE) {
            return "PROFILBILD.jpg";
        }
        if (identifier == IDENTIFIER_DRIVERSLICENCE) {
            return "FUEHRERSCHEIN.jpg";
        }
        if (identifier == IDENTIFIER_CHECKITCARD) {
            return "CHECKITCARD.jpg";
        }
        if (identifier == IDENTIFIER_OEBBCARD) {
            return "OEBB.jpg";
        }
        if (identifier == IDENTIFIER_PASSPORT) {
            return "REISEPASS.jpg";
        }
        if (identifier == IDENTIFIER_PERONSALFRONT) {
            return "PERSONALAUSWEIS_VS.jpg";
        }
        if (identifier == IDENTIFIER_PERSONALBACK) {
            return "PERSONALAUSWEIS_RS.jpg";
        }
        if (identifier == IDENTIFIER_DETAILS) {
            return "DETAILS.txt";
        } else {
            System.err.println(" else wurde aufgerufen >>EntryCreator->checkIdentifierPosition(" + identifier + ")");
            return null;
        }

    }

    private int createAge(String date) {
        int age;
        String day, month, year;

        day = date.substring(0, 2);
        month = date.substring(3, 5);
        year = date.substring(6, 10);


        int d = Integer.parseInt(day);
        int m = Integer.parseInt(month);
        int y = Integer.parseInt(year);


        age = dateHandler.getAgeInYears(d, m, y);
        return age;
    }

    class MyBinder extends Binder {
        TestEntryCreationService getService() {
            return TestEntryCreationService.this;
        }
    }

}
