VER="3.2.0"

while [ ! -f opencv.zip ]
do
	curl -q -L "https://sourceforge.net/projects/opencvlibrary/files/opencv-unix/$VER/opencv-$VER.zip/download"  > opencv.zip
done

unzip -q opencv.zip
rm -f opencv.zip

mkdir -p opencv-$VER/release
cd opencv-$VER/release
cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D BUILD_PYTHON_SUPPORT=ON -D BUILD_JAVA_SUPPORT=ON -D WITH_XINE=ON -D WITH_TBB=ON ..
make -j 4 && make install
cd /
rm -rf opencv-$VER
