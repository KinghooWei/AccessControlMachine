package com.xwlab.attendance.logic.network;

import com.xwlab.attendance.logic.model.Person;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AttendanceService {

    @Headers({"Connection:Keep-Alive", "Charset:UTF-8", "Content-Type:application/json; charset=UTF-8", "accept:application/json"})
    @POST("door/api")
    public Call<List<Person>> getChangedPersonInfos(@Body RequestBody body);
}
