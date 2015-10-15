#!/usr/bin/env bash

# Using the configuration file 'selfsigned.cnf' and RSA key 'private-key.pem',
# creates a new Java keystore, a new cert, and installs the cert into the keystore and root cacerts.

if [ ! -f selfsigned.cnf ]; then
  echo "File 'selfsigned.cnf' not found!"
fi

if [ ! -f private-key.pem ]; then
  echo "File 'private-key.pem' not found!"
fi

# Clean up any existing output files
rm -f selfsigned.crt
rm -f selfsigned.pem
rm -f keystore.p12
rm -f keystore.jks

keytool -delete -alias selfsigned -keystore /usr/lib/jvm/jre/lib/security/cacerts -storepass changeit 2> /dev/null

# Generate the new cert
openssl req -new -x509 -days 365 -key private-key.pem -config selfsigned.cnf -out selfsigned.pem
openssl x509 -outform der -in selfsigned.pem -out selfsigned.crt

# Generate the new keystore
openssl pkcs12 -export -in selfsigned.pem -inkey private-key.pem -certfile selfsigned.pem -name selfsigned -out keystore.p12 -storepass changeit
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype pkcs12 -destkeystore keystore.jks -deststoretype JKS -storepass changeit

# Install the keystore
cp keystore.jks /var/lib/derpwizard/keystore.jks

# Install the cert to the root cacerts
keytool -import -trustcacerts -alias selfsigned -file selfsigned.crt -keystore /usr/lib/jvm/jre/lib/security/cacerts -storepass changeit
