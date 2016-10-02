package nanodegree.mal.udacity.android.attendenceapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by MOSTAFA on 30/09/2016.
 */

//class to check if it is the first run of application
public class MyPreferences {
    private static final String MY_PREFERENCES = "my_preferences";
     static boolean first ;
    static SharedPreferences reader;

    public static boolean isFirst(Context context){
        reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        first = reader.getBoolean("is_first", true);
//        if(first){
//            final SharedPreferences.Editor editor = reader.edit();
//            editor.putBoolean("is_first", false);
//            editor.commit();
//        }
        return first;
    }

    //to change first to not first
    public static void editFirst(){
        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean("is_first", false);
        editor.commit();
    }
}
