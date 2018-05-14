package com.lopez.richard.nova_v6_en2;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LightService {
    @GET("light/{number}/on")
    Call<String> setLightOn(@Path("number") int number);

    @GET("light/{number}/off")
    Call<String> setLightOff(@Path("number") int number);
}
