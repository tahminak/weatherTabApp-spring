package com.android.tamzeveloper.myweather.data;

import com.survivingwithandroid.weather.lib.model.City;

/**
 * Created by tahmina on 15-06-08.
 */
public class UniqueCity extends City {

    /*
     * Unique city identfier
     * */
    private String id;


    public UniqueCity(){
        super(new CityBuilder());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueCity that = (UniqueCity) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
