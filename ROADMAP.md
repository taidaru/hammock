# Roadmap

The goal of the first release (0.1) is the three Hammock features on top of a Lawnchair 15 base
without Quickstep. Everything else is post-MVP.

Phases are done in order. A phase is done when its acceptance criteria are verified on a real Pixel
(see "Build and verification" in `CLAUDE.md`).

## Phase 0. Foundation

- [x] Fork Lawnchair `15-dev`, rename to Hammock, new icon, `applicationId` `io.github.taidaru.hammock`.
- [x] Remove Lawnchair-only files and integrations: store metadata, Crowdin, nightly updater,
      bug report upload, announcements and remote defaults from `lawnchair.app`, Play/nightly channels.
- [ ] Commit the current state, create `github.com/taidaru/hammock`, add it as `origin`.
- [ ] Smoke test on Android 17: the base targets SDK 35, the device runs SDK 37.
      Home, drawer, widgets, search, settings, work profile, Private Space, rotation.

## Phase 1. Remove Quickstep

Without root a third-party launcher never gets Recents, gesture navigation or remote transitions,
so this code never runs. Removing it first gives a smaller codebase to build the features on.

- [ ] Inventory everything that depends on `quickstep/`: `LawnchairLauncher extends QuickstepLauncher`,
      taskbar, overview, recents-related settings and gesture actions, accessibility "open Recents" action.
- [ ] Make `LawnchairLauncher` extend `Launcher`; build through `src_no_quickstep/`.
- [ ] Replace the `withQuickstep` flavor dimension; the variant becomes `lawnGithubDebug`.
- [ ] Remove root features: QuickSwitch, `RootHelperManager`.
- [ ] Remove modules that only Quickstep needed (`quickstep/`, `wmshell/`, `systemUI/`, `hidden-api/`, `prebuilts/`),
      each only after checking nothing else uses it.
- [ ] Remove settings and strings that only made sense with Quickstep.

Acceptance: no references to Quickstep classes; app launch and close animations, widgets, drag and drop,
work profile, Private Space and predictive back on home all work as before.

## Phase 2. App drawer sections

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
- Lawnchair drawer folders are removed; their Room tables are reused for sections.

Tasks:
- [ ] Data: Room entities for sections and app-to-section mapping (keyed by component and user).
- [ ] Core hook: generalize the two adapters (`AdapterHolder.MAIN` / `WORK`) in
      `ActivityAllAppsContainerView` and the Personal / Work tab bar to N tabs.
- [ ] "Move to…" system shortcut.
- [ ] Settings screen for sections (Compose).
- [ ] Remove Lawnchair drawer folders (UI, settings, routes).
- [ ] Strings in English and Russian.

Acceptance: 1 and 5 sections, swipe and tab switching, search, install and uninstall of an app,
work profile pause, Private Space, rotation, launcher restart.

## Phase 3. Optional app drawer

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

## Phase 4. Choosable home page

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

## Phase 5. Release 0.1

- [ ] Own versioning (`0.1.0`), version shown on the About screen.
- [ ] Release signing key, stored as GitHub secrets.
- [ ] Release workflow: signed APK attached to a GitHub Release on a tag.
- [ ] `CHANGELOG.md`.

## Post-MVP backlog

- Manual app order in sections: alphabetical until the first manual move, new apps go to the end,
  "Sort alphabetically" action in the section menu. Needs a solution for the long-press conflict
  (long-press currently drags the icon to the home screen).
- Drag an app onto another section's tab.
- Private Space access in drawer-less mode.
- Review which Lawnchair features to keep (currently all are kept).
- Verify Startpage web suggestions still work without Lawnchair partner parameters.
- Revisit the base: Lawnchair `15-dev` is frozen; `16-dev` targets Android 17.
