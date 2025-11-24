#!/bin/bash
# Script to generate self-signed TLS certificate for proxy-client

CERT_DIR="./certs"
NAMESPACE="default"
SECRET_NAME="proxy-client-tls"

# Create certs directory
mkdir -p $CERT_DIR

# Generate private key
openssl genrsa -out $CERT_DIR/tls.key 2048

# Generate certificate signing request
openssl req -new -key $CERT_DIR/tls.key -out $CERT_DIR/tls.csr \
  -subj "/C=US/ST=State/L=City/O=Ecommerce/OU=IT/CN=proxy-client-service"

# Generate self-signed certificate (valid for 365 days)
openssl x509 -req -days 365 -in $CERT_DIR/tls.csr \
  -signkey $CERT_DIR/tls.key -out $CERT_DIR/tls.crt

# Create Kubernetes secret
kubectl create secret tls $SECRET_NAME \
  --cert=$CERT_DIR/tls.crt \
  --key=$CERT_DIR/tls.key \
  --namespace=$NAMESPACE \
  --dry-run=client -o yaml > $CERT_DIR/tls-secret.yaml

echo "✅ TLS certificate generated successfully!"
echo "📁 Files created in $CERT_DIR/"
echo "🔐 Apply secret with: kubectl apply -f $CERT_DIR/tls-secret.yaml"

# Clean up CSR (not needed)
rm $CERT_DIR/tls.csr

echo ""
echo "📋 Next steps:"
echo "1. Apply the secret: kubectl apply -f $CERT_DIR/tls-secret.yaml"
echo "2. Update proxy-client deployment to use HTTPS"
echo "3. Test with: curl -k https://localhost:8900/app/actuator/health"
