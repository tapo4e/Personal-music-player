package com.example.player

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.marginBottom
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.Locale


class MainActivity : AppCompatActivity(),RecyclerViewInterface{
    private val trackInfo : ArrayList<String> = arrayListOf("0","0","0","trackName")
    private  var list:ArrayList<String> = ArrayList()
    private var listData:ArrayList<String> = ArrayList()
    private lateinit var recyclerView : RecyclerView
    private lateinit var play : ConstraintLayout
    private lateinit var playButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var trackName: TextView
    private  lateinit var searchView: androidx.appcompat.widget.SearchView
    private  lateinit var trackRecyclerViewAdapter:TrackRecyclerViewAdapter
    private lateinit  var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var  bottomSheet:View
    private lateinit var   layoutParams:ViewGroup.MarginLayoutParams
    @SuppressLint("Range", "MissingInflatedId", "CutPasteId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_tracks)
        searchView=findViewById(R.id.search)
        trackName=findViewById(R.id.trackName)
        seekBar=findViewById(R.id.seekBar)
        seekBar.isEnabled = false
        playButton=findViewById(R.id.playButton)
        play=findViewById(R.id.playBar)
        recyclerView=findViewById(R.id.recyclerView)
        layoutParams = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
        recyclerView.layoutManager = LinearLayoutManager(this)

        //Остановился тут доделать bottomsheet
        bottomSheet=findViewById(R.id.playBar)
        bottomSheetBehavior=BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state=BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isDraggable=false
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.READ_MEDIA_AUDIO,permission.FOREGROUND_SERVICE),1)
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor : Cursor? =contentResolver.query(uri,null,null,null)
        cursor?.use {
            while (cursor.moveToNext()) {
                val nameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val name= cursor.getString(nameColumn)
                listTracks(name)
                listData.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)))
            }
        }
         trackRecyclerViewAdapter= TrackRecyclerViewAdapter(this,list,this)
        recyclerView.adapter=trackRecyclerViewAdapter
        recyclerView.layoutManager= LinearLayoutManager(this)
        //Open buttomsheet
        play.setOnClickListener{
            val newTaskSheetFragment = NewTaskSheet.newInstance(trackInfo)
            newTaskSheetFragment.show(supportFragmentManager, "newTaskTag")
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("playStatus"))
        Intent(this, MediaService::class.java).also {
            it.action = MediaService.Actions.DATALIST.toString()
            it.putStringArrayListExtra("listDataSong", listData)
            it.putStringArrayListExtra("NameListSong",list)
            startService(it)
        }
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(nextText: String?): Boolean {
                filterList(nextText)
                return true
            }

        })

    }
    private fun filterList(query:String?){
        if(query!=null){
            val filteredList = ArrayList<String>()
            for(i in list){
                if(i.lowercase(Locale.ROOT).contains(query)){
                    filteredList.add(i)
                }
            }
            trackRecyclerViewAdapter.setFilterList(filteredList)
        }


    }
override fun onDestroy() {
    super.onDestroy()
    Intent(this, MediaService::class.java).also {
        stopService(it)
    }
}
    private fun listTracks(track:String){
        list.add(track)
    }
    fun buttonPlayClick(view : View){

        if(trackInfo[0]!="0") {
            println(trackInfo[0])
            if (!trackInfo[0].toBoolean()) {
                playButton.setImageResource(R.drawable.pause1)
                Intent(this, MediaService::class.java).also {
                    it.action = MediaService.Actions.PLAY.toString()
                    startService(it)
                }
            } else {
                playButton.setImageResource(R.drawable.play1)
                Intent(this, MediaService::class.java).also {
                    it.action = MediaService.Actions.PAUSE.toString()
                    startService(it)
                }
            }
        }
//
//
    }
    override fun onItemClick(position: Int) {
            Intent(this, MediaService::class.java).also {
                it.action = MediaService.Actions.START.toString()
                it.putExtra("dataSong", position)
                startService(it)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                layoutParams.bottomMargin=350

        }

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "playStatus") {
                trackInfo[0] = intent.getStringExtra("play_stop").toString()
                trackInfo[1]= intent.getStringExtra("duration").toString()
                trackInfo[2]=intent.getStringExtra("currentPosition").toString()
                trackInfo[3] = intent.getStringExtra("trackName").toString()
                if(trackInfo[0].toBoolean())
                    playButton.setImageResource(R.drawable.pause1)
                else
                    playButton.setImageResource(R.drawable.play1)
                seekBar.max=trackInfo[1].toInt()/1000
                seekBar.progress=trackInfo[2].toInt()/1000
                trackName.text=trackInfo[3]
            }
        }
    }
}


