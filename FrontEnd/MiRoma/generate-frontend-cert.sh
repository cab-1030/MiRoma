#!/bin/bash

# Script para generar certificados SSL para el frontend (Vite)
# Estos certificados son autofirmados pero más compatibles que los generados automáticamente por Vite

CERT_DIR="certs"
KEY_FILE="$CERT_DIR/localhost-key.pem"
CERT_FILE="$CERT_DIR/localhost-cert.pem"

echo "Generando certificados SSL para el frontend..."

# Crear directorio de certificados si no existe
mkdir -p "$CERT_DIR"

# Generar clave privada
echo "1. Generando clave privada..."
openssl genrsa -out "$KEY_FILE" 2048

if [ $? -ne 0 ]; then
    echo "Error: OpenSSL no está instalado o no está disponible."
    echo "Instala OpenSSL:"
    echo "  macOS: brew install openssl"
    echo "  Linux: sudo apt-get install openssl"
    exit 1
fi

# Generar certificado autofirmado
echo "2. Generando certificado autofirmado..."
openssl req -new -x509 -key "$KEY_FILE" -out "$CERT_FILE" -days 365 -subj "/CN=localhost/O=MiRoma Development/C=CO" \
    -addext "subjectAltName=DNS:localhost,DNS:*.localhost,IP:127.0.0.1,IP:::1"

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Certificados generados exitosamente:"
    echo "  - Clave privada: $KEY_FILE"
    echo "  - Certificado: $CERT_FILE"
    echo ""
    echo "IMPORTANTE:"
    echo "1. Acepta el certificado en tu navegador cuando lo solicite"
    echo "2. En Chrome/Edge: Ve a chrome://flags/#allow-insecure-localhost y habilítalo"
    echo "3. En Firefox: Acepta la excepción de seguridad cuando aparezca"
    echo ""
    echo "Los certificados son válidos por 365 días."
else
    echo ""
    echo "✗ Error al generar certificados"
    exit 1
fi

