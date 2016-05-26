package sid.comslav.com.circleofmusic.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class dbHandler extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "circle_of_music.db";
    //    TABLE 1
    public static final String TABLE_TRACKS = "track_list";
    public static final String COLUMN_TRACK_ID = "track_id";
    public static final String COLUMN_TRACK_ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String COLUMN_TRACK_NAME = "track_name";
    public static final String COLUMN_TRACK_NAME_TYPE = "TEXT";
    //    TABLE 2
    public static final String TABLE_VERSION = "version_data";
    public static final String COLUMN_VERSION_ID = "version_id";
    public static final String COLUMN_VERSION_ID_TYPE = "REAL PRIMARY KEY";

    public dbHandler(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

//    public static final String CHANGELOG_INFO = "changelog_info";
//    public static final String CHANGELOG_INFO_TYPE = "TEXT";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_TRACKS + "\n(\n" + COLUMN_TRACK_ID + " " + COLUMN_TRACK_ID_TYPE + " , " + COLUMN_TRACK_NAME + " " + COLUMN_TRACK_NAME_TYPE + "\n);";
        String query2 = "CREATE TABLE IF NOT EXISTS " + TABLE_VERSION + "\n(\n" + COLUMN_VERSION_ID + " " + COLUMN_VERSION_ID_TYPE + "\n);";
        try {
            db.execSQL(query);
            db.execSQL(query2);
        } catch (SQLException e) {
            loggingHandler loggingHandler = new loggingHandler();
            loggingHandler.addLog(e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSION);
        onCreate(db);
    }

    public boolean addTrack(String track_name) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_TRACKS + " WHERE " + COLUMN_TRACK_NAME + " == " + track_name + ";";
        try {
            Cursor c = db.rawQuery(query, null);
            if (c == null) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TRACK_NAME, track_name);
                try {
                    db.insert(TABLE_TRACKS, null, values);
                    db.close();
                    return true;
                } catch (Exception e) {
                    loggingHandler loggingHandler = new loggingHandler();
                    loggingHandler.addLog(e.getMessage());
                }
            } else {
                c.close();
            }
        } catch (Exception e) {
            loggingHandler loggingHandler = new loggingHandler();
            loggingHandler.addLog(e.getMessage());
        }
        return false;
    }

    public String[] fetchTracks() {
        ArrayList<String> track_list = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_TRACKS + ";";
        try {
            Cursor c = db.rawQuery(query, null);

            c.moveToFirst();
            int index = 0;
            while (!c.isAfterLast()) {
                if (c.getString(c.getColumnIndex(COLUMN_TRACK_NAME)) != null) {
                    track_list.add(index, c.getString(c.getColumnIndex(COLUMN_TRACK_NAME)));
                    index++;
                }
                c.moveToNext();
            }
            c.close();
            db.close();
            return (String[]) track_list.toArray();
        } catch (Exception e) {
            loggingHandler loggingHandler = new loggingHandler();
            loggingHandler.addLog(e.getMessage());
        }
        return (String[]) track_list.toArray();
    }
}
