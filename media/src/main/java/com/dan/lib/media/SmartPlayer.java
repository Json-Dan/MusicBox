package com.dan.lib.media;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.dan.lib.media.entity.PlayerBean;

import java.io.IOException;
import java.util.List;


/**
 * @Author : yzd
 * @Date : 2020/1/2 14:32
 * @Remart : 简易播放器
 */
public class SmartPlayer extends RelativeLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, Handler.Callback, View.OnClickListener {

    private ImageView ivBg;

    private ConstraintLayout clMusic;

    private ImageView ivMusic;

    private ImageView ivFastLeft, ivControllerPlay, ivFastRight;
    //播放器
    private MediaPlayer mediaPlayer;
    //处理音视频焦点
    private AudioManager audioManager;

    private List<PlayerBean> datas;

    private ObjectAnimator animator;

    private long animatorTime;

    private Status status = Status.NONE;

    private Handler mHandler;

    private int position;

    private static final int seekWhat = 0x00124;

    public SmartPlayer(Context context) {
        this(context, null);
    }

    public SmartPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.media_smart_media_play_layout, this);
        ivBg = findViewById(R.id.iv_bg);
        clMusic = findViewById(R.id.cl_music);
        ivMusic = findViewById(R.id.iv_music);
        ivFastLeft = findViewById(R.id.iv_fast_left);
        ivControllerPlay = findViewById(R.id.iv_controller_play);
        ivFastRight = findViewById(R.id.iv_fast_right);
        ivFastLeft.setOnClickListener(this);
        ivControllerPlay.setOnClickListener(this);
        ivFastRight.setOnClickListener(this);
        mHandler = new Handler(this);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_fast_left) {
            switchMusic(Action.UP);
        } else if (i == R.id.iv_fast_right) {
            switchMusic(Action.NEXT);
        } else if (i == R.id.iv_controller_play) {
            Log.e("tag", "播放器 next " + status);
            if (status == Status.PREPARE) {
                play();
            } else if (status == Status.PAUSE) {
                play();
            } else if (status == Status.PLAY) {
                pause();
            } else if (status == Status.STOP) {
                mediaPlayer.reset();
            } else {

                showToast("资源未准备好");
            }
            if (status != Status.STOP)
                updateUI();
        }
    }

    private void switchMusic(Action action) {
        if (null == datas || datas.size() == 0) {
            showToast("没有下一曲");
            return;
        }
        if (action == Action.NEXT) {
            if (datas.size() - 1 == position) {
                position = 0;
            } else {
                position++;
            }
        } else {
            if (position == 0 && datas.size() > 1) {
                position = datas.size() - 1;
            } else {
                position--;
            }
        }

        try {
            onCompletion(mediaPlayer);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(datas.get(position).getResources());
            mediaPlayer.prepareAsync();
            Glide.with(getContext())
                    .load(datas.get(position).getBg())
                    .transform(new CircleCrop())
                    .into(ivMusic);
            Glide.with(getContext())
                    .load(datas.get(position).getBg())
                    .into(ivBg);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 更新界面样式
     */
    private void updateUI() {
        switch (status) {
            case NONE:
                ivControllerPlay.setImageResource(R.drawable.ic_music_pause);
                break;
            case PREPARE:
                ivControllerPlay.setImageResource(R.drawable.ic_music_pause);
                break;
            case PLAY:
                ivControllerPlay.setImageResource(R.drawable.ic_music_play);
                break;
            case PAUSE:
                ivControllerPlay.setImageResource(R.drawable.ic_music_pause);
                break;
            case STOP:
                ivControllerPlay.setImageResource(R.drawable.ic_music_stop);
                break;
            case ERROR:
                ivControllerPlay.setImageResource(R.drawable.ic_music_pause);
                break;
        }
    }

    /**
     * 播放
     */
    private void play() {
        if (status == Status.PLAY)
            return;
        if (null != mediaPlayer) {
            status = Status.PLAY;
            startRotation();
        }
    }

    /**
     * 暂停
     */
    private void pause() {
        if (status == Status.PAUSE)
            return;
        if (null != mediaPlayer) {
            status = Status.PAUSE;
            mediaPlayer.pause();
            stopRotation();
        }
    }

    /**
     * 停止
     */
    private void stop() {
        if (status == Status.STOP)
            return;
        if (null != mediaPlayer) {
            status = Status.STOP;
            mediaPlayer.stop();
            stopRotation();
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (null != mediaPlayer) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public void setDatas(List<PlayerBean> playerBeans) {
        this.datas = playerBeans;
        if (null != playerBeans && playerBeans.size() > 0) {
            setPlayerData(0);
        }
    }

    /**
     * 设置播放对象,初始化player和Audio
     *
     * @param position
     */
    private void setPlayerData(int position) {
        if (null != datas.get(position)) {
            initPlayer();
            initAudio();

            Glide.with(getContext())
                    .load(datas.get(position).getBg())
                    .transform(new CircleCrop())
                    .into(ivMusic);
            Glide.with(getContext())
                    .load(datas.get(position).getBg())
                    .into(ivBg);
        }
    }


    /**
     * 初始化播放器
     */
    private void initPlayer() {
        if (null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            try {
                mediaPlayer.setDataSource(datas.get(position).getResources());
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "初始化播放器异常：" + e.getMessage());
            }
        }
    }

    /**
     * 初始化音频焦点
     */
    private void initAudio() {
        if (null == audioManager) {
            audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            //8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setOnAudioFocusChangeListener(focusChangeListener).build();
                focusRequest.acceptsDelayedFocusGain();
                audioManager.requestAudioFocus(focusRequest);
            } else {
                int result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case seekWhat:
                Log.e("TAG", "总时长：" + mediaPlayer.getDuration() + " 当前进度： " + mediaPlayer.getCurrentPosition() + "");
                mHandler.sendEmptyMessageDelayed(seekWhat, 1000);
                break;
        }
        return false;
    }


    /**
     * 准备就绪
     * status: Prepared
     *
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e("TAG", "播放器 已准备 " + status);
        /**
         * 刚打开播放器时
         */
        if (status == Status.NONE) {
            status = Status.PREPARE;
        } else {
            play();
        }
        updateUI();
    }

    /**
     * 播放完
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        status = Status.COMPLETION;
        resetRotation();
    }

    /**
     * 发生异常
     * status: error
     *
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        status = Status.ERROR;
        Log.e("TAG", "播放器 error");
        updateUI();
        Toast.makeText(getContext(), "what : " + what + " extra: " + extra, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * status: Started
     *
     * @param mp
     * @param percent
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    private void stopRotation() {
        animatorTime = animator.getCurrentPlayTime();
        animator.cancel();
    }

    @SuppressLint("WrongConstant")
    private void startRotation() {
        if (null == animator) {
            animator = ObjectAnimator.ofFloat(clMusic, "rotation", 360f)
                    .setDuration(10000);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.INFINITE);
            //设置动画差值器
            animator.setInterpolator(new LinearInterpolator());
        }
        animator.addListener(new SmartAnimtorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mediaPlayer.start();
                if (animatorTime == 0) {
                    mHandler.sendEmptyMessageDelayed(seekWhat, 1000);
                }
            }
        });
        animator.start();
        if (animatorTime != 0)
            animator.setCurrentPlayTime(animatorTime);
    }

    @SuppressLint("NewApi")
    private void resetRotation() {
        if (null == animator)
            return;
        animator.setCurrentFraction(0);
        animatorTime = 0;
        animator.cancel();
    }


    private AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:// 获取Audio focus
                    if (null == mediaPlayer) {
                        mediaPlayer = new MediaPlayer();
                    } else if (!mediaPlayer.isPlaying()) {
                        status = Status.PLAY;
                        startRotation();
                    }
                    //设置音量
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS://失去Audio focus 很长时间
                    stop();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://暂时失去audio faucs
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://暂时失去audio faucs,但允许播放
                    if (null != mediaPlayer && mediaPlayer.isPlaying())
                        mediaPlayer.setVolume(0.1f, 0.1f);
                    break;
            }
        }
    };

    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

}
