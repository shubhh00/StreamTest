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
