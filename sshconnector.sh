echo "shh Connecting"
ssh -i "BerkUbuntu.pem"  ubuntu@ec2-52-58-228-209.eu-central-1.compute.amazonaws.com << 'ENDSSH'
	echo "ssh connection is ready."
	sudo rm -rf jarar
	cd jarar
	echo "Now we are in jarar directory"
	chmod +x installation.sh
	echo "installation.sh yetki verildi"
	sh installation.sh 
	echo "çalıştırıldı."
ENDSSH
