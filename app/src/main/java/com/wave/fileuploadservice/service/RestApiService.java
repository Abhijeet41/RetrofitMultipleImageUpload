package com.wave.fileuploadservice.service;

import java.util.List;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created on : Feb 25, 2019
 * Author     : AndroidWave
 */
public interface RestApiService {


    @Multipart
    @POST("fileUpload.php")
    Single<ResponseBody> onFileUpload(@Part("email") RequestBody mEmail, @Part MultipartBody.Part file);

    @Multipart
    @POST("talent/image-upload")
    Single<ResponseBody> onFileUploadMultiple(@Part("userId") RequestBody userId, @Part List<MultipartBody.Part> file);

}
