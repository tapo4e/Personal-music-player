package com.example.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MediaService : Service() {
    private  var listData:ArrayList<String> = java.util.ArrayList()
    private var listNames:ArrayList<String> = java.util.ArrayList()
    private lateinit var runnable: Runnable
    private var handler = Handler()
    private var position:Int = 0
    private val intent = Intent("playStatus")
    private val mediaPlayer:MediaPlayer=MediaPlayer()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.START.toString()-> start(intent.getIntExtra("dataSong",0))
            Actions.PAUSE.toString()->pause()
            Actions.PLAY.toString()->play()
            Actions.POSITION.toString()-> position(intent.getIntExtra("positionSong",0))
            Actions.NEXTTRACK.toString()->nextTrack()
            Actions.PREVTRACK.toString()->prevTrack()
            Actions.DATALIST.toString()-> {
                intent.getStringArrayListExtra("listDataSong")
                    ?.let {
                        listData.addAll(it)
                    }
                intent.getStringArrayListExtra("NameListSong")
                    ?.let {
                        listNames.addAll(it)
                    }
            }
            }
        mediaPlayer.setOnCompletionListener {
            start(++position)
        }
        trackPos()
        return  START_STICKY
    }

    private  fun start(dataSong:Int){
        mediaPlayer.stop()
        mediaPlayer.reset()
        position=dataSong
        mediaPlayer.setDataSource(listData[dataSong])
        println(listData[dataSong])
        mediaPlayer.prepare()
        mediaPlayer.start()
        intent.putExtra("trackName",listNames[position])
        intent.putExtra("play_stop", mediaPlayer.isPlaying.toString())
        intent.putExtra("duration", (mediaPlayer.duration).toString())
        intent.putExtra("currentPosition", mediaPlayer.currentPosition.toString())
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)


    }

    private fun pause(){
            mediaPlayer.pause()
        intent.putExtra("play_stop", mediaPlayer.isPlaying.toString())
        LocalBroadcastManager.getInstance(this@MediaService).sendBroadcast(intent)
        }
        private fun play(){
            mediaPlayer.start()
            intent.putExtra("play_stop", mediaPlayer.isPlaying.toString())
            LocalBroadcastManager.getInstance(this@MediaService).sendBroadcast(intent)
    }

    private fun position(position: Int){
        mediaPlayer.seekTo(position*1000)
        intent.putExtra("currentPosition", mediaPlayer.currentPosition.toString())
        LocalBroadcastManager.getInstance(this@MediaService).sendBroadcast(intent)
    }
    private fun nextTrack(){
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(listData[++position])
            mediaPlayer.prepare()
            mediaPlayer.start()
        intent.putExtra("currentPosition", mediaPlayer.currentPosition.toString())
        intent.putExtra("trackName",listNames[position])
        intent.putExtra("duration", (mediaPlayer.duration).toString())
        LocalBroadcastManager.getInstance(this@MediaService).sendBroadcast(intent)
    }
    private fun prevTrack(){
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(listData[--position])
            mediaPlayer.prepare()
            mediaPlayer.start()
        intent.putExtra("currentPosition", mediaPlayer.currentPosition.toString())
        intent.putExtra("trackName",listNames[position])
        intent.putExtra("duration", (mediaPlayer.duration).toString())
        LocalBroadcastManager.getInstance(this@MediaService).sendBroadcast(intent)
    }
    enum class Actions {
        START,PLAY,PAUSE,NEXTTRACK,POSITION,PREVTRACK,DATALIST,NAMELIST
    }
    private fun trackPos() {
            runnable= Runnable {
                if(mediaPlayer.isPlaying) {
                    intent.putExtra("currentPosition", mediaPlayer.currentPosition.toString())
                    intent.putExtra("trackName",listNames[position])
                    LocalBroadcastManager.getInstance(this@MediaService).sendBroadcast(intent)
                    handler.postDelayed(runnable, 1000)
                }
            }
            handler.post(runnable)
        }
    }


