# Script to start "hovermonkey" on the device, which has a very rudimentary
# shell.
# Fix permissions before running the script:
# chmod 755 supermonkey.sh
# chown root.shell supermonkey.sh
ret=`pm path hk.hku.cs.srli.supermonkey`
package=${ret#package:}
base=/system/bin
export CLASSPATH=$package
trap "" HUP
exec app_process $base com.android.commands.monkey.Monkey $*
