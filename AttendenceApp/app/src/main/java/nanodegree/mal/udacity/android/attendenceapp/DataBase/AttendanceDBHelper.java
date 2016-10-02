package nanodegree.mal.udacity.android.attendenceapp.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MOSTAFA on 01/10/2016.
 */
public class AttendanceDBHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "AttendanceDB";
    private static final int DB_VERSION = 1;

    public AttendanceDBHelper(Context context) {
        super(context, DB_NAME,null,DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("Create Table UserInfo ( " +
                                "_id Integer primary key AutoIncrement, "+
                                "UserName Text, "+
                                "CompanyName Text, "+
                                "Latitude Text, "+
                                "Longitude Text, "+
                                "WorkTimeFrom Text, "+
                                "WorkTimeTo Text, "+
                                "DaysOff Text );");

        sqLiteDatabase.execSQL("Create Table AttendanceData ( " +
                                "_id Integer primary key AutoIncrement, " +
                                "UserId Integer, " +
                                "Date DATETIME DEFAULT CURRENT_TIMESTAMP, "+
                                "FOREIGN KEY (UserId) REFERENCES UserInfo (_id) );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
