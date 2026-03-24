package com.example.token.core

/**
 * 外部トークン取得APIを呼び出し、結果を [List<ExternalToken>] として返す汎用インターフェース。
 * テナントごとに具体的なAPI実装をラップして使用する。
 */
interface ExternalTokenFetcher {

    /**
     * 外部トークン取得APIを呼び出し、結果を返す。
     *
     * suspend 関数のため Coroutineコンテキストで呼び出すこと。
     * 実装側で適切なディスパッチャ（例: `withContext(Dispatchers.IO)`）を使用すること。
     *
     * @return 取得したトークンリスト
     * @throws Throwable API呼び出し失敗時
     */
    suspend fun fetch(): List<ExternalToken>
}
