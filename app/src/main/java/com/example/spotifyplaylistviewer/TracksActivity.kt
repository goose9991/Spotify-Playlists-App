package com.example.spotifyplaylistviewer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TracksActivity : AppCompatActivity() {

    private lateinit var trackRecycler: RecyclerView
    private lateinit var adapter: TrackAdapter

    private lateinit var accessToken: String
    private lateinit var playlistId: String
    private lateinit var playlistName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "TracksActivity onCreate")
        setContentView(R.layout.activity_tracks)

        accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        playlistId = intent.getStringExtra("PLAYLIST_ID") ?: ""
        playlistName = intent.getStringExtra("PLAYLIST_NAME") ?: ""

        if (accessToken.isBlank() || playlistId.isBlank()) {
            Log.e("TRACKS", "Missing data, closing activity")
            finish()
            return
        }

        title = playlistName

        trackRecycler = findViewById(R.id.trackRecycler)
        trackRecycler.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter()
        trackRecycler.adapter = adapter

        fetchTracks()
    }

    private fun fetchTracks() {
        Log.d("TRACKS", "Fetching tracks for playlist: $playlistId")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/playlists/$playlistId/tracks")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TRACKS", "Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("TRACKS", "Response: $body")

                try {
                    val items = JSONObject(body ?: "").optJSONArray("items") ?: return
                    val tracks = mutableListOf<String>()

                    for (i in 0 until items.length()) {
                        val trackObj = items.getJSONObject(i).getJSONObject("track")
                        val name = trackObj.getString("name")
                        val artist = trackObj.getJSONArray("artists")
                            .getJSONObject(0).getString("name")
                        tracks.add("$name - $artist")
                    }

                    runOnUiThread {
                        adapter.submitList(tracks)
                    }
                } catch (e: Exception) {
                    Log.e("TRACKS", "JSON parse error: ${e.message}")
                }
            }
        })
    }
}
