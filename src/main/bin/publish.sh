#!/bin/bash

###
# #%L
# Cron job for WiscScan/UW-Forward integration.
# %%
# Copyright (C) 2011 - 2012 Board of Regents of the University of
# Wisconsin-Madison.
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
###

# This cron job is used to upload METS files and thumbnails to Forward.
# Written by Eric Alexander.

FORWARD_PATH='/var/www/forward/adds'
DATA_PATH='/data'

METS_FILES=`find $DATA_PATH -name '*.xml'`
SYM_LINKS=`find $FORWARD_PATH -name '*.xml'`
DATA_FILES=`find $DATA_PATH -name '*.ome.tiff'`

if [ "$DEBUG" ]; then
	echo "DATA_FILES: $DATA_FILES"
	echo "METS_FILES: $METS_FILES"
	echo "SYM_LINKS: $SYM_LINKS"
fi

for mets_file in $METS_FILES
do
	new=true
	if [ "$DEBUG" ]; then
		echo "mets_file: $mets_file"
	fi

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

		# Make sure a matching data file exists.
		match=false
		for temp_data in $DATA_FILES
		do
			extractedXML=`showinf -nopix -novalid -omexml-only $temp_data`
			if [[ $extractedXML == *urn:lsid:loci.wisc.edu:Dataset:$basename\"* ]]
			then
				data_file=$temp_data
				match=true
				if [ "$DEBUG" ]; then
					echo "MATCH: $data_file"
				fi
				break
			fi
		done
		if ! $match
		then
			echo "Error: No data file matches: $basename"
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
		# Usage: javac -cp '*' CreateThumbnail originalPath newThumbnailPath
		thumbnail_path="$FORWARD_PATH/$basename.jpg"
		if [ "$DEBUG" ]; then
			echo "About to javac"
			echo "data_file: $data_file"
			echo "thumbnail_path: $thumbnail_path"
		fi
		java -cp '*' CreateThumbnail "$data_file" "$thumbnail_path" > /dev/null

		# If everything went smoothly, symlink the metadata
		if [ $? -eq 0 ]
		then
			symlink_path="$FORWARD_PATH/$basename.xml"
			ln -s "$mets_file" "$symlink_path"
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
