package com.thesaan.android.business.austria.keywest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.thesaan.android.business.austria.keywest.saandroid.ProActivity;

/**
 * Created by Michael on 18.09.2015.
 */
public class PasswordDialog extends Dialog implements KeyWestInterface {
    EditText username, password;
    Button confirm, cancel;

    private boolean isCancelable = false;

    private int wrongInputCounter = 3;

    Intent returnToStartActivity;

    private int min_rank;

    ProActivity currentActivity;
    //accessable variable to check if the user passed the password prompt successfully
    public boolean passed = false;
    Database db;

    Context context;

    /**
     * A Dialog Version for Person Registration App with a different functionality for
     * different internal Activities
     *
     * @param context
     * @param cancel_status Defines if the Dialog is cancelable or not
     * @param activity      The kind of activity defines in the {@link #PasswordDialog(Context, boolean, ProActivity)#onClick(View)} method
     *                      which rank is required to get passed through password control
     */
    PasswordDialog(Context context, boolean cancel_status, ProActivity activity) {
        super(context);

        setTitle("Anmeldedaten eingeben");
        setCanceledOnTouchOutside(false);
        this.context = context;

        setCancelable(false);
        setContentView(R.layout.dialog_password_prompt);


        db = new Database(context);
        /*
         *Defines the minimum rank to use the required functionality
         */
        min_rank = PersonRegister.getMinimumAccountRank(activity);

        //define the currently used activity
        currentActivity = activity;

        confirm = (Button) findViewById(R.id.checkB);
        cancel = (Button) findViewById(R.id.cancelB);

        if (cancel_status) {

            addCancelButton();
            setCancelable(cancel_status);
            isCancelable = cancel_status;

        } else {
            removeCancelButton();
        }

        username = (EditText) findViewById(R.id.usernameET);
        password = (EditText) findViewById(R.id.pwdET);

        confirm = (Button) findViewById(R.id.checkB);
        cancel = (Button) findViewById(R.id.cancelB);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserData();
            }
        });

        if(isCancelable()){
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel();
                }
            });
        }
    }

    protected void addCancelButton() {
        cancel.setVisibility(View.VISIBLE);
    }

    protected void removeCancelButton() {
        cancel.setVisibility(View.GONE);
    }

    public void checkUserData() {
        String u = username.getText().toString();
        String p = password.getText().toString();

        System.out.println("RankId:"+ db.readRankID(u)+"\nPassword correct:"+db.isPasswordCorrect(u,p));

        if (db.readRankID(u) >= min_rank && db.isPasswordCorrect(u, p)) {
            passed = true;
            dismiss();
        } else {
            if (!db.isUserAssigned(u)) {
                System.out.println(u);
                currentActivity.toast("Benutzer nicht registriert!", currentActivity.SHORT);
            } else if (!db.isPasswordCorrect(u, p)) {
//                wrongInputCounter--;
//                if (wrongInputCounter == 0) {
                    Toast.makeText(context, "Keine Berechtigung!", Toast.LENGTH_SHORT).show();
//                    returnToStartActivity = new Intent(context, StartActivity.class);
//                    context.startActivity(returnToStartActivity);
//                } else {
//                    Toast.makeText(context, "Passwort nicht korrekt!\n" + wrongInputCounter + " weitere Versuche...", Toast.LENGTH_SHORT).show();
//                }
            } else if (db.readRankID(u) < RANK_MASTER_ID) {
                Toast.makeText(context, "Sie haben für diese Funktion keine Berechtigung!", Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void cancel(){
        if(isCancelable)
            dismiss();
    }
    public boolean hasPassed() {
        return passed;
    }

    public boolean isCancelable() {
        return isCancelable;
    }
}
