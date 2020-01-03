package com.dan.lib.media;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.dan.lib.media.entity.PlayerBean;


/**
 * @Author : yzd
 * @Date : 2020/1/2 14:32
 * @Remart : 简易播放器
 */
public class SmartMediaPlayer extends RelativeLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, View.OnClickListener {

    private ImageView ivMusic;

    private Button btnPlay, btnPause, btnStop;

    private View view;

    //播放器
    private MediaPlayer mediaPlayer;
    //处理音视频焦点
    private AudioManager audioManager;
    //播放对象
    private PlayerBean playerBean;

    private ObjectAnimator animator;

    private ObjectAnimator animatorPole;

    private long animatorTime;

    private boolean isPause;

    private boolean isStop;

    public SmartMediaPlayer(Context context) {
        this(context, null);
    }

    public SmartMediaPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartMediaPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.media_smart_media_play_layout, this);
        ivMusic = findViewById(R.id.iv_music);
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        view = findViewById(R.id.v);
        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    /**
     * 设置播放对象,初始化player和Audio
     *
     * @param playerBean
     */
    public void setPlayerData(PlayerBean playerBean) {
        this.playerBean = playerBean;
        if (null != playerBean) {
            initPlayer();
            initAudio();
            Glide.with(getContext())
                    .load(playerBean.getBg())
                    .transform(new CircleCrop())
                    .into(ivMusic);
        }
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        if (null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            try {
                mediaPlayer.setDataSource(playerBean.getResources());
            } catch (Exception e) {
                e.printStackTrace();
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
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_play) {
            play();
        } else if (i == R.id.btn_pause) {
            pause();
        } else if (i == R.id.btn_stop) {
            stop();
        }
    }

    @SuppressLint("WrongConstant")
    private void play() {
        if (null == mediaPlayer)
            return;
        if (mediaPlayer.isPlaying())
            return;
        if (isPause) {
            mediaPlayer.start();
            isPause = false;
        } else if (isStop) {
            mediaPlayer.prepareAsync();
        } else {
            mediaPlayer.prepareAsync();
        }
    }

    private void pause() {
        if (isPause || isStop)
            return;

        if (null != mediaPlayer && mediaPlayer.isPlaying()) {
            isPause = true;
            mediaPlayer.pause();
        }
        movePole(false);
    }


    private void stop() {
        if (isStop)
            return;
        if (null != mediaPlayer) {
            isPause = false;
            isStop = true;
            mediaPlayer.stop();
            movePole(false);
        }
    }

    public void destroy() {
        if (null != mediaPlayer) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    /**
     * 准备就绪
     * status: Prepared
     *
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        isStop = false;
        movePole(true);
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


    /**
     * 移动摇杆
     *
     * @param open
     */
    @SuppressLint("WrongConstant")
    private void movePole(final boolean open) {
        animatorPole = ObjectAnimator.ofFloat(view, "rotation", open ? -30f : 0f).setDuration(500);
        animatorPole.setInterpolator(new LinearInterpolator());
        animatorPole.addListener(new SmartAnimtorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (open) {
                    startRotation();
                } else {
                    animatorTime = animator.getCurrentPlayTime();
                    animator.cancel();
                }
            }
        });
        animatorPole.start();
    }


    @SuppressLint("WrongConstant")
    private void startRotation() {
        if (null == animator) {
            animator = ObjectAnimator.ofFloat(ivMusic, "rotation", 360f)
                    .setDuration(3000);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.INFINITE);
            //设置动画差值器
            animator.setInterpolator(new LinearInterpolator());
        }
        animator.addListener(new SmartAnimtorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mediaPlayer.start();
            }
        });
        animator.start();
        if (animatorTime != 0)
            animator.setCurrentPlayTime(animatorTime);
    }


    private AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:// 获取Audio focus
                    if (null == mediaPlayer) {
                        mediaPlayer = new MediaPlayer();
                    } else if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
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


}
