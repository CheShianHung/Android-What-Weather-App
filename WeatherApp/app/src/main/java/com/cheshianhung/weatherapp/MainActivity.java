package com.cheshianhung.weatherapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView testText;
    EditText input;
    Button button;
    String cityName;
    DownloadWebContent downloadWebContent;

    public class DownloadWebContent extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            String result = "";
            String data;

            try {
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                data = bufferedReader.readLine();

                while(data != null){
                    result += data;
                    data = bufferedReader.readLine();
                }

                return result;
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String weather = "";
            try {
                JSONObject jsonObject = new JSONObject(s);
                String weatherJSON = jsonObject.getString("weather");

                JSONArray weatherArray = new JSONArray(weatherJSON);

                for(int i = 0; i < weatherArray.length(); i++){

                    JSONObject jsonPart = new JSONObject(weatherArray.getString(i));
                    weather += jsonPart.getString("main") + ": ";
                    weather += jsonPart.getString("description") + "\n";
                }


                String name = jsonObject.getString("name");
                String country = jsonObject.getJSONObject("sys").getString("country");

                Double minTemp = Float.valueOf(jsonObject.getJSONObject("main").getString("temp_min")) - 273.15;
                String minTempStr = String.format(Locale.getDefault(),"%.1f",minTemp);

                Double maxTemp = Float.valueOf(jsonObject.getJSONObject("main").getString("temp_max")) - 273.15;
                String maxTempStr = String.format(Locale.getDefault(),"%.1f",maxTemp);

                String temperature;

                if(minTempStr.equals(maxTempStr))
                    temperature = minTempStr + "°C\n";
                else
                    temperature = minTempStr + "°C" + " ~ " + maxTempStr + "°C\n";

                testText.setText(name + ", " + country + ":\n\n" + temperature + weather);
            }
            catch(Exception e)
            {
                e.printStackTrace();

                testText.setText("The name you have entered is not available. Please try another one.");
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testText = (TextView) findViewById(R.id.result);
        input = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input.clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(R.id.editText).getWindowToken(), 0);

                cityName = input.getText().toString();
                try {
                    String encodedCityName = URLEncoder.encode(input.getText().toString(), "UTF-8");
                    if (!cityName.equals("")) {
                        testText.setText("Searching...");
                        DownloadWebContent downloadWebContent = new DownloadWebContent();
                        downloadWebContent.execute("http://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&appid=fa38a3a0d503ad58faa3c8a227dc6ee5");
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    testText.setText("Unrecognized character(s). Please try again.");
                }

            }
        });
    }
}
