# Roadmap

The goal of the first release (0.1) is the three Hammock features on top of a Lawnchair 15 base
without Quickstep, with Pixel Launcher-like app animations. Everything else is post-MVP.

Phases are done in order. A phase is done when its acceptance criteria are verified on a real Pixel
(see "Build and verification" in `CLAUDE.md`).

## Phase 0. Foundation

- [x] Fork Lawnchair `15-dev`, rename to Hammock, new icon, `applicationId` `io.github.taidaru.hammock`.
- [x] Remove Lawnchair-only files and integrations: store metadata, Crowdin, nightly updater,
      bug report upload, announcements and remote defaults from `lawnchair.app`, Play/nightly channels.
- [x] Commit the current state, create `github.com/taidaru/hammock`, add it as `origin`.
- [x] Smoke test on Android 17: the base targets SDK 35, the device runs SDK 37.
      Home, drawer, widgets, search, settings, work profile, Private Space, rotation.

## Phase 1. Remove Quickstep

Without root a third-party launcher never gets Recents, gesture navigation or remote transitions,
so this code never runs. Removing it first gives a smaller codebase to build the features on.

- [x] Inventory everything that depends on `quickstep/` (see below).
- [x] Remove root features: `root/` (`RootHelperManager`, `autoLaunchRoot`).
- [x] Remove Lawnchair code that only works as the Recents provider: `overview/`, `RecentHelper`,
      `TaskIconUtils`, `TaskUtilLockState`, `LawnchairLockedStateController`, the Quickstep settings screen,
      the QuickSwitch check in `LawnchairApp`, ASI search, the Pause apps shortcut, the status bar clock toggle;
      `isRecentsEnabled` branches keep their "false" side. `isRecentsEnabled` itself stays, hardwired to `false`,
      while Quickstep still reads it.
- [x] Cut the Launcher3 core (`src/`, `res/`) loose from `quickstep/`.
- [x] Make `LawnchairLauncher` extend `Launcher` and build through `src_no_quickstep/` without the `withQuickstep`
      flavor dimension; the variant becomes `lawnGithubDebug`. One step: while Quickstep compiles, its
      `uioverrides.states` expect `RecentsView`. The Quickstep depth controller (wallpaper zoom and blur
      in transitions) is dropped: it needs hidden APIs.
- [x] Remove modules that only Quickstep needed: `quickstep/`, `wmshell/`, `compatLib/`, `hidden-api/`, `androidx-lib/`,
      all of `systemUI/` except `plugin_core/`, and the unused JARs in `prebuilts/libs/`. Kept: `framework-15.jar`
      and `framework-14.jar` (compile-only, for the core and the submodule libraries) and `SystemUI-statsd-15.jar`
      (`SysUiStatsLog`). Dropping `framework-*.jar` needs the hidden API audit from the backlog.
- [x] Remove settings and strings that only made sense with Quickstep: Recents actions and background,
      window corner radius, taskbar on phone, suggested apps in the drawer, ASI search results, and the Lawnchair
      resources only they used. AOSP strings in `res/` stay, as in the AOSP Launcher3 target.

Inventory (as of the Phase 0 base). Each step above keeps the build green and the app working.
- Core `src/`: `LauncherState` uses `uioverrides.states`, which `src_no_quickstep/` provides as in AOSP.
  Mentions of `QuickstepLauncher`, `QuickstepTransitionManager` and `TaskView` in `Launcher`, `BaseActivity`,
  `FloatingIconView` and `ClipIconView` are javadoc only. `res/layout/floating_{split_select,app_pair,widget}_view.xml`
  name `com.android.quickstep.views` classes but only Quickstep inflates them, as in AOSP.
- Lawnchair: `LawnchairLauncher` (base class, Overview/BackgroundApp states, `QuickStepContract` window corner
  radius, `LawnchairQuickstepCompat` launch options), `LawnchairApp` (`RecentsActivity`), `LawnchairProcessInitializer`,
  `ReloadHelper` (`TouchInteractionService`), `LawnchairFloatingSurfaceView` (`QuickstepTransitionManager`),
  `LawnchairUtils`, `res/layout-v28,v31/task_desktop.xml`.
- Modules used outside Quickstep and kept: `flags` (`com.android.launcher3.Flags`) and `plugincore`
  (Launcher3 plugin interfaces). `android.app.search.SearchTarget` (`SearchTargetCompat`) compiles against
  `framework-15.jar`.
- Not a Quickstep dependency: the "Open Recents" gesture uses `AccessibilityService.GLOBAL_ACTION_RECENTS`
  and stays. Gesture Nav Contract (`handleGestureContract`) is how Pixel Launcher animates the swipe home
  into a third-party launcher and stays.

Acceptance: no references to Quickstep classes; app launch and close animations, widgets, drag and drop,
work profile, Private Space and predictive back on home all work as before.

