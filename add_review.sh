#!/bin/bash

rm lint.txt

for file in $(find . -type f -name "*-lint.txt");
do
  module="$(cut -d '/' -f 2 <<< "$file")"
  while read line;
  do
    i="$(pwd)/$module/$line"
    echo "$i" >> lint.txt
  done < $file
done

#cat lint.txt | ./reviewdog -efm="%f:%l:%c: %m" -name="ktlint" -diff="git diff master"
cat lint.txt | ./reviewdog -efm="%f:%l:%c: %m" -name="ktlint" -reporter=github-pr-review
