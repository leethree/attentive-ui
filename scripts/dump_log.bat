echo ====== start of log dump ======
date /T
time /T
adb logcat -d -s -v time FFApp.Log:I
adb logcat -c
echo ====== end of log dump ======
