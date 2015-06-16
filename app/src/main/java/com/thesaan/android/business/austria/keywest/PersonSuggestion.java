package com.thesaan.android.business.austria.keywest;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

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
        ;
        MatrixCursor mc;
        String banned;
        selection = "LIKE ?";
        String select =
                Database.COL_LASTNAME + selection + " OR "+
                        Database.COL_FIRSTNAME + selection +  " OR "+
                        Database.COL_AGE + selection +  " OR "+
                        Database.COL_BIRTHDATE + selection;

        String query = uri.getLastPathSegment();
        System.out.println("Query:\t"+query);
        Cursor c = null;

        if(query != null) {
            switch (mUriMatcher.match(uri)) {

                case SUGGESTIONS_OF_PERSONS: {
                    System.out.println("SUGGESTIONS_OF_PERSONS");

                    mc = new MatrixCursor(new String[]{"_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA});
                    c = db.getPersons(query.split(" "));

                    c.moveToFirst();

                    int count = c.getCount();

                    System.out.println(count+" possible matches");
                    System.out.println(mc.getColumnCount()+" mc columns");
                    System.out.println(c.getColumnCount()+" c columns");


                    //mc.moveToFirst();
                    if(c instanceof MatrixCursor){
                        mc.newRow()
                                .add(0)
                                .add("Keine Treffer")
                                .add("Gründe: Person nicht vorhanden/fehlerhafte Schreibweise/...")
                                .add(0);
                        return mc;
                    }else
                    if(c.getCount() > 0 && c != null) {
                        for (int i = 0; i < count; i++) {
                            if(c.getInt(c.getColumnIndex(Database.COL_BANNED))==1)
                                banned = "Hat Hausverbot";
                            else
                                banned = "";
                            mc.newRow()
                                    .add(c.getInt(c.getColumnIndex(Database.COL_ID)))
                                    .add(c.getString(c.getColumnIndex(Database.COL_FIRSTNAME)) + " " + c.getString(c.getColumnIndex(Database.COL_LASTNAME)))
                                    .add(c.getInt(c.getColumnIndex(Database.COL_AGE)) + " Jahre / " + banned)
                                    .add(c.getInt(c.getColumnIndex(Database.COL_ID)));

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
                    c = db.getPersons(query.split(" "));
                    break;
                }
                case GET_PERSON: {
                    System.out.println("GET_PERSON");
                    String id = uri.getLastPathSegment();
                    c = db.getPerson(id);

                    break;
                }
                default:
                    System.out.println("no suggestion data found for uri[" + uri + "]");
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