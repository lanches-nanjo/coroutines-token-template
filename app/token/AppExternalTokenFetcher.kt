package com.example.token.app

import com.example.token.core.AppError
import com.example.token.core.ExternalToken
import com.example.token.core.ExternalTokenFetcher
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [ExternalTokenFetcher] の実装。
 * [AppExternalTokensApi] を使用してトークン取得APIを呼び出し、
 * レスポンスを `List<ExternalToken>` に変換して返す。
 *
 * API呼び出しは [Dispatchers.IO] 上で実行される。
 *
 * ## RxJava2 との橋渡しについて
 * [AppExternalTokensApi.get] は RxJava2 の `Single` を返すが、
 * `withContext(Dispatchers.IO)` の中で `blockingGet()` を呼び出すことで
 * Coroutines と橋渡ししている。
 * メインスレッドはブロックしないため、この使い方は問題ない。
 */
class AppExternalTokenFetcher : ExternalTokenFetcher {

    override suspend fun fetch(): List<ExternalToken> = withContext(Dispatchers.IO) {
        val api = ServiceFactory.createServiceFor(AppExternalTokensApi::class.java)
        try {
            val response = api.get().blockingGet()
            val content = response.content
                ?: throw IllegalStateException("response content is null")
            content.tokens?.map { it.toExternalToken() } ?: emptyList()
        } catch (e: CancellationException) {
            throw e  // Coroutineのキャンセルは必ず再スロー
        } catch (e: Throwable) {
            throw e as? AppError ?: e.toAppError()
        }
    }
}
