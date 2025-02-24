#!/bin/bash

BROKER_NAMES=("kafka-0" "kafka-1" "kafka-2")
PASSWORD=${PASSWORD:-"password"}  # The password to protect the keystore and private keys.
KEYSTORE_BASE_PATH=${KEYSTORE_BASE_PATH:-"./keystore"}  # The base path to store the generated keystores.
TRUSTSTORE_PATH=${TRUSTSTORE_PATH:-"./truststore"}  # The path to store the generated truststore.
TRUSTSTORE_FILE=${TRUSTSTORE_FILE:-"truststore.jks"}  # The generated truststore file name.
CN=${CN:-"localhost"}  # The Certificate's Common Name.

# Let's create the directories if they don't exist yet.
mkdir -p ${TRUSTSTORE_PATH}
for BROKER_NAME in "${BROKER_NAMES[@]}"; do
  mkdir -p "${KEYSTORE_BASE_PATH}/${BROKER_NAME}"
done

# Create the Root Key.
echo "Creating root key..."
openssl req -new -x509 -keyout "${TRUSTSTORE_PATH}/ca-key" -out "${TRUSTSTORE_PATH}/ca-cert" -days 365 -subj "/CN=${CN}"

# Create Truststore that contains the Root Key.
echo "Creating truststore..."
keytool -keystore "${TRUSTSTORE_PATH}/${TRUSTSTORE_FILE}" -alias CARoot -import -file "${TRUSTSTORE_PATH}/ca-cert" -storepass "${PASSWORD}" -noprompt

# Generate keys, certificates and keystores for Kafka brokers.
for BROKER_NAME in "${BROKER_NAMES[@]}"
do
  echo "Processing ${BROKER_NAME}"

  # Key and certificate generation for each broker.
  keytool -genkey -noprompt -alias "${BROKER_NAME}" -dname "CN=${CN}" -keystore "${KEYSTORE_BASE_PATH}/${BROKER_NAME}/${BROKER_NAME}.keystore.jks" -keyalg RSA -storepass "${PASSWORD}" -keypass "${PASSWORD}"

  # Create a Certificate Signing Request (CSR).
  keytool -certreq -alias "${BROKER_NAME}" -keystore "${KEYSTORE_BASE_PATH}/${BROKER_NAME}/${BROKER_NAME}.keystore.jks" -file "${KEYSTORE_BASE_PATH}/${BROKER_NAME}/${BROKER_NAME}.csr" -storepass "${PASSWORD}" -keypass "${PASSWORD}"

  # Sign the CSR with the Root Key.
  openssl x509 -req -CA "${TRUSTSTORE_PATH}/ca-cert" -CAkey "${TRUSTSTORE_PATH}/ca-key" -in "${KEYSTORE_BASE_PATH}/${BROKER_NAME}/${BROKER_NAME}.csr" -out "${KEYSTORE_BASE_PATH}/${BROKER_NAME}/${BROKER_NAME}.cert-signed" -days 365 -CAcreateserial -passin pass:${PASSWORD}

  # Import the Root certificate into the broker's keystore. This truststore contains the Root certificate only.
  keytool -keystore "${KEYSTORE_BASE_PATH}/${BROKER_NAME}/${BROKER_NAME}.keystore.jks" -alias CARoot -import -file "${TRUSTSTORE_PATH}/ca-cert" -storepass "${PASSWORD}" -noprompt

  # Import the signed broker certificate into the broker's keystore. So the broker's keystore contains both the broker certificate and the Root certificate.
  keytool -keystore "${KEYSTORE_BASE_PATH}/${BROKER_NAME}/${BROKER_NAME}.keystore.jks" -alias "${BROKER_NAME}" -import -file "${KEYSTORE_BASE_PATH}/${BROKER_NAME}/${BROKER_NAME}.cert-signed" -storepass "${PASSWORD}" -noprompt
done

echo "Brokers' keystores generated successfully in ${KEYSTORE_BASE_PATH} directory. Each broker has its own subdirectory."