#!/bin/bash

docker build .
docker cp `docker ps -alq`:/usr/local/share/OpenCV/java/opencv-320.jar jar/


