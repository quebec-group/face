from	fedora:25

# Adapted from https://github.com/steeve/docker-opencv
# (barey recognisable)

# First, update new system
run	dnf --setopt=deltarpm=false -y -q update

# Setup RPMFusion for ffmpeg and similar
run dnf -y -q install https://download1.rpmfusion.org/free/fedora/rpmfusion-free-release-$(rpm -E %fedora).noarch.rpm https://download1.rpmfusion.org/nonfree/fedora/rpmfusion-nonfree-release-$(rpm -E %fedora).noarch.rpm

# Install dependencies
run	dnf install -y -q wget make automake gcc gcc-c++ kernel-devel cmake python python-devel ffmpeg-compat-devel unzip ffmpeg-devel libjpeg-devel libpng-devel libtiff-devel libjasper-devel zlib-devel java-1.8.0-openjdk-devel ant

# Get pip
run	wget -q 'https://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11-py2.7.egg' && /bin/sh setuptools-0.6c11-py2.7.egg && rm -f setuptools-0.6c11-py2.7.egg
run	curl -q 'https://raw.github.com/pypa/pip/master/contrib/get-pip.py' | python2.7
run	pip install numpy

# Compile OpenCV
add	build_opencv.sh	/build_opencv.sh
run	/bin/sh /build_opencv.sh
run	rm -rf /build_opencv.sh
