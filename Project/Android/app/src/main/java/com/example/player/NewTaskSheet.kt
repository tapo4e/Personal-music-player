package com.example.player

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.player.databinding.FragmentNewTaskSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale
import java.util.concurrent.TimeUnit


class NewTaskSheet : BottomSheetDialogFragment() {
    private lateinit var receiver: BroadcastReceiver
    private var playStop = false
    var trackInfo = arrayListOf("false", "0", "0", "trackName")
    private lateinit var binding: FragmentNewTaskSheetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null && intent.action == "playStatus") {
                    trackInfo[0] = intent.getStringExtra("play_stop").toString()
                    trackInfo[1] = intent.getStringExtra("duration").toString()
                    trackInfo[2] = intent.getStringExtra("currentPosition").toString()
                    trackInfo[3] = intent.getStringExtra("trackName").toString()
                    updateUI()
                    println(trackInfo[2])
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewTaskSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.getStringArrayList("param_key")?.get(0)?.toBoolean() == true) {
            playStop = true
            println(trackInfo[0])
            binding.playButton.setImageResource(R.drawable.vector_ek5)
        }
        binding.playButton.setOnClickListener {
            if (!playStop) {
                playStop = true
                binding.playButton.setImageResource(R.drawable.vector_ek5)
                Intent(context, MediaService::class.java).also {
                    it.action = MediaService.Actions.PLAY.toString()
                    context?.startService(it)
                }
            } else {
                playStop = false
                binding.playButton.setImageResource(R.drawable.play)
                Intent(context, MediaService::class.java).also {
                    it.action = MediaService.Actions.PAUSE.toString()
                    context?.startService(it)
                }
            }
        }


        if (arguments?.getStringArrayList("param_key") != null) {
            binding.seekBar.max = arguments?.getStringArrayList("param_key")!![1].toInt()
            binding.seekBar.progress =
                arguments?.getStringArrayList("param_key")!![2].toInt() / 1000
            binding.trackName.text = arguments?.getStringArrayList("param_key")!![3]
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, pos: Int, changed: Boolean) {
                if (changed) {
                    Intent(context, MediaService::class.java).also {
                        it.action = MediaService.Actions.POSITION.toString()
                        it.putExtra("positionSong", pos)
                        context?.startService(it)
                    }
                    binding.startTime.text=setStartTime(pos.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        binding.nextTrack.setOnClickListener {
            Intent(context, MediaService::class.java).also {
                it.action = MediaService.Actions.NEXTTRACK.toString()
                context?.startService(it)
            }
        }
        binding.prevTrack.setOnClickListener {
            Intent(context, MediaService::class.java).also {
                it.action = MediaService.Actions.PREVTRACK.toString()
                context?.startService(it)
            }
        }


        val bottomSheet: FrameLayout =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!

        // Height of the view
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        // Behavior of the bottom sheet
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.apply {
            peekHeight = resources.displayMetrics.heightPixels // Pop-up height
            state = BottomSheetBehavior.STATE_EXPANDED
            // Expanded state
        }
        val filter = IntentFilter("playStatus")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, filter)
    }

    override fun onDestroyView() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        super.onDestroyView()
    }

    companion object {
        fun newInstance(parameter: ArrayList<String>): NewTaskSheet {
            val fragment = NewTaskSheet()
            val args = Bundle()
            args.putStringArrayList("param_key", parameter)
            fragment.arguments = args
            return fragment
        }
    }


    private fun updateUI() {
        requireActivity().runOnUiThread {
            binding.seekBar.max = trackInfo[1].toInt()/1000
            binding.seekBar.progress = trackInfo[2].toInt() / 1000
            binding.trackName.text = trackInfo[3]
            // Обновление UI на основе новых данных trackInfo
            binding.startTime.text=setStartTime(trackInfo[2].toLong())
            binding.endTime.text=setStartTime(trackInfo[1].toLong())
        }
    }

    private fun setStartTime(currentPosition: Long): String {
        return String.format(
            Locale.getDefault(),
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(currentPosition),
            TimeUnit.MILLISECONDS.toSeconds(currentPosition) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(currentPosition)
            )
        )
    }
}
