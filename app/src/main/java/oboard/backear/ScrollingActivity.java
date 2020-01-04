package oboard.backear;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ScrollingActivity extends AppCompatActivity implements Runnable {
    int frequency = 44100;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
    int plyBufSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
    AudioRecord audioRecord;
    AudioTrack audioTrack;
    Thread thread = null;
    private boolean isAlive = true;

//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            //if (msg != null)
//            //voiceLineView.setVolume(msg.what);//主线程更新分贝大小
//        }
//    };

    public static void checkPermission(AppCompatActivity activity) {
        int checkPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.RECORD_AUDIO}, 123);
        }
    }

    //想做一个耳机插入时手机喇叭也能外发功能，这个方法没什么用有待改进
    //检查当前系统是否已开启暗黑模式
    public static boolean getDarkModeStatus(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
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
    private void setOutSpeak() {
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        //audioManager.setMicrophoneMute(false);
        //audioManager.setSpeakerphoneOn(true);//使用扬声器外放，即使已经插入耳机
        setVolumeControlStream(AudioManager.STREAM_MUSIC);//控制声音的大小
        assert audioManager != null;
        audioManager.setMode(AudioManager.MODE_NORMAL);
    }

    @Override
    public void run() {

        //录音对象
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);

        setOutSpeak();
        //声音播放对象
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, plyBufSize, AudioTrack.MODE_STREAM);
        audioRecord.startRecording();
        byte[] recBuf = new byte[recBufSize];
        audioRecord.startRecording();
        audioTrack.play();
        while (isAlive) {
            int readLen = audioRecord.read(recBuf, 0, recBufSize);//获取录音缓存值
            audioTrack.write(recBuf, 0, readLen);//将获取到录音数据向声音播放对象写入
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

        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

}