## Phase 2. Pixel Launcher animations

The app open and close animations and the return to home differ from Pixel Launcher, which hurts
the experience too much to leave for later.

Decisions:
- Public APIs only. The app open window animation cannot be changed by a third-party launcher on Android 17:
  WindowManager applies `ActivityOptions` animations to a launched task only for `overrideTaskTransition`
  (hidden API, system permission) or scene transitions (`Transition.shouldApplyAnimOptionsToTask` in AOSP);
  clip reveal, scale up, thumbnail and custom animations were verified on the device to be ignored.
  Pixel Launcher uses a remote animation, which also needs a system permission. So the open animation stays
  the system default, and Hammock only animates its own home screen in step with it.
- The swipe-home animation is driven by Pixel Launcher through the Gesture Nav Contract; Hammock reports the
  icon position and draws the icon surface (`LawnchairFloatingSurfaceView`). The window travelling above the
  dock before the icon settles is the same in Pixel Launcher. The home screen fading in (most visible on the
  dock over a bright wallpaper) also comes from Pixel Launcher: its `FallbackSwipeHandler` animates the home
  task alpha of a third-party launcher from 0 to 1 with `ACCELERATE`, and the launcher cannot change it.
- The Home button with gesture navigation plays the system default close transition (no Gesture Nav Contract,
  `DefaultTransitionHandler`); the black background before the wallpaper appears is the system's and cannot be
  changed through public APIs.

Tasks:
- [x] Swipe home: remove the translucent rectangles over the dock and the search bar (the "dock blur"). They came
      from Lawnchair's "app closing animation" overlay (`FullScreenOverlayView`, default "suck in"), drawn on top
      of the system animation; the default is now "none".
- [x] Swipe home: settle the home screen in 350 ms (AOSP `CONTENT_SCALE_DURATION`) instead of Lawnchair's 1050 ms.
- [x] App open: scale the home screen (and fade All Apps) in step with the system open animation, as AOSP does
      (`AppLaunchContentAnimator`).
- [x] Swipe home: the window sometimes flew to the wrong place and the icon came from another side. Lawnchair's
      `LawnchairFloatingSurfaceView` sent the previous app's icon position when the app had no icon on screen and
      moved the icon surface one frame late; both now follow AOSP `FloatingSurfaceView`.

Acceptance: side-by-side recordings with Pixel Launcher on the same phone: opening from the home screen,
the dock, the drawer and search; closing with the swipe gesture and the Home button; work profile apps.

## Phase 3. App drawer sections

The drawer is split into up to 5 swipeable sections, like the Personal / Work tabs.

Decisions:
- Each app belongs to exactly one section. The first section is the main one: unassigned and newly
  installed apps go there.
- At most 5 sections (`MAX_DRAWER_SECTIONS`). With a single section the tab bar is hidden, which is
  the Pixel Launcher look.
- The work profile is an extra system section at the end, shown only when the profile exists,
  not counted in the limit, with the pause button as today.
- Private Space stays at the bottom of the main section.
- Search covers all sections.
- Apps inside a section are sorted alphabetically (manual order is post-MVP).
- Moving an app: "Move to…" item in the icon long-press menu.
- Sections are created, renamed, reordered and deleted in settings. Deleting a section returns its apps
  to the main section.
- The first section is "Personal" by default; it can be renamed but not deleted.
- The tab bar shows at most three tabs at a time. With more sections every tab is a third of the bar,
  long titles are ellipsized, and the bar follows the pager with the active tab centered; tabs out of view
  slide in and out past the screen edges like the app pages.
- Lawnchair's drawer folders and drawer categories are both removed: inside a section the drawer is a plain
  alphabetical list, as in Pixel Launcher. The folder Room tables are replaced by section tables in one
  migration; Hammock has no released users, so folder data is not migrated.
- Core hook: `MAIN`, `WORK` and `SEARCH` keep their adapter indices; extra sections get adapters from index 3,
  and the core maps pager pages to adapters (main section, extra sections, work). Section logic lives in
  `app.lawnchair.sections`.

Tasks:
- [x] Data: Room entities for sections and app-to-section mapping (keyed by component and user):
      `DrawerSections` and `DrawerSectionApps` (database version 4), `DrawerSectionsRepository`. The main section
      is implicit: apps without a row belong to it, its title is a `PreferenceManager2` setting.
- [x] Core hook: generalize the two adapters (`AdapterHolder.MAIN` / `WORK`) in
      `ActivityAllAppsContainerView` and the Personal / Work tab bar to N tabs: extension points
      `getExtraSectionCount`, `getSectionFilter`, `onBindSectionTabs`, `onSectionsChanged`.
- [x] Sections in the drawer: a tab and a page per section (`DrawerSectionsController`), the tab strip
      (`DrawerSectionTabStrip`); changes apply live.
