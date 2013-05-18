#!/bin/sh

project=$1

git subtree add --prefix=import/$project git@github.com:sonatype/${project}.git master