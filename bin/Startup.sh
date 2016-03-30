#!/bin/bash
#1. read the file IPAdress and ssh into all the servers, kill any process on the port number you are using. 
#2. start the server process in all the servers in the list.

#transfer all the class files from local ro remote 
# arg 1: IPAddress.txt
# arg 2: port number
# arg 3: password

javac *.java
while read line
do 
	sshpass -p "$3" ssh -f srs6573@"$line" 'fuser -k -n tcp 8999 2> /dev/null;'
	sshpass -p "$3" scp Server.class FileTransfer.class IPAddress.txt srs6573@"$line":~/Desktop/DistributedSystems/RandomTree 
	#sshpass -p "$3" ssh -f srs6573@"$line" 'cd ~/Desktop/DistributedSystems/RandomTree; javac *.java'
	sshpass -p "$3" ssh -f srs6573@"$line" 'cd ~/Desktop/DistributedSystems/RandomTree; java Server '"$2 $1"' >> my_server_log_file.txt &>/dev/null &'
	#echo "Server running"
done < "$1"
#sshpass -p "$3" ssh -f srs6573@glados.cs.rit.edu 'cd ~/Desktop/DistributedSystems/RandomTree; ls -l'
