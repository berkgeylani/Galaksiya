#!/bin/bash
echo "Screen's sessions killed."
pkill screen
echo "Screen's sessions created."
echo "app will be run."
screen -d -m -S bla bash -c 'java -jar jarar.jar /home/ubuntu/new.txt'
