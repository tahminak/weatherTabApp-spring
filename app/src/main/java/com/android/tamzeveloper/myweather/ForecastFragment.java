package com.android.tamzeveloper.myweather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.tamzeveloper.myweather.data.WeatherContract;
import com.android.tamzeveloper.myweather.sync.MyWeatherSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


   private ForecastAdapter mForecastAdapter;
 //  private ArrayAdapter<String> mForecastAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private final String LOG_TAG= "TAMZ";
    private final String CLASS_TAG=ForecastFragment.class.getSimpleName()+" : ";


    // Identifies a particular Loader being used in this component
    private static final int LOADER_ID = 1;


    private boolean mUseTodayLayout;
// static int position;
    private String[] locations={"toronto","dhaka","atlanta"};
    private JSONArray cityArray;
    private int position;

    private Callback mCallback;



    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    ListView listView;


    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        //View rootView = inflater.inflate(R.layout.fragment_main, container, false);

       // mForecastAdapter = new ForecastAdapter(getActivity(),null, 0);
//
//        mForecastAdapter =
//                new ArrayAdapter<String>(
//                        getActivity(), // The current context (this activity)
//                        R.layout.list_item_forecast, // The name of the layout ID.
//                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
//                        new ArrayList<String>());
//
//        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
//        mListView.setAdapter(mForecastAdapter);

       // String locationSetting = Utility.getPreferredLocation(getActivity());

        String locationSetting = "toronto";

        //Get the city names
        SharedPreferences sharedPref = getActivity().getSharedPreferences("locations", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        try {
        cityArray=new JSONArray(sharedPref.getString("citynames",""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG,"The saved cities in Fragement are "+cityArray.toString());


        // We'll call our MainActivity
      /*  mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                }
                mPosition = position;

            }
        });*/
//
//        if(savedInstanceState!=null && savedInstanceState.containsKey(SELECTED_KEY)){
//            mPosition=savedInstanceState.getInt(SELECTED_KEY);
//
//        }

        // Sort order:  Ascending, by date.
//        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
//      Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
//            locationSetting, System.currentTimeMillis());
////currentTimeMillis
//        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
//                null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        Bundle bundle=getArguments();
        position=bundle.getInt("position");

       // mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
//        if (mPosition != ListView.INVALID_POSITION) {
//            outState.putInt(SELECTED_KEY, mPosition);
//        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        Log.d(LOG_TAG,CLASS_TAG+ "Starting loader");

        getLoaderManager().initLoader(LOADER_ID,null,this);

        super.onActivityCreated(savedInstanceState);
    }



    @Override
    public void onStart() {

        Log.d(LOG_TAG,CLASS_TAG+" OnStart() ");

        super.onStart();
        getLocations();
        updateWeather();
    }

    private void getLocations() {



    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {


      //  String locationSetting="toronto";
                // Sort order:  Ascending, by date.
        // cities=getActivity().getResources().getStringArray(R.array.locations);
        //String locationSetting=locations[position];

        String locationSetting= null;
        try {
            locationSetting = cityArray.getJSONObject(position).getString("city").toLowerCase();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG,CLASS_TAG+" Location in onCreateLoader() "+locationSetting);
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        locationSetting, System.currentTimeMillis());

      //  Log.d(LOG_TAG,CLASS_TAG+ weatherForLocationUri.toString());

                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        weatherForLocationUri,        // Table to query
                        FORECAST_COLUMNS,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        sortOrder             // Default sort order
                );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {


     //   Log.d(LOG_TAG,"On Finish Loader : ");

        if(mForecastAdapter!=null && cursor!=null) {
            mForecastAdapter.swapCursor(cursor);

           // Log.d(LOG_TAG, "Adapter is NOT null "+cursorLoader.getId());

        }
        else
        {
            Log.d(LOG_TAG, "Adapter is null ");
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

//Setting the option menu to


    @Override
    public void onCreate(Bundle bundle){

        super.onCreate(bundle);
       // setHasOptionsMenu(true);
        //updateWeather();

        Log.d(LOG_TAG,CLASS_TAG +"On Create() ");
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
//        if (mForecastAdapter != null) {
//            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
//        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    //Adding the menu to the fragement

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {

        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id ==R.id.action_refresh ) {

            updateWeather();


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
      //  updateWeather();
     //   getLoaderManager().restartLoader(LOADER_ID, null, this);
    }



    public void updateWeather(){

       // Log.d("TAMZ","On Update Weather Forcast Fragement "+ WeatherContract.WeatherEntry.CONTENT_URI.toString());

      MyWeatherSyncAdapter.syncImmediately(getActivity());


//       FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String location = prefs.getString(getString(R.string.pref_location_key),
//                getString(R.string.pref_location_default));
//        weatherTask.execute("toronto");


    }

    public static ForecastFragment getInstance(int position) {

        ForecastFragment fragment=new ForecastFragment();

        Bundle args=new Bundle();
        args.putInt("position",position);

       //args.putString("location",MainActivity.locations[position]);
        fragment.setArguments(args);
       // Log.d("TAMZ","Location is : "+MainActivity.locations[position]);

        //ForecastFragment.position =position;

        return  fragment;
    }


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

}