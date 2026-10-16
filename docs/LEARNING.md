# Learning notes

Plain-English notes written after each milestone, for interview preparation.

---

## Milestone 1 — Skeleton

### What was built

- A Gradle project with three modules, following clean architecture:
  - `:domain`: plain Kotlin with no Android code. Empty for now; models and
    repository interfaces arrive in Milestone 2.
  - `:data`: an Android library that will hold Room, JSON parsing and device
    info. Empty for now.
  - `:app`: the Android app with UI, navigation and Hilt.
  - Dependencies point inwards: `app → data → domain`, and `app → domain`.
- Hilt is set up (`@HiltAndroidApp`, `@AndroidEntryPoint`, one `@HiltViewModel`).
- A dark theme with the Schibsted Grotesk font and the design colors.
- The TV manifest: leanback feature, no touchscreen required, banner, and
  `LEANBACK_LAUNCHER`.
- Three placeholder screens (Library, Log, Player) connected with Compose
  Navigation. The remote's BACK button works.
- Checked on the `Television_1080p` emulator (API 36).

### New concepts

**Android TV apps are normal Android apps.** Nothing special is installed. A few
manifest lines make the app show up on a TV:
- `LEANBACK_LAUNCHER` puts the app on the TV home screen.
- `android:banner` is the 320×180 tile the TV home screen shows (TVs use this wide
  banner instead of a square icon).
- `touchscreen required="false"` matters because Android assumes every app needs
  touch, and TVs have none.

**D-pad focus.** A TV has no touch. One item on screen is always "focused" (the
highlighted one). The arrow keys move focus to the nearest focusable item in that
direction, and OK clicks it. Compose works out "nearest" from the positions on
screen.

**Compose for TV (`androidx.tv.material3`).** This is the TV version of Material.
A clickable `Surface` from this library is focusable and changes its colors by
itself when it gains focus. Our style (filled off-white, dark text, no scaling)
is set once in `FocusFillSurface`.

**FocusRequester.** A handle attached to one item with `Modifier.focusRequester(...)`.
Calling `requestFocus()` moves the D-pad focus there. When a screen opens, nothing
is focused until a key is pressed, so we request focus on the first row from a
`LaunchedEffect`.

**Navigation arguments reach the ViewModel.** The route `player/{streamId}` carries
the stream id. Navigation copies it into `SavedStateHandle`, which Hilt passes
into `PlayerViewModel`.

**Variable fonts.** `schibsted_grotesk.ttf` is one file that contains every weight.
In Compose, we declare the same file three times with `FontVariation.weight(400/500/600)`.
This needs API 26 or higher, which matches our minSdk.

### Gotchas

- **Android Studio limits the AGP version.** Each Android Studio release can only
  sync up to a certain Android Gradle Plugin version. AGP 9.4.1 built fine with
  `./gradlew` from the terminal, but Android Studio 2026.1.1 refused to sync it
  ("Latest supported version is AGP 9.2.1"). The fix was to use AGP 9.2.1.
  "Sync" is only the IDE loading the project; the build itself is done by Gradle.
- **AGP 9 has Kotlin built in.** The Android modules don't apply the
  `org.jetbrains.kotlin.android` plugin any more. Only `:domain` (pure Kotlin)
  applies `kotlin.jvm`.
- **`hiltViewModel()` moved.** It now comes from
  `androidx.hilt:hilt-lifecycle-viewmodel-compose`
  (`androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`). The old
  `hilt-navigation-compose` import is deprecated.
- **Run `./gradlew test`, not `testDebugUnitTest`.** `:domain` is plain Kotlin and
  only has a `test` task.
- **dp size of a TV.** A 1080p TV runs at density 2.0, so the screen is
  **960×540 dp**, not 1280×720. Sizes taken from a 1280×720 design need
  converting.

---

## Milestone 2 — Library

### What was built

