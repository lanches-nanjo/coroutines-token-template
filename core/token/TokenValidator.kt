package com.example.token.core

/**
 * 保持しているトークンの有効性を判定するインターフェース。
 * 有効期限ベースの判定や、特定条件でのトークン無効化など、
 * テナントごとに独自のバリデーションロジックを実装できる。
 *
 * [FetchExternalTokenPresenter.fetchTokenIfNeeded] では
 * [FetchExternalTokenViewInterface.shouldFetchToken] が `true` の場合に
 * さらに本インターフェースの [isValid] を確認し、
 * 有効なトークンが存在する場合はAPI呼び出しをスキップする。
 */
interface TokenValidator {

    /**
     * 現在保持しているトークンが有効かどうかを判定する。
     *
     * @param tokens 現在保存されているトークンリスト。`null` の場合は未取得。
     * @return 有効な場合 `true`（API呼び出しをスキップ）、無効または期限切れの場合 `false`（再取得が必要）
     */
    fun isValid(tokens: List<ExternalToken>?): Boolean
}
