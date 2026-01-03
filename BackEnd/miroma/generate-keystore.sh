#!/bin/bash

# Script para generar keystore para desarrollo
# Uso: ./generate-keystore.sh

KEYSTORE_PATH="src/main/resources/keystore.p12"
KEYSTORE_PASSWORD="changeit"
ALIAS="miroma"
VALIDITY_DAYS=365

echo "Generando keystore para desarrollo..."
echo "Ubicación: $KEYSTORE_PATH"
echo "Password: $KEYSTORE_PASSWORD"
echo ""

# Verificar si keytool está disponible
if ! command -v keytool &> /dev/null; then
    echo "Error: keytool no está disponible. Asegúrate de tener Java instalado."
    exit 1
fi

# Crear directorio si no existe
mkdir -p src/main/resources

# Generar keystore
keytool -genkeypair \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE_PATH" \
  -validity "$VALIDITY_DAYS" \
  -storepass "$KEYSTORE_PASSWORD" \
  -keypass "$KEYSTORE_PASSWORD" \
  -dname "CN=localhost, OU=Development, O=MiRoma, L=City, ST=State, C=CO"

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Keystore generado exitosamente en $KEYSTORE_PATH"
    echo ""
    echo "IMPORTANTE:"
    echo "- Este certificado es autofirmado y solo para desarrollo"
    echo "- Para producción, usa un certificado válido (Let's Encrypt, etc.)"
    echo "- El password por defecto es 'changeit'"
    echo "- En producción, configura SSL_KEYSTORE_PASSWORD como variable de entorno"
else
    echo ""
    echo "✗ Error al generar keystore"
    exit 1
fi

