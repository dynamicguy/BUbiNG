#!/bin/bash

# Install the yum-utils package for repo selection
yum install -y yum-utils

# Disable Preview and Developer channels
yum-config-manager --disable ol7_preview ol7_developer\* > /dev/null

yum install -y ant

cd /usr/local/src/bubing

sh /usr/local/src/bubing/ant_ivy_bootstrap.sh

export ANT_HOME="/home/vagrant/.ant/home"
export PATH="/bin:/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/vagrant/.local/bin:/home/vagrant/bin"
export LOCAL_IVY_SETTINGS="/home/vagrant/.ant/ivysettings.xml"

ant ivy-setupjars

ant jar