# Hammock

A home screen launcher for Google Pixel phones that feels like Pixel Launcher
but lets you shape it the way you use your phone. No root required.

Hammock keeps the familiar Pixel look and behavior by default. Everything beyond that is opt-in:
turn on what you need and leave the rest exactly as it was.

## Why Hammock

- **Flexible.** Most of the home screen and app drawer behavior is configurable,
  from icons, fonts and gestures to how the drawer is organized.
- **Convenient.** Features are designed around everyday use: fewer taps, no surprises,
  and nothing disappears when you change a setting.
- **Pixel first.** Built and tested for regular Pixel phones, as an ordinary app.
  No system permissions and no root.

## Features

Planned for the first release:

- **App drawer sections.** Split the drawer into up to five sections and switch between
  them with a swipe, the same way as between personal and work apps.
- **Optional app drawer.** Keep all apps on your home screens. New apps are added there
  automatically, and swipe up can do something else.
- **Any page as home.** Choose which home screen page the Home button returns to.

Inherited from Lawnchair:

- Icon packs, themed icons, fonts and Material You colors.
- Customizable grid, dock, labels and search bar.
- Gestures: double tap, swipe up and down, Home and Back buttons.
- Search across apps, contacts, files and the web.
- At a Glance widget.
- Backup and restore of your layout and settings.
- Work profile and Private Space support.

See [ROADMAP.md](ROADMAP.md) for what is being worked on.

## Building

Requirements: JDK 21, Android SDK platform `android-36.1` and build-tools `36.1.0`.

```bash
git submodule update --init --recursive
./gradlew assembleLawnGithubDebug
```

## Credits

Hammock is based on [Lawnchair](https://github.com/LawnchairLauncher/lawnchair),
which is based on Launcher3 from the Android Open Source Project.
Many thanks to the Lawnchair team and contributors.

## License

Apache License 2.0, see [LICENSE.txt](LICENSE.txt).
