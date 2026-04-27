package com.zpl.handcricket.api;

import com.zpl.handcricket.models.AuthResponse;
import com.zpl.handcricket.models.LeaderboardEntry;
import com.zpl.handcricket.models.MatchDetail;
import com.zpl.handcricket.models.MatchSummary;
import com.zpl.handcricket.models.PageResponse;
import com.zpl.handcricket.models.Room;
import com.zpl.handcricket.models.Team;
import com.zpl.handcricket.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/signup")
    Call<AuthResponse> signup(@Body Map<String, String> body);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body Map<String, String> body);

    @GET("api/me")
    Call<User> me();

    @PUT("api/me")
    Call<User> updateProfile(@Body Map<String, String> body);

    @GET("api/teams")
    Call<List<Team>> teams();

    @POST("api/teams/pick")
    Call<Map<String, Object>> pickTeam(@Body Map<String, Integer> body);

    @POST("api/rooms/create")
    Call<Room> createRoom();

    @POST("api/rooms/join")
    Call<Room> joinRoom(@Body Map<String, String> body);

    @GET("api/rooms/{code}")
    Call<Room> getRoomByCode(@Path("code") String code);

    /* --- Match history --- */

    // Last N recent matches for the logged-in user (used on Home)
    @GET("api/matches/recent")
    Call<List<MatchSummary>> recentMatches(@Query("limit") int limit);

    // Paginated list with search/filter/sort
    //   filter:  all | won | lost | ranked | friendly
    //   sort:    latest | oldest | highest
    @GET("api/matches")
    Call<PageResponse<MatchSummary>> matchHistory(
            @Query("page") int page,
            @Query("size") int size,
            @Query("q") String query,
            @Query("filter") String filter,
            @Query("sort") String sort);

    // Detail of a single match, including ball-by-ball
    @GET("api/matches/{id}")
    Call<MatchDetail> matchDetail(@Path("id") String id);

    @GET("api/leaderboard")
    Call<PageResponse<LeaderboardEntry>> leaderboard(
            @Query("page") int page,
            @Query("size") int size,
            @Query("period") String period);
}
