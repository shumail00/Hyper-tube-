# HyperTube Pad — Xiaomi HyperOS 3 Distraction-Free YouTube Studio

A distraction-free YouTube video player and study workspace designed specifically for **16:10 Xiaomi tablets** using **Xiaomi HyperOS 3 ("Alive Design")** aesthetics. Paste any YouTube link (or auto-detect from clipboard) to watch immediately without algorithmic feeds, comments, or recommendations—paired with ambient thumbnail lighting and timestamped study notes saved locally on-device.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural plan below reflects your confirmed preferences from our design interview:

- **Confirmed Visual Theme**: **Dynamic Auto-Switching Light & Dark HyperOS 3 Theme** — Adapts to system light/dark mode (with a quick HyperOS Control Center override toggle in the top pill bar), combining translucent frosted acrylic surfaces, ultra-smooth squircle corners (`28dp`–`32dp`), subtle hairline borders, and Xiaomi HyperOS Blue (`#2A68F6` / `#4D88FF`) accents.
- **Confirmed Distraction-Free Features**:
  - **Quick Clipboard Auto-Detect**: Automatically inspects clipboard contents when opening or resuming the app, surfacing a non-intrusive HyperOS "Dynamic Island / Super Island" floating toast banner to load a copied YouTube URL in one tap.
  - **Ambient Thumbnail Glow**: Dynamically extracts dominant color palettes from the active video's high-resolution thumbnail and renders a soft, breathing radial ambient light behind the 16:9 theatre canvas inside the 16:10 tablet frame.
  - **Timestamped Study Notes**: Side-by-side glassmorphic studio dock where you can jot down notes pinned to the current video playback timestamp (`MM:SS`) and tap any timestamp chip to seek the player directly to that moment.
- **Confirmed Storage Strategy**: **Local On-Device Storage (Room SQLite)** — Zero sign-in required. All pasted videos, watch history, bookmarked queues, and timestamped study notes are stored locally on your tablet.

---

## 1. Overview & Core Concept

