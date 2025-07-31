---
title: '**APP_NAME_HERE**'

---

# **Spotify Playlist Editor**

## Table of Contents

1. [App Overview](#App-Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
1. [Build Notes](#Build-Notes)

## App Overview

### Description 

**An app that allows users to compare their spotify playlists to others and add the songs they are missing.**

### App Evaluation

<!-- Evaluation of your app across the following attributes -->

**Category:** Entertainment

**Mobile:** High – Offers a smoother, more intuitive experience for playlist management on mobile, with gestures and drag-and-drop interfaces not present in Spotify’s native app.

**Story:** Strong – Users often want more control over playlists than Spotify’s mobile app allows. This tool makes playlist curation easier and more enjoyable, especially for power users.

**Market:** Medium to High – Spotify has hundreds of millions of users; this app targets the segment that actively curates playlists and music libraries.

**Habit:** Medium – Users may not open it daily, but music lovers would use it regularly when organizing or discovering music.

**Scope:** Medium – Relies on the Spotify Web API for playlist access and modification. No backend needed initially.

## Product Spec

### 1. User Features (Required and Optional)

Required Features:

- **Log in to personal Spotify Account**
- **View Playlists**

Stretch Features:

- **Compare user's playlists to others**
- **Add songs that are not in user's playlist**

### 2. Chosen API(s)

- **`GET /v1/playlists/{playlist_id}`**
    - Retrieve full details of a playlist (name, description, tracks, etc.)
    - Required for loading both user and comparison playlists
- **`GET /v1/playlists/{playlist_id}/tracks`**

    - Retrieve the track list from a given playlist
    - Needed to extract song IDs for comparison logic

- **`GET /v1/me/playlists`**

    - Fetch the current user's playlists to let them choose one to compare
    - Enables playlist selection within the UI

- **`POST /v1/playlists/{playlist_id}/tracks`**

    - Add songs to a user’s playlist
    - Used to let users add missing songs they discover during comparison

- **`GET /v1/tracks?ids={ids}`**
    - Get metadata (title, artist, album, etc.) for multiple tracks by ID
    - Useful for showing detailed info on missing or matched songs

- **`GET /v1/me`**

    - Identify the current user and authenticate access
    - Required to personalize the experience and access their playlists

### 3. User Interaction

Required Feature

- **Enter Spotify username and password**
  - => **logs into spotify**
- **Scroll through playlists in recyclerView**
  - => **allows user to see playlists**

## Wireframes

<!-- Add picture of your hand sketched wireframes in this section -->
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Build Notes

Here's a place for any other notes on the app, it's creation 
process, or what you learned this unit!  

For Milestone 2, include **2+ Videos/GIFs** of the build process here!

## License

Copyright **yyyy** **your name**

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.