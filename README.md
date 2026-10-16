# StreamTest

**A speed test for video on Android TV.** Pick a film, it plays, and the app shows how
well the TV handles it: how fast it starts, which quality it plays at, whether it
freezes, and how much video is buffered. Every test is saved so you can compare.

<!-- DEMO VIDEO: drag the screen recording here in GitHub's editor. -->

The library is built around **films that exist in three versions**: HLS, DASH, and
**Protected** (DASH + Widevine DRM). Because it's the same film each time, the
results compare fairly. For example, you can see how much the license request adds
to the start time. A *More streams* section adds single test streams: 4K at 60 fps,
HEVC, AV1 with DRM, a live stream, and a low-bitrate stream.

No video is hosted or stored; everything streams from free public test sources. The
app only stores test results.

---

## Screens

**Library.** The focused film's artwork behind the list, its three versions with each
one's last result and a mini quality line. Along the top: what this device can decode,
its screen on its own (decoding 4K and showing 4K are different), the sharpest quality
actually played, and what it can't play.

<!-- SCREENSHOT: Library -->

**Player.** Six live numbers (playing at, bitrate, buffered ahead, time to start,
freezes, dropped frames), the start time broken into steps, and a quality line that
doubles as the progress bar. **▼** opens the quality picker: Auto (showing what adaptive
bitrate chose), or force any quality the stream offers.

<!-- SCREENSHOT: Player -->
<!-- SCREENSHOT: Player with the quality picker open -->

**Log.** Every saved test, including ones that never started, grouped by film.
Summary: median time to start, freezes per test, share of *watch time* in HD, and how
many didn't start.

<!-- SCREENSHOT: Log -->

**Settings.** This TV (Widevine level, HDCP), every video decoder, display modes and
HDR, clearing results, and credits.

<!-- SCREENSHOT: Settings -->

### Known limitations

- **A test is saved when you press BACK in the Player.** Pressing HOME pauses it; if
  Android then closes the app in the background, that test is lost. Failed starts are
  saved too (BACK on the error screen).
- **Time to start includes background time** if you press HOME *before* the first frame.
- **Results saved before the database update** (version 1) have no quality mode or test
  duration; they count as Auto, and the summary weights them by how far they played.
- **Forcing a quality causes a short re-buffer**, which counts as a freeze. Those
  results are marked "forced" so they aren't mistaken for network problems.

### Remote controls

| Key | Library | Player |
|---|---|---|
| ▲ ▼ | Move between films, versions and streams | ▲ shows/hides stats · ▼ opens the quality picker |
| ▶ / OK on a film | Go to its versions | — |
| OK on a version | Start the test | Pick a quality (in the picker) |
| BACK | — | Close the picker, or save the test and go back |

---

## Findings

Time from `prepare()` to the **first frame on screen**: one run per stream, debug
build, fresh app start each time.

| Device | Widevine | Screen | Notes |
|---|---|---|---|
| Android TV emulator: Google TV, API 36, x86 | **L3** (software) | 1920×1080, SDR | Decoders: H.264, HEVC, VP8, VP9 hardware; AV1 software |
| Phone: OnePlus 7 (GM1901), Android 12, home Wi-Fi | **L1** (hardware) | 2340×1080, HDR10 / HLG / HDR10+ | Protected video plays through the secure hardware decoder (`OMX.qcom.video.decoder.avc.secure`) |

### Time to first frame

| Film | Version | TV emulator (L3) | Phone (L1) |
|---|---|---|---|
| Tears of Steel | HLS | 3.9 s | 3.9 s |
| | DASH | 2.7 s | 1.4 s |
| | Protected | 5.5 s | 2.2 s |
| Big Buck Bunny | HLS | 1.3 s | 0.5 s |
| | DASH | 2.6 s | 1.6 s |
| | Protected | 3.6 s | 1.8 s |
| Sintel | DASH | 2.1 s | 1.8 s |
| | Protected | 3.5 s | 2.2 s |
| Angel One | HLS | 8.4 s | 2.9 s |
| | DASH | 1.8 s | 1.1 s |
| | Protected | 3.6 s | 2.1 s |

Sintel has no HLS version.

| Extra stream | TV emulator | Phone | What it showed |
|---|---|---|---|
| Big Buck Bunny 4K (60 fps, H.264) | 2.4 s | 2.5 s | Both decoded 3840×2160, though neither screen is 4K |
| Tears of Steel HEVC | 2.6 s | 2.2 s | Real HEVC (`hvc1`) decoding |
| AV1 sample (Widevine) | 3.1 s | 1.8 s | AV1 + DRM |
| Live stream (Unified Streaming) | 3.8 s | 8.4 s | 720p live, no fixed end |
| Apple basic stream (low bitrate) | 3.6 s | 4.0 s | 640×480 on the emulator; climbed to 960×720 on the phone |

### What the numbers say

- **DRM costs much less with hardware Widevine.** Protected vs the same film's clear
  DASH: **+1.0 to +2.8 s** on the emulator (L3, software), but only **+0.2 to +1.0 s** on
  the phone (L1). In one run with the full event log, the phone's license round trip took about 0.7 s.
- **The step-by-step milestones are approximate.** On the phone, the "DRM license
  received" event sometimes arrived just after the "first frame" event and sometimes
  just before. The panel shows milestones in the order they happened, not exact costs.
- **Adaptive bitrate starts low and climbs.** Tears of Steel HLS on the emulator started
  at 784×350 and moved to 1680×750 about 12 s later. On the phone, both Tears of Steel
  and Angel One HLS were still on their lowest quality 25 s in, while their DASH
  versions reached 1920×856 and 720×576. Forcing a quality skips the climb, but costs a
  short re-buffer.
- **The first video chunk often finishes *after* the first frame** on DASH. The
  player starts decoding while a long segment is still downloading.
- **A playlist that loads is not a stream that plays.** One popular live test stream
  answered 200 but failed in the player (`ERROR_CODE_IO_BAD_HTTP_STATUS`), and
  Apple's "HEVC" sample quietly played its H.264 version. Every stream in the
  catalog was checked by actually playing it.
- **Freezes are visible.** Throttling the emulator's network (`adb emu network speed
  edge`) on the single-quality live stream produced a real 18 s freeze: a red count
  and a red dot on the quality line.

> Single runs on two devices on different networks, so read them as rough
> comparisons, not benchmarks.

### Measuring on another device

1. Install the APK on a real Android TV or phone (`adb install app-debug.apk`).
2. Play each version for ~20 s and press BACK. Each test is saved.
3. Read the numbers from the **Log** and the Widevine level from **Settings → This TV**.

---

## How to run

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

Unit tests (51, plain JVM, no device needed) and lint:

```bash
./gradlew test lintDebug
```

### Adding streams

The catalog is [`data/src/main/assets/streams.json`](data/src/main/assets/streams.json).
Add a film with up to three versions (`HLS`, `DASH`, `DASH_DRM` + `licenseUrl`), or an
entry under `extras`, then rebuild. Unknown types, and protected streams without a
license URL, are skipped instead of crashing the app.

---

## How it's built

Clean architecture in three Gradle modules:

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

`minSdk 26`, `targetSdk 37`. No analytics or tracking SDKs.

[`docs/LEARNING.md`](docs/LEARNING.md) has plain-English notes for each milestone:
HLS vs DASH, how Widevine gets a key, how D-pad focus moves, and the gotchas hit
along the way.

---

## Credits

- **Test streams:** Google Media3 & Widevine samples, Shaka Player demo assets,
  Unified Streaming, Mux and Apple examples.
- **Artwork:** Tears of Steel, Big Buck Bunny and Sintel © Blender Foundation,
  [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), via Wikimedia Commons.
- **Font:** Schibsted Grotesk, [SIL Open Font License 1.1](licenses/SchibstedGrotesk-OFL.txt).
