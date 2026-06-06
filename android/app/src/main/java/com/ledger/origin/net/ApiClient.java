package com.ledger.origin.net;

import com.ledger.origin.util.PrefManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Retrofit 客户端单例 */
public class ApiClient {

    // 模拟器访问宿主机用 10.0.2.2；真机请改成电脑局域网IP
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static ApiService service;

    public static ApiService get() {
        if (service == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        String token = PrefManager.getToken();
                        okhttp3.Request.Builder b = chain.request().newBuilder();
                        if (token != null && !token.isEmpty()) {
                            b.addHeader("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(b.build());
                    })
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(ApiService.class);
        }
        return service;
    }
}
