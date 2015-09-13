package com.simpleapps.hx.myfarecalc;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    AutoCompleteTextView to_address;
    AutoCompleteTextView from_address;
    Button submit_button, to_address_clear_button, from_address_clear_button;
    TextView fare_title_textview, fare_value_textview,
            distance_title_textview, distance_value_textview,
            duration_title_textview, duration_value_textview;

    String distance = "";
    String duration = "";
    String duration_text = "";
    String distance_text = "";

    private PlacesAutoCompleteAdapter adapter;

    // Min and Max rate set by admin
    int max_hourly_rate = 150;
    int min_hourly_rate = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        * PlacesAutoCompleteAdapter is used to get the suggestion from google map api.
        *
        * */
        adapter = new PlacesAutoCompleteAdapter(getApplicationContext(),
                R.layout.autocomplete_list_text);

        to_address = (AutoCompleteTextView) findViewById(R.id.to_address);
        // set adapter for auto completion
        to_address.setAdapter(adapter);

        from_address = (AutoCompleteTextView) findViewById(R.id.from_address);
        from_address.setAdapter(adapter);

        fare_value_textview = (TextView) findViewById(R.id.fare_value_textview);
        fare_title_textview = (TextView) findViewById(R.id.fare_title_textview);

        distance_title_textview = (TextView) findViewById(R.id.distance_title_textview);
        distance_value_textview = (TextView) findViewById(R.id.distance_value_textview);

        duration_title_textview = (TextView) findViewById(R.id.duration_title_textview);
        duration_value_textview = (TextView) findViewById(R.id.duration_value_textview);

        to_address_clear_button = (Button) findViewById(R.id.to_addresss_clear_button);
        to_address_clear_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                to_address.setText("");
            }
        });


        from_address_clear_button = (Button) findViewById(R.id.from_addresss_clear_button);
        from_address_clear_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                from_address.setText("");
            }
        });

        from_address.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int actionId,
                                          KeyEvent arg2) {
                // hide the keyboard and search the web when the enter key
                // button is pressed

                if (actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_SEARCH
                        || (arg2.getAction() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(from_address.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });


        submit_button = (Button) findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // calling backend thread to get approximate distance and time
                new getDistance().execute();
            }
        });




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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ShowFareDistanceAndTraveltime(
            String fareString, String distanceString, String durationString) {
        fare_value_textview.setText(fareString);
        duration_value_textview.setText(durationString);
        distance_value_textview.setText(distanceString);

        fare_title_textview.setVisibility(View.VISIBLE);
        fare_value_textview.setVisibility(View.VISIBLE);

        distance_title_textview.setVisibility(View.VISIBLE);
        distance_value_textview.setVisibility(View.VISIBLE);

        duration_title_textview.setVisibility(View.VISIBLE);
        duration_value_textview.setVisibility(View.VISIBLE);
    }

    private class getDistance extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // call the loading view here
        }

        // Http request using OkHttpClient
        String callOkHttpRequest(URL url, OkHttpClient httpClient)
                throws IOException{

            HttpURLConnection connection = httpClient.open(url);

            connection.setConnectTimeout(40000);
            InputStream in = null;
            try {
                // Read the response.
                in = connection.getInputStream();
                byte[] response = readFully(in);
                return new String(response, "UTF-8");
            } finally {
                if (in != null)
                    in.close();
            }
        }

        byte[] readFully(InputStream in) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            return out.toByteArray();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = "http://maps.googleapis.com/maps/api/distancematrix/json?" + "origins" + "="
                    + to_address.getText().toString() + "&"
                    + "destinations" + "="
                    + from_address.getText().toString() +"&"
                    + "units=metric";
            Log.i("pavan", "url:" + url);

            // Http requesting OkHttpClient
            OkHttpClient client_to_get_distance = new OkHttpClient();

            String response = null;

            try {
                url = url.replace(" ", "%20");
                response = callOkHttpRequest(new URL(url),
                        client_to_get_distance);

                for (String substring : response.split("<script", 2)) {
                    response = substring;
                    break;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if(response != null) {
                try {
                    // turn response into JSON object
                    JSONObject jObject = new JSONObject(response);

                    // checking status
                    if (jObject.getString("status").equals("OK")) {

                        // Parsing the value from row array
                        JSONArray jArray = jObject.getJSONArray("rows");

                        for (int i = 0; i < jArray.length(); i++) {

                            JSONObject jobj = jArray.getJSONObject(i);

                            JSONArray jaArray2 = jobj.getJSONArray("elements");

                            for (int j = 0; j < jaArray2.length(); j++) {

                                JSONObject jobj1 = jaArray2.getJSONObject(j);

                                JSONObject jobj_duration = jobj1.
                                        getJSONObject("duration");
                                JSONObject jobj_distance = jobj1.
                                        getJSONObject("distance");
                                duration = jobj_duration.getString("value");
                                duration_text = jobj_duration.getString("text");
                                distance = jobj_distance.getString("value");
                                distance_text = jobj_distance.getString("text");

                                double dur_in_hr = (Double.valueOf(duration) * 1 / (60 * 60));

                                // Minimum is Rs 75 base fare + Re 1 per minute + Rs 10 per km
                                String estimatedFareMinimum = String.valueOf((int) (75
                                        + 1 * Integer.valueOf(duration)/ 60
                                        + 10 * Integer.valueOf(distance)/1000));
                                // Maximum is Rs 100 base fare + Re 1.5 per minute + Rs 15 per km
                                String estimatedFareMaximum = String.valueOf((int) (100
                                        + 1.5 * Integer.valueOf(duration)/ 60
                                        + 15 * Integer.valueOf(distance)/1000));

                                // Returned format string
                                String estimatedFare =
                                        "\u20B9" + estimatedFareMinimum
                                        + " - " + "\u20B9" + estimatedFareMaximum;

                                ShowFareDistanceAndTraveltime(
                                        estimatedFare, distance_text, duration_text);
                            }

                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }

}
