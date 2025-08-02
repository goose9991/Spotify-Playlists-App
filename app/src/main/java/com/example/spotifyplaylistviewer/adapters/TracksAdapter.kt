package com.example.spotifyplaylistviewer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spotifyplaylistviewer.R
import com.example.spotifyplaylistviewer.model.TrackItem

class TrackAdapter : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private val tracks = mutableListOf<TrackItem>()

    fun submitList(newList: List<TrackItem>) {
        tracks.clear()
        tracks.addAll(newList)
        notifyDataSetChanged()
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trackInfo: TextView = itemView.findViewById(R.id.trackInfo)
        val albumImage: ImageView = itemView.findViewById(R.id.albumImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.trackInfo.text = "${track.title} - ${track.artist}"

        Glide.with(holder.albumImage.context)
            .load(track.albumImageUrl)
            .into(holder.albumImage)
    }

    override fun getItemCount(): Int = tracks.size
}
