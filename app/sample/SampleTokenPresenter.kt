package com.example.token.app.sample

import com.example.token.app.AppExternalTokenFetcher
import com.example.token.core.AppError
import com.example.token.core.ApiError
import com.example.token.core.ApiErrorCase
import com.example.token.core.ErrorReceiver
import com.example.token.core.ExternalTokenFetcher
import com.example.token.core.FetchExternalTokenPresenter
import com.example.token.core.FetchExternalTokenViewInterface
import com.example.token.core.NetworkError
import com.example.token.core.TokenFetchRetryPolicy
import com.example.token.core.TokenValidator
import kotlinx.coroutines.CoroutineScope

/**
 * トークン取得テンプレートを利用するPresenterのサンプル実装。
 *
 * [FetchExternalTokenPresenter] を実装し、
 * トークン取得・リトライ・有効期限チェックの一連のフローを提供する。
 *
 * ## エラーハンドリングのカスタマイズポイント:
 *
 * ### 1. リトライポリシーによる振り分け（[retryPolicy]）
 * [TokenFetchRetryPolicy.shouldRetry] を使って、エラー種別ごとにリトライ対象かどうかを制御する。
 * - `true` を返す → リトライダイアログ表示
 * - `false` を返す → リトライせず [handleError] → [FetchExternalTokenViewInterface.onRetryExhausted] へ
 *
 * ### 2. handleError のオーバーライド
 * [ErrorHandler.handleError] をオーバーライドして、
 * 標準エラーハンドリングの前にテナント固有のエラー処理を差し込む。
 * - 特定のAPIエラーに対して独自ダイアログを表示
 * - ハンドリング済みのエラーは `return` で標準処理をスキップ
 * - それ以外は `super.handleError()` に委譲
 *
 * ### 3. View層のコールバック
 * [FetchExternalTokenViewInterface.onRetryExhausted] / [FetchExternalTokenViewInterface.onDismiss] で
 * リトライ失敗後・キャンセル後の画面遷移やUI更新を行う。
 */
class SampleTokenPresenter(
    override val view: SampleTokenContract.View
) : SampleTokenContract.Presenter, FetchExternalTokenPresenter {

    // ========== FetchExternalTokenPresenter 実装 ==========

    override val tokenViewInterface: FetchExternalTokenViewInterface
        get() = view as FetchExternalTokenViewInterface

    override val tokenFetcher: ExternalTokenFetcher = AppExternalTokenFetcher()

    override val errorReceiver: ErrorReceiver
        get() = view as ErrorReceiver

    /**
     * リトライポリシーのカスタマイズ例。
     *
     * ネットワークエラーのみリトライし、APIエラー（認証エラー等）はリトライせず
     * 直接 [handleError] に委譲する。
     *
     * その他のカスタマイズ例:
     * - 全エラーリトライ（デフォルト）: TokenFetchRetryPolicy()
     * - 無制限リトライ: TokenFetchRetryPolicy(maxRetryCount = Int.MAX_VALUE)
     * - リトライなし: TokenFetchRetryPolicy(maxRetryCount = 0)
     */
    override val retryPolicy: TokenFetchRetryPolicy
        get() = TokenFetchRetryPolicy(
            shouldRetry = { error ->
                // ネットワークエラーのみリトライ対象とする例
                error is NetworkError
            }
        )

    /**
     * 有効期限チェックの有効化。
     * [SampleTokenValidator] を使用して、30分以内に取得済みのトークンは再取得をスキップする。
     */
    override val tokenValidator: TokenValidator
        get() = sampleTokenValidator

    private val sampleTokenValidator = SampleTokenValidator()

    // ========== エラーハンドリングのカスタマイズ ==========

    /**
     * [ErrorHandler.handleError] のオーバーライド。
     *
     * リトライ不可と判断されたエラーがここに到達する。
     * テナント固有のエラーに対して独自ダイアログを表示したい場合はここで分岐し、
     * 標準で処理すべきエラー（認証エラー、メンテナンス等）は super に委譲する。
     *
     * 呼び出し順序:
     * 1. FetchExternalTokenPresenter.handleFetchError()
     * 2. → TokenFetchRetryPolicy.shouldRetry == false or リトライ上限到達
     * 3. → この handleError() が呼ばれる
     * 4. → その後 FetchExternalTokenViewInterface.onRetryExhausted() が呼ばれる
     */
    override fun handleError(error: AppError) {
        when {
            // テナント固有: 認証エラーに対して独自ダイアログを表示する例
            error is ApiError && error.code == ApiErrorCase.UNAUTHORIZED -> {
                view.showTokenFetchErrorAlert(
                    title = "認証エラー",
                    message = "セッションが無効です。再ログインしてください。",
                    onAction = {
                        errorReceiver.showRestartApplicationAlert(error.message)
                    }
                )
            }

            // テナント固有: メンテナンスエラーに対して独自ダイアログを表示する例
            error is ApiError && error.code == ApiErrorCase.MAINTENANCE -> {
                view.showTokenFetchErrorAlert(
                    title = "メンテナンス中",
                    message = "ただいまメンテナンス中です。しばらくお待ちください。",
                    onAction = { /* アプリのホーム画面に戻す等 */ }
                )
            }

            // 上記以外: 標準エラーハンドリングに委譲
            else -> super.handleError(error)
        }
    }

    // ========== SampleTokenContract.Presenter 実装 ==========

    override fun onViewCreated() {
        // Fragment側から lifecycleScope を渡して fetchTokenIfNeeded() を呼び出す
    }

    /**
     * トークン取得を開始する。
     * Fragment の lifecycleScope を受け取り、取得フローを実行する。
     *
     * @param scope コルーチンスコープ（通常は Fragment の lifecycleScope）
     */
    fun startTokenFetch(scope: CoroutineScope) {
        fetchTokenIfNeeded(scope) {
            sampleTokenValidator.recordFetchedAt()
            view.onTokenReady()
        }
    }
}
