package app.com.example.noahpatterson.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("lifecycle", "activity onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("lifecycle", "activity onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("lifecycle", "activity onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("lifecycle", "activity onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("lifecycle", "activity onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("lifecycle", "activity onDestroy");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
//            Intent settingsIntent = new Intent(this, SettingsActivity.class);
//            startActivity(settingsIntent);
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
