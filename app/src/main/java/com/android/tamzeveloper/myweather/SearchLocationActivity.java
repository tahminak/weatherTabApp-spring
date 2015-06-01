package com.android.tamzeveloper.myweather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.exception.LocationProviderNotFoundException;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.City;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by tahmina on 15-05-08.
 */
public class SearchLocationActivity extends Activity {

    private ListView cityListView;
    private ProgressBar bar;
    private CityAdapter adp;
    private WeatherClient client;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_location_activity);

        client = WeatherContext.getInstance().getClient(this);

        Log.d("TAMZ", "Client [" + client + "]");

        cityListView = (ListView) findViewById(R.id.cityList);
        bar = (ProgressBar) findViewById(R.id.progressBar2);
        adp = new CityAdapter(SearchLocationActivity.this, new ArrayList<City>());
        cityListView.setAdapter(adp);

        ImageView searchView = (ImageView) findViewById(R.id.imgSearch);
        final EditText edt = (EditText) findViewById(R.id.cityEdtText);

        edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(v.getText().toString());
                    return true;
                }

                return false;
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                search(edt.getEditableText().toString());
            }
        });

        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos,
                                    long id) {
                SharedPreferences sharedPref = getSharedPreferences("locations", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                City city = (City) parent.getItemAtPosition(pos);

                String cities= sharedPref.getString("cities",null);
                JSONArray cityArray=new JSONArray();

                if(cities!=null){
                    try {
                        cityArray=new JSONArray(cities);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }



                JSONObject cityJson=new JSONObject();
                try {
                    cityJson.put("cityid", city.getId());
                    cityJson.put("cityName", city.getName());
                    //Get Long Country Name
                    Locale loc = new Locale("",city.getCountry());
                    cityJson.put("country", loc.getDisplayCountry());


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                editor.putString("cityid", city.getId());
                editor.putString("cityName", city.getName());

                //Get Long Country Name
                Locale loc = new Locale("",city.getCountry());
                editor.putString("country", loc.getDisplayCountry());

                cityArray.put(cityJson);


                editor.putString("cities", cityArray.toString());
                editor.commit();
                Log.d("TAMZ", "City Name: " + city.getName());


//                SharedPreferences sharedPreferences=getPreferences(Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor1 = sharedPreferences.edit();
//                editor


                String format = "json";
                String units = "metric";
                int numDays = 14;
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, city.getName())
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();



                try {
                    URL url = new URL(builtUri.toString());
                    Log.i("TAMZ","URL in search Activity : "+url.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                NavUtils.navigateUpFromSameTask(SearchLocationActivity.this);
            }
        });

        ImageView locImg = (ImageView) findViewById(R.id.imgLocationSearch);
        locImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bar.setVisibility(View.VISIBLE);
                try {
                    client.searchCityByLocation(WeatherClient.createDefaultCriteria(), new WeatherClient.CityEventListener() {

                        @Override
                        public void onCityListRetrieved(List<City> cityList) {
                            bar.setVisibility(View.GONE);
                            adp.setCityList(cityList);
                            adp.notifyDataSetChanged();
                        }

                        @Override
                        public void onWeatherError(WeatherLibException wle) {
                            bar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onConnectionError(Throwable t) {
                            bar.setVisibility(View.GONE);
                        }
                    });
                }
                catch(LocationProviderNotFoundException lpnfe) {

                }
            }

        });
    }

    private void search(String pattern) {
        bar.setVisibility(View.VISIBLE);
        client.searchCity(pattern, new WeatherClient.CityEventListener() {
            @Override
            public void onCityListRetrieved(List<City> cityList) {
                bar.setVisibility(View.GONE);
                adp.setCityList(cityList);
                adp.notifyDataSetChanged();
            }

            @Override
            public void onWeatherError(WeatherLibException t) {
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onConnectionError(Throwable t) {
                bar.setVisibility(View.GONE);
            }
        });
    }
}