- `streams.json` with 4 films and 5 extra streams. Every URL was checked to
  answer with a real HLS playlist or DASH manifest.
  - Tears of Steel, Big Buck Bunny and Angel One have all three versions.
  - Sintel has no HLS version, so its row shows "Not available".
  - Extras: 4K 60 fps, HEVC, AV1 (with DRM), a live stream and a low-bitrate stream.
  - Actual playback is checked in Milestone 3 (clear streams) and Milestone 5 (DRM).
- `:domain`: catalog models (`Film`, `StreamVersion`, `ExtraStream`), device models,
  and two repository interfaces.
- `:data`:
  - JSON parsing with kotlinx.serialization.
  - Reading the asset file.
  - Reading device capabilities.
  - A Hilt `@Module` that connects each interface to its implementation.
- `:app`:
  - The Library screen: films on the left, the selected film's versions on the
    right, "More streams" below the films, and the device footer.
  - `LibraryViewModel` holds which item is selected.
- 6 unit tests: JSON parsing (good file, missing version, unknown type, DRM
  without a license URL) and the footer's plays / can't-play lists.

### New concepts

**HLS vs DASH, in one line each.** Both split a video into small chunks at several
qualities and give the player an index file:
- **HLS** (Apple) uses an `.m3u8` text *playlist*.
- **DASH** uses an `.mpd` XML *manifest*.

The player picks a quality, downloads chunks, and switches quality as the network
changes.

**Why Protected streams need a license URL.** The video chunks are encrypted. To
play them, the player sends a request to a *license server*, which answers with the
decryption key. No license URL means no key, so the parser skips such an entry.

**Widevine levels.** `MediaDrm(C.WIDEVINE_UUID).getPropertyString("securityLevel")`:
- **L1**: keys and decoding stay inside secure hardware. Netflix and other paid
  services need this for HD/4K.
- **L3**: software only.

The Android TV emulator reports **L3**.

**What a TV can decode vs display.** `MediaCodecList` lists the *decoders* (HEVC,
AV1…). HDR and 4K are about the *screen*, read from `Display`. Dolby Vision needs
both a decoder and a screen that supports it.

**Hilt `@Binds`.** The ViewModel asks for `CatalogRepository` (an interface in
`:domain`). `DataModule` tells Hilt to hand it `AssetCatalogRepository` (from
`:data`). This is how clean architecture keeps the UI unaware of where the data
comes from.

**Steering D-pad focus.** By default, Compose moves focus to the item that is
geometrically nearest in the pressed direction. `Modifier.focusProperties { right = … }`
overrides that:
- RIGHT on any film always lands on its first available version.
- LEFT from a version returns to its film.
- RIGHT on an extra does nothing (`FocusRequester.Cancel`).

**Focus drives what's shown.** `onFocusChanged` on each film tells the ViewModel
which film is focused, and the right column re-draws with that film's versions.

**Scrolling with the D-pad.** A `Column` with `verticalScroll` scrolls by itself
when focus moves to an item below the edge. Compose brings the focused item into view.

### Gotchas

- **54sp film titles didn't fit.** On a 960dp-wide TV, "Big Buck Bunny" doesn't
  fit in half the screen at 54sp, so the titles are 48sp.
- **The "4K HEVC" Apple sample is 1080p.** Its playlist tops out at 1920×1080, so
  it's labelled "up to 1080p", not 4K.
- **Several popular Bitmovin test URLs now return 403 Forbidden.** Check URLs
  instead of trusting old lists.
- **A debug build takes up to 6 seconds to open on the emulator.** A D-pad press
  sent before the first frame is lost, because nothing has focus yet. This only
  matters for scripted tests.

---

## Milestone 3 — Playback

### What was built

- `PlayerFactory` builds the `ExoPlayer` and a `MediaItem` for a stream. For
  Protected streams it also adds the Widevine `DrmConfiguration`.
- `PlayerViewModel` owns the player: it looks up the stream, calls `prepare()`,
  and calls `release()` in `onCleared()`.
