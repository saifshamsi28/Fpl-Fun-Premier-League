package com.zpl.handcricket.api;

import com.zpl.handcricket.BuildConfig;
import com.zpl.handcricket.utils.AppState;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static ApiService instance;

    public static synchronized ApiService get() {
        if (instance != null) return instance;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    String token = AppState.get().getToken();
                    okhttp3.Request.Builder b = chain.request().newBuilder();
                    if (token != null) b.header("Authorization", "Bearer " + token);
                    return chain.proceed(b.build());
                })
                .addInterceptor(logging)
                .build();
        Retrofit r = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL.endsWith("/") ? BuildConfig.BASE_URL : BuildConfig.BASE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        instance = r.create(ApiService.class);
        return instance;
    }
}
