#!/bin/bash

if [ "$1" == "--help" ]
then
	echo "USAGE: $0 [--full]"
	exit 1
fi

VER="3.2.0"
STARTDIR=`pwd`

# Perform the build in home directory to avoid shared filesystem problems with make
cd ~

# Remove any stale sources
if [ "$1" == "--full" ]
then
	echo "Performing full install (deleting old source directories)"
	rm -rf opencv_contrib-$VER/ opencv-$VER/
fi

if [ ! -d opencv-$VER/ ]
then
	# Fetch OpenCV sources
	echo "Fetching OpenCV sources"
	opencv_zip=/tmp/opencv.zip
	curl -q -L "https://github.com/opencv/opencv/archive/$VER.zip"  > $opencv_zip
	unzip -q $opencv_zip
	rm -f $opencv_zip
fi

if [ ! -d opencv-$VER/opencv_contrib-$VER/ ]
then
	# Fetch OpenCV contrib repo sources (includes the face module)
	echo "Fetching OpenCV Contrib sources"
	opencv_contrib_zip=/tmp/opencv_contrib.zip
	curl -q -L "https://github.com/opencv/opencv_contrib/archive/$VER.zip" > $opencv_contrib_zip
	unzip -q $opencv_contrib_zip
	rm -f $opencv_contrib_zip
	
	# The face module needs to be patched to force compilation of the Java bindings
	patch opencv_contrib-$VER/modules/face/CMakeLists.txt << EOF
2c2
< ocv_define_module(face opencv_core opencv_imgproc opencv_objdetect WRAP python)
---
> ocv_define_module(face opencv_core opencv_imgproc opencv_objdetect WRAP python java)
EOF
fi

# Create build directory
mkdir -p opencv-$VER/release
mv opencv_contrib-$VER/ opencv-$VER/
cd opencv-$VER/release

# Build!
cmake -D OPENCV_EXTRA_MODULES_PATH=../opencv_contrib-$VER/modules -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D BUILD_PYTHON_SUPPORT=OFF -D BUILD_JAVA_SUPPORT=ON -D WITH_XINE=ON -D WITH_TBB=ON ..
make -j 4
make install

# Tidy up
cd ../../
#rm -rf opencv-$VER
cd $STARTDIR

# Copy the JAR out so that the likes of IntelliJ can use it
mkdir -p libs
rm -f libs/opencv-$VER.jar
cp /usr/local/share/OpenCV/java/opencv-`echo $VER | sed 's/\.//g'`.jar libs/opencv-$VER.jar