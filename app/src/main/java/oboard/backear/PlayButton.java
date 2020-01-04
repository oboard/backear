package oboard.backear;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * Created by will on 2018/8/4.
 */

@SuppressLint("AppCompatCustomView")
public class PlayButton extends FloatingActionButton {

    //当前播放状态 true正在播放  false暂停播放
    private boolean isPlay = false;

    //提供给使用者的点击监听
    private OnPlayOrPauseClick onPlayOrPauseClick;

    public PlayButton(Context context) {
        super(context);
    }

    public PlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PlayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView() {
        setScaleType(ScaleType.FIT_CENTER);
        //初始化图像，将选择器配置进去
        setImageResource(R.drawable.animated_selector_play_and_pause);
        //设置点击事件
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                changeState();

            }
        });
    }

    //根据当前状态进行切换
    public void changeState() {
        if (!isFastClick()) {
            isPlay = !isPlay;
            final int[] state = {android.R.attr.state_checked * (isPlay ? 1 : -1)};
            setImageState(state, true);
            if (null != onPlayOrPauseClick) {
                onPlayOrPauseClick.onClick(!isPlay);
            }
        }
    }

    //播放
    public void toPlay() {
        if (isPlay) {
            return;
        } else {
            changeState();
        }
    }

    //暂停
    public void toPause() {
        if (!isPlay) {
            return;
        } else {
            changeState();
        }
    }

    private long lastClickTime;

    //防止快速点击
    public boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= 500) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return false;//flag;
    }

    //获取当前播放状态
    public boolean isPlay() {
        return isPlay;
    }


    public void setOnPlayOrPauseClick(OnPlayOrPauseClick onPlayOrPauseClick) {
        this.onPlayOrPauseClick = onPlayOrPauseClick;
    }

    public interface OnPlayOrPauseClick {
        void onClick(boolean isPlay);
    }
}
