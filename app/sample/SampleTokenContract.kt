package com.example.token.app.sample

import com.example.token.core.PresenterInterface
import com.example.token.core.ViewInterface

/**
 * トークン取得テンプレートのサンプル実装用MVPコントラクト。
 * テナント側で [FetchExternalTokenPresenter] を利用する際の実装例として参照すること。
 */
interface SampleTokenContract {

    /**
     * View層インターフェース。
     * Fragment が実装する。
     */
    interface View : ViewInterface<View, Presenter> {
        /** トークン取得完了後の画面遷移等を行う */
        fun onTokenReady()

        /** エラー時のUI更新 */
        fun showError(message: String)

        /**
         * テナント固有のエラーダイアログを表示する。
         * 標準の [ErrorHandler.handleError] では対応できない
         * 固有のエラーメッセージやアクションを提供したい場合に使用する。
         *
         * @param title ダイアログタイトル（nullの場合はタイトルなし）
         * @param message エラーメッセージ
         * @param onAction ダイアログのボタン押下時のアクション
         */
        fun showTokenFetchErrorAlert(title: String?, message: String, onAction: () -> Unit)
    }

    /**
     * Presenter層インターフェース。
     * [FetchExternalTokenPresenter] と併せて実装する。
     */
    interface Presenter : PresenterInterface<View, Presenter> {
        /** 画面初期化時に呼ばれる */
        fun onViewCreated()
    }
}
