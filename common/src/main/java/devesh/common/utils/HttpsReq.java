package devesh.common.utils;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import devesh.common.R;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpsReq {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    Context mContext;
    OkHttpClient client;

    public HttpsReq(Context context) {
        mContext = context;
        client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }


    public String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", mContext.getString(R.string.comm_app_name))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}


//  ExecutorService executorService = Executors.newFixedThreadPool(4);

    /*public void httpGet(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {


                } catch (Exception e) {

                }
            }
        });
    }*/