- The Player screen shows the video full screen with Media3's `PlayerView`
  inside `AndroidView`, with the built-in controls switched off.
- Playback pauses when the app goes to the background and resumes when it comes back.
- BACK returns to the Library with focus on the exact row that was opened.
- Every stream in the catalog was played on the Android TV emulator (API 36,
  Widevine L3). Two extras were replaced because they didn't do their job.

### New concepts

**What ExoPlayer does after `prepare()`:**
1. It downloads the playlist (HLS) or manifest (DASH).
2. It picks a starting quality.
3. It downloads the first video and audio chunks.
4. It decodes them and shows the first frame.

`playWhenReady = true` means "start as soon as enough is buffered".

**MediaItem and MIME type.** A `MediaItem` says *what* to play. We set the MIME
type (`APPLICATION_M3U8` or `APPLICATION_MPD`) explicitly, so Media3 doesn't have
to guess from the URL. Some URLs end in `.ism/.m3u8`, which is easy to guess wrong.

**Adaptive bitrate in action.** Tears of Steel HLS started at 784×350 and switched
to 1680×750 about 12 seconds later, once the player had measured the network speed.
Starting low and climbing is normal ABR behaviour.

**Widevine on the emulator.** All Protected streams played. The log showed
`drmKeysLoaded`, which means the license server sent the key. The emulator only
has L3 (software). A real TV with L1 decodes protected video in secure hardware.

**Why `useController = false`.** `PlayerView` comes with its own play/pause bar.
We hide it because we draw our own overlay in Compose. The built-in controls would
also grab D-pad focus.

**Who owns the player.** The ViewModel creates and releases the player, so it
isn't recreated when the screen recomposes. `release()` matters on TVs, which
have very few hardware video decoders.

**Restoring focus after BACK.** Leaving for the Player removes the Library from
composition, but its ViewModel stays alive on the back stack. The ViewModel
remembers `lastOpenedStreamId`. When the Library comes back, a `LaunchedEffect`
calls `requestFocus()` on that row's `FocusRequester`.

**`LifecycleStartEffect`.** Runs code when the screen becomes visible, and runs
`onStopOrDispose` when it's hidden (HOME on the remote) or closed. We use it to
pause the video when nobody can see it.

**`EventLogger`.** One line, `player.addAnalyticsListener(EventLogger())` (debug
builds only), prints every Media3 event to Logcat under the tag `EventLogger`. It
was used to verify each stream: `renderedFirstFrame`, `videoInputFormat` (the
resolution and codec playing) and `drmKeysLoaded`.

### First results on the Android TV emulator (debug build)

Time from `prepare()` to the first frame:

| Film | HLS | DASH | Protected |
|---|---|---|---|
| Tears of Steel | 3.9 s | 2.7 s | 5.5 s |
| Big Buck Bunny | 1.3 s | 2.6 s | 3.6 s |
| Sintel | — | 2.1 s | 3.5 s |
| Angel One | 8.4 s | 1.8 s | 3.6 s |

The Protected version is roughly 1–3 s slower to start than plain DASH. Most of
that is the license request. These are single runs on an emulator; Milestone 4
measures this properly.

### Gotchas

- **A stream that answers 200 can still fail in the player.** The Akamai live
  playlist loaded, but playback failed with `ERROR_CODE_IO_BAD_HTTP_STATUS`, so it
  was replaced with Unified Streaming's live stream.
- **A stream that offers both H.264 and HEVC may never use HEVC.** For Apple's
  "HEVC" sample, ExoPlayer picked the H.264 version, so it didn't test HEVC at all.
  It was replaced with an HEVC-only DASH stream.
- **Sintel and Angel One's DASH versions are VP9, not H.264.** The codec differs
  between versions, which matters when comparing results.

---

## Milestone 4 — Stats

### What was built

- `StatsCollector`: a Media3 `AnalyticsListener` that turns player events into
  `StateFlow<LiveStats>`. A `PlaybackStatsListener` is attached next to it to count freezes.
