package com.example.spotifyplaylistviewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private val clientId = "CLIENT_ID"
    private val redirectUri = "thisapp://callback"
    private lateinit var codeVerifier: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            startSpotifyLogin()
        }

        handleAuthRedirect(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthRedirect(intent)
    }

    private fun handleAuthRedirect(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.toString().startsWith(redirectUri)) {
            Log.d("AUTH", "Redirect URI detected: $uri")
            val code = uri.getQueryParameter("code")
            if (!code.isNullOrEmpty()) {
                Log.d("AUTH", "Authorization code received: $code")
                exchangeCodeForToken(code)
            } else {
                val error = uri.getQueryParameter("error")
                Log.e("AUTH", "Authorization failed or denied: $error")
            }
        }
    }

    private fun startSpotifyLogin() {
        codeVerifier = generateCodeVerifier()
        Log.d("AUTH", "Saving code_verifier: $codeVerifier")

        getSharedPreferences("auth", MODE_PRIVATE).edit()
            .putString("code_verifier", codeVerifier)
            .apply()

        val codeChallenge = generateCodeChallenge(codeVerifier)

        val uri = Uri.Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .appendPath("authorize")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("scope", "playlist-read-private playlist-modify-private playlist-modify-public")

            .build()

        Log.d("AUTH", "Launching Spotify login with URI: $uri")

        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, uri)
    }

    private fun exchangeCodeForToken(code: String) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val savedVerifier = prefs.getString("code_verifier", null)

        Log.d("TOKEN", "Loaded code_verifier: $savedVerifier")

        if (savedVerifier == null) {
            Log.e("TOKEN", "No code_verifier saved")
            return
        }

        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("client_id", clientId)
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", redirectUri)
            .add("code_verifier", savedVerifier)
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TOKEN", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("TOKEN", "Response: $responseBody")

                try {
                    val json = JSONObject(responseBody ?: "")
                    if (json.has("access_token")) {
                        val accessToken = json.getString("access_token")
                        Log.d("TOKEN", "Access token received")

                        runOnUiThread {
                            val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
                            intent.putExtra("ACCESS_TOKEN", accessToken)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Log.e("TOKEN", "No access_token found in response")
                    }
                } catch (e: Exception) {
                    Log.e("TOKEN", "Failed to parse token JSON: ${e.message}")
                }
            }
        })
    }

    private fun generateCodeVerifier(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..64)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
