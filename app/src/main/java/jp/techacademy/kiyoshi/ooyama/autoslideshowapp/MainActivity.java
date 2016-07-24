package jp.techacademy.kiyoshi.ooyama.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  {


    private static final int PERMISSIONS_REQUEST_CODE = 100;

    //イメージＩＤ格納
    long [] imageId;
    //取得イメージ最大数
    int imageIdCountMax;
    //現在表示イメージ
    int imageCountNow=-1;
    //自動再生フラグ 自動再生=True
    boolean mode=false;

    TextView textView;

    //タイマー
    Timer timer = null;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonInit();

        textView=(TextView)findViewById(R.id.textView);


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {
                    //Log.d("ANDROID","許可されなかった");
                    //許可されなかった場合の処理
                    new AlertDialog.Builder(this)
                            .setTitle("許可されなかったのでこのアプリは利用できません")
                            .setPositiveButton( "了解", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("ANDROID",String.valueOf(which));
                                    finish();
                                }
                            }).show();
                }

                break;
            default:
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getContentsInfo();

    }


    //ボタン処理
    private void buttonInit(){
        final Button buttonPrev=(Button)findViewById(R.id.buttonPrev);
        final Button buttonNext=(Button)findViewById(R.id.buttonNext);
        final Button buttonStartStop=(Button)findViewById(R.id.buttonStartStop);

        buttonPrev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Log.d("ANDROID","Prev");
                imageSet(-1);
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Log.d("ANDROID","Next");
                imageSet(1);
            }
        });

        buttonStartStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Log.d("ANDROID","StartStop");
                //ボタン制御（戻る・進む）
                if (mode){
                    buttonStartStop.setText("再生");
                    buttonNext.setEnabled(true);
                    buttonPrev.setEnabled(true);
                    mode=false;
                    timerMode(false);
                } else {
                    //自動再生開始
                    buttonStartStop.setText("停止");
                    buttonNext.setEnabled(false);
                    buttonPrev.setEnabled(false);
                    mode=true;
                    timerMode(true,2000);
                }


            }
        });

    }

    //タイマー
    private void timerMode(boolean m,int second){
        timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run(){
                handler.post(new Runnable(){
                    public void run(){
                        imageSet(1);
                    }
                });
            }
        }, 0, second);
    }

    private void timerMode(boolean m){
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    //イメージ表示
    private void imageSet(int cnt){
        //イメージ現在値　増減
        imageCountNow=imageCountNow+cnt;
        if (imageCountNow >= imageIdCountMax){
            imageCountNow=0;
        } else if (imageCountNow < 0){
            imageCountNow = imageIdCountMax-1;
        }

        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId[imageCountNow]);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);

        textView.setText(String.valueOf(imageCountNow+1) + " / " + String.valueOf(imageIdCountMax));
        Log.d("ANDROID",imageUri.toString());

    }

    //
    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        imageId=new long[cursor.getCount()];

        if (cursor.moveToFirst()) {
                imageIdCountMax=0;
                do {
                    // indexからIDを取得し、そのIDから画像のURIを取得する
                    int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    imageId[imageIdCountMax] = cursor.getLong(fieldIndex);
                    imageIdCountMax=imageIdCountMax+1;

                } while (cursor.moveToNext());
        }
        cursor.close();
        imageCountNow=0;
        imageSet(0);
        textView.setText(String.valueOf(imageCountNow+1) + " / " + String.valueOf(imageIdCountMax));
    }




}
