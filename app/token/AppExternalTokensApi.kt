package com.example.token.app

import io.reactivex.Single
import retrofit2.http.GET

/**
 * 外部トークン取得APIインターフェース。
 * Retrofit + RxJava2 の既存スタイルで定義する。
 * [AppExternalTokenFetcher] 内で Coroutines との橋渡しを行う。
 */
interface AppExternalTokensApi {

    @GET("v1/external_tokens")
    fun get(): Single<ApiResponse<ExternalTokensResponse>>
}
