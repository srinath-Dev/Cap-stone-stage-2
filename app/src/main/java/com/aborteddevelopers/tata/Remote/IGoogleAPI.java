package com.aborteddevelopers.tata.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleAPI {

    @GET
    Call<String> getPatch(@Url String url);
}
