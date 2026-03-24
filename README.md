# coroutines-token-template

RxJava2が使われているAndroidプロジェクトにCoroutinesを導入した際のサンプルコードです。

詳細は記事を参照してください。

## ディレクトリ構成

```
core/token/
├── ExternalTokenFetcher.kt          # APIの呼び出し口（suspend fun）
├── TokenFetchRetryPolicy.kt         # リトライポリシー
├── TokenValidator.kt                # キャッシュ有効期限チェック
├── FetchExternalTokenPresenter.kt   # 全体のフロー制御
└── FetchExternalTokenViewInterface.kt  # View層インターフェース

app/token/
├── AppExternalTokensApi.kt          # Retrofit + RxJava2 APIインターフェース
└── AppExternalTokenFetcher.kt       # ExternalTokenFetcher の実装（blockingGet で橋渡し）

app/sample/
├── SampleTokenContract.kt           # MVP コントラクト
├── SampleTokenPresenter.kt          # カスタマイズ例付きPresenter実装
├── SampleTokenValidator.kt          # TTLベースのバリデータ実装
└── SampleTokenFragment.kt           # Fragment実装
```

## ポイント

- `core` 側は Coroutines で実装し、複雑なフロー制御（リトライ・エラーハンドリング・キャッシュ）を集約
- `app` 側（テナント）は `ExternalTokenFetcher` を実装するだけ。既存の RxJava2 スタイルのまま書ける
- `blockingGet()` + `withContext(Dispatchers.IO)` で RxJava2 → Coroutines を橋渡し
- `viewLifecycleOwner.lifecycleScope` を使うことで Fragment 破棄時に自動キャンセル
