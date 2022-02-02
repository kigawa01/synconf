#!/bin/bash

NAMES=(
"raid-setting"
)
URLS=(
"https://github.com/kigawa8390/raid-setting.git"
)

function commands() {
  echo setting command branch
    for i in {0..$((${#NAMES[@]}-1))};do
      echo NAMES[i]
    done
}

function yn() {
  if [ $# -eq 1 ]; then

    while [ "$r" = 'y' ] || [ "$r" = 'n' ]; do
      echo "$1 type y/n"
      read r
    done
    if [ "$r" = 'y' ] || [ "$r" = '' ]; then
      return 0
    fi
    return 1
  fi
  exit 1
}

function var() {
  while [ $allow -eq 0 ]; do
    read r
    for i; do
      if [ $i = $r ] || [ $i = "*" ]; then
        allow=0
        break
      fi
    done
  done
  echo $r
}

if [ $# -eq 0 ]; then
    commands
    exit 1
fi
if [ $# -eq 1 ]; then
  echo type branch
  BRANCH=$(var *)
elif [ $# -eq 2 ]; then
    BRANCH="$1"
else
  commands
  exit 1
fi

for i in {0..$((${#NAMES[@]}-1))};do
  NAME="${NAMES[i]}"
  URL="${URLS[i]}"
  yn "are you setup ${NAME}?"
  if [ "$?" -eq 0 ]; then
    if [ ! -d "./${NAME}" ]; then
      git clone "${URL}"
    fi
    (
    cd "${NAME}"
    git fetch
    git checkout "${BRANCH}"
    git merge
    chmod 700 ./setting.bash
    ./setting.bash
    )
  fi
done
