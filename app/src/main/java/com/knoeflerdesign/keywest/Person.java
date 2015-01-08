package com.knoeflerdesign.keywest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Person {
	DateCalculator datecalc;
	String firstname, lastname, date, profilepicturepath, driverlicencepath,
			checkitcardpath, oebbpath, passportpath, idfrontpath, idbackpath,
			detailspath;
	int age, id, isBanned;
	String[] allValues = { Integer.toString(id), Integer.toString(age),
			firstname, lastname, date, profilepicturepath, driverlicencepath,
			checkitcardpath, oebbpath, passportpath, idfrontpath, idbackpath,
			detailspath };

	String[] allNames = { "id", "age", "firstname", "lastname", "date",
			"pp path", "dl path", "checkit path", "oebb path", "pass path",
			"idf path", "idb path", "det path" };

	// constructor
	public Person(int id, String firstname, String lastname, String date,
			String profilepicturepath, String driverlicencepath,
			String checkitcardpath, String oebbpath, String passportpath,
			String idfrontpath, String idbackpath, String detailspath,
			int isBanned) {
		datecalc = new DateCalculator();

		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.date = date;
		this.profilepicturepath = profilepicturepath;
		this.driverlicencepath = driverlicencepath;
		this.checkitcardpath = checkitcardpath;
		this.oebbpath = oebbpath;
		this.passportpath = passportpath;
		this.idfrontpath = idfrontpath;
		this.idbackpath = idbackpath;
		this.detailspath = detailspath;
		this.isBanned = isBanned;

		System.out.println("in person itself:\n");
		printAll();
		calcAge();

	}

	public void showInformationInList(ImageView thumbnail,TextView description,ImageView banned,ImageView information) {

		// set thumbnail of profile picture
		
		Bitmap thumbnailBMP = getBitmap(profilepicturepath);
		thumbnail.setImageBitmap(thumbnailBMP);

		// set Text of TextView
		// TODO possible i don't need to parse (memory???)
		String age = Integer.toString(this.age);
	    String fullText = age + "\t" + firstname + ", " + lastname;
		/*if the text has more than 20 letters/digits
		* show the text as "17, Firstname La..."
		* */
        if(fullText.toCharArray().length > 20){
            fullText.split(null,17); //cuts the text at 20 signs, so actually at 17 but plus 3 dots
            fullText += "...";
        }
		description
				.setText(fullText);
        //TODO delete new Activity for saving resources
        //gets the text which should be shown as the persons data in a listview element
        Toast.makeText(new SearchActivity(),
                description.getText().toString(), Toast.LENGTH_SHORT).show();
		// set the house for banned status to grey or red

		// isBanned only can be 1 or 0
		if (isBanned == 1) {
			banned.setImageResource(R.drawable.ic_house_banned_black);
		} else {
			banned.setImageResource(R.drawable.ic_house_banned_grey);
		}

		// set the information status to grey (has no info) or black (has
		// information)

		if (getDetails(detailspath) == "") {
			information.setImageResource(R.drawable.ic_info_grey);
		} else {
			information.setImageResource(R.drawable.ic_info_black);
		}

	}

	public void printAll() {
		for (int i = 0; i < allValues.length; i++) {
			System.out.println("Persons " + allNames[i] + ":\t" + allValues[i]);
		}
	}

	private String getDetails(String path) {
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
				System.out.println("could not read line...");
				return null;
			}

		} catch (FileNotFoundException ex) {
			System.out.println("details file not found!");
			return null;
		}

	}

	private Bitmap getBitmap(String path) {
		File f = new File(path);
		Bitmap b = null;
		if (f.exists()) {
			b = BitmapFactory.decodeFile(f.getAbsolutePath());
		}
		return b;
	}

	private void calcAge() {
		System.out.println("Date in calcAge:  " + date);
		String[] dates = date.split("\\.");
		System.out.println("dates lenght:  " + dates.length);
		int day = Integer.parseInt(dates[0]);
		int month = Integer.parseInt(dates[1]);
		int year = Integer.parseInt(dates[2]);

		age = datecalc.getAgeInYears(day, month, year);
	}
}
