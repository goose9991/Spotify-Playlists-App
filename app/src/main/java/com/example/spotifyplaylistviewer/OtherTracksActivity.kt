package com.example.spotifyplaylistviewer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyplaylistviewer.adapters.TrackAdapterWithAddButton
import com.example.spotifyplaylistviewer.model.TrackItem
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class OtherTracksActivity : AppCompatActivity() {

    private lateinit var trackRecycler: RecyclerView
    private lateinit var adapter: TrackAdapterWithAddButton

    private lateinit var accessToken: String
    private lateinit var sourcePlaylistId: String
    private lateinit var userPlaylistId: String
    private lateinit var playlistName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_tracks)

        accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        sourcePlaylistId = intent.getStringExtra("SOURCE_PLAYLIST_ID") ?: ""
        userPlaylistId = intent.getStringExtra("USER_PLAYLIST_ID") ?: ""
        Log.d("D", userPlaylistId)
        playlistName = intent.getStringExtra("PLAYLIST_NAME") ?: ""

        if (accessToken.isBlank() || sourcePlaylistId.isBlank() || userPlaylistId.isBlank()) {
            Log.e("OTHER_TRACKS", "Missing required data, finishing activity")
            finish()
            return
        }

        title = playlistName

        trackRecycler = findViewById(R.id.otherTrackRecycler)
        trackRecycler.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapterWithAddButton { track ->
            addTrackToUserPlaylist(track)
        }
        trackRecycler.adapter = adapter

        fetchTracks()
    }

    private fun fetchTracks() {
        Log.d("OTHER_TRACKS", "Fetching tracks for playlist: $sourcePlaylistId")

        val client = OkHttpClient()
        val allTracks = mutableListOf<TrackItem>()

        fun fetchPage(url: String) {
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("OTHER_TRACKS", "Failed to fetch tracks: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(this@OtherTracksActivity, "Failed to fetch tracks", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    Log.d("OTHER_TRACKS", "Response: $body")

                    try {
                        val json = JSONObject(body ?: "")
                        val items = json.optJSONArray("items") ?: return

                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            if (item.isNull("track")) {
                                Log.w("OTHER_TRACKS", "Null track at index $i, skipping")
                                continue
                            }
                            val trackObj = item.getJSONObject("track")
                            val name = trackObj.getString("name")
                            val artist = trackObj.getJSONArray("artists").getJSONObject(0).getString("name")
                            val albumImages = trackObj.getJSONObject("album").getJSONArray("images")
                            val albumImageUrl = if (albumImages.length() > 0) albumImages.getJSONObject(0).getString("url") else ""
                            val uri = trackObj.getString("uri")

                            allTracks.add(TrackItem(name, artist, albumImageUrl, uri))
                        }

                        val nextUrl = json.optString("next")
                        if (!nextUrl.isNullOrEmpty() && nextUrl != "null") {
                            fetchPage(nextUrl)
                        } else {
                            runOnUiThread {
                                adapter.submitList(allTracks)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("OTHER_TRACKS", "JSON parse error: ${e.message}")
                        runOnUiThread {
                            Toast.makeText(this@OtherTracksActivity, "Failed to parse track data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        val initialUrl = "https://api.spotify.com/v1/playlists/$sourcePlaylistId/tracks"
        fetchPage(initialUrl)
    }

    private fun addTrackToUserPlaylist(track: TrackItem) {
        Log.d("OTHER_TRACKS", "Adding track to user playlist: ${track.title}")

        val client = OkHttpClient()

        val jsonBody = JSONObject().apply {
            put("uris", JSONArray().put(track.uri))
        }

        val requestBody = jsonBody.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/playlists/$userPlaylistId/tracks")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OTHER_TRACKS", "Failed to add track: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@OtherTracksActivity, "Failed to add track", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val respBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("OTHER_TRACKS", "Track added successfully: ${track.title}")
                    runOnUiThread {
                        Toast.makeText(this@OtherTracksActivity, "Added: ${track.title}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("OTHER_TRACKS", "Add track failed with code: ${response.code}")
                    Log.e("OTHER_TRACKS", "Response body: $respBody")
                    runOnUiThread {
                        Toast.makeText(this@OtherTracksActivity, "Failed to add track: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }
}
