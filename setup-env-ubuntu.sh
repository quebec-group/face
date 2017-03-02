#!/bin/bash

sudo apt-get -qq update

# Install dependencies
sudo apt-get install -y -q wget unzip patch curl build-essential cmake python2.7 python2.7-dev ant openjdk-8-jre openjdk-8-jdk gradle
sudo apt-get install -y -q libavformat-dev libavcodec-dev libavfilter-dev libswscale-dev
sudo apt-get install -y -q libjpeg-dev libpng-dev libtiff-dev libjasper-dev zlib1g-dev libopenexr-dev libxine2-dev libeigen3-dev libtbb-dev
