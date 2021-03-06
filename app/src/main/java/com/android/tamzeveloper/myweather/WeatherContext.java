package com.android.tamzeveloper.myweather;

import android.content.Context;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;

/**
 * Created by tahmina on 15-05-08.
 */
public class WeatherContext {
    private static WeatherContext me;
    private WeatherClient client;

    private WeatherContext() {}

    public static WeatherContext getInstance() {
        if (me == null)
            me = new WeatherContext();

        return me;
    }

    public WeatherClient getClient(Context ctx) {
        if (client != null)
            return client;

        try {
            client = new WeatherClient.ClientBuilder()
                    .attach(ctx)
                    .config(new WeatherConfig())
                    .provider(new OpenweathermapProviderType())
                    .httpClient(WeatherDefaultClient.class)
                    .build();
        }
        catch (WeatherProviderInstantiationException e) {
            e.printStackTrace();
        }

        return client;
    }
}