- The Player overlay:
  - **Bottom panel:** title and time, then six big numbers (playing at, bitrate,
    buffered ahead, time to start, freezes, dropped frames), then the quality line.
  - **Top-right panel:** "Start time, step by step".
  - **▲** hides and shows the overlay.
- Pure functions in `:domain`, with unit tests:
  - `buildQualityLine()` turns quality changes and freezes into segments and dots.
  - `steps()` turns the start-up milestones into steps with gaps.
  - `standardHeight()` gives the "1080p" name for any resolution.
- 9 new unit tests, 18 in total.

### New concepts

**AnalyticsListener.** Media3's "tell me everything" listener. The callbacks used:

| Callback | Used for |
|---|---|
| `onLoadCompleted` | Manifest loaded (`DATA_TYPE_MANIFEST`) and first video chunk (`DATA_TYPE_MEDIA`) |
| `onDrmKeysLoaded` | The license server sent the key |
| `onRenderedFirstFrame` | Time to start |
| `onVideoInputFormatChanged` | Resolution, codec and bitrate; each change is an ABR switch |
| `onDroppedVideoFrames` | Frames the device was too slow to show |
| `onPlaybackStateChanged` | Freezes: BUFFERING after the first frame while playing |

**PlaybackStatsListener.** A ready-made listener that keeps session counters.
`totalRebufferCount` and `totalRebufferTimeMs` are the freeze numbers.

**Polling vs events.** Buffered ahead (`player.totalBufferedDuration`) and the
playhead change all the time without an event, so the ViewModel reads them every
500 ms in a `viewModelScope` loop, which stops by itself when the ViewModel is cleared.

**`SystemClock.elapsedRealtime()`.** A clock that never jumps. The wall clock can be
changed by the network or the user, which would break timings.

**`@UnstableApi` / `@OptIn`.** Media3 marks advanced APIs (AnalyticsListener,
PlaybackStatsListener, EventLogger) as "may change between versions". Using them
means opting in on purpose.

**Catching remote keys.** Keys go to whatever has focus. The Player screen has no
buttons, so the screen itself is `focusable()`, requests focus on start, and
catches `Key.DirectionUp` in `onKeyEvent`. It returns `false` for other keys, so
BACK still works.

**Drawing with Canvas.** The quality line is plain `drawRect` and `drawCircle`
calls in a Compose `Canvas`, with no extra libraries.

### Gotchas

- **The first video chunk often finishes *after* the first frame.**
  `onLoadCompleted` fires when the whole chunk has downloaded. On DASH with long
  segments, the player decodes and shows the first frame before that. That's why
  the step is called "fully downloaded" and the steps are listed in the order they
  happened. The note "milestones, not exact costs" in the spec is about this.
- **Live streams have no fixed timeline.** Their position is inside a moving
  window, so for live streams the quality line uses *time watched* instead of
  playback position.
- **Use `onDrmKeysLoaded(eventTime, keyRequestInfo)`.** Media3 1.11 deprecated the
  one-argument version.
- **Testing freezes:** `adb emu network speed edge` throttles the emulator's
  network. On a single-quality live stream this caused a real 18 s freeze, shown as
  a red count and a red dot. `gsm` was so slow that playback failed with
  `ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT`.
- **Tabular numbers also widen the "." in values like "1.3".** That's a font
  detail, not a bug.

---

## Milestone 5 — DRM + quality picker

### What was built

- **Protected streams.** Widevine playback already worked from Milestone 3 (the
  `DrmConfiguration` in `PlayerFactory`). All four Protected streams play on the
  emulator's Widevine L3, and the start panel shows the "DRM license received" step.
- **The Quality panel** (top left):
  - ▼ opens it.
  - It lists **Auto**, with what Auto is playing right now ("now 720p · 7.2 Mbps"),
    then every quality the stream offers, sharpest first, with its resolution and bitrate.
  - OK forces that quality, and OK on Auto gives control back to the player.
  - BACK closes the panel; a second BACK leaves the Player.

