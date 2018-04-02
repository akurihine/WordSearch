package com.arkady.wordsearch.data;

import retrofit2.Call;
import retrofit2.http.GET;


public interface WordSearchApi {
    @GET("duolingo-data/s3/js2/find_challenges.txt")
    Call<String> getWordSearch();
}
