#!/bin/bash
for OUTPUT in $(ps -ax | grep jarar | cut -c1-5)
do
	echo killed with this processID $OUTPUT
	kill $OUTPUT
done
PSPATH='jarar/jarar.jar'
PSID=$(ps -ax | grep jarar )
var=$(echo $PSID | cut -f7 -d" ")
sleep 15
if [ "$var" == "" ] || [ "$var" != "$PSPATH" ]
then
	echo "App will be started"
	nohup java -jar jarar/jarar.jar /home/ubuntu/new.txt > /home/ubuntu/mylog.log 2>&1 & 
	echo "Terminal is working in background."
else 
	echo "App already working on back side."
	echo "please control yourscript cause it can't stop your background app."
        $(basename $0) && exit
fi
