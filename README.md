<p align="center">
  <img src="/mbcompass_txt.png" width="700"  alt="MBCompass Logo Text"/>
</p>

<h3 align="center">Modern FOSS Compass and Navigation app for Android</h3>

<p align="center"> 
  <a href="https://www.gnu.org/licenses/gpl-3.0">
    <img src="https://img.shields.io/badge/license-GPL%20v3-2B6DBE.svg?style=for-the-badge" alt="GPLv3 License"/>
  </a>
  <a href="https://github.com/MubarakNative/MBCompass/releases">
    <img src="https://img.shields.io/github/v/release/MubarakNative/MBCompass?include_prereleases&color=4B95DE&style=for-the-badge" alt="Latest Release"/>
  </a>
  <a href="https://apilevels.com/">
    <img src="https://img.shields.io/badge/API-23%2B-1450A8?style=for-the-badge" alt="Min API"/>
  </a>
</p>

<h4 align="center">
  <a href="/CONTRIBUTING.md">Contribute</a> | 
  <a href="https://github.com/MubarakNative/MBCompass/blob/main/LICENSE">License</a>
</h4>

<div align="center" style="display: flex; justify-content: center; align-items: flex-start; flex-wrap: wrap;">

  <a href="https://groups.google.com/g/mbcompass-beta">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="170" alt="Get it on F-Droid"/>
  </a>
  <a href="https://f-droid.org/app/com.mubarak.mbcompass">
    <img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" width="170" alt="Get it on F-Droid"/>
  </a>
  <a href="https://github.com/CompassMB/MBCompass/wiki/Installing-MBCompass-from-GitHub-using-Obtainium">
    <img src="https://raw.githubusercontent.com/MubarakNative/mb-icons-pack/refs/heads/main/obt_badge.png" width="170" alt="Get it on Obtainium" />
  </a>
  <a href="https://apt.izzysoft.de/fdroid/index/apk/com.mubarak.mbcompass">
    <img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="64" alt="Get it on IzzyOnDroid" />
  </a>
</div>

## About

**MBCompass** is a modern, free, and open-source compass and navigation app built from the ground up for Android, without **ads**, **IAP**, or **tracking**. Built with Jetpack Compose, it supports compass and navigation features while being **lightweight**, simple and battery-efficient.

>Not just a compass. Not a full navigation app.
>
>MBCompass sits in between, a focused **navigation app** for hiking, trekking and everyday use, combining direction, live location, and track recording without unnecessary complexity.

---

<p align="center">
  <img width="40%" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" />
  <img width="40%" src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" />
</p>

<p align="center">
  <img width="40%" src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" />
  <img width="40%" src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" />
</p>

<p align="center">
  <img width="40%" src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" />
  <img width="40%" src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" />
</p>

<p align="center">
  <img width="40%" src="fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" />
  <img width="40%" src="fastlane/metadata/android/en-US/images/phoneScreenshots/8.png" />
</p>


## Features

### Compass & Navigation
- Accurate compass with **magnetic north** and **true north**
- Sensor fusion for improved accuracy (accelerometer, magnetometer, gyroscope)
- Real-time magnetic field strength display (**µT**)
- Live GPS location tracking on OpenStreetMap
- Keeps screen on during navigation

### Track Recording & Maps
- Real-time track recording with start, pause, and resume
- Smooth polyline rendering for clear path visualization
- Track statistics: distance, duration, average speed, elevation gain/loss (if available), min/max altitude
- Export tracks as **GPX files** via the system file picker
- View, star, and sort all saved tracks in one place
- Offline Mapsforge vector maps loaded from your device
- Optional online OpenStreetMap tiles
- Switch between offline and online maps in Settings

### App
- Light, Dark, and **AMOLED** themes
- Extremely lightweight (**~2 MB**) and battery efficient
- Landscape orientation support
- Clean Jetpack Compose UI with Material Design
- No ads, no tracking, no in-app purchases
- No Google Play Services dependency (Works on Custom ROM's too)
- Supports Android 6.0+

---

> [!NOTE]
> **v1.1.16 is currently in closed beta on Google Play.**
> This version includes offline Mapsforge map support, per-app language
> settings, full Compose map migration, and a GPS waypoint density fix.
>
> Once testing is complete, the same build will be released on both
> Google Play and F-Droid simultaneously.
>
> **Join MBCompass Google Group**: https://groups.google.com/g/mbcompass-beta
>
> **Opt-in for tester:** https://play.google.com/apps/testing/com.mubarak.mbcompass
>
> **Install:** https://play.google.com/store/apps/details?id=com.mubarak.mbcompass

---

## Roadmap

<details>
  <summary><strong>Q2 planned features</strong></summary>
  <br/>

- Topographic map support (contours)
- Bearing to waypoint
- Gpx import
</details>

---

## Recognition

MBCompass has gained recognition from the global developer community:

-  #13 Product of the Day on Product Hunt
-  Featured in two consecutive issues of Android Weekly
-  Reached the front page of Hacker News
-  OSM newsletter

[See full highlights](./Featured.md)

## Permissions

MBCompass only requests the minimum permissions required for navigation and tracking features:

- **Location**
  - Used to detect and display your current location on the map
  - Required for real-time track recording and navigation features

- **Notification (Android 13+)**
  - Used to show foreground service notifications during active tracking
  - Enables track progress visibility and quick controls (pause/resume)

MBCompass does **not** collect, store, or share any personal data.
All location and tracking data stays on your device unless you explicitly export it (e.g., GPX files).

## Translations

MBCompass is open for **community translations** on [Weblate](https://hosted.weblate.org/engage/mbcompass/)!  
You can help make the app accessible to more users by translating it into your language.

<a href="https://hosted.weblate.org/engage/mbcompass/">
<img src="https://hosted.weblate.org/widget/mbcompass/287x66-grey.png" alt="Translation status" />
</a>

##  Contributing

Contributions are welcome! If you encounter bugs or have feature suggestions, please open an issue or submit a pull request. See [Contributing Guidelines](CONTRIBUTING.md) for details.

## Support

MBCompass is developed independently and will always remain free, open source, and ad-free.

If you find it useful, consider supporting its continued development.

[Support MBCompass](https://compassmb.github.io/donate.html)

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

MBCompass is Free Software: you can use, study, share, and improve it at your will. You may use, modify, and redistribute this project only if your modifications remain open-source under the same license.

> Proprietary use, commercial redistribution, or publishing modified versions with ads or tracking is strictly prohibited under GPLv3 or later.

>[!NOTE]
> **AI Notice**
> Due to the increasing use of AI/LLM tools in modifying and redistributing FOSS projects, this notice is provided for clarity.
>
> Redistributing this project (modified or unmodified) **without proper attribution** is a violation of the GPL-3.0 license.
>
> This includes cases where modifications are made using AI/LLM tools.
>
> All redistributions must retain copyright notices, provide attribution, and indicate changes.

See more information [here](https://github.com/MubarakNative/MBCompass/blob/main/LICENSE).

###  Artwork License:
Compass rose : [MBCompass rose](https://github.com/MubarakNative/MBCompass/blob/main/app/src/main/res/drawable/mbcompass_rose.xml) (c) 2025 by Mubarak Basha is licensed under **CC BY-SA 4.0**