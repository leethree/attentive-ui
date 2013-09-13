attentive-ui
============

Research project on attentive UI for Android devices.

by **Li Sirui**, Department of Computer Science, The University of Hong Kong.

## Overview

The system is the implementation of Attentive Gestural User Interfaces, which make use of eye input data from eye trackers to augment touch-screen interfaces.

**Supermonkey**, **trackerd** are core components of the system (**scripts** are tool scripts of the system). **MonkeyDemo** and **FactFinder** are applications running on the system. **HoverLib** is an interface library used by the applications.

## trackerd

Daemon for eye trackers. It works as a service that provides network interfaces for controlling the eye tracker as well as the ability to send eye-tracking data over the network.

### Runtime environment:
* Windows
* Python 2.6
* Tobii SDK 3.0 RC1 for Win32
* A Tobii eye tracker that is supported by the above SDK 

### Run:
```console
python trackerd.py
```

### Network:
Listens to port 10800, connects to Monkey via port 1080.

## SuperMonkey

A client for trackerd, which controls the eye tracker on the host PC from Android device. It is a regular Android app that connects to trackerd via TCP sockets and provides an easy-to-use graphical interface for users.

A modified version of _UI/Application Exerciser Monkey_ by Android is bundled with SuperMonkey.

### Runtime environment:
* _Rooted_ Android 4.2

### Development environment:
* Android SDK 14+
* Android SDK Platform with internal classes (please refer to [this](http://devmaze.wordpress.com/2011/01/18/using-com-android-internal-part-1-introduction/).)

### Install monkey:
1. Install SuperMonkey just as a normal app. 
2. Put file `scripts/supermonkey.sh` into `/data/` on Android file system
3. Correct its permissions.

Please see `scripts/install_monkey.bat` for details.

### Run monkey:
1. Connect Android device with USB Debugging.
2. Set up port forwarding with ADB:
```console
adb forward tcp:1080 tcp:1080
```
3. Run monkey from Android shell:
```bash
sh /data/supermonkey.sh --port 1080 --ignore-crashes
```

Then you can start trackerd and SuperMonkey app. Please see `scripts/monkey.bat` for details.

### Network:
Monkey listens to port 1080 on the Android system.
Supermonkey connects to trackerd via port 10800.

## HoverLib, MonkeyDemo, and FactFinder

**HoverLib**: Library project of Android interface components that supports attentive input.

**MonkeyDemo**: Demo application for the above interface components.

**FactFinder**: Renamed as "FoodFinder" in its interface, sample application that makes use of the components.

### Runtime environment:
* Android 4.2

### Development environment:
* Android SDK 16+
* Android support library v4 and v13
* ActionBarSherlock 4.4.0 with a tiny bit of modification.

You can find the modified ActionBarSherlock [here](https://github.com/leethree/ActionBarSherlock/tree/hover).


----------

For more detailed and in-depth information regarding this project, please refer to my thesis _Attentive Gestural Interface on Touch Screens_ (to be appear at [The HKU Scholars Hub](http://hub.hku.hk/advanced-search?field1=title&thesis=1)).

Last updated: 13 Sep 2013.