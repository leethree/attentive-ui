:: kill existing monkey first.
adb shell "killall com.android.commands.monkey"
adb forward tcp:1080 tcp:1080
adb shell "sh /data/supermonkey.sh --port 1080"