### New concepts

**Tracks and track groups.** `player.currentTracks` lists everything the stream
offers, in groups. An adaptive stream has one video group holding the same video
at several qualities, and the player may switch freely between the tracks of a group.

**Adaptive bitrate (ABR) = "Auto".** The player measures how fast chunks download
and keeps choosing the best quality the network can sustain. In this test, Auto
started at 144p, went to 480p, then 720p, then 1920×856 as the bandwidth estimate grew.

**Forcing a quality.** A `TrackSelectionOverride(group, trackIndex)` means "for this
group, use exactly this track". It's set through
`player.trackSelectionParameters.buildUpon().setOverrideForType(...)`.
`clearOverridesOfType(C.TRACK_TYPE_VIDEO)` turns Auto back on. The player re-selects
right away and re-buffers from the current position: forcing 144p took about 4 seconds.

**`BackHandler`.** Catches BACK before the NavHost does, but only while
`enabled = true`. Here it's enabled only while the Quality panel is open, so the
first BACK closes the panel and the next BACK leaves the screen.

**Keeping focus inside a panel.** `Modifier.focusProperties { onExit = { cancelFocusChange() } }.focusGroup()`
stops D-pad focus from leaving the panel while it's open.

**Key events bubble up.** A key press goes to the focused item first, then to its
parents. The Player screen catches ▲ / ▼ on the parent `Box`, so it ignores them
while the panel is open. Otherwise ▼ couldn't move down the list.

### Gotchas

- **▼ opens the panel, but BACK closes it.** ▼ is needed to move down the list,
  so it can't also close the panel.
- **Changing quality causes a short re-buffer, and it counts as a freeze.** Forcing
  144p gave a 2.7 s freeze, and going back to Auto gave another. The viewer really
  does see that stall, but it isn't a network or TV problem.
- **Emulator only has Widevine L3.** L1 (hardware-protected) playback needs a real
  Android TV or phone, for the README findings.

---

## Milestone 6 — Results

### What was built

- **Saving.** BACK in the Player saves a `TestResult` to Room, but only if the first
  frame was shown, then leaves the screen. If the Quality panel is open, BACK
  closes it first.
- **Library rows** show the last saved result per stream ("1507 ms to start",
  "4143 ms to start · 1 freeze") instead of "Not tested yet".
- **Log screen:**
  - The device name and test count.
  - A summary line: median time to start · freezes per test · % of time in HD or better.
  - A table grouped by film: Film · Version · To start · Freezes · a 3dp quality
    line · When. Focus starts on the first row, and ▲▼ moves through the rows.
- **New unit tests:** stats math (time in HD, median, summary), grouping by film,
  and a database-row round trip. 27 tests in total.

### New concepts

**Room in three parts:**
- `@Entity`: a table with one column per field.
- `@Dao`: an interface of queries; Room writes the code for it.
- `@Database`: ties them together.

The DAO's `observeAll(): Flow<List<…>>` re-runs the query and sends a new list
whenever the table changes. That's why the Library updates right after a save,
without any refresh code.

**"Latest per stream" in SQL:**
`WHERE id IN (SELECT MAX(id) FROM test_results GROUP BY streamId)`. The biggest
auto-generated id in each group is the newest row.

**JSON columns as a shortcut.** The quality timeline and the start milestones are
stored as JSON text (kotlinx.serialization). We never search inside them, only
redraw them, so this is simpler than two extra tables. A unit test checks that a
result survives the trip to a row and back unchanged.

**Hilt `@Provides` vs `@Binds`:**
- `@Binds` maps an interface to a class Hilt can already build (it has an
  `@Inject` constructor).
- `@Provides` is a function for things built another way, like
  `Room.databaseBuilder(...)`.

**Saving before leaving.** `onExit(close)` launches a coroutine that awaits the
insert, then calls `close()` (`popBackStack`). The Library comes back already
showing the new result. A flag ignores a second BACK press during the save.

