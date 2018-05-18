package com.lopez.richard.nova_v6_en2;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    private static Retrofit retrofit = null;

    private RetrofitInstance() {}

    public static Retrofit getRetrofitInstance(String URL) {
        //if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        //}
        return retrofit;
    }
}
