package com.example.chat_app.Listners;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiService {
    @POST("send")
    Call<String> sendMessage(
        @HeaderMap HashMap<String,String> stringStringHashMap,
                @Body String MessageBody
    );

    }


