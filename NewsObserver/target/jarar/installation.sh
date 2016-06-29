#!/bin/bash

for OUTPUT in $(ps -ax | grep jarar | cut -c1-5)
do
	echo killed with this processID $OUTPUT
	kill $OUTPUT
done


echo "In jarar directory"
echo "App will be started"
java -jar jarar/jarar.jar /home/ubuntu/new.txt 2>&1  | tee output.txt & << 'ENDAPP'
disown
ENDAPP
