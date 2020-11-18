#!/usr/bin/env bash


KEY_NAME="$1"
KEY_TARGET="./keys/${KEY_NAME}/${KEY_NAME}"

set -eu

mkdir -p "keys/${KEY_NAME}"

# Generate private and public keys in DER format
openssl genpkey -algorithm ED25519 > "${KEY_TARGET}.pem"
cat "${KEY_TARGET}.pem" | head -n 2 | tail -n 1 | base64 --decode > "${KEY_TARGET}.der"
openssl pkey -outform DER -pubout -in "${KEY_TARGET}.pem" > "${KEY_TARGET}.pub.der"

# TRIM ASN headers
cat "${KEY_TARGET}.der" | tail -c +17 > "${KEY_TARGET}"
cat "${KEY_TARGET}.pub.der" | tail -c +13 > "${KEY_TARGET}.pub"

# Print bytes for C assignment
./bytes_numbers "${KEY_TARGET}"
./bytes_numbers "${KEY_TARGET}.pub"

