package be.yarin.vidapp

import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.SeekBar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import be.yarin.vidapp.BuildConfig.YOUTUBE_API_KEY
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView


class VidActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {
    private var youTubeView: YouTubePlayerView? = null
    private  var seekBar: SeekBar?= null
    private  var youTubePlayer: YouTubePlayer?= null
    private  var mainHandler: Handler?= null
    private  var runnable: Runnable?= null
    private var totalLength: Int? = null

    private var vidId: String? = null


    override fun onResume() {
        super.onResume()

        youTubeView = (findViewById<View>(R.id.youtubePlayer) as YouTubePlayerView?)
        mainHandler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                seekBar?.progress?.plus(1)?.let { seekBar?.setProgress(it, false) }
                mainHandler?.postDelayed(this, (0.01 * totalLength!!).toLong())
            }
        }
        seekBar = findViewById(R.id.seekBar)
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                var seekVal: Double = p1 * 0.01
                if (p2) {
                    youTubePlayer?.seekToMillis((seekVal * totalLength!!).toInt())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                mainHandler?.removeCallbacks(runnable!!)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        youTubeView?.initialize(YOUTUBE_API_KEY, this)

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vid)
        hideSystemBars()
        val extras = intent.extras
        if (extras != null) {
            vidId = extras.getString("vidid")
        }

        Handler().postDelayed(this::onSkip, 5000)


    }

    fun onSkip(){
        val skipButton = findViewById<View>(R.id.btn_skip)
        skipButton.visibility = VISIBLE
        skipButton.setOnClickListener{
            skipButton.visibility = INVISIBLE
            seekBar?.visibility = VISIBLE
            seekBar?.progress = 0
            youTubePlayer?.loadVideo(vidId)
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer?,
        wasRestored: Boolean
    ) {
        if (player != null) {
            youTubePlayer = player
        }
        youTubePlayer?.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)



        if (!wasRestored) {
            youTubePlayer?.setPlaybackEventListener(object : YouTubePlayer.PlaybackEventListener {
                override fun onPlaying() {
                    if (!mainHandler?.hasCallbacks(runnable!!)!!) {
                        mainHandler?.post(runnable!!)
                    }
                }

                override fun onPaused() {
                    mainHandler?.removeCallbacks(runnable!!)
                }

                override fun onStopped() {
                    mainHandler?.removeCallbacks(runnable!!)
                }

                override fun onBuffering(b: Boolean) {
                    if (!b) {

                        totalLength = player?.durationMillis
                        mainHandler?.post(runnable!!)
                    }

                }

                override fun onSeekTo(i: Int) {
                    totalLength = player?.durationMillis
                }
            })

            youTubePlayer?.loadVideo("MOMeNHaN-x4")


//
        }

    }

    override fun onInitializationFailure(
        arg0: YouTubePlayer.Provider?,
        arg1: YouTubeInitializationResult?
    ) {
    }

}