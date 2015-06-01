package com.android.tamzeveloper.myweather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.tamzeveloper.myweather.sync.MyWeatherSyncAdapter;
import com.android.tamzeveloper.myweather.tabs.SlidingTabLayout;
import com.survivingwithandroid.weather.lib.model.City;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;


public class MainActivity extends ActionBarActivity implements MaterialTabListener{

    private String LOG_CAT= "TAMZ";



    private ViewPager mPager;


    private MaterialTabHost mTabs;

    private SlidingTabLayout mTabsReg;


   //private String[] locationsTabs ={"toronto","dhaka","atlanta"};


    //Navigation Drawer
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    private NavbarAdapter mNavbarAdapter;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;


    //Get Current Location
    private double longitude;
    private double latitude;

    private GPSTracker gpsTracker;
    private List<Address> addresses;


    private City currentLocation;
    private List<City> locations;
    private List<City> locationsTabs;
    JSONArray cityArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


     //   Toolbar toolbar=(Toolbar)findViewById(R.id.tool_bar);

      //  setSupportActionBar(toolbar);
       // getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Setup Navigation Drawer
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();



        locations=new ArrayList<>();
        locationsTabs=new ArrayList<>();

        getCurrentCityAndAddLocations();

        addDrawerItems();
        setupDrawer();




        //Save city names to shared pref