- [x] "Move to…" system shortcut (`MoveToSectionShortcut`): drawer apps of the main profile, while there is
      more than one section.
- [ ] Settings screen for sections (Compose).
- [x] Remove Lawnchair drawer folders and categories (drawer list, settings, routes, strings). The core keeps
      its now unused drawer-folder rendering; the categorization utilities stay for Lawndeck until Phase 4.
- [ ] Strings in English and Russian.

Acceptance: 1 and 5 sections, swipe and tab switching, search, install and uninstall of an app,
work profile pause, Private Space, rotation, launcher restart.

## Phase 4. Optional app drawer

A "home screens only" mode, off by default.

Decisions:
- Own implementation; Lawnchair's Lawndeck is removed.
- Turning the mode on places every app that is not on a home screen onto new pages at the end,
  alphabetically, work profile apps included. Nothing is ever lost.
- Turning it off leaves the home screens as they are and brings the drawer back.
- New apps are added to the home screen automatically; uninstalled apps are removed as usual.
- Swipe up runs a configurable gesture action (existing Lawnchair gesture handlers); default is search.
- Search opens the drawer panel directly in search mode, without the app list.
- Private Space is not supported in this mode for now; the setting shows a warning.

Tasks:
- [ ] Setting in `PreferenceManager2` and its UI.
- [ ] Placing apps on enable, as a background model task.
- [ ] Auto-add new apps while the mode is on.
- [ ] Swipe-up action and search-only panel.
- [ ] Remove Lawndeck (`deck/`, its settings).

Acceptance: enable and disable with 0, a few and many apps; install and uninstall; work profile;
restart; rotation; no app missing in either direction.

## Phase 5. Choosable home page

Decisions:
- Chosen in workspace edit mode (long-press on the home screen, `LauncherState.EDIT_MODE`):
  a home button above each page.
- Stored as a screen ID, not an index, so deleting other pages does not shift it.
  If the home page itself is removed, fall back to the first page.

Tasks:
- [ ] Core hook: replace `Workspace.DEFAULT_PAGE` usages with a call to our code.
- [ ] Home button in edit mode.
- [ ] Handle page removal.

Acceptance: Home button, returning from apps, launcher restart, page add and remove, rotation.

## Phase 6. Release 0.1

- [ ] Own versioning (`0.1.0`), version shown on the About screen.
- [ ] Release signing key, stored as GitHub secrets.
- [ ] Release workflow: signed APK attached to a GitHub Release on a tag.
- [ ] `CHANGELOG.md`.

## Post-MVP backlog

- Brand leak from Phase 0: "Lawnchair" is still in the text of translated strings (63 `values-*` files, e.g.
  "Managed by Lawnchair" in English variants) and in the sponsored search provider description.
- Hidden API audit: remove hidden API use in `src/` and Lawnchair (`Unseal`, ChickenHook RestrictionBypass,
  `framework-15.jar` on the compile classpath), as `CLAUDE.md` requires public APIs only.
- Background blur in transitions and the drawer through the public `Window.setBackgroundBlurRadius` (API 31).
  Wallpaper zoom has no public API.
- Icon customization: improve per-app icon customization.
- Themed (adaptive color) icons: some icons are not recolored in the mode where all icons share one
  color palette; make them follow it.
- Manual app order in sections: alphabetical until the first manual move, new apps go to the end,
  "Sort alphabetically" action in the section menu. Needs a solution for the long-press conflict
  (long-press currently drags the icon to the home screen).
- Drag an app onto another section's tab.
- Private Space access in drawer-less mode.
- Review which Lawnchair features to keep (currently all are kept).
- Verify Startpage web suggestions still work without Lawnchair partner parameters.
- Revisit the base: Lawnchair `15-dev` is frozen; `16-dev` targets Android 17.

## Known platform limitations

Checked on a Pixel 9 Pro with Android 17; a third-party launcher cannot change these through public APIs.

- App open window animation: see Phase 2.
- Swipe home fades the home screen in (most visible on the dock): Pixel Launcher's `FallbackSwipeHandler`
  animates the third-party home task alpha from 0 to 1. See Phase 2.
- Leaving Recents for home goes through Pixel Launcher's fallback `RecentsActivity.startHomeInternal()`
  (`StartHomeFromRecents`, 250 ms); Hammock is ready to draw within ~20 ms. Once seen as the last card
  freezing and then cutting to home, later not reproduced. With a card other than the last one centered the
  animation is wrong, the same as with Pixel Launcher as the home app.
- Cold app start: the app's splash screen fades in full screen and, in the dark theme, leaves a near-black frame
  between the splash icon and the app content. With Pixel Launcher the same splash grows from the icon, so it is
  less visible; Hammock cannot change the window animation (see Phase 2).
