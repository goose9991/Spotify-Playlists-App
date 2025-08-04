package com.example.spotifyplaylistviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyplaylistviewer.adapters.PlaylistAdapter
import com.example.spotifyplaylistviewer.model.PlaylistItem
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistRecycler: RecyclerView

    private lateinit var adapter: PlaylistAdapter
    private lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "PlaylistActivity onCreate")
        setContentView(R.layout.activity_playlist)

        playlistRecycler = findViewById(R.id.playlistRecycler)

        supportActionBar?.setDisplayShowTitleEnabled(false)




        playlistRecycler.layoutManager = LinearLayoutManager(this)

        accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: run {
            Log.e("PLAYLIST", "No access token provided, cannot fetch playlists")
            finish()
            return
        }

        adapter = PlaylistAdapter { playlistItem ->
            Log.d("PLAYLIST", "Clicked: ${playlistItem.name} (${playlistItem.id})")
            val intent = Intent(this, TracksActivity::class.java)
            intent.putExtra("ACCESS_TOKEN", accessToken)
            intent.putExtra("USER_PLAYLIST_ID", playlistItem.id)
            intent.putExtra("PLAYLIST_NAME", playlistItem.name)
            startActivity(intent)
        }

        playlistRecycler.adapter = adapter

        fetchUserPlaylists(accessToken)
    }

    private fun fetchUserPlaylists(accessToken: String) {
        Log.d("PLAYLIST", "Fetching playlists with token")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/playlists")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PLAYLIST", "Fetch failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("PLAYLIST", "Response: $responseBody")

                try {
                    val json = JSONObject(responseBody ?: "")
                    val items = json.optJSONArray("items") ?: run {
                        Log.e("PLAYLIST", "No playlists found")
                        return
                    }

                    val playlists = mutableListOf<PlaylistItem>()
                    for (i in 0 until items.length()) {
                        val obj = items.getJSONObject(i)
                        val name = obj.getString("name")
                        val id = obj.getString("id")
                        playlists.add(PlaylistItem(id, name))
                    }

                    runOnUiThread {
                        adapter.submitList(playlists)
                    }
                } catch (e: Exception) {
                    Log.e("PLAYLIST", "Failed to parse JSON: ${e.message}")
                }
            }
        })
    }
}
