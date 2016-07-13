#!/bin/bash
killBackGroundApp () {
for i in {1..7}
do
	for PROCESSID in $(ps -ax | grep jarar | cut -c1-5)
	do
		echo killed with this processID $PROCESSID
		kill $PROCESSID
	done
	PSPATH='jarar/jarar.jar'
	PSID=$(ps -ax | grep jarar )
	PSNAMEFROMTERMINAL=$(echo $PSID | cut -f7 -d" ")
	sleep 15
	if [ "$PSNAMEFROMTERMINAL" == "" ] || [ "$PSNAMEFROMTERMINAL" != "$PSPATH" ]
	then
		echo "App will be started"
		nohup java -jar jarar/jarar.jar /home/ubuntu/new.txt > /home/ubuntu/mylog.log 2>&1 & 
		echo "Terminal is working in background."
		break
	else 
		echo "App already working on back side."
		echo "please control your script because it can't stop your background app."
		echo "App cant be killed.Please control your app or installation.sh"
	fi
done
}


for PROCESSID in $(ps -ax | grep jarar | cut -c1-5)
do
	echo killed with this processID $PROCESSID
	kill $PROCESSID
done
PSPATH='jarar/jarar.jar'
PSID=$(ps -ax | grep jarar )
PSNAMEFROMTERMINAL=$(echo $PSID | cut -f7 -d" ")
sleep 15
if [ "$PSNAMEFROMTERMINAL" == "" ] || [ "$PSNAMEFROMTERMINAL" != "$PSPATH" ]
then
	echo "App will be started"
	nohup java -jar jarar/jarar.jar /home/ubuntu/new.txt derby > /home/ubuntu/mylog.log 2>&1 & 
	echo "Terminal is working in background."
else 
	echo "App already working on back side."
	echo "please control your script because it can't stop your background app."
	killBackGroundApp
fi