- **What It Does**: Eliminates the YouTube home feed, Shorts shelf, recommendations sidebar, and comment sections. Users bring intentional links (`youtube.com/watch?v=...`, `youtu.be/...`, `youtube.com/live/...`, or `youtube.com/embed/...`), resolve real video metadata (title, channel/author, high-res thumbnail via YouTube's public oEmbed endpoint), and watch inside an embedded, distraction-free YouTube IFrame player with full JS bridge controls and timestamp synchronization.
- **Target Audience / Persona**: Xiaomi Pad owners and focused learners who want a native-feeling HyperOS 3 tablet workspace for watching lectures, tutorials, deep-dive essays, and technical talks while taking structured timestamped notes.
- **Key Value**: Transforms passive scrolling into intentional viewing and active note-taking, utilizing the full horizontal and vertical real estate of a 16:10 tablet screen.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Zero-Distraction Launch & Clipboard Super-Island**:
   - Upon launching or returning to the app, if a valid YouTube link is detected on the clipboard (or shared via Android's system Share sheet `ACTION_SEND`), a sleek floating **HyperOS Super Island pill** animates down from the top center: *"YouTube link detected in clipboard • Watch Now"*.
   - Alternatively, the prominent HyperOS glass URL Omni-Bar lets users paste a link directly or pick from their saved local library.
2. **16:10 Split-Canvas Theatre & Study Mode**:
   - **Default Split Studio (Landscape 16:10)**:
     - **Left Floating Navigation Dock**: Compact vertical glass pill housing mode switches (*Theatre*, *Library & Queue*, *All Study Notes*, *HyperOS Control Quick-Settings*) and a quick theme/ambient glow toggle.
     - **Center Primary Stage (68% width)**: Houses the URL Omni-Bar at the top, the `28dp`-rounded 16:9 YouTube video player surrounded by a real-time **Ambient Glow Canvas**, and a HyperOS tactile control deck beneath (playback speed pills `0.75x`–`2.0x`, loop toggle, quick `-10s` / `+10s` seek, and one-tap *"Capture Timestamp Note"* trigger).
     - **Right Side-Stage Dock (32% width)**: Switchable between **Timestamped Study Notes** for the current video and your **Saved Video Queue**.
   - **Pure Zen Cinema Mode**: Collapses the side dock and navigation rail with a spring animation so the video and its ambient radial glow fill the 16:10 canvas.
3. **Timestamped Note-Taking & Instant Seeking**:
   - Typing a note in the right-hand Study Dock automatically captures the player's current playback second (e.g., `04:32`).
   - Clicking any saved note's glowing blue timestamp pill calls `seekTo(seconds)` on the embedded player to jump straight to that concept.

### Visual Identity & Theme (Xiaomi HyperOS 3 "Alive Design")
- **Aesthetic Direction**: Xiaomi HyperOS 3 tablet OS language—frosted multi-layer glassmorphism, generous squircle curvature, tactile pill sliders, and fluid spring physics.
- **Color Palette & Mood**:
  - **Light Mode ("HyperOS Porcelain")**:
    - Background Canvas: `#F2F4F8` (Cool Alabaster)
    - Translucent Acrylic Cards: `#FFFFFF` at `78%` opacity with `#FFFFFF` `1.dp` specular border highlight
    - Primary Accent: `#2A68F6` (Xiaomi Hyper Blue)
    - Secondary Accent: `#00B8A9` (Hyper Teal for timestamp badges)
    - Text Hierarchy: `#12151C` (Primary Ink), `#5E6573` (Muted Slate)
  - **Dark Mode ("HyperOS Obsidian Glass")**:
    - Background Canvas: `#0B0D13` (Deep Space Obsidian)
    - Translucent Acrylic Cards: `#1A1E29` at `72%` opacity with `#FFFFFF` `10%` subtle rim light
    - Primary Accent: `#4D88FF` (Luminous Hyper Blue)
    - Secondary Accent: `#2CE5D3` (Cyan Glow for timestamp pills)
    - Text Hierarchy: `#F5F7FA` (Pure Frost), `#8C95A6` (Secondary Mist)
- **Typography & Hierarchy**:
  - **Display & UI Headers**: **Plus Jakarta Sans** (bundled locally via `font-util`) — closely mirrors Xiaomi's geometric `MiSans` typeface with crisp medium/semibold weights.
  - **Body & Notes**: **Inter** — clean, high-legibility proportions for reading and writing multi-line study notes.
  - **Timestamps & Telemetry**: **JetBrains Mono** — tabular monospace numerals for `00:00:00` timecodes and playback speed readouts (`1.25x`).
- **Component Styling & Layout**:
  - **16:10 Tablet Ergonomics**: Uses `BoxWithConstraints` to automatically deliver the 3-zone HyperOS tablet layout on wide screens while adapting cleanly to portrait or split-screen multi-window states.
  - **HyperOS Control Center Toggles**: Custom pill-shaped quick toggles for *Ambient Glow Intensity*, *Auto-Clipboard Prompt*, *Playback Speed*, and *Theme Mode (Auto / Light / Dark)*.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Embedded IFrame Player with Custom JS Bridge & Real oEmbed Metadata**
  - *Chosen Approach*: Combine a hardware-accelerated Android `WebView` running the official YouTube IFrame Player API (`youtube-nocookie.com` privacy-enhanced embed) with a Kotlin `@JavascriptInterface` bridge for accurate current-time polling, playback speed control, and timestamp seeking—paired with YouTube's `noembed.com` / `youtube.com/oembed` JSON endpoint for instant title, channel name, and high-res thumbnail retrieval.
  - *Why*: Provides genuine YouTube video playback for any pasted URL without requiring a personal Google Cloud API key or exposing the distracting YouTube web chrome, recommendations, or comments.
  - *Alternatives Considered*: Opening links in an external browser or full YouTube mobile web page (rejected because it reintroduces comments, Shorts, and recommendation feeds, and prevents in-app timestamp note synchronization).
- **Decision 2: Local Room Database + Preferences Storage**
  - *Chosen Approach*: Store all saved videos, playback resume positions, favorite flags, and timestamped study notes in a local Room SQLite database with reactive Kotlin `Flow` queries.
  - *Why*: Matches your preference for instant, private, zero-login local persistence while surviving app restarts.
- **Decision 3: Real-Time Thumbnail Color Extraction for Ambient Glow**
  - *Chosen Approach*: Sample dominant vibrant/warm/cool pixels from the loaded YouTube thumbnail bitmap (with a deterministic color fallback derived from the video ID) and render a multi-stop radial gradient behind the player frame.
  - *Why*: Recreates the signature Xiaomi HyperOS 3 ambient media glow on a 16:10 display without hurting video frame rates.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                   Xiaomi Pad 16:10 Adaptive Shell (UI Layer)               │
│                                                                            │
│  ┌──────────┐  ┌──────────────────────────────────────┐  ┌──────────────┐  │
│  │ HyperOS  │  │      Center Theatre Stage (68%)      │  │  Side Studio │  │
│  │ Floating │  │ ┌──────────────────────────────────┐ │  │  Dock (32%)  │  │
│  │ Nav Rail │  │ │ Super-Island Clipboard Banner    │ │  │ ┌──────────┐ │  │
│  │          │  │ └──────────────────────────────────┘ │  │ │Timestamp │ │  │
│  │ • Studio │  │ ┌──────────────────────────────────┐ │  │ │Notes Tab │ │  │
│  │ • Library│  │ │ HyperOS Glass URL Omni-Bar       │ │  │ ├──────────┤ │  │
│  │ • Notes  │  │ └──────────────────────────────────┘ │  │ │Saved     │ │  │
│  │ • Control│  │ ┌──────────────────────────────────┐ │  │ │Queue Tab │ │  │
│  │   Center │  │ │ Ambient Thumbnail Glow +         │ │  │ ├──────────┤ │  │
│  │          │  │ │ YouTube IFrame JS Bridge Player  │ │  │ │Quick Note│ │  │
│  │          │  │ └──────────────────────────────────┘ │  │ │Composer  │ │  │
│  │          │  │ ┌──────────────────────────────────┐ │  │ └──────────┘ │  │
│  │          │  │ │ HyperOS Tactile Deck (Speed/Seek)│ │  │              │  │
│  │          │  │ └──────────────────────────────────┘ │  │              │  │
│  └──────────┘  └──────────────────────────────────────┘  └──────────────┘  │
└───────────────────────────────────────┬────────────────────────────────────┘
                                        │ StateFlow / UI Intents
                                        ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     HyperTubeViewModel (State & Coordinator)               │
│  • URL Parser (watch?v=, youtu.be/, shorts/, live/, embed/)                │
│  • Clipboard Monitor & Android ACTION_SEND Share Intent Handler            │
│  • Active Playback Telemetry (currentSecond, playbackRate, isZenMode)      │
│  • HyperOS Theme & Ambient Glow Controller                                 │
└───────────────────┬────────────────────────────────────┬───────────────────┘
                    │                                    │
                    ▼                                    ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────────┐
│     YouTubeMetadataService (HTTP)    │  │   HyperTubeRepository + Room DB  │
│  • Fetches title, author, thumbnail  │  │  • VideoEntity (videoId, title,  │
│    via YouTube oEmbed endpoint       │  │    author, thumbnail, dominantHex│
│  • Extracts dominant ambient color   │  │    lastPositionSec, isPinned)    │
│    from high-res video thumbnail     │  │  • StudyNoteEntity (id, videoId, │
│                                      │  │    timestampSec, content, tag)   │
└──────────────────────────────────────┘  └──────────────────────────────────┘
```

### Data Model & State
- **`VideoEntity` (Room Table `videos`)**:
  - `videoId` (Primary Key, 11-char YouTube ID)
  - `title`, `channelName`, `thumbnailUrl`, `originalUrl`
  - `dominantColorHex` (Extracted ambient glow color)
  - `lastPositionSeconds` (Auto-saved playback position)
  - `isFavorite` / `isWatchLater`, `addedAtTimestamp`, `lastWatchedTimestamp`
- **`StudyNoteEntity` (Room Table `study_notes`)**:
  - `id` (Auto-generated Primary Key)
  - `videoId` (Foreign key indexed to `VideoEntity`)
  - `timestampSeconds` (Exact playback second when note was captured)
  - `noteText` (Markdown/plain study note content)
  - `categoryTag` (e.g., *Key Idea*, *Formula*, *Action Item*, *Question*)
  - `createdAt`

### Interactive Component & State Mapping
- **URL Input & Clipboard Auto-Detection**:
  - Pasting or typing any YouTube URL format validates the 11-character video ID in real time, queries the oEmbed metadata service, extracts the thumbnail's ambient palette, inserts/updates the `VideoEntity` in Room, and loads the video into the player.
  - On `ON_RESUME`, the app checks `ClipboardManager` for a YouTube URL not currently playing and displays the dismissible **Super Island Banner** with a one-tap *"Load Video"* button.
- **Bidirectional Player ↔ Study Notes Sync**:
  - The embedded YouTube player reports `onCurrentTimeUpdated(seconds)` every 500ms to `HyperTubeViewModel`.
  - Pressing *"Add Note at [MM:SS]"* stamps the current second into the note composer; submitting saves to Room and highlights the active note card whenever playback passes its timestamp.
  - Tapping a note's timestamp pill triggers `playerBridge.seekTo(note.timestampSeconds)` and resumes playback smoothly.
- **HyperOS Control Center Popover**:
  - Accessible from the floating rail or top bar, allowing instant adjustment of **Theme Mode** (*Auto System*, *HyperOS Light*, *HyperOS Dark*), **Ambient Glow Radius & Opacity**, **Default Playback Speed**, and **Zen Cinema Mode**.
