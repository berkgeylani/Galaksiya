echo "shh Connecting"
ssh -i "BerkUbuntu.pem"  ubuntu@ec2-52-58-228-209.eu-central-1.compute.amazonaws.com
cd jarar
chmod +x installation.sh
sh installation.sh

