#!/usr/bin/env sh

# This is just a reference for installing sarasa gothic font
# you may download it manually

# mkdir res
cd res
mkdir font
cd font
wget https://github.com/be5invis/Sarasa-Gothic/releases/download/v0.5.2/sarasa-gothic-ttf-0.5.2.7z
7z x sarasa-gothic-ttf-0.5.2.7z
rm sarasa-gothic-ttf-0.5.2.7z
cd ..
cd ..