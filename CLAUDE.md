# CLAUDE.md

Guidelines for Claude (and humans) working in this repository.

## Project idea

**Hammock** is a home screen launcher for **regular Google Pixel phones without root**.
It looks and behaves like Pixel Launcher, plus a few features of its own.

It is a fork of [Lawnchair](https://github.com/LawnchairLauncher/lawnchair) (branch `15-dev`,
Launcher3 from Android 15) with Quickstep removed.

### Target users and devices

- Pixel phones with a regular (non-foldable) form factor, no root.
- The launcher is an ordinary third-party app. No system permissions, platform signature or Magisk modules.

### Own features (the reason this project exists)

1. **App drawer sections, at most 5.** The drawer is split into swipeable sections, like the
   Personal / Work tabs. They replace Lawnchair's drawer folders (`lawnchair/src/app/lawnchair/data/folder/`).
2. **Optional app drawer.** A "home screens only" mode: all apps live on the home screens,
   newly installed apps are added to a screen automatically, swipe up is reassigned.
3. **Choosable default home screen.** Any page can be the home page, not only the first one.
   In Launcher3 this is the constant `Workspace.DEFAULT_PAGE = 0` (`src/com/android/launcher3/Workspace.java`),
   which `moveToDefaultScreen()` and the Home button handling in `Launcher.java` rely on.
   Replace it with a configurable value and handle deletion of that page.

### Non-goals

- **Quickstep**: the Recents screen, navigation gesture handling, remote transition animations,
  taskbar, split screen, desktop windowing. Without root Android hands all of this to Pixel Launcher anyway,
  so it is dead code. Do not bring it back or add dependencies on it.
- **Root features**: QuickSwitch, `RootHelperManager` and anything similar.
- **Foldables and tablets**: two-panel layouts, taskbar, special grids. Don't break them
  on purpose, but don't spend time on them.
- Publishing to Google Play is not a goal for now (it has its own restrictions, e.g. on AccessibilityService).

## Source layout

| Path | Contents | How to treat it |
|---|---|---|
| `src/` | Launcher3 core from AOSP (Java) | Touch **minimally and surgically** |
| `lawnchair/src/app/lawnchair/` | Lawnchair code (Kotlin) | May be changed |
| `src_no_quickstep/` | Stubs replacing Quickstep (removed) | Part of the build, as in the AOSP Launcher3 target |
| `platform_frameworks_libs_systemui/` | git submodule (iconloaderlib, animationlib, etc.) | Do not edit directly |
| `res/`, `lawnchair/res/` | Launcher3 and Lawnchair resources | New resources go into our resource folder |

Useful places in the Lawnchair code:
- Settings: `preferences2/PreferenceManager2.kt` (DataStore) and `preferences/PreferenceManager.kt` (legacy SharedPreferences).
- Compose settings screens: `ui/preferences/`, routes in `ui/preferences/navigation/PreferenceRoutes.kt`.
- App drawer: `allapps/`. Drawer folders: `data/folder/` (Room: `FolderEntity`, `FolderDao`, `FolderService`).
- Main class: `LawnchairLauncher.kt`, extends `Launcher`.

## Good practices

### Where to put new code

- **New code goes into a separate package** (e.g. `lawnchair/src/app/lawnchair/<our-module>/`)
  instead of being spread over existing files. The smaller the diff in upstream files, the easier it is to see what is ours.
- **Only "hooks" go into the Launcher3 core (`src/`)**: replace a constant with a method call, add an
  extension point, call our code. The logic itself lives in our package.
- Every change in an upstream file is marked exactly as described in "Marking changes in upstream files".

### Language and style

- **Everything in the codebase is written in English only**: code, identifiers, comments, commit messages,
  documentation, resource names. The only exception is translated strings in `values-*/strings.xml`.
- New code is **Kotlin**. Don't rewrite existing Launcher3 Java files in Kotlin without a reason.
- [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html); the style of the surrounding code beats personal preference.
- **Keep comments to a minimum.** Write them only to explain complex logic or non-obvious behavior.
  Don't restate what the code already says; prefer clear names over comments.
- Settings UI is **Jetpack Compose**, like the rest of Lawnchair. The launcher UI itself
  (home screen, drawer) is the Launcher3 View system; don't bring Compose there.
- User-facing strings live only in resources (`strings.xml`), translated into English and Russian.

### Features and settings

- **Every feature of ours sits behind a toggle in settings**, with a safe default
  (behaves like Pixel Launcher). New settings go through `PreferenceManager2`.
- Limits and constants (e.g. the maximum of 5 folders) are defined in one place as a named constant, no magic numbers.
- Think about **migration**: what happens to user data when a feature is enabled, disabled, or the app
  is updated. Example: disabling the app drawer must never make an app "disappear"; lay them out on the home screens.

### Platform compatibility

- **Public Android APIs only.** No hidden APIs, no reflection into system classes, no `hidden-api`,
  no system permissions. If a feature can't be done without them, it is not our feature.
