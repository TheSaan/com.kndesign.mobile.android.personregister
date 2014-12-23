package com.knoeflerdesign.keywest;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PersonView extends View{

	ImageView profilePicture;
	String firstname,lastname;
	ArrayList<View> children;
	int age;
	Drawable drawableImage;
	TextView description;
	
	public PersonView(Context context) {
		super(context);
		description = new TextView(getContext());
		profilePicture = new ImageView(getContext());
//		setVisibility(VISIBLE);
		setBackgroundColor(Color.BLUE);
		
	}
	
	@SuppressWarnings("deprecation")
	public void addData(String firstname, String lastname, int age, String profilepicpath){
		
		description.setText(lastname +" "+firstname+", "+age);
		//get the profile picutre image path from file
		Bitmap bitmapImage = BitmapFactory.decodeFile(profilepicpath);
		Drawable drawableImage = new BitmapDrawable(bitmapImage);
        

        profilePicture.setBackgroundDrawable(drawableImage);

		children = new ArrayList<View>();
		children.add(profilePicture);
		children.add(description);
		
		this.addTouchables(children);
	}

}
