package com.example.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrackRecyclerViewAdapter(var context: Context, var list: ArrayList<String>, private var recyclerViewInterface: RecyclerViewInterface) :
    RecyclerView.Adapter<TrackRecyclerViewAdapter.MyViewHolder>() {
     public fun setFilterList(list:ArrayList<String>){
        this.list = list
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
       val layoutInflater :LayoutInflater=LayoutInflater.from(context)
        val view:View= layoutInflater.inflate(R.layout.trac_view,parent,false)
        return MyViewHolder(view,recyclerViewInterface)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.trackName.text = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    companion object class MyViewHolder(itemView: View,recyclerViewInterface:RecyclerViewInterface) : RecyclerView.ViewHolder(itemView){
        var trackName:TextView

        init {
            trackName = itemView.findViewById(R.id.textView)

           itemView.setOnClickListener{
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION)
                    recyclerViewInterface.onItemClick(pos)
            }
        }

    }
}