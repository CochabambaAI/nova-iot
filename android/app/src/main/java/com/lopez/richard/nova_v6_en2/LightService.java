package com.lopez.richard.nova_v6_en2;

import retrofit2.Call;
import retrofit2.http.GET;

public interface LightService {
    @GET("light/on")
    Call<String> setLightOn();

    @GET("light/off")
    Call<String> setLightOff();
}
