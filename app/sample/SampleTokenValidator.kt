package com.example.token.app.sample

import com.example.token.core.ExternalToken
import com.example.token.core.TokenValidator

/**
 * トークンの有効期限を判定するバリデータ実装。
 * 最終取得時刻を SharedPreferences に保存し、
 * 指定された有効期間（[ttlMillis]）を超えた場合はトークンを無効と判定する。
 *
 * 使用例:
 * ```
 * // 30分間有効なバリデータ
 * val validator = SampleTokenValidator(ttlMillis = 30 * 60 * 1000L)
 * ```
 *
 * @property ttlMillis トークンの有効期間（ミリ秒）。デフォルトは30分。
 */
class SampleTokenValidator(
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS
) : TokenValidator {

    companion object {
        /** デフォルトの有効期間: 30分 */
        const val DEFAULT_TTL_MILLIS = 30 * 60 * 1000L

        private const val PREFS_KEY_TOKEN_FETCHED_AT = "external_token_fetched_at"
    }

    /**
     * 現在保持しているトークンが有効かどうかを判定する。
     *
     * 以下の条件をすべて満たす場合に有効（`true`）と判定する:
     * - [tokens] が null でなく、空でもない
     * - 最終取得時刻から [ttlMillis] 以内である
     */
    override fun isValid(tokens: List<ExternalToken>?): Boolean {
        if (tokens.isNullOrEmpty()) return false

        val fetchedAt = getFetchedAt()
        if (fetchedAt <= 0L) return false

        val elapsed = System.currentTimeMillis() - fetchedAt
        return elapsed < ttlMillis
    }

    /**
     * トークン取得成功時に現在時刻を記録する。
     * [FetchExternalTokenPresenter] でのトークン取得成功後に呼び出すこと。
     */
    fun recordFetchedAt() {
        PreferencesManager.instance.preferences
            .edit()
            .putLong(PREFS_KEY_TOKEN_FETCHED_AT, System.currentTimeMillis())
            .apply()
    }

    /**
     * 記録された最終取得時刻をクリアする。
     * ログアウト時等に呼び出す。
     */
    fun clearFetchedAt() {
        PreferencesManager.instance.preferences
            .edit()
            .remove(PREFS_KEY_TOKEN_FETCHED_AT)
            .apply()
    }

    private fun getFetchedAt(): Long {
        return PreferencesManager.instance.preferences
            .getLong(PREFS_KEY_TOKEN_FETCHED_AT, 0L)
    }
}
