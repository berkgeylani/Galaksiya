#!/bin/bash

for OUTPUT in $(ps -ax | grep jarar | cut -c1-5)
do
	echo killed with this processID $OUTPUT
	kill $OUTPUT
done
