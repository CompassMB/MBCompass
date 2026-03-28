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
>MBCompass sits in between, a focused **navigation utility** for hiking, trekking and everyday use, combining direction, live location, and track recording without unnecessary complexity.

---

<p align="center">
  <img width="400" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" />
  <img width="400" src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" />
</p>
<p align="center">
  <img width="400" src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" />
  <img width="400" src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" />
</p>

## Features

### Core Compass & Navigation
- Displays clear cardinal directions with both **magnetic north** and **true north**
- Live **GPS location tracking** on _OpenStreetMap_
- Sensor fusion for improved accuracy (_accelerometer_, _magnetometer_, _gyroscope_)
- Shows magnetic field strength in **µT**

### Tracking & GPX
- **Real-time track recording** directly on the map (start, pause, resume)
- **Track management screen** to view and organize all saved tracks
- **Track statistics**:
    - Distance, duration, average speed
    - Elevation gain/loss
    - Min/max altitude
- Smooth polyline rendering for recorded tracks
- **Waypoint recording** during tracking
- **GPX export support** using system file picker
- **Share tracks** via Android share sheet
- **Star and sort tracks** (favorites, organization)

### App Experience
- Light and dark theme (AMOLED Dark also supported) support via Settings
- Keeps screen on during navigation
- Custom bottom bar for ease navigation
- Landscape orientation support
- Built with Jetpack Compose and Material Design
- No ads, no in-app purchases, no tracking
- No Google Play Services dependency
- Uses Android’s native location APIs
- Runs on Android 6.0+

[Learn more on the website](https://compassmb.github.io/MBCompass-site/#features)

---

## Upcoming (MBCompass Q2 Roadmap & Design Preview)

<details>
  <summary><strong>Planned improvements and features currently under active development</strong></summary>

  <br />

  <p align="center">
    <img width="600" src="/MBCompass_Q2.png" alt="MBCompass Q2 design preview"/>
  </p>

- Offline maps (evaluating lightweight approaches)
- Topographic (topo) map support
- Navigation UX improvements and refinements

</details>

---

## Recognition

MBCompass has gained recognition from the global developer community:

-  #13 Product of the Day on Product Hunt
-  Featured in two consecutive issues of Android Weekly
-  Reached the front page of Hacker News

[See full highlights](./Featured.md)

## Permissions

MBCompass only requests the minimum permissions required for navigation and tracking features:

- **Location**
    - Used to detect and display your current location on the map
    - Required for real-time track recording and navigation features

- **Notification (Android 13+)**
    - Used to show foreground service notifications during active tracking
    - Enables track progress visibility and quick controls (start/pause/stop)

- **Activity Recognition (Android 10+)**
    - Used to improve tracking accuracy by detecting user movement state (e.g., walking)

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

Open-source projects couldn't survive in the long run without donations or funding.

MBCompass is a fully open-source project - free of ads, trackers, or in-app purchases.
If you find it useful, consider supporting its continued development and maintenance:

Find more info on [MBCompass page](https://compassmb.github.io/MBCompass-site/donate.html)

Your support helps ensure the project stays sustainable and continues to improve for everyone. Thank you!

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

MBCompass is Free Software: you can use, study, share, and improve it at your will. You may use, modify, and redistribute this project only if your modifications remain open-source under the same license.

> Proprietary use, commercial redistribution, or publishing modified versions with ads or tracking is strictly prohibited under GPLv3 or later.

See more information [here](https://github.com/MubarakNative/MBCompass/blob/main/LICENSE).

###  Artwork License:
Compass rose : [MBCompass rose](https://github.com/MubarakNative/MBCompass/blob/main/app/src/main/res/drawable/mbcompass_rose.xml) © 2025 by Mubarak Basha is licensed under **CC BY-SA 4.0**