#!/bin/bash -e

ANT_HOME="$HOME/.ant/home"
PATH="$ANT_HOME/bin:$PATH"
LOCAL_IVY_SETTINGS="$HOME/.ant/ivysettings.xml"

ant ivy-setupjars clean crawl