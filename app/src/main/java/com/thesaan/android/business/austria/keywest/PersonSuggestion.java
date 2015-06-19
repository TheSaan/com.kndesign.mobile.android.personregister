package com.thesaan.android.business.austria.keywest;

import android.app.Notification;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.io.File;

public class PersonSuggestion extends ContentProvider {

    private static final String SUGGESTION_TABLE = Database.DATABASE_TABLE_PERSONS;

    private static final String COL_ID = "_ID";
    private static final String COL_FIRSTNAME = "VORNAME";
    private static final String COL_LASTNAME = "NACHNAME";
    private static final String COL_AGE = "ALTER";
    private static final String COL_BANNED = "HAUSVERBOT";

    private static final String AUTHORITY = KeyWestInterface.PACKAGE+".PersonSuggestion";
    private static final String URL = "content://"+AUTHORITY+"/personen";
    private static final Uri CONTENT_URI = Uri.parse(URL);


    private static final int SUGGESTIONS_OF_PERSONS= 1;
    private static final int LISTED_PERSONS = 2;
    private static final int GET_PERSON = 3;

    public static final String ANY_WORD = "/*";
    public static final String ANY_NUMBER = "/#";

    UriMatcher mUriMatcher = buildUriMatcher();



    private Database db;


    public PersonSuggestion() {
    }
    private UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Suggestion items of Search Dialog is provided by this uri
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + ANY_WORD,SUGGESTIONS_OF_PERSONS);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + ANY_NUMBER,SUGGESTIONS_OF_PERSONS);

        // This URI is invoked, when user presses "Go" in the Keyboard of Search Dialog
        // Listview items of SearchableActivity is provided by this uri
        uriMatcher.addURI(AUTHORITY, SUGGESTION_TABLE, LISTED_PERSONS);

        // This URI is invoked, when user selects a suggestion from search dialog or an item from the listview
        // Country details for CountryActivity is provided by this uri
        // See, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID in CountryDB.java
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_COLUMN_INTENT_DATA , GET_PERSON);

        return uriMatcher;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.

        db = new Database(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        MatrixCursor mc;
        String banned, reason;

        String query = uri.getLastPathSegment().toLowerCase();

        Cursor c = null;

        if(query != null) {
            switch (mUriMatcher.match(uri)) {

                case SUGGESTIONS_OF_PERSONS: {
//                    System.out.println("SUGGESTIONS_OF_PERSONS");

                    //TODO Add image preview
                    mc = new MatrixCursor(new String[]{"_id",SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA});
                    c = db.getPersons(query);

                    int count = c.getCount();

                    c.moveToFirst();

                    if(c instanceof MatrixCursor){
                        mc.newRow()
                                .add(0)
                                .add("Keine Treffer")
                                .add("Gründe: Person nicht vorhanden/fehlerhafte Schreibweise/...")
                                .add(0);
                        return mc;
                    }else
                    if(count > 0 && c != null) {

                        for (int i = 0; i < count; i++) {

                            reason = c.getString(c.getColumnIndex(Database.COL_DETAILS));
                            banned = "Hat Lokal verbot!";
                            if(c.getInt(c.getColumnIndex(Database.COL_BANNED))==1) {

                                if(reason == null) reason = "Keine Angabe";
                                else
                                if(reason == "") reason = "Keine Angabe";

                                banned = "Hat Lokalverbot (" + reason + ")";
                            }else
                                banned = reason;

                            String bdate = c.getString(c.getColumnIndex(Database.COL_BIRTHDATE));

                            String age;

                            if(KeyWestInterface.UNKNOWN_BIRTHDATE.equals(bdate))
                                age = "Keine Angabe";
                            else
                                age = Integer.toString(c.getInt(c.getColumnIndex(Database.COL_AGE)))+" Jahre";

                            int id = c.getInt(c.getColumnIndex(Database.COL_ID));

                            String name_age =
                                            c.getString(c.getColumnIndex(Database.COL_LASTNAME)) +
                                            " " + c.getString(c.getColumnIndex(Database.COL_FIRSTNAME)) +
                                            " ("+ age+")";



                                    mc.newRow()
                                    .add(id)//id
                                    .add(R.drawable.ic_person)
                                    .add(name_age)
                                    .add(banned)
                                    .add(id);

                            c.moveToNext();
                        }
                        return mc;
                    }else{
                        System.err.println("No data matched for cursor");
                        c = null;
                    }

                    break;
                }
                case LISTED_PERSONS: {
                    System.out.println("LISTED_PERSONS");
                    c = db.getPersons(query);
                    break;
                }
                case GET_PERSON: {
                    System.out.println("GET_PERSON");
                    String id = uri.getLastPathSegment();
                    c = db.getPerson(id);

                    break;
                }
                default: {
                    System.out.println("no suggestion data found for uri[" + uri + "]");
                    break;
                }
            }
        }else{
            System.out.println("SelectionArgs queried with null value");
        }
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}