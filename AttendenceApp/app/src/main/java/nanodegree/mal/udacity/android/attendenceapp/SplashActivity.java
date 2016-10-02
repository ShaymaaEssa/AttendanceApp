package nanodegree.mal.udacity.android.attendenceapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class SplashActivity extends ActionBarActivity {

    //splash screen timer
    static int SPLASH_TIME_OUT = 3*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splach);

        //after splash screen timer, the BrandsActivity will open
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //to open RegisterActivity just one time when first run of application
                if ( MyPreferences.isFirst(SplashActivity.this)){
                    Intent i = new Intent(SplashActivity.this, RegisterActivity.class);
                    startActivity(i);
                    finish();
                }
                else {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }

            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splach, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
