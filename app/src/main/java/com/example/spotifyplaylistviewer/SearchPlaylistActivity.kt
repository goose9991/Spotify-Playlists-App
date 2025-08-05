package com.example.spotifyplaylistviewer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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

class SearchPlaylistActivity : AppCompatActivity() {

    private lateinit var playlistRecycler: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: PlaylistAdapter
    private lateinit var accessToken: String
    private lateinit var userPlaylistID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "SearchPlaylistActivity onCreate")
        setContentView(R.layout.activity_search_playlist)

        playlistRecycler = findViewById(R.id.otherPlaylistRecycler)
        searchView = findViewById(R.id.search_view)

        val searchEditText = findSearchAutoComplete(searchView)
        searchEditText?.setTextColor(Color.WHITE)
        searchEditText?.setHintTextColor(Color.LTGRAY)

        playlistRecycler.layoutManager = LinearLayoutManager(this)
        accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: run {
            Log.e("SEARCH_PLAYLIST", "No access token provided, cannot search playlists")
            finish()
            return
        }

        userPlaylistID = intent.getStringExtra("USER_PLAYLIST_ID")?:""


        adapter = PlaylistAdapter { playlistItem ->
            Log.d("PLAYLIST", "Clicked: ${playlistItem.name} (${playlistItem.id})")
            val intent = Intent(this, OtherTracksActivity::class.java)
            intent.putExtra("ACCESS_TOKEN", accessToken)
            intent.putExtra("SOURCE_PLAYLIST_ID", playlistItem.id)
            intent.putExtra("USER_PLAYLIST_ID", userPlaylistID)
            intent.putExtra("PLAYLIST_NAME", playlistItem.name)
            startActivity(intent)
        }
        playlistRecycler.adapter = adapter




        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    Log.d("SEARCH_PLAYLIST", "Searching for: $query")
                    fetchPlaylistsFromSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return true
            }
        })
    }

    private fun findSearchAutoComplete(searchView: SearchView): EditText? {
        val linearLayout1 = searchView.getChildAt(0) as? ViewGroup ?: return null
        val linearLayout2 = linearLayout1.getChildAt(2) as? ViewGroup ?: return null
        val linearLayout3 = linearLayout2.getChildAt(1) as? ViewGroup ?: return null
        return linearLayout3.getChildAt(0) as? EditText
    }

    private fun fetchPlaylistsFromSearch(query: String) {
        val client = OkHttpClient()
        val url = "https://api.spotify.com/v1/search?q=${query.trim()}&type=playlist"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SEARCH_PLAYLIST", "Search failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("SEARCH_PLAYLIST", "Response: $responseBody")

                try {
                    val json = JSONObject(responseBody ?: "")
                    val items = json.optJSONObject("playlists")?.optJSONArray("items") ?: run {
                        Log.e("SEARCH_PLAYLIST", "No playlists found")
                        return
                    }

                    val playlists = mutableListOf<PlaylistItem>()
                    for (i in 0 until items.length()) {
                        if (items.isNull(i)) {
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
                    Log.e("SEARCH_PLAYLIST", "Failed to parse JSON: ${e.message}")
                }
            }
        })
    }

}
