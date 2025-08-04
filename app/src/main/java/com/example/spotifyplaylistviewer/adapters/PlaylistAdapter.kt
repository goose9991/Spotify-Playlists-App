package com.example.spotifyplaylistviewer.adapters

import com.example.spotifyplaylistviewer.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyplaylistviewer.model.PlaylistItem


class PlaylistAdapter(
    private val onItemClick: (PlaylistItem) -> Unit)
    :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private val playlists = mutableListOf<PlaylistItem>()

    fun submitList(newList: List<PlaylistItem>) {
        playlists.clear()
        playlists.addAll(newList)
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById<TextView>(R.id.playListInfo)

        fun bind(playlistItem: PlaylistItem) {
            textView.text = playlistItem.name
            itemView.setOnClickListener {
                onItemClick(playlistItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size
}
