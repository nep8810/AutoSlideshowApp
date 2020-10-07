package com.example.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ID：各Buttonに「btn」を割り当て、setOnClickListenerを設定
        val btn1 = this.findViewById<Button>(R.id.Button1);btn1.setOnClickListener(this)
        val btn2 = this.findViewById<Button>(R.id.Button2);btn2.setOnClickListener(this)
        val btn3 = this.findViewById<Button>(R.id.Button3);btn3.setOnClickListener(this)
        val btn4 = this.findViewById<Button>(R.id.Button4);btn4.setOnClickListener(this)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // checkSelfPermissionメソッドでパーミッションの許可状態を確認する
            //許可が与えられていればPackageManager.PERMISSION_GRANTEDが返ってくる
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {

                // 許可されていないので、requestPermissionsメソッドを使って許可ダイアログを表示する
                requestPermissions(
                    //第1引数には許可を求めたいパーミッションを配列で与える
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    //第2引数は結果を受け取る際に識別するための数値を与える
                    //今回は private val PERMISSIONS_REQUEST_CODE = 100 だが、0でも10でも問題なし
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    //ユーザーの選択結果を受ける取るためにonRequestPermissionsResultメソッドをoverride
    override fun onRequestPermissionsResult(

        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        //引数のrequestCodeはrequestPermissionsメソッドで与えた値が渡ってくる
        //when文でrequestCodeがPERMISSIONS_REQUEST_CODE定数と一致しているか判断
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    var cursor: Cursor? = null //cursorをメンバ変数として定義し、nullで初期化

    //getContentsInfoメソッドでcontentResolver(ContentProviderのデータを参照するためのクラス)を使って端末内の画像の情報を取得
    private fun getContentsInfo() {

        //contentResolverクラスのqueryメソッドを使って条件を指定して検索し、情報を取得
        var resolver = contentResolver
        cursor = resolver.query(  //resolverからcursorを取得


            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 外部ストレージの画像を指定
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )


        //moveToFirstメソッドでカーソルを最初に移動する
        if (cursor!!.moveToFirst()) {

            // cursor.getColumnIndexメソッドで現在カーソルが指しているデータの中から画像のIDがセットされている位置を取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            //cursor.getLongメソッドで画像のIDを取得する
            val id = cursor!!.getLong(fieldIndex)
            // ContentUris.withAppendedIdメソッドでそこから実際の画像のURIを取得する
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            //imageViewのsetImageURIメソッドでURIが指している画像ファイルをImageViewに表示させる
            imageView.setImageURI(imageUri)
        }
    }

    private var mTimer: Timer? = null
    // タイマー用の時間のための変数
    private var mTimerSec = 0.0
    private var mHandler = Handler()

    override fun onClick(v: View) {

        when(v.id) {

            R.id.Button1 ->

                if (mTimer == null) {
                        // タイマーの作成
                        mTimer = Timer()

                        // mTimer.schedule()でタイマー始動,アプリが終了するまでrun()内のコードを実行
                        mTimer!!.schedule(object : TimerTask() {  //サブスレッド開始

                            override fun run() {

                                mTimerSec += 0.1

                                if (cursor!!.moveToNext()) {

                                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                                val id = cursor!!.getLong(fieldIndex)
                                val imageUri =
                                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                                //mHndlerはHandlerクラスのインスタンスで、スレッドを超えて依頼するために使用する(今回はサブ→メイン)
                                //mHandler.post()内の処理はUI描画なので、メインスレッドに依頼する必要あり
                                mHandler.post {
                                    timer.text = String.format("%.1f", mTimerSec)
                                    imageView.setImageURI(imageUri) //setImageURIメソッドで画像ファイルをImageViewに表示させるのはUI描画
                                }

                            }else {
                                    cursor!!.moveToFirst()

                                    val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                                    val id = cursor!!.getLong(fieldIndex)
                                    val imageUri =
                                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                                    mHandler.post {
                                        timer.text = String.format("%.1f", mTimerSec)
                                        imageView.setImageURI(imageUri)
                                    }
                                }
                            }
                        }, 2000, 2000) // 最初に始動させるまで 2000ミリ秒、ループの間隔を 2000ミリ秒 に設定　//サブスレッド終了

                }


            R.id.Button3 ->

                //moveToNextメソッドでカーソルを次の画像に移動する(カーソルが1~(n-1)にある場合)
            if (cursor!!.moveToNext()) {

                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageView.setImageURI(imageUri)


            }else {
                //nから次の画像に移動できなかった場合moveToFirstメソッドで1に戻す
                cursor!!.moveToFirst()

                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageView.setImageURI(imageUri)
            }


            R.id.Button2 ->

                //moveToPreviousメソッドでカーソルを前の画像に移動する(カーソルが2~nにある場合)
                if (cursor!!.moveToPrevious()) {

                    val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = cursor!!.getLong(fieldIndex)
                    val imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    imageView.setImageURI(imageUri)

                }else {
                    //1から前の画像に移動できなかった場合moveToLastメソッドでnに戻す
                    cursor!!.moveToLast()

                    val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = cursor!!.getLong(fieldIndex)
                    val imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    imageView.setImageURI(imageUri)
                }

            R.id.Button4 ->

                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                }


        }

    }


    override fun onStop() {
        super.onStop()
        //カーソルを使い終えたためcloseメソッドを呼び出す
        cursor!!.close()
    }
}

