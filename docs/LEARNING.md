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
