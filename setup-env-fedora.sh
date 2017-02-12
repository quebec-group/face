#!/bin/bash

dnf --setopt=deltarpm=false -y -q update

# Setup RPMFusion for ffmpeg and similar
dnf -y -q install "https://download1.rpmfusion.org/free/fedora/rpmfusion-free-release-$(rpm -E %fedora).noarch.rpm" "https://download1.rpmfusion.org/nonfree/fedora/rpmfusion-nonfree-release-$(rpm -E %fedora).noarch.rpm"

# Install dependencies
dnf install -y -q wget make automake gcc gcc-c++ kernel-devel cmake python python-devel ffmpeg-compat-devel unzip ffmpeg-devel libjpeg-devel libpng-devel libtiff-devel libjasper-devel zlib-devel java-1.8.0-openjdk-devel ant gradle

# Get pip
wget -q 'https://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11-py2.7.egg' && /bin/sh setuptools-0.6c11-py2.7.egg && rm -f setuptools-0.6c11-py2.7.egg
curl -q 'https://raw.github.com/pypa/pip/master/contrib/get-pip.py' | python2.7
pip install numpy