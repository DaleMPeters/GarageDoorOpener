package dalepi.garagedooropener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ButtonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(ButtonActivity.this, SettingsActivity.class);
                startActivityForResult(settingsIntent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //    return true;
        //}

        //return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user clicks on the Open/Close button
     *
     * @param view //TODO add comment here
     */
    public void sendRequest(View view) {
        final Button openCloseButton = (Button) findViewById(R.id.button);
        //temporarilyDisableButton(openCloseButton);
        ConnectivityManager connectionManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new RunRequestTask().execute();
        } else {
            Toast connectionErrorToast = Toast.makeText(getApplicationContext(),
                    "An error occurred, please ensure network connectivity", Toast.LENGTH_LONG);
            connectionErrorToast.show();
        }
    }

    /* public void temporarilyDisableButton(Button buttonToDisable){
        buttonToDisable.setEnabled(false);
        new Thread(new Runnable(){
           @Override
            public void run(){
               try{
                   Thread.sleep(5000);
               } catch (InterruptedException e) {

               }
           }
        }).start();
        //buttonToDisable.setEnabled(true);
    } */

    private class RunRequestTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... urls) {
            try {
                SharedPreferences userPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String strGarageDoorOpenerUrl = userPreferences.getString("pref_url", "http://dalepi/index.php?trigger=1");
                int response = runRequest(strGarageDoorOpenerUrl);
                return response;

            } catch (IOException e) {
                return 0;
            }
        }

        private int runRequest(String strUrlToGarageOpener) throws IOException {
            int response;
            try {
                URL urlObjectToGarageOpener = new URL(strUrlToGarageOpener);
                HttpURLConnection connection = (HttpURLConnection) urlObjectToGarageOpener.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                connection.connect();
                response = connection.getResponseCode();
            } catch (IOException e) {
                return 0;
            }
            return response;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String toastMessageText;
            if (result == 0) {
                toastMessageText = "Error - make sure Pi is switched on";
            } else {
                toastMessageText = Integer.toString(result);
            }
            Toast connectionToast = Toast.makeText(getApplicationContext(), toastMessageText, Toast.LENGTH_SHORT);
            connectionToast.show();
        }
    }
}
