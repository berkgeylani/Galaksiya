#!/bin/bash
for OUTPUT in $(ps -ax | grep jarar | cut -c1-5)
do
	echo killed with this processID $OUTPUT
	kill $OUTPUT
done
PSPATH='github/Galaksiya/NewsObserver/target/jarar/jarar.jar'
PSID=$(ps -ax | grep jarar )
var=$(echo $PSID | cut -f7 -d" ")
sleep 3
if [ "$var" == "" ] || [ "$var" != "$PSPATH" ]
then
	echo "App will be started"
	nohup java -jar github/Galaksiya/NewsObserver/target/jarar/jarar.jar /home/francium/new.txt > /home/francium/mylog.log 2>&1 & 
	echo "Terminal is working in background."
else 
	echo "App already working on back side."
	echo "please control yourscript cause it can't stop your background app."
fi
