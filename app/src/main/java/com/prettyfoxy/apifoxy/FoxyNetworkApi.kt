package com.prettyfoxy.apifoxy

import com.prettyfoxy.datafoxy.FoxyInstallEntity
import com.prettyfoxy.datafoxy.PushEntity
import com.prettyfoxy.datafoxy.RequestPushToken
import com.prettyfoxy.datafoxy.UserFoxyEntity
import retrofit2.http.*

interface FoxyNetworkApi {
    @PATCH("api/installs/{user_id}/push-token")
    @Headers(
        "Content-Type: application/json",
        "X-AUTH-TOKEN: 1fafe63df11cdb3a40a52b4f265b087f68fb6ebc05f35ef042"
    )
    suspend fun installFoxyUseridToken(
        @Path("user_id") userId : String,
        @Body requestPushToken : RequestPushToken
    ): PushEntity


    @POST("api/installs")
    @Headers(
        "Content-Type: application/json",
        "X-AUTH-TOKEN: 1fafe63df11cdb3a40a52b4f265b087f68fb6ebc05f35ef042"
    )
    suspend fun setFoxyData(
        @Body installRequest: UserFoxyEntity
    ): FoxyInstallEntity

    @GET("api/installs/{user_id}")
    @Headers(
        "Content-Type: application/json",
        "X-AUTH-TOKEN: 1fafe63df11cdb3a40a52b4f265b087f68fb6ebc05f35ef042"
    )
    suspend fun installUser(
        @Path("user_id") userid : String
    ) : FoxyInstallEntity
}