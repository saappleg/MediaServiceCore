package com.liskovsoft.youtubeapi.videoinfo.V2;

import com.liskovsoft.googlecommon.common.converters.gson.WithGson;
import com.liskovsoft.youtubeapi.videoinfo.models.VideoInfoHlsGson;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/** Lightweight typed endpoint for the optional HLS/storyboard supplement. */
@WithGson
public interface VideoInfoHlsApi {
    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/player")
    Call<VideoInfoHlsGson> getVideoInfoHls(
            @Body String videoQuery,
            @Header("X-Goog-Visitor-Id") String visitorId,
            @Header("User-Agent") String userAgent,
            @Header("X-Youtube-Client-Name") String clientName,
            @Header("X-Youtube-Client-Version") String clientVersion);
}
