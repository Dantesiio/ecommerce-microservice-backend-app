# PowerShell script to generate self-signed TLS certificate for proxy-client

$CERT_DIR = ".\certs"
$NAMESPACE = "default"
$SECRET_NAME = "proxy-client-tls"

# Create certs directory
New-Item -ItemType Directory -Force -Path $CERT_DIR | Out-Null

Write-Host "🔐 Generating TLS certificate..." -ForegroundColor Cyan

# Generate private key
Write-Host "  Generating private key..."
openssl genrsa -out "$CERT_DIR\tls.key" 2048 2>$null

# Generate certificate signing request
Write-Host "  Generating CSR..."
openssl req -new -key "$CERT_DIR\tls.key" -out "$CERT_DIR\tls.csr" `
  -subj "/C=US/ST=State/L=City/O=Ecommerce/OU=IT/CN=proxy-client-service" 2>$null

# Generate self-signed certificate (valid for 365 days)
Write-Host "  Generating certificate..."
openssl x509 -req -days 365 -in "$CERT_DIR\tls.csr" `
  -signkey "$CERT_DIR\tls.key" -out "$CERT_DIR\tls.crt" 2>$null

# Create Kubernetes secret YAML
Write-Host "  Creating Kubernetes secret YAML..."
kubectl create secret tls $SECRET_NAME `
  --cert="$CERT_DIR\tls.crt" `
  --key="$CERT_DIR\tls.key" `
  --namespace=$NAMESPACE `
  --dry-run=client -o yaml > "$CERT_DIR\tls-secret.yaml"

# Clean up CSR (not needed)
Remove-Item "$CERT_DIR\tls.csr" -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "✅ TLS certificate generated successfully!" -ForegroundColor Green
Write-Host "📁 Files created in $CERT_DIR\" -ForegroundColor Yellow
Write-Host ""
Write-Host "📋 Next steps:" -ForegroundColor Cyan
Write-Host "  1. Apply the secret:" -ForegroundColor White
Write-Host "     kubectl apply -f $CERT_DIR\tls-secret.yaml" -ForegroundColor Gray
Write-Host "  2. Proxy-client deployment will use HTTPS on port 8443" -ForegroundColor White
Write-Host "  3. Test with:" -ForegroundColor White
Write-Host "     kubectl port-forward svc/proxy-client-service 8443:8443" -ForegroundColor Gray
Write-Host "     curl -k https://localhost:8443/app/actuator/health" -ForegroundColor Gray
