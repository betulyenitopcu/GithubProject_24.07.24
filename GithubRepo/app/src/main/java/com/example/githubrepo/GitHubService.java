package com.example.githubrepo;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface GitHubService {
    @GET("users/{username}")
    Call<GitHubUser> getUser(@Path("username") String username, @Header("Authorization") String authHeader);

    @GET("users/{username}/repos")
    Call<List<GitHubRepo>> getRepos(@Path("username") String username, @Header("Authorization") String authHeader);
}