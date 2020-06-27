package oboard.backear;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity implements Runnable {
    int frequency = 44100;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
    int plyBufSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
    AudioRecord audioRecord;
    AudioTrack audioTrack;
    Thread thread = null;
    float increase = 1;
    int increaseLate = 0;
    float stereoVolume = 0;
    int inputSource = 1;
    AudioManager audioManager;
    List<byte[]> recBufL = new ArrayList<byte[]>();
    List<Long> recBufT = new ArrayList<Long>();
    //    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            //if (msg != null)
//            //voiceLineView.setVolume(msg.what);//主线程更新分贝大小
//        }
//    };
    private boolean isAlive = true;

    public static void checkPermission(AppCompatActivity activity) {
        int checkPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.RECORD_AUDIO}, 123);
        }
    }

    //    //需要录音权限
//
//    @Override
//
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        voiceLineView = findViewById(R.id.voicLine);
//        start = findViewById(R.id.start);
//        checkPermission(this);
//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (thread == null) {
//
//                    thread = new Thread(MainActivity.this);//启动线程
//                    isAlive = true;
//                    thread.start();
//                    start.setText("停止");
//                } else {
//                    isAlive = false;//关闭线程
//                    thread = null;
//                    start.setText("开始");
//                }
//            }
//        });
//    }

    //检查当前系统是否已开启暗黑模式
    public static boolean getDarkModeStatus(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void run() {

        //录音对象
        audioRecord = new AudioRecord(inputSource, frequency, channelConfiguration, audioEncoding, recBufSize);
        //setOutSpeak();
        //声音播放对象
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, plyBufSize, AudioTrack.MODE_STREAM);
        audioRecord.startRecording();
        byte[] recBuf = new byte[recBufSize];
        recBufL.clear();
        recBufT.clear();
        //延迟增强
        long startT = System.currentTimeMillis();
        audioTrack.play();
        while (isAlive) {

            final int readLen = audioRecord.read(recBuf, 0, recBufSize);//获取录音缓存值
            for (int i = 0; i < recBuf.length; i++) recBuf[i] *= increase;
            recBufL.add(recBuf.clone());
            recBufT.add(System.currentTimeMillis() + increaseLate * 1000);
//            if (System.currentTimeMillis() - increaseLate * 1000 > startT) {
            if (!recBufL.isEmpty()) {
                if (System.currentTimeMillis() > recBufT.get(0)) {
                    recBufL.remove(0);
                    recBufT.remove(0);
                    byte[] re = recBufL.get(0).clone();
                    audioTrack.write(re, 0, re.length);//将获取到录音数据向声音播放对象写入
//                    }
                }
            }
            //计算声音分贝大小
            //long v = 0;
            // 将 buffer 内容取出，进行平方和运算
            //for (byte b : recBuf) {
            //    v += b * b;
            //}
            // 平方和除以数据总长度，得到音量大小。
            //double mean = v / (double) readLen;
            //double volume = 10 * Math.log10(mean);
            //Message msg = handler.obtainMessage();
            //msg.what = (int) volume;
            //handler.sendMessage(msg);//发送到主线程更新分贝显示
        }
        audioTrack.stop();//停止播放
        audioRecord.stop();//停止录音

    }

    @Override
    protected void onDestroy() {
        isAlive = false;//退出时停止麦克风功能
        thread = null;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getDarkModeStatus(this)) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        checkPermission(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getDarkModeStatus(this)) {
            toolbar.setPopupTheme(R.style.AppThemeDark_PopupOverlay);
        } else {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
        }

        final PlayButton fab = (PlayButton) findViewById(R.id.fab);
        fab.setOnPlayOrPauseClick(new PlayButton.OnPlayOrPauseClick() {
            @Override
            public void onClick(boolean isPlay) {
                if (thread == null) {
                    thread = new Thread(ScrollingActivity.this);//启动线程
                    isAlive = true;
                    thread.start();
                } else {
                    isAlive = false;//关闭线程
                    thread = null;
                }
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                //String[] channels = getResources().getStringArray(R.array.channels);
                channelConfiguration = pos + 2;
                fab.toPause();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                //String[] channels = getResources().getStringArray(R.array.channels);
                inputSource = pos;
                fab.toPause();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                //String[] channels = getResources().getStringArray(R.array.channels);
                switch (pos) {
                    //切换到外放
//注意此处，蓝牙未断开时使用MODE_IN_COMMUNICATION而不是MODE_NORMAL
                    case 0:
                        audioManager.setMode(audioManager.isBluetoothScoOn() ? AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL);
                        audioManager.stopBluetoothSco();
                        audioManager.setBluetoothScoOn(false);
                        audioManager.setSpeakerphoneOn(true);
                        break;
/*
  切换到蓝牙音箱*/
                    case 1:
                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        audioManager.startBluetoothSco();
                        audioManager.setBluetoothScoOn(true);
                        audioManager.setSpeakerphoneOn(false);
                        break;
//注意：以下两个方法还未验证
                    case 2:
                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        audioManager.stopBluetoothSco();
                        audioManager.setBluetoothScoOn(false);
                        audioManager.setSpeakerphoneOn(false);
                        break;
/*
  切换到听筒
 */
                    case 3:
                        audioManager.setSpeakerphoneOn(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        } else {
                            audioManager.setMode(AudioManager.MODE_IN_CALL);
                        }
                        break;
                }
                fab.toPause();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView textView = (TextView) findViewById(R.id.textView);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView.setText(" " + i + "x");
                increase = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        final TextView textView2 = (TextView) findViewById(R.id.textView2);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView2.setText(String.valueOf(Float.parseFloat(String.valueOf(i - 5)) / 5));
                stereoVolume = Float.valueOf(String.valueOf(Integer.valueOf(i).toString())) / 10;
                if (audioTrack != null)
                    audioTrack.setStereoVolume(1 - stereoVolume, stereoVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar seekBar3 = (SeekBar) findViewById(R.id.seekBar3);
        final TextView textView3 = (TextView) findViewById(R.id.textView3);
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView3.setText(" " + i + "秒");
                increaseLate = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void juanzeng(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String str = "";
        try {
            str = "alipays://platformapi/startapp?saId=10000007&qrcode=" + URLEncoder.encode("https://qr.alipay.com/tsx00700h1zpzwoodysyhda", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//str是支付宝或微信链接，直接在微信里面点击链接，也可调起

        Uri uri = Uri.parse(str);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        startActivity(intent);
    }

}
