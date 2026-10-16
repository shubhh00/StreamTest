<div align="center">

# 📺 StreamTest

**A speed test for video on Android TV.**<br/>
Pick a film, it plays, and the app measures how well the TV handles it.

[![Build](https://github.com/shubhh00/StreamTest/actions/workflows/build.yml/badge.svg)](https://github.com/shubhh00/StreamTest/actions/workflows/build.yml)
![Android TV](https://img.shields.io/badge/Android_TV-minSdk_26-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Compose for TV](https://img.shields.io/badge/Compose_for_TV-1.1-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Media3](https://img.shields.io/badge/Media3-1.11-FF6F00?style=flat-square)
![Widevine](https://img.shields.io/badge/DRM-Widevine-E5674A?style=flat-square)
![Tests](https://img.shields.io/badge/unit_tests-53_passing-2EA043?style=flat-square)

</div>

https://github.com/user-attachments/assets/3e81e576-6ae6-4881-acda-32c6fe6b0b98

<table>
<tr>
<td align="center" width="25%"><h3>16 / 16</h3>streams play on<br/>both test devices</td>
<td align="center" width="25%"><h3>0.5 s</h3>fastest start<br/><sub>Big Buck Bunny HLS, phone</sub></td>
<td align="center" width="25%"><h3>+0.2–1.0 s</h3>🔒 DRM cost with<br/>hardware Widevine (L1)</td>
<td align="center" width="25%"><h3>+1.0–2.8 s</h3>🔒 DRM cost with<br/>software Widevine (L3)</td>
</tr>
</table>

## 🎬 What it does

The library is built around **films that exist in three versions**:
`HLS` · `DASH` · `🔒 Protected` (DASH + Widevine DRM). Because it's the same film each
time, the results compare fairly: you can see exactly how much the license request adds
to the start time. **More streams** adds single test streams: `4K 60 fps` · `HEVC` ·
`AV1 + DRM` · `Live` · `Low bitrate`.

While a video plays, the app shows **six live numbers**: playing at, bitrate, buffered
ahead, time to start, freezes and dropped frames. It also breaks the start time into
steps and draws a quality line that marks every freeze. Every test is saved to a log.

> 🎞️ No video is hosted or stored. Everything streams from free public test sources;
> the app only stores test results.

## 🖼️ Screens

<table>
<tr>
<td width="50%"><img src="https://github.com/user-attachments/assets/b7859e21-faa5-459a-81a6-870280c3cf35" alt="Library"/></td>
<td width="50%"><img src="https://github.com/user-attachments/assets/977a0942-1074-4ce6-8984-2afc7cb684e6" alt="Player"/></td>
</tr>
<tr>
<td><b>📚 Library</b> · each version's last result and a mini quality line. Along the top: what the device decodes, its screen, the sharpest quality actually played, and what it <i>can't</i> play.</td>
<td><b>▶️ Player</b> · six live numbers, the start time step by step, and a quality line that doubles as the progress bar.</td>
</tr>
<tr>
<td><img src="https://github.com/user-attachments/assets/454b999e-6362-4a72-bbcb-4bc2381916f6" alt="Quality picker"/></td>
<td><img src="https://github.com/user-attachments/assets/f36eaf1d-9fcd-4e7d-800f-427354610950" alt="Log"/></td>
</tr>
<tr>
<td><b>🎚️ Quality picker</b> (▼) · <i>Auto</i> shows what adaptive bitrate chose; any other row forces that quality.</td>
<td><b>📋 Log</b> · every test, grouped by film, including ones that never started. Median start, freezes per test, share of watch time in HD.</td>
</tr>
<tr>
<td><img src="https://github.com/user-attachments/assets/83a2884b-6eab-4514-a5e9-187fef32e02c" alt="Settings"/></td>
<td>
<b>🎮 Remote controls</b><br/><br/>
<b>Library</b><br/>
▲ ▼ move between films and streams<br/>
▶ go to a film's versions · <b>OK</b> start a test<br/><br/>
<b>Player</b><br/>
▲ show / hide stats · ▼ quality picker<br/>
<b>BACK</b> close the picker, or save the test and go back
</td>
</tr>
<tr>
<td><b>⚙️ Settings</b> · Widevine level, HDCP, every video decoder, display modes and HDR, clearing results, credits.</td>
<td></td>
</tr>
</table>

---

## 📊 Findings

Time from `prepare()` to the **first frame on screen**: one run per stream, debug
build, fresh app start each time.

| | Device | Widevine | Screen |
|---|---|---|---|
| 🖥️ | Android TV emulator · Google TV, API 36, x86 | `L3` software | 1920×1080 · SDR |
| 📱 | OnePlus 7 (GM1901) · Android 12, home Wi-Fi | `L1` hardware | 2340×1080 · HDR10 / HLG / HDR10+ |

### ⏱️ Time to first frame

Each cell is **emulator → phone**.

| Film | HLS | DASH | 🔒 Protected | 🔒 DRM cost |
|---|---|---|---|---|
| Tears of Steel | 3.9 → **3.9 s** | 2.7 → **1.4 s** | 5.5 → **2.2 s** | +2.8 → **+0.8 s** |
| Big Buck Bunny | 1.3 → **0.5 s** | 2.6 → **1.6 s** | 3.6 → **1.8 s** | +1.0 → **+0.2 s** |
| Sintel | — | 2.1 → **1.8 s** | 3.5 → **2.2 s** | +1.4 → **+0.4 s** |
| Angel One | 8.4 → **2.9 s** | 1.8 → **1.1 s** | 3.6 → **2.1 s** | +1.8 → **+1.0 s** |

<sub>DRM cost = Protected minus the same film's DASH. Sintel has no HLS version.</sub>

| Extra stream | 🖥️ Emulator | 📱 Phone | What it showed |
|---|---|---|---|
| Big Buck Bunny 4K · 60 fps | 2.4 s | **2.5 s** | Both decoded **3840×2160**, though neither screen is 4K |
| Tears of Steel HEVC | 2.6 s | **2.2 s** | Real HEVC (`hvc1`) decoding |
| AV1 sample · 🔒 Widevine | 3.1 s | **1.8 s** | AV1 + DRM |
| Live stream | 3.8 s | **8.4 s** | **720p** live, no fixed end |
| Apple low bitrate | 3.6 s | **4.0 s** | **640×480** on the emulator; climbed to **960×720** on the phone |

### 💡 What the numbers say

- 🔒 **DRM costs much less with hardware Widevine:** **+1.0 to +2.8 s** on the emulator
  (L3, software), only **+0.2 to +1.0 s** on the phone (L1). In one run with the full
  event log, the phone's license round trip took about **0.7 s**. Protected video there
  plays through the **secure hardware decoder** (`OMX.qcom.video.decoder.avc.secure`).
- 🧭 **The step-by-step milestones are approximate.** On the phone, "DRM license
  received" sometimes arrived just after "first frame" and sometimes just before. They
  are milestones in the order they happened, not exact costs.
- 📈 **Adaptive bitrate starts low and climbs.** Tears of Steel HLS on the emulator went
  from **784×350** to **1680×750** in about **12 s**. On the phone, both HLS films were
  still on their lowest quality **25 s** in, while their DASH versions reached
  **1920×856** and **720×576**. Forcing a quality skips the climb but costs a short
  re-buffer.
- 🧩 **The first video chunk often finishes *after* the first frame** on DASH: the
  player starts decoding while a long segment is still downloading.
- ⚠️ **A playlist that loads is not a stream that plays.** One popular live test stream
  answered **200** but failed in the player (`ERROR_CODE_IO_BAD_HTTP_STATUS`), and
  Apple's "HEVC" sample quietly played its H.264 version. Every stream in the catalog
  was checked by actually playing it.
- 🔴 **Freezes are visible.** Throttling the emulator's network (`adb emu network speed
  edge`) on the live stream produced a real **18 s** freeze: a red count and a red dot
  on the quality line.

> Single runs on two devices on different networks: read them as rough comparisons,
> not benchmarks.

### 📏 Measuring on another device

1. Install the APK on a real Android TV or phone (`adb install app-debug.apk`).
2. Play each version for ~20 s and press BACK. Each test is saved.
3. Read the numbers from the **Log** and the Widevine level from **Settings → This TV**.

### ⚠️ Known limitations

- **A test is saved when you press BACK in the Player.** Pressing HOME pauses it; if
  Android then closes the app in the background, that test is lost. Failed starts are
  saved too (BACK on the error screen).
- **Time to start includes background time** if you press HOME *before* the first frame.
- **Results saved before the database update** (version 1) have no quality mode or test
  duration; they count as Auto, and the summary weights them by how far they played.
- **Forcing a quality causes a short re-buffer**, which counts as a freeze. Those
  results are marked "forced" so they aren't mistaken for network problems.

---

## 🚀 How to run

**You need:** Android Studio 2026.1.1 or newer (it bundles JDK 21), and an
**Android TV** emulator (Device Manager → Television → 1080p) or a real Android TV.
The app also runs on phones.

```bash
git clone https://github.com/shubhh00/StreamTest.git
```

Open the folder in Android Studio, let Gradle sync, and run the `app` configuration.
From the command line:

```bash
./gradlew assembleDebug
```

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

🧪 Unit tests (**53**, plain JVM, no device needed) and lint:

```bash
./gradlew test lintDebug
```

### ➕ Adding streams

The catalog is [`data/src/main/assets/streams.json`](data/src/main/assets/streams.json).
Add a film with up to three versions (`HLS`, `DASH`, `DASH_DRM` + `licenseUrl`), or an
entry under `extras`, then rebuild. Unknown types, and protected streams without a
license URL, are skipped instead of crashing the app.

---

## 🧱 How it's built

Clean architecture in **three Gradle modules**:

| Module | What's in it |
|---|---|
| `:domain` | Plain Kotlin: models, repository interfaces, and the pure math (quality line segments, start-up steps, time in HD, median), all unit-tested |
| `:data` | Android: `streams.json` parsing, device capabilities (`MediaDrm`, `MediaCodecList`, `Display`), Room database, Hilt bindings |
| `:app` | Compose for TV screens, ViewModels, Media3 player and the stats collector |

Screen → ViewModel → repository interface (`:domain`) → implementation (`:data`).

| Purpose | Library |
|---|---|
| Language | Kotlin 2.4 |
| UI | Jetpack Compose + Compose for TV (`tv-material` 1.1) |
| Playback | Media3 1.11: ExoPlayer, HLS, DASH, UI |
| Stats | Media3 `AnalyticsListener`, plus a small pure `PlaybackSession` for the watch clock and freezes |
| DRM | Widevine via Media3 `DrmConfiguration` |
| DI | Hilt |
| Database | Room |
| JSON | kotlinx.serialization |
| Async | Coroutines + StateFlow |
| Tests | JUnit 4 + MockK |
| CI | GitHub Actions: build + unit tests on every push |

`minSdk 26` · `targetSdk 37` · no analytics or tracking SDKs.

📖 [`docs/LEARNING.md`](docs/LEARNING.md) has plain-English notes for each milestone:
HLS vs DASH, how Widevine gets a key, how D-pad focus moves, and the gotchas hit along
the way.

---

## 🙏 Credits

- **Test streams:** Google Media3 & Widevine samples, Shaka Player demo assets,
  Unified Streaming, Mux and Apple examples.
- **Artwork:** Tears of Steel, Big Buck Bunny and Sintel © Blender Foundation,
  [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), via Wikimedia Commons.
- **Font:** Schibsted Grotesk, [SIL Open Font License 1.1](licenses/SchibstedGrotesk-OFL.txt).