        SharedPreferences sharedPref = getSharedPreferences("locations", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();



        JSONArray  cityNamesJsonArray=new JSONArray();

        for(City city:locationsTabs){
            JSONObject cityJson=new JSONObject();

            try {
                cityJson.put("city",city.getName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cityNamesJsonArray.put(cityJson);


        }

        editor.putString("citynames",cityNamesJsonArray.toString());
        editor.commit();

        Log.d(LOG_CAT,"The saved cities are "+cityNamesJsonArray.toString());


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(false);


        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //  getSupportActionBar().setHomeButtonEnabled(true);


//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new ForecastFragment())
//                    .commit();
//        }

        //Log.d(LOG_CAT, "in OnCreate of MainActiviy");


       // cities=res.getStringArray(R.array.cities);


        Resources res = getResources();
      //  locationsTabs =res.getStringArray(R.array.locations);






        //Set Material Tabs
       setMaterialTabs();

       // LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

       // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, this);

        //Set Regular Tabs
      //  setRegularTabs();




       MyWeatherSyncAdapter.initializeSyncAdapter(this);
       // Log.d(LOG_CAT, "in OnCreate of MainActiviy after sync started" );
    }




    private void getCurrentCityAndAddLocations(){




        //Get the current location
        gpsTracker=new GPSTracker(MainActivity.this);



        if(gpsTracker.canGetLocation()){

            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();


            Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());


            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
                Log.d(LOG_CAT, "Address Is : " + addresses);

                if (addresses.size() > 0) {
                    currentLocation=new City(new City.CityBuilder());
                    currentLocation.setName(addresses.get(0).getLocality());

                    //Get Long Country Name

                    currentLocation.setCountry(addresses.get(0).getCountryName());

                    locations.add(currentLocation);
                    //add current locations for the tabs
                    locationsTabs.add(currentLocation);

                    Log.d(LOG_CAT, "Current City is: " + currentLocation.getCountry());
                }


            } catch (IOException e) {
                e.printStackTrace();
            }



        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }


        SharedPreferences sharedPref = getSharedPreferences("locations", Context.MODE_PRIVATE);
//        String city = sharedPref.getString("cityName", "Toronto");
//        String country = sharedPref.getString("country", "Canada");
//        String cityID = sharedPref.getString("cityid", "id");



        try {
            //  JSONObject cityJson=new JSONObject(sharedPref.getString("cityJson", "id"));

            String sharedCities=sharedPref.getString("cities",null);



            if(sharedCities!=null){

                cityArray=new JSONArray(sharedCities);



                for(int i=0;i<cityArray.length();i++){

                    JSONObject cityJson=new JSONObject(cityArray.get(i).toString());

                    City city=new City(new City.CityBuilder());

                    city.setId(cityJson.getString("cityid"));
                    city.setName(cityJson.getString("cityName"));
                    city.setCountry(cityJson.getString("country"));




                    locations.add(city);

                    //add location for the tabs
                    locationsTabs.add(city);

                    Log.d("TAMZ", "Adding City : " + i + " City Name : " + city.getName());
                }

            }

            //add the last Item on the list
            City lastCity=new City(new City.CityBuilder());
            lastCity.setName("Add Place");
            lastCity.setCountry("Search for city");

            locations.add(lastCity);


        //    Log.d("TAMZ","Shared Prefered : "+"Number of city is "+locations.size()+" City Array : "+locations.get(0).getCountry());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    // Add drwaerItems
    private void addDrawerItems() {

     // final List<City> osArray=new ArrayList<>();
//        if (addresses.size() > 0)
//      Log.d("TAMZ","City is : "+addresses.get(0).getLocality());

/*

        gpsTracker=new GPSTracker(MainActivity.this);



        if(gpsTracker.canGetLocation()){

            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());


            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
                Log.d(LOG_CAT, "Address Is : " + addresses);

                if (addresses.size() > 0) {



                    osArray.add(addresses.get(0).getLocality());


                    Log.d(LOG_CAT, "Location is: " + addresses.get(0).getCountryName());

                }


            } catch (IOException e) {
                e.printStackTrace();
            }



        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }
*/

       // osArray.add("Add Location");


       // mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);

//        for(City city:locations){
//            Log.d(LOG_CAT,"City Name : "+city.getName());
//        }


        mNavbarAdapter= new NavbarAdapter(this,locations);
     //   mNavbarAdapter.notifyDataSetChanged();
        mDrawerList.setAdapter(mNavbarAdapter);

        //int defaultValue = getResources().getInteger(R.string.saved_high_score_default);



        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {


                //Selection option for added locations

                if(position>0 && position<locations.size()-1){


                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete location")
                            .setMessage("Are you sure you want to delete this location?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                  //  locations.remove(position);


                                    SharedPreferences sharedPref = getSharedPreferences("locations", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                //    City city = (City) parent.getItemAtPosition(pos);

                                    String cities= sharedPref.getString("cities",null);
                                    JSONArray cityArray=new JSONArray();

                                    if(cities!=null){
                                        try {
                                            cityArray=new JSONArray(cities);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    cityArray.remove(position-1);

                                    editor.putString("cities", cityArray.toString());
                                    editor.commit();


                                finish();
                                 startActivity(getIntent());
                                    Log.d(LOG_CAT, "Removed City Name: " + (position-1));








                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                   // Toast.makeText(MainActivity.this, "Time for an upgrade! Clicked item " + position, Toast.LENGTH_SHORT).show();
                }



                if(position==(locations.size()-1)){

                    startActivity(new Intent(MainActivity.this,SearchLocationActivity.class));



                }
               // Toast.makeText(MainActivity.this, "Time for an upgrade! Clicked item " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateSharedPreference(List<City> locations){





    }


    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void setMaterialTabs(){

        mPager=(ViewPager)findViewById(R.id.pager);

        //mPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));




        mTabs=(MaterialTabHost)findViewById(R.id.materialTabHost);


        ViewPagerAdapter pagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());

        mTabs=(MaterialTabHost)findViewById(R.id.materialTabHost);
        mPager.setAdapter(pagerAdapter);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
                mTabs.setSelectedNavigationItem(position);
            }
        });

        // insert all tabs from pagerAdapter data
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            mTabs.addTab(
                    mTabs.newTab()
                            .setText(pagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setRegularTabs(){

    /*    mPager=(ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(new ViewTabPagerAdapter(getSupportFragmentManager()));

        mTabsReg = (SlidingTabLayout)findViewById(R.id.tabs);

        mTabsReg.setViewPager(mPager);
*/

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

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        mPager.setCurrentItem(materialTab.getPosition());

    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {

    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }


    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
   // private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {


            ForecastFragment forecastFragment=ForecastFragment.getInstance(position);

            return forecastFragment;
        }

        @Override
        public int getCount() {
            return locationsTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {



            return locationsTabs.get(position).getName();

        }
    }


    /*private class ViewTabPagerAdapter extends FragmentPagerAdapter {

        public ViewTabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {


            ForecastFragment forecastFragment=ForecastFragment.getInstance(position);

            return forecastFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {



            return locationsTabs[position];

        }
    }*/
}
