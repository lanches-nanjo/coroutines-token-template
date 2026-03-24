package com.example.token.core

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 外部トークン取得の汎用テンプレート（デフォルト実装）。
 *
 * [ErrorHandler] を継承しており、リトライ不可と判断されたエラーは
 * [ErrorHandler.handleError] に委譲して標準的なエラー処理
 * （強制ログアウト、メンテナンス、強制アップデート等）を自動的に行う。
 *
 * [TokenFetchRetryPolicy] によるリトライ制御はすべてのエラーに対して適用され、
 * リトライ不可と判断された場合に標準エラーハンドリングへ委譲する。
 *
 * API呼び出しは [ExternalTokenFetcher.fetch] (suspend fun) を介して行われ、
 * 全体のフローが Coroutines で完結する。
 */
interface FetchExternalTokenPresenter : ErrorHandler {

    /** View層インターフェース */
    val tokenViewInterface: FetchExternalTokenViewInterface

    /** トークン取得API呼び出し */
    val tokenFetcher: ExternalTokenFetcher

    /** リトライポリシー（デフォルト: 3回まで、全エラーリトライ） */
    val retryPolicy: TokenFetchRetryPolicy
        get() = TokenFetchRetryPolicy()

    /**
     * トークンの有効性を判定するバリデータ。
     * デフォルトは `null`（バリデーションなし = 常にAPI呼び出しを実行）。
     * 有効期限ベースの判定等を行いたい場合は、テナント側で [TokenValidator] を実装して設定する。
     */
    val tokenValidator: TokenValidator?
        get() = null

    /**
     * トークン取得を実行する。
     *
     * 以下の順序で取得要否を判断する:
     * 1. [FetchExternalTokenViewInterface.shouldFetchToken] が `false` の場合 → スキップ
     * 2. [tokenValidator] が設定されており、現在のトークンが有効な場合 → スキップ
     *
     * いずれかでスキップされた場合は何もせず [onComplete] を呼び出す。
     *
     * @param scope コルーチンスコープ
     * @param onComplete トークン取得完了（成功、または取得不要）時のコールバック
     */
    fun fetchTokenIfNeeded(scope: CoroutineScope, onComplete: () -> Unit) {
        if (!tokenViewInterface.shouldFetchToken) {
            onComplete()
            return
        }
        tokenValidator?.let { validator ->
            if (validator.isValid(PreferencesManager.instance.tokenSettings)) {
                onComplete()
                return
            }
        }
        executeFetch(scope, retryCount = 0, onComplete = onComplete)
    }

    private fun executeFetch(scope: CoroutineScope, retryCount: Int, onComplete: () -> Unit) {
        scope.launch {
            try {
                val tokens = tokenFetcher.fetch()
                PreferencesManager.instance.tokenSettings = tokens
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: CancellationException) {
                throw e  // Coroutineのキャンセルは必ず再スロー
            } catch (e: Throwable) {
                val error = e as? AppError ?: e.toAppError()
                withContext(Dispatchers.Main) {
                    handleFetchError(scope, error, retryCount, onComplete)
                }
            }
        }
    }

    private fun handleFetchError(
        scope: CoroutineScope,
        error: AppError,
        retryCount: Int,
        onComplete: () -> Unit
    ) {
        val policy = retryPolicy
        if (retryCount < policy.maxRetryCount && policy.shouldRetry(error)) {
            tokenViewInterface.showTokenFetchFailedAlert(
                retry = {
                    executeFetch(scope, retryCount + 1, onComplete)
                }
            )
        } else {
            handleError(error)
            tokenViewInterface.onRetryExhausted(error)
        }
    }
}
