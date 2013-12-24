#!/bin/sh
# creates Android icon files with the required paths and sizes
# from a single SVG file
# the source file should be square
filedir="$( cd "$( dirname "$0" )" && pwd )";
# script Gimp with a little TinyScheme
gimp -i -b - <<EOF
(define (svg2png myinfile myoutfile mysize)
    (define myimage (car (file-svg-load 1 myinfile myinfile 90.0 mysize mysize 0)))
    (gimp-image-convert-indexed myimage 0 0 256 0 0 "")
    (define mylayer (car (gimp-image-get-active-layer myimage)))
    (file-png-save2 1 myimage mylayer myoutfile myoutfile 0 5 0 0 0 0 0 0 0)
    (gimp-image-delete myimage)
)
(svg2png "$filedir/ic_launcher.svg" "$filedir/res/drawable-mdpi/ic_launcher.png" 48)
(svg2png "$filedir/ic_launcher.svg" "$filedir/res/drawable-hdpi/ic_launcher.png" 72)
(svg2png "$filedir/ic_launcher.svg" "$filedir/res/drawable-xhdpi/ic_launcher.png" 96)
(svg2png "$filedir/ic_launcher.svg" "$filedir/res/drawable-xxhdpi/ic_launcher.png" 144)
(svg2png "$filedir/ic_launcher.svg" "$filedir/ic_launcher-web.png" 512)
(gimp-quit 0)
EOF
echo $?
