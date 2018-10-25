#!/bin/bash

for file in $(find . -type f -name "*-lint.txt");
do
  module="$(cut -d '/' -f 2 <<< "$file")"
  while read line;
  do
    i="$module/$line"
    $i | ./reviewdog -efm="%f:%l:%c:%m" -name="ktlint" -reporter=github-pr-review
  done < $file
done