- Support the work profile and Private Space (via `LauncherApps`); don't break them.
- The target platform is current Android on Pixel. Verify behavior changes in new Android versions
  against the official documentation, not from memory.

### Performance

- A launcher is always in memory and always visible: no heavy work on the main thread,
  no unnecessary home screen redraws.
- Database and `PackageManager` queries run in the background (coroutines / Launcher3 executors such as `MODEL_EXECUTOR`).

## Marking changes in upstream files

An upstream file is any file that came from another project: Lawnchair, AOSP, the submodule.
Apache 2.0 (section 4(b)) requires modified files to state that they were changed and by whom,
and the markers keep our changes easy to find. The format below is fixed; don't vary it.

### 1. File notice

Every modified upstream file gets exactly one notice line:

```
Modifications copyright <YEAR>, Taidaru
```

- `<YEAR>` is the year of the first Hammock change to the file; it is never updated later.
- The holder is the same as in `LICENSE.txt`.

Placement:

- **The file has a license header comment**: add an empty comment line and the notice as the last lines
  of that comment, using the header's own line prefix. Existing notices (e.g. Lawnchair's) stay untouched,
  ours goes below them.

  ```
   * Modifications copyright 2021, Lawnchair
   *
   * Modifications copyright 2026, Taidaru
   */
  ```

- **The file has no license header**: add a new comment as the very first lines of the file
  (after the `<?xml ...?>` declaration in XML), followed by one empty line:

  Kotlin, Java, Gradle, KTS:

  ```
  /*
   * Modifications copyright 2026, Taidaru
   */
  ```

  XML:

  ```
  <!-- Modifications copyright 2026, Taidaru -->
  ```

  YAML, properties, TOML, shell, Python:

  ```
  # Modifications copyright 2026, Taidaru
  ```

### 2. Change markers

Each change is marked with a one-line comment:

| File type | Marker |
|---|---|
| Kotlin, Java, Gradle, KTS | `// hammock: <description>` |
| XML | `<!-- hammock: <description> -->` |
| YAML, properties, TOML, shell, Python | `# hammock: <description>` |

- `<description>` is English, starts with a lowercase letter, has no trailing period, fits on one line
  and says why the change was made (for a removal: what was removed and why).
- **Changed or added block**: the marker goes on its own line directly above the block,
  with the block's indentation. One marker per contiguous block.
- **Removed block**: the marker goes on its own line where the block used to be,
  and starts with `removed`: `// hammock: removed the nightly flavor, Hammock ships only the github channel`.
- **No marker** for import changes and formatting-only changes.
- **File-level marker**: if the same mechanical change runs through the whole file (e.g. renaming
  Lawnchair to Hammock in strings) or most of the file is rewritten, use a single marker on its own line
  right after the file notice instead of per-block markers.
- These markers are the one exception to the "minimal comments" rule.

### 3. When nothing is needed

- Files we created, and upstream files whose content we replaced entirely (e.g. `README.md`, the launcher icon).
- Deleted files.

## Plan

The phases, decisions and backlog are in `ROADMAP.md`. Update it when a phase task is done
or a decision changes.

## Build and verification

```bash
git submodule update --init --recursive   # after cloning: the platform_frameworks_libs_systemui submodule
./gradlew assembleLawnGithubDebug
./gradlew installLawnGithubDebug
```

- Run Gradle on JDK 21 (as in the upstream CI); bytecode targets Java 17 (`jdkRelease` in `gradle/libs.versions.toml`).
  Android Gradle Plugin and Kotlin versions come from the same file.
- Android SDK: platform `android-36.1` and build-tools `36.1.0` (see `build.gradle`).
- NixOS: set `android.aapt2FromMavenOverride=<ANDROID_HOME>/build-tools/36.1.0/aapt2`
  in `~/.gradle/gradle.properties`, otherwise the `aapt2` downloaded from Maven won't run.
- A change is done only when it is **built and verified on a real Pixel** (or a Pixel emulator):
  set the launcher as the default home app, check the feature both enabled and disabled,
  launcher restart, screen rotation, work profile.
- Before committing non-trivial changes, run `./gradlew lint` for the affected module.

## Lawnchair base

- Hammock is based on Lawnchair `15-dev` and is not synced with it: there is no `upstream` remote,
  and moving to a newer Lawnchair version is not planned for now.
- `origin` is our repository.
- If a specific upstream fix is needed, port it by hand as a separate commit that names the source
  commit, and mark the changes as usual.

## Commits

- Conventional commits, like Lawnchair: `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`, `style:`.
- One commit, one logical change. Quickstep removal is a separate series of commits,
  not mixed with new features.

## License

The project is based on Lawnchair and Launcher3 and, like them, is distributed under the **Apache License 2.0**
(`LICENSE.txt`).

Every modified file that came from another project must say so; see "Marking changes in upstream files".

Credit Lawnchair in the README and in the "About" screen. Don't use Lawnchair's
name, logo or `applicationId` (`app.lawnchair*`); we have our own.
