package com.thesaan.android.business.austria.keywest.Handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Michael
 */


public class FilesHandler {

    public final static String JPEG = ".jpg";
    public final static String BMP = ".bmp";
    public final static String TXT = ".txt";


    public String checkFileType(File file){

        //1. get the path
        String type = file.getAbsolutePath();

        //2. split at the dot
        String[] parts = type.split("\\.");

        //3. get the file ending
        type = "."+parts[parts.length-1];

        return type;
    }
    public File[] listFiles(String path, boolean printIt) {
        File user_dir = new File(path);

        File[] files = user_dir.listFiles();

        if (printIt) {
            for (int i = 0; i < files.length; i++) {
                System.out.println("File: " + files[i].toString());
            }
        }
        return files;
    }


    public void copy(File src, File dst) throws IOException {
        try {


            FileInputStream inStream = new FileInputStream(src);
            FileOutputStream outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            System.err.println("Copying files failed. " + e);
        }
    }

    public String readTextFile(String path) throws FileNotFoundException {
        String details, line;

        File f = new File(path);

        BufferedReader file = new BufferedReader(new FileReader(f));
        try {
            details = "";
            while ((line = file.readLine()) != null) {
                details += line + "\n";
            }
            return details;
        } catch (IOException e) {
            System.out.println("could not read line...");
            return null;
        }
    }

    public void checkFileForNewVersion(File[] asset_files, File[] svn_files) throws FileNotFoundException {

        /*
        + asset_files sind die dateien an denen du gerade arbeitest
        +svn_files sind die Spiegel Dateien
        */
        
        /*
        Hier wird jetzt jede Datei ausgelesen und mit der dazugehörigen 
        Spiegeldatei verglichen
        */

        //prüfe ob die Anzahl der dateien übereinstimmt

        for (int i = 0; i > asset_files.length; i++) {

            //nehme die aktuellen dateien
            File asset_file = asset_files[i];
            File svn_file = svn_files[i];

            //speichere deren Pfade
            String asset_file_path = asset_file.getPath();
            String svn_file_path = svn_file.getPath();

            //lies die dateien aus
            String asset_content = readTextFile(asset_file_path);
            String svn_content = readTextFile(svn_file_path);

            //vergleiche die dateien
            if (!asset_content.equals(svn_content)) {

                //mache die Spiegel Datei beschreibbar
                svn_file.canWrite();
                //schreibe den Text der aktuellen datei in die 
                //Spiegel Datei

                writeToFile(svn_file, asset_content);
            }
        }
    }

    public void compareTextFileForContent(File old_file, String newText) throws FileNotFoundException {

        //speichere deren Pfade
        String old_file_path = old_file.getPath();

        //lies die dateien aus
        String old_content = readTextFile(old_file_path);

        //vergleiche die dateien
        if (!newText.equals(old_content)) {

            //mache die Spiegel Datei beschreibbar
            old_file.canWrite();
            //schreibe den Text der aktuellen datei in die
            //Spiegel Datei

            writeToFile(old_file, newText);
        }
    }

    public void writeToFile(File file, String newContent) {

        FileWriter w;
        try {
            w = new FileWriter(file);
            w.write(newContent);
            w.close();
        } catch (IOException ex) {
            System.err.println("Write Details Error" + ex);
        }
    }


}
