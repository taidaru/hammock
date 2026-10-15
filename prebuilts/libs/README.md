# Prebuilt JARs

Launcher3 and its libraries compile against a few internal AOSP modules.
These JARs come from an AOSP build and are taken from Lawnchair as is.

| File                   | Used by                                         | AOSP command        |
|------------------------|-------------------------------------------------|---------------------|
| framework-15.jar       | the app, `flags`, `plugin_core`, `iconloaderlib` | `m framework`       |
| framework-14.jar       | `animationlib`                                  | `m framework`       |
| SystemUI-statsd-15.jar | the app (`SysUiStatsLog`)                        | `m SystemUI-statsd` |

Location of the generated JARs:

| Module          | Path                                                                                                                     |
|-----------------|--------------------------------------------------------------------------------------------------------------------------|
| Framework       | ./soong/.intermediates/frameworks/base/framework/android_common/turbine-combined/framework.jar                           |
| SystemUI-StatsD | ./soong/.intermediates/frameworks/base/packages/SystemUI/shared/SystemUI-statsd/android_common/javac/SystemUI-statsd.jar |
