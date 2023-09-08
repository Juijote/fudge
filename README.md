# fujiapp
This is a reverse-engineered alternative to Fujifilm's official WiFi app. This is an Android-only project with a very simple goal to serve as a photo viewer
and downloader. It will also serve as a testbed for a portable implementation Fujifilm's superset of PTP/IP, and possibly a more advanced and cross-platform app in the future.

![screenshots](https://eggnog.danielc.dev/f/76-s9xg1g9bj8rm7u1r604z92yy4xyitc.png)

This app isn't finished yet, so don't set high expectations. Fuji's version of PTP/IP has many different quirks and features, so it's not easy to implement them all from
a single camera. Currently, it's only tested on the X-A2, but functionality for newer cameras will slowly be implemented (as well as bluetooth) over time.

It will be available [here](https://play.google.com/store/apps/details?id=dev.danielc.fujiapp) once finished.

## Help needed
- WiFi Packet dumps of using either of the official apps (send to brikbusters@gmail.com)
- Dart & Flutter UI dev

## Tech stack
- Java & native Android activities
- JNI & Portable C99
- [camlib](https://github.com/petabyt/camlib) with Java communication backend

Here's a confusing flow chart of how the app is put together:
![image of a disaster](docs/fudge-chart.png)

## TODO:
- [x] Stable communication with camera (connect, disconnect, ping)
- [x] Poll camera for unlocked event
- [x] Tested & working on X-A2
- [x] thumbnail gallery of images
- [x] ZoomView image viewer + button to download to DCIM/fuji
- [x] Downloading progress bar popup
- [x] Share images (Discord doesn't seem to like it)
- [ ] Implement Bluetooth communication (decode encrypted packets)
- [ ] Polish UI (more animations)
- [ ] Implement 100s of Fuji property codes, determine the unknown ones
- [ ] Implement liveview (video stream on another port)
- [ ] Reimplement front-end in Flutter