**`api` vs `implementation` in Gradle.** `:domain` exposes `Flow` in its public
interfaces, so it uses `api(coroutines)`, which makes `Flow` visible to the
modules that depend on `:domain`.

### Gotchas

- **The focus background hid the quality line's track.** The focused Log row
  first used the same color as the empty track, so the track vanished. Focused
  rows now use their own shade (`RowFocusColor`).
- **Results are only saved through BACK.** Pressing HOME, or the app being killed,
  saves nothing. That's fine for v1, and the spec asks for exactly this.
- **A forced quality switch is saved as a freeze too** (see Milestone 5).

---

## Polish pass (after Milestone 6)

### What changed

- **Numbers:** tabular widths now apply to the *digits only* (`tabularDigits()`
  builds an `AnnotatedString`), so "1.3 Mbps" no longer looks like "1 . 3 Mbps".
- **Player:** a quiet "Starting…" message with the step being waited on (playlist,
  DRM license, first video) until the first frame. The bottom stats panel is
  shorter, so more of the picture shows.
- **App icon:** an adaptive vector icon drawn as a tiny quality line, with dim,
  mid and bright segments, a red freeze dot and the playhead. The TV banner moved
  to `drawable-xhdpi`, where Android TV expects it.
- **Lint is clean.** Media3's `Format` bitrate fields are `@UnstableApi`, so
  `bitrateOrNull()` opts in, in one small function. `app/lint.xml` records the two
  warnings we accept, with the reason for each.
- **ViewModel tests with MockK:** `LibraryViewModelTest` and `LogViewModelTest`
  mock the repositories. 34 unit tests in total.
- **Removed unused things:** `LibraryState.isLoading`, the Room Gradle plugin entry,
  and MockK in modules that don't use it.

### New concepts

**MockK.** `mockk<CatalogRepository>()` makes a fake object.
`coEvery { repo.getCatalog() } returns catalog` sets what a `suspend` function
returns (`every` is for normal functions). Returning a `MutableStateFlow` from the
mock lets a test "save a result" by setting its value.

**Testing ViewModels on the JVM.** `viewModelScope` runs on `Dispatchers.Main`,
which only exists on a device. `Dispatchers.setMain(UnconfinedTestDispatcher())` in
`@Before` swaps in a test dispatcher that runs coroutines immediately, and
`resetMain()` in `@After` puts it back.

**AnnotatedString.** A string with styles on parts of it. Here it gives digits the
"tnum" font feature and leaves everything else alone.

**Adaptive icons (Android 8+).** A background layer and a foreground layer that
the launcher masks into its own shape. Keep the drawing inside the middle 66dp of
the 108dp canvas, because the rest may be cropped.

**Android Lint.** Run `./gradlew lintDebug`. It found real issues: unstable API use
without opt-in, a missing app icon, and a banner in the wrong folder.

---

## Home screen + Settings

### What changed

- **Library backdrop:** the focused film's artwork (Tears of Steel, Big Buck Bunny,
  Sintel) fills the screen behind the list. It crossfades when focus moves and is
  darkened by gradient scrims so text stays readable. The images are CC BY 3.0
  © Blender Foundation, from Wikimedia Commons, resized to 1280px (about 130–175 KB
  each) and credited in Settings → About.
- **Motion:** the selected film title grows and the others settle smaller
  (`animateFloatAsState` + `graphicsLayer`, no recomposition per frame).
- **Data on the rows:** each tested version shows its last test's mini quality line (no box behind it: on the off-white focus fill it switches to dark shades),
  freezes included.
- **Capability strip:** outlined chips for what the TV plays, then "Can't play…" in
  the problem color.
- **Settings is a real screen** (list/detail, like the Library):
  - This TV
  - Video decoders
  - Display
  - Saved results, with a two-step clear
  - About & credits
- **Navigation:** Log and Settings open on top of the Library (`popUpTo(LIBRARY)`),
  so BACK from any tab returns home.
