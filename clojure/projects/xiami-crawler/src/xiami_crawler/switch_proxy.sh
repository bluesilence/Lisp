#!/bin/bash

proxy_list="./src/xiami_crawler/proxy_list.txt"
count=$(cat $proxy_list | wc -l)
echo "Count: "$count
picked=$(($RANDOM%$count))
echo "Picked: "$picked

i=0
cat $proxy_list | while read line
do
    if [ "$picked" = "$i" ];then
	new_proxy="http://"$line
        echo $new_proxy
	break
    fi
    i=`expr $i + 1`
done
export http_proxy=$new_proxy
env | grep proxy
