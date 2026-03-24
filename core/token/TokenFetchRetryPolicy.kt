package com.example.token.core

/**
 * トークン取得失敗時のリトライポリシーを定義するデータクラス。
 * テナントごとにリトライ回数やリトライ対象エラーの判定をカスタマイズできる。
 *
 * @property maxRetryCount 最大リトライ回数。この回数を超えた場合はリトライせず終了する。
 * @property shouldRetry エラー内容に基づきリトライ可否を判定するラムダ。
 *                       デフォルトは全エラーでリトライ可。
 *                       このラムダが `false` を返した場合、またはリトライ回数が上限に達した場合は
 *                       [ErrorHandler.handleError] による標準エラーハンドリングに委譲される。
 */
data class TokenFetchRetryPolicy(
    val maxRetryCount: Int = DEFAULT_MAX_RETRY_COUNT,
    val shouldRetry: (AppError) -> Boolean = { true }
) {
    companion object {
        /** デフォルトの最大リトライ回数 */
        const val DEFAULT_MAX_RETRY_COUNT = 3
    }
}