- **Tests:** `SettingsViewModelTest` (decoder grouping, two-press clear, cancel on
  leaving). 37 unit tests in total.

### New concepts

**`Crossfade`.** Animates between two versions of some content whenever its
`targetState` changes. Here that's the drawable id of the focused film's artwork.

**Scrims.** A gradient from the background color to transparent, laid over a photo
so text on top stays readable. It's the one place the spec allows a gradient.

**Explicit resource mapping instead of lookup by name.** `backdropFor(filmId)` is a
plain `when` returning `R.drawable.…`. `Resources.getIdentifier("backdrop_$id")`
would also work, but it's slow and fails silently if a name is mistyped, while the
`when` is checked by the compiler.

**More of `MediaDrm` and `MediaCodecInfo`.** `getPropertyString("version")` and
`"maxHdcpLevel"` describe the Widevine module and HDMI protection.
`MediaCodecInfo.isHardwareAccelerated` (Android 10+) tells a hardware decoder chip
from a software one.

**Destructive actions on a remote.** A TV has no undo and OK is easy to press twice,
so clearing takes two presses, and moving to another section cancels the half-done
clear. A unit test checks that `deleteAll()` is called only on the second press.

**Steering focus out of content.** LEFT from the Clear button would go to whichever
section was geometrically nearest. `focusProperties { left = … }` sends it back to
"Saved results".

---

## Consistent D-pad focus

### What changed

- **Current tab underline:** the current screen's tab has a short underline on
  every screen, so "where am I" (underline) is never confused with "where is the
  focus" (filled box).
- **No more focus jumps:** pressing past an edge (RIGHT on the last column, LEFT
  past the first tab, RIGHT on a Settings section with nothing to press) now keeps
  focus where it is. Before, Compose picked the nearest item anywhere on screen,
  often a header tab.
- **Entering the header** from below lands on the current screen's tab.
- **DOWN from the header** returns to the selected film, extra or Settings section.
- **Artwork for extras:** extras that are one of our films reuse its artwork
  (Big Buck Bunny 4K, Tears of Steel HEVC, and the AV1 sample, which is also Big
  Buck Bunny). The live and Apple test patterns say "Artwork not available".

### New concepts

**How D-pad focus search works.** On each arrow press Compose looks for the
*geometrically nearest* focusable item in that direction, anywhere on screen. When
there's nothing in that direction nearby, "nearest" can be something far away.

**`focusGroup()` + `focusProperties { onEnter / onExit }`.** A focus group is an area
treated as one unit:
- `onExit` runs when focus is about to leave the area; `cancelFocusChange()` keeps it
  inside. Our `blockFocusExit(…)` helper wraps this.
- `onEnter` runs when focus comes in; the header uses it to jump straight to the
  current tab.

