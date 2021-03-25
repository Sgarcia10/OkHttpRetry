package com.example.demo;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class OkController {
    @RequestMapping("/")
    public String index() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new OkInterceptor())
                .build();
        Request request = new Request.Builder()
                .url("https://mock.codes/503")
                .header("User-Agent", "OkHttp Example")
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return e.toString();
        }
    }
}
