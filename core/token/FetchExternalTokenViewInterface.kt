package com.example.token.core

/**
 * トークン取得時のView層の振る舞いを定義するインターフェース。
 * テナント側で必要に応じてオーバーライドして独自処理を追加できる。
 */
interface FetchExternalTokenViewInterface {

    /**
     * トークン取得要否の判断。
     * `false` の場合は何もせず完了通知する。
     */
    val shouldFetchToken: Boolean

    /**
     * リトライダイアログ表示。
     * リトライ可能なエラー発生時に呼ばれる。
     *
     * ダイアログには「再試行」「閉じる」ボタンを表示する。
     * - 「再試行」ボタン押下時: [retry] を呼び出す
     * - 「閉じる」ボタン押下時: [onDismiss] を呼び出す
     *
     * @param retry 「再試行」ボタン押下時に呼び出すアクション
     */
    fun showTokenFetchFailedAlert(retry: () -> Unit)

    /**
     * 強制ログアウトダイアログ表示。
     * 認証エラー発生時に呼ばれる。
     * デフォルトは空実装。テナント側でオーバーライドして独自処理を追加できる。
     *
     * @param message エラーメッセージ
     */
    fun showRestartApplicationAlert(message: String?) {
        // デフォルトは何もしない
    }

    /**
     * リトライダイアログで「閉じる」を選択した時に呼ばれる。
     * デフォルトは空実装。テナント側でオーバーライドして独自処理を追加できる。
     */
    fun onDismiss() {
        // デフォルトは何もしない
    }

    /**
     * リトライ上限に達した時、または [TokenFetchRetryPolicy.shouldRetry] が `false` を返した時に呼ばれる。
     * デフォルトは空実装。テナント側でオーバーライドして独自処理を追加できる。
     *
     * @param error リトライ不可と判断されたエラー
     */
    fun onRetryExhausted(error: AppError) {
        // デフォルトは何もしない
    }
}