**Explicit targets beat clever restoring.** `focusRestorer()` sounded right ("return
to the last focused item"), but in testing it did not restore when focus arrived by
arrow key from the header. An explicit `focusProperties { down = selectedItemFocus }`
on the header tabs does, so it was used instead.

**Testing focus without screenshots.** `adb exec-out uiautomator dump /dev/tty` prints
the screen's accessibility tree. The node marked `focused="true"` names the focused
item, so a script can press keys and check where focus went after each press.

---

## Measurement fixes after a review

### What changed

- **`PlaybackSession`** (in `:domain`, unit-tested) now owns the watch clock and the
  freezes. `StatsCollector` just feeds it player events with the current time.
  - **Watched time leaves out paused and background time.** Before, a live stream's
    timeline kept running while HOME had it paused.
  - **A freeze still going when you press BACK is closed and saved.** Before, the
    count said 1 but the line had no dot and no duration.
  - **The count, total and dots come from one place.** That replaced
    `PlaybackStatsListener`, which can't be finalized or tested.
- **Failed starts are saved**, with an outcome (`PLAYED`, `FAILED_TO_START`,
  `FAILED_WHILE_PLAYING`) and Media3's error code. Rows say "Didn't start · io network
  connection timeout", and the Log summary counts them.
- **Results remember** whether a quality was forced (and which) and how long the test
  was watched.
- **A saving error can't crash BACK** (`saveSafely`).
- **"Time in HD" is weighted by watch time**, so a 10-minute test counts more than a
  10-second one.
- **"Can decode 4K" is separate from "4K screen".** The Library strip shows the screen
  on its own, plus "played up to …p" from real results.
- **Database version 2,** with a migration that keeps existing results.
- **New test-card icon and TV banner,** made from our font's letters as vector outlines.
- 51 unit tests, up from 37.

### New concepts

**Injecting time to make timing testable.** `PlaybackSession` never reads a clock;
every call passes `nowMs`. A test can then say "first frame at 0, HOME at 10 s, back
at 70 s" and check that 15 s were watched at 75 s, with no device and no waiting.

**Room migrations.** Changing a table means bumping `@Database(version = …)` and
giving Room a `Migration(old, new)` with the SQL to upgrade. SQLite can't make a NOT
NULL column nullable in place, so `MIGRATION_1_2` creates a new table, copies the rows
across with defaults for the new columns, drops the old table and renames the new one.
It was checked by installing the new app over the old database: all saved tests were
still there.

**Saving without crashing.** `saveSafely()` catches exceptions and returns `false`, but
re-throws `CancellationException`. In coroutines, cancellation is how "stop this work"
is signalled, so swallowing it would be a bug.

**Duration-weighted averages.** Averaging percentages per test lets a 5-second test
outweigh a 10-minute one. Weighting each test's share by its watch time gives the share
of *all watch time*.

**Reported vs tested.** `MediaCodecInfo.VideoCapabilities.isSizeSupported(3840, 2160)`
says a decoder *claims* 4K. "Played up to 2160p" in the strip comes from a test that
actually decoded it.

**Vector text without fonts.** Vector drawables can't use fonts, so the icon's
letters were converted to outline path data (Windows' GDI+ `GraphicsPath.AddString`).
That gives the real Schibsted Grotesk shapes, sharp at every size.

### Gotchas

- **Forcing a test failure on the emulator.** Airplane mode didn't cut its network,
  and `ping` fails even when it's online. Throttling with `adb emu network speed gsm`
  made the player time out before the first frame
  (`ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT`, after about 75 s).
- **TV banners are 320×180 px at xhdpi, which is 160×90 dp.** Lint caught the banner
  declared as 320×180 dp.
- **A phone reports its screen as portrait** (1080×2340), so "largest mode height"
  read "2340p screen". A resolution like "1080p" means the short side, so the code
  now uses `minOf(width, height)`.
- **The app is locked to landscape** (`sensorLandscape` in the manifest). In portrait
  on a phone, the TV layout overflowed and the film list could be dragged by touch.
  TVs are always landscape, so this only affects phones used for testing. Android 16
  ignores fixed orientations on large screens only, not on phones.

---

## Continuous integration (GitHub Actions)

### What was built

`.github/workflows/build.yml` runs `./gradlew assembleDebug test` on every push and
pull request. The README shows a badge with the result of the latest run.

### New concepts

**What CI is for.** A clean machine builds the project on every push. It catches
"works on my machine" problems: a file not committed, a test broken by a change in
another module, a setting that only lives in Android Studio.

**A workflow file.** A workflow is YAML in `.github/workflows/`. `on:` says when it runs,
`runs-on:` picks the machine, and `steps:` either run a shell command (`run:`) or use a
ready-made action (`uses:`):
- `actions/checkout` gets the code.
- `actions/setup-java` installs the JDK.
- `gradle/actions/setup-gradle` caches Gradle downloads between runs.

**No Android setup step.** GitHub's Ubuntu runners come with the Android SDK, and the
Android Gradle Plugin downloads the missing SDK platform (37) itself.

### Gotchas

- **`gradlew` must be executable in git** (mode `100755`), or Linux refuses to run it.
  Projects created on Windows sometimes lose that.
