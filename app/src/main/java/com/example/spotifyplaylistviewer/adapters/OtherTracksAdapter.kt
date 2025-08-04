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
import com.google.android.material.button.MaterialButton

class TrackAdapterWithAddButton(
    private val onAddClick: (TrackItem) -> Unit
) : RecyclerView.Adapter<TrackAdapterWithAddButton.TrackViewHolder>() {

    private val tracks = mutableListOf<TrackItem>()

    fun submitList(newList: List<TrackItem>) {
        tracks.clear()
        tracks.addAll(newList)
        notifyDataSetChanged()
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trackInfo: TextView = itemView.findViewById(R.id.trackInfo)
        val albumImage: ImageView = itemView.findViewById(R.id.albumImage)
        val addButton: MaterialButton = itemView.findViewById(R.id.addButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.other_track_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.trackInfo.text = "${track.title} - ${track.artist}"

        Glide.with(holder.albumImage.context)
            .load(track.albumImageUrl)
            .into(holder.albumImage)

        holder.addButton.setOnClickListener {
            onAddClick(track)
        }
    }

    override fun getItemCount(): Int = tracks.size
}
