#!/bin/bash
# This cron job is used to upload METS files and thumbnails to Forward.
# Written by Eric Alexander.

FORWARD_PATH='/var/www/forward/adds'
DATA_PATH='/data'

METS_FILES=`find $DATA_PATH -name '*.xml'`
SYM_LINKS=`find $FORWARD_PATH -name '*.xml'`
TIFF_FILES=`find $DATA_PATH -name '*.ome.tiff'`

#echo "TIFF_FILES: "$TIFF_FILES
#echo "METS_FILES: "$METS_FILES
#echo "SYM_LINKS: "$SYM_LINKS

for mets_file in $METS_FILES
do
    new=true
    #echo "CURRENT METS_FILE " $mets_file
    
    # If file is already in directory, ignore it.
    for sym_link in $SYM_LINKS
    do
	cmp -s $mets_file $sym_link
	if [ $? -eq 0 ]
	then
	    new=false
	    break
	fi
    done
 
    # Else, create a symlink to it.
    if $new
    then
	# Get the base filename.
	i=0
	lastslash=-1
	while [ $i -lt ${#mets_file} ]
	do
	    if [ "${mets_file:$i:1}" = "/" ]
	    then
		lastslash=$i
	    fi
	    i=$((i+1))
	done
	basename=${mets_file:$((lastslash+1))}
	basename=${basename:0:$((${#basename}-4))}

	# Make sure a matching tiff exists.
	match=false
	for temp_tiff in $TIFF_FILES
	do
	    extractedXML=`tiffcomment $temp_tiff`
	    if [[ $extractedXML == *urn:lsid:loci.wisc.edu:Dataset:$basename\"* ]]
	    then
		tiff_file=$temp_tiff
		match=true
		#echo "MATCH" $tiff_file
		break
	    fi
	done
	if ! $match
	then
	    echo "Error: No tiff file matches" $basename
	    echo "Skipping to next METS file."
	    continue
	fi

	# Make sure no file exists of same name.
	# e.g. If 'test.xml' already exists, link as 'test_1.xml'.
	if [ -e $FORWARD_PATH/$basename.xml ]
	then
	    i=1
	    basename2=''
	    while [ true ]
	    do
		basename2=$basename'_'$i
		if [ ! -e $FORWARD_PATH/$basename2.xml ]
		then
		    break
		fi
		i=$((i+1))
	    done
	    basename=$basename2
	fi

	# Generate a thumbnail
	# Usage: javac -cp loci_tools.jar:. CreateThumbnail originalPath newThumbnailPath
	#echo "About to javac"
	#echo "tiff_file: "$tiff_file
	#echo "newThumbnailPath: "$FORWARD_PATH/$basename.tiff
	java -cp loci_tools.jar:. CreateThumbnail \
	    $tiff_file $FORWARD_PATH/$basename.jpg > /dev/null

        # If everything went smoothly, symlink the metadata
	if [ $? -eq 0 ]
	then
	    ln -s $mets_file $FORWARD_PATH/$basename.xml
	fi
    fi
done

# Curtis' Notes:
#
# 1. symlink metadata (into /var/www/forward) from hits that are new
# 2. generate a thumbnail in /var/www/forward using Bio-Formats
#    put loci_tools.jar in /opt/forward
#    put .java file there too, compile to .class file:
#      "javac -cp loci_tools.jar MyProgram.java"
#    call "java -cp loci_tools.jar:. MyProgram arguments"

# Once this script works well, it will be symlinked into /etc/cron.hourly
# Also needs to have 755 permission set

# I recommend the Bash Advanced Scripting Guide... google it

# Cron jobs should output nothing unless there is a problem
