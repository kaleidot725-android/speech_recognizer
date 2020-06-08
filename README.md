# 2020/06/08 ［Android］音声入力はわりと簡単に実装できる（ Speech Recognizer ）

# はじめに
Andorid で音声入力するのは Speech Recognizer を利用すればわりと簡単に実装できる。今回は Speech Recognizer の使い方を調べて、サンプルを実装してみたのでその解説をしてみたいと思います。

# 実装

## 1. 必要なパーミションを許可する

Speech Recognizer での音声入力では `android.permissionn.RECORD_AUDIO` を許可する必要があります。なので `android.permission.RECORD_AUDIO`を AndroidManifest.xml に追加しておきます。また Speech Recognizer ではオンラインでの音声入力とオフラインでの音声入力の両方に対応しています。オンラインで音声入力できるようにするには`android.permissions.INTERNET`を許可する必要があるのでこちらも追加しておきます。

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="jp.kaleidot725.sample">
         ︙
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
         ︙
</manifest>
```

`android.permissions.RECORD_AUDIO` ですがパーミッションレベルが dangerous なのでユーザーによるパーミッション許可が必要になります。MainActivity.kt に次のコードを追加して起動時にユーザーにパーミッションを許可してもらうようにしておきます。

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val granted = ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
        if (granted == PackageManager.PERMISSION_GRANTED) {
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), PERMISSIONS_RECORD_AUDIO)
    }
    
    companion object {
        private const val PERMISSIONS_RECORD_AUDIO = 1000
    }
}    
```

[![Image from Gyazo](https://i.gyazo.com/be17bf1debf1d9b27cd1dd37b57782eb.png)](https://gyazo.com/be17bf1debf1d9b27cd1dd37b57782eb)
## 2. SpeechRecognizer を操作するレイアウトを作成する

次のようなレイアウトを作成し SpeechRecognizer を操作できるようにします。音声入力の結果を表示する TextView、音声入力の開始と停止する Button を配置したシンプルなレイアウトを準備します。
　
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/recognize_text_view"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:gravity="center"
        android:text="Default"
        android:textSize="32sp"
        app:layout_constraintBottom_toTopOf="@id/recognize_start_button"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/recognize_start_button"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Start"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/recognize_stop_button"/>

    <Button
        android:id="@+id/recognize_stop_button"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Stop"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/recognize_start_button" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

[![Image from Gyazo](https://i.gyazo.com/ee9f8b6bb93935ea03293e95e7cf043c.png)](https://gyazo.com/ee9f8b6bb93935ea03293e95e7cf043c)

## 3. SpeechRecognizer をセットアップする

前準備が終わったのでさっそく SpeechRecogtnizer のセットアップします。

**SpeechRercognizer のインスタンスを生成する**

SpeechRecognizer のインスタンスは SpeechRercognizer の createSpeechRecognizer という static 関数にて生成します。 createSpeechRecognizer の引数には Context を渡してやるようになっているので、生成場所に応じた Context を渡してやります。

```kotlin
    private var speechRecognizer : SpeechRecognizer? = null

    // Activity のライフサイクルにあわせて SpeechRecognizer を生成する
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
           ︙
        // Activity での生成になるので、ApplicationContextを渡してやる
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
     }
}
```

**必要がなくなった SpeechRecognizer は破棄する**

必要がなくなった SpeechRecognizer は破棄しなければならないです。SpeechRecognizer の破棄は destory で行いますので、使い終わったら destory で破棄してやります。

```kotlin
    private var speechRecognizer : SpeechRecognizer? = null

    // Activity のライフサイクルにあわせて SpeechRecognizer を破棄する
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}
```

**RecognizerListnerを登録して、イベントや結果を受け取れるようにする**

SpeechRecognizer から音声入力の結果を受け取るには RecognizerListener を登録する必要があります。 RecognitionListner は次のようなインタフェースを持っていて音声入力の状態遷移やEventを検知できるようになっています。

| 名称 | 説明 |
| ------- | ------- |
| onBeginningOfSpeech | ユーザーが発話を始めたら呼び出される |
| onBufferReceived | 音声が受信できたら呼び出される |
| onEndOfSpeech | ユーザーが発話を終えたら呼び出される |
| onError | ネットワークエラー、音声入力に関するエラーが発生したら呼び出される |
| onEvent | 追加イベントを受信したら呼び出される |
| onPartialResults | 部分的な認識結果が利用可能なときに呼び出される |
| onReadyForSpeech | 準備が整いユーザーが発話してもよくなったら呼び出される |
| onResults | 音声入力が終わり、結果が準備できたら呼び出される |
| onRmsChanged | 音声のレベルが変化されたら、呼び出される |

今回は SpeechRecognizer がどのような挙動になるかも確認したいので、次のような何か状態が変化したら TextView にその内容を出力する RecognitionListener を作成し、SpeechRecognizer に登録しようと思います。

```kotlin
class MainActivity : AppCompatActivity() {
    private var speechRecognizer : SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
          ︙
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext) 
        speechRecognizer?.setRecognitionListener(createRecognitionListenerStringStream { recognize_text_view.text = it })
    }
    
    /** 公開関数で受け取った TextView の更新処理を各関数で呼び出す*/
    private fun createRecognitionListenerStringStream(onResult : (String)-> Unit) : RecognitionListener {
        return object : RecognitionListener {
            override fun onRmsChanged(rmsdB: Float) { /** 今回は特に利用しない */ }
            override fun onReadyForSpeech(params: Bundle) { onResult("onReadyForSpeech") }
            override fun onBufferReceived(buffer: ByteArray) { onResult("onBufferReceived") }
            override fun onPartialResults(partialResults: Bundle) { onResult("onPartialResults") }
            override fun onEvent(eventType: Int, params: Bundle) { onResult("onEvent") }
            override fun onBeginningOfSpeech() { onResult("onBeginningOfSpeech") }
            override fun onEndOfSpeech() { onResult("onEndOfSpeech") }
            override fun onError(error: Int) { onResult("onError") }
            override fun onResults(results: Bundle) {
                val stringArray = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                onResult("onResults " + stringArray.toString())
            }
        }
    }
}
```

**RecognizerListnerにて音声入力を開始・停止できるようにする**

RecognizerLisnter にて音声入力の開始と停止を操作ができるように各ボタンのクリック処理で呼び出されるように設定します。音声入力を開始は startListening で実行できるようになっています。引数には RecognizerIntent を指定するようになっていますが ACTION_RECOGNIZE_SPEECH の Intent を渡してやればOKです。また音声入力の停止は stopListnening で実行できるようになっています。引数に関しては特に指定できないのでそのまま呼び出すだけで良いです。

```kotlin
class MainActivity : AppCompatActivity() {
    private var speechRecognizer : SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
          ︙
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        speechRecognizer?.setRecognitionListener(createRecognitionListenerStringStream { recognize_text_view.text = it })
        
        // setOnClickListener でクリック動作を登録し、クリックで音声入力が開始するようにする
        recognize_start_button.setOnClickListener { speechRecognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)) }
        
        // setOnclickListner でクリック動作を登録し、クリックで音声入力が停止するようにする
        recognize_stop_button.setOnClickListener { speechRecognizer?.stopListening() }
    }
}
```

今回は RecognizerIntent　として ACTION_RECOGNIZE_SPEECH を選択しましたが、RecognizerIntentには 3つの種類があります。そのため場合によって使い分けが必要になります。

| 名称 | 説明 |
| ------- | ------- |
| ACTION_RECOGNIZE_SPEECH | SpeechRecognizerを通して、音声入力を開始する |
| ACTION_VOICE_SEARCH_HANDS_FREE | ユーザーのタッチ入力なしで、音声入力を開始する。 |
| ACTION_WEB_SEARCH | SpeechRecognizerを通して、音声入力を開始し、Web検索結果を表示する |

# おわりに

動作確認してみます。こんな感じで簡単に音声入力できます。

[![Image from Gyazo](https://i.gyazo.com/bbd3825f72d36d74ace3b2d7e5e60fc1.gif)](https://gyazo.com/bbd3825f72d36d74ace3b2d7e5e60fc1)
今回作成したサンプルはこちらにあります。

<a href="https://github.com/kaleidot725-android/speech_recognizer"><img src="https://github-link-card.s3.ap-northeast-1.amazonaws.com/kaleidot725-android/speech_recognizer.png" width="460px"></a>

# 参考文献

- [SpeechRecognizer | Android Developer](https://developer.android.com/reference/android/speech/SpeechRecognizer)
- [Recognizer Intent | Android Developer](https://developer.android.com/reference/android/speech/RecognizerIntent)