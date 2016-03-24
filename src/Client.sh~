#!/bin/bash
#arg 1: port number, 2: action, 3: Filenumber if insertion, or filename if retrieval
sshpass -p "$5" ssh -f srs6573@"$1" 'kill $(fuser -n tcp' "$2" '2> /dev/null);'
echo "srs6573@$1"
sshpass -p "$5" scp Client.java FileTransfer.java IPAddress.txt File_*.txt srs6573@newyork.cs.rit.edu:~/Desktop/DistributedSystems/RandomTree/Client
sshpass -p "$5" ssh -f srs6573@"$1" 'cd ~/Desktop/DistributedSystems/RandomTree/Client; javac *.java'
sshpass -p "$5" ssh -f srs6573@"$1" 'cd ~/Desktop/DistributedSystems/RandomTree/Client; ls -l'
sshpass -p "$5" ssh -f srs6573@"$1" 'cd ~/Desktop/DistributedSystems/RandomTree/Client; java Client '"$2"' IPAddress.txt' "$3" "$4" '>> my_client_log_file.txt'
echo "Client running"
