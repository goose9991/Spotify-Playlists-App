package com.example.spotifyplaylistviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView
    private lateinit var adapter: PlaylistAdapter
    private lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "PlaylistActivity onCreate")
        setContentView(R.layout.activity_playlist)

        playlistRecycler = findViewById(R.id.playlistRecycler)
        toolbar = findViewById(R.id.toolbar)
        searchView = findViewById(R.id.search_view)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setSupportActionBar(toolbar) // Set the Toolbar as the ActionBar

        // Apply insets to the Toolbar to push it down below the status bar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply top padding to the Toolbar to account for the status bar height
            view.updatePadding(top = insets.top)
            // You might also want to apply left and right insets if needed for cutouts, etc.
            // view.updatePadding(left = insets.left, right = insets.right)

            // Return CONSUMED if you've handled the insets
            WindowInsetsCompat.CONSUMED
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    Log.d("SEARCH", "Query submitted: $query")
                    fetchPlaylistsFromSearch(accessToken, query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    Log.d("SEARCH", "Query changed: $newText")
                }
                return true
            }
        })


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
            intent.putExtra("PLAYLIST_ID", playlistItem.id)
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

    private fun fetchPlaylistsFromSearch(accessToken: String, query: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/search?q=$query&type=playlist")
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
                    val items = json.optJSONObject("playlists")?.optJSONArray("items") ?: run {
                        Log.e("PLAYLIST", "No playlists found")
                        return
                    }

                    val playlists = mutableListOf<PlaylistItem>()
                    for (i in 0 until items.length()) {
                        Log.d("PLAYLIST", "Processing playlist $i")
                        Log.d("PLAYLIST", "JSON: ${items[i]}")

                        if (items[i].toString() == "null") {
                            Log.e("PLAYLIST", "Null playlist found")
                            continue
                        }

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
