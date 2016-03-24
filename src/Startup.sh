#!/bin/bash
#1. read the file IPAdress and ssh into all the servers, kill any process on the port number you are using. 
#2. start the server process in all the servers in the list.

#transfer all the class files from local ro remote 
# arg 1: IPAddress.txt
# arg 2: port number
# arg 3: password
while read line
do 
	sshpass -p "$3" ssh -f srs6573@"$line" 'kill $(fuser -n tcp 8999 2> /dev/null);'
	sshpass -p "$3" scp Server.java FileTransfer.java IPAddress.txt srs6573@"$line":~/Desktop/DistributedSystems/RandomTree 
	sshpass -p "$3" ssh -f srs6573@"$line" 'cd ~/Desktop/DistributedSystems/RandomTree; javac *.java'
	sshpass -p "$3" ssh -f srs6573@"$line" 'cd ~/Desktop/DistributedSystems/RandomTree; java Server '"$2"' IPAddress.txt 2>&1 &'
	#echo "Server running"
done < "$1"
sshpass -p "$3" ssh -f srs6573@glados.cs.rit.edu 'cd ~/Desktop/DistributedSystems/RandomTree; ls -l'
