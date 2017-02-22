#!/bin/bash

if [ "$1" == "" ]
then
	echo "Error: java class to execute is required."
	exit 1
fi

java -cp "build/libs/face.jar:libs/opencv-3.2.0.jar:lib/*" -Djava.library.path=/usr/local/share/OpenCV/java uk.ac.cam.cl.quebec.face.$@
