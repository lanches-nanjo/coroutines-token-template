package com.example.token.app.sample

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.token.core.AppError
import com.example.token.core.FetchExternalTokenViewInterface

/**
 * トークン取得テンプレートを利用するFragmentのサンプル実装。
 *
 * [FetchExternalTokenViewInterface] を実装することで、
 * トークン取得のUI層ハンドリングを提供する。
 *
 * ## テンプレートの使い方:
 * 1. Fragment で [FetchExternalTokenViewInterface] を実装する
 * 2. Presenter で [FetchExternalTokenPresenter] を実装する
 * 3. Fragment から `presenter.startTokenFetch(lifecycleScope)` を呼び出す
 *
 * ## エラーハンドリングのカスタマイズポイント:
 *
 * | カスタマイズ箇所 | 役割 | 実装場所 |
 * |---|---|---|
 * | [TokenFetchRetryPolicy.shouldRetry] | エラー種別ごとのリトライ可否判定 | Presenter |
 * | [ErrorHandler.handleError] | 標準エラーハンドリング前の独自処理 | Presenter |
 * | [showTokenFetchFailedAlert] | リトライダイアログの表示内容 | Fragment |
 * | [showTokenFetchErrorAlert] | テナント固有エラーダイアログの表示 | Fragment |
 * | [onRetryExhausted] | リトライ上限到達後の後処理 | Fragment |
 * | [onDismiss] | ユーザーキャンセル後の後処理 | Fragment |
 */
class SampleTokenFragment :
    Fragment(),
    SampleTokenContract.View,
    FetchExternalTokenViewInterface {

    private lateinit var presenter: SampleTokenPresenter

    // ========== FetchExternalTokenViewInterface 実装 ==========

    /**
     * トークン取得要否の判断。
     * テナントの要件に応じて判定ロジックを実装する。
     *
     * 例:
     * - ログイン済みユーザーのみ: AccountManager.isAuthorized
     * - 特定画面のみ: 固定値 true
     */
    override val shouldFetchToken: Boolean
        get() = true // サンプルでは常に取得対象

    /**
     * リトライダイアログを表示する。
     * 「再試行」ボタン押下時に [retry] を呼び出す。
     * 「閉じる」ボタン押下時に [onDismiss] を呼び出す。
     */
    override fun showTokenFetchFailedAlert(retry: () -> Unit) {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setMessage("通信に失敗しました。再試行しますか？")
            .setPositiveButton("再試行") { _, _ -> retry() }
            .setNegativeButton("閉じる") { _, _ -> onDismiss() }
            .setCancelable(false)
            .show()
    }

    /**
     * リトライダイアログで「閉じる」を選択した時の処理。
     * カスタマイズ例: 前画面に戻る、エラー状態のUIを表示する等
     */
    override fun onDismiss() {
        // findNavController().popBackStack()
    }

    /**
     * リトライ上限に達した時の処理。
     * [handleError] による標準エラーハンドリング実行後に呼ばれる。
     * カスタマイズ例: エラー画面を表示する、ホーム画面に戻す等
     */
    override fun onRetryExhausted(error: AppError) {
        // showErrorView()
    }

    // ========== SampleTokenContract.View 実装 ==========

    override fun onTokenReady() {
        // トークン取得完了 → WebView読み込み開始 / 次画面へ遷移する等
    }

    override fun showError(message: String) {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * テナント固有のエラーダイアログを表示する。
     * Presenterの [handleError] オーバーライドから呼び出される。
     */
    override fun showTokenFetchErrorAlert(title: String?, message: String, onAction: () -> Unit) {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .apply { title?.let { setTitle(it) } }
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> onAction() }
            .setCancelable(false)
            .show()
    }

    // ========== ライフサイクル ==========

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = SampleTokenPresenter(this)

        // viewLifecycleOwner.lifecycleScope を渡すことで
        // Fragment破棄時にCoroutineが自動キャンセルされる
        presenter.startTokenFetch(viewLifecycleOwner.lifecycleScope)
    }
}
