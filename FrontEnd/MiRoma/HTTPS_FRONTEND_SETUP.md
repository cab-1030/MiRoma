# Configuración HTTPS para Frontend (Vite)

## Opción 1: Usar Certificados Generados (Recomendado)

### Paso 1: Generar Certificados

Ejecuta el script incluido:

```bash
./generate-frontend-cert.sh
```

Este script generará:
- `certs/localhost-key.pem` - Clave privada
- `certs/localhost-cert.pem` - Certificado

### Paso 2: Iniciar el Servidor

```bash
npm run dev
```

Vite detectará automáticamente los certificados y los usará.

### Paso 3: Aceptar el Certificado en el Navegador

Cuando accedas a `https://localhost:5173`, el navegador mostrará una advertencia. Debes aceptar el certificado:

**Chrome/Edge:**
1. Haz clic en "Avanzado"
2. Haz clic en "Continuar a localhost (no seguro)"
3. Opcional: Ve a `chrome://flags/#allow-insecure-localhost` y habilítalo para evitar advertencias futuras

**Firefox:**
1. Haz clic en "Avanzado"
2. Haz clic en "Aceptar el riesgo y continuar"

**Safari:**
1. Haz clic en "Mostrar detalles"
2. Haz clic en "Visitar este sitio web"
3. Confirma la excepción

## Opción 2: Usar mkcert (Más Confiable)

`mkcert` genera certificados que son confiables localmente sin advertencias del navegador.

### Instalación

**macOS:**
```bash
brew install mkcert
brew install nss # Para Firefox
mkcert -install
```

**Linux:**
```bash
sudo apt install libnss3-tools
wget -O mkcert https://github.com/FiloSottile/mkcert/releases/latest/download/mkcert-v1.4.4-linux-amd64
chmod +x mkcert
sudo mv mkcert /usr/local/bin/
mkcert -install
```

**Windows:**
```powershell
choco install mkcert
# O descarga desde: https://github.com/FiloSottile/mkcert/releases
mkcert -install
```

### Generar Certificados con mkcert

```bash
mkdir -p certs
cd certs
mkcert localhost 127.0.0.1 ::1
mv localhost+2.pem localhost-cert.pem
mv localhost+2-key.pem localhost-key.pem
cd ..
```

Los certificados generados con `mkcert` son confiables localmente y no mostrarán advertencias en el navegador.

## Opción 3: Usar Certificados del Backend

Si ya tienes certificados del backend, puedes reutilizarlos:

```bash
# Convertir keystore del backend a PEM
openssl pkcs12 -in ../BackEnd/miroma/src/main/resources/keystore.p12 \
  -nodes -nocerts -out certs/localhost-key.pem \
  -password pass:changeit

openssl pkcs12 -in ../BackEnd/miroma/src/main/resources/keystore.p12 \
  -nokeys -out certs/localhost-cert.pem \
  -password pass:changeit
```

## Verificación

Después de configurar los certificados:

1. Inicia el servidor: `npm run dev`
2. Accede a `https://localhost:5173`
3. Verifica que no haya errores de SSL en la consola del navegador
4. Las peticiones a `/api/*` deberían funcionar correctamente

## Troubleshooting

### Error: "ERR_SSL_VERSION_OR_CIPHER_MISMATCH"

**Solución:** Regenera los certificados con el script o usa mkcert.

### Error: "NET::ERR_CERT_AUTHORITY_INVALID"

**Solución:** 
- Acepta el certificado en el navegador (ver Paso 3 arriba)
- O usa mkcert para certificados confiables localmente

### Error: "Cannot find module 'fs'"

**Solución:** Esto no debería pasar, pero si ocurre, verifica que estás usando Node.js 14+.

### El navegador sigue mostrando advertencias

**Solución:** Usa mkcert (Opción 2) para certificados que son confiables localmente sin advertencias.

## Producción

En producción, usa certificados válidos de:
- Let's Encrypt (gratuito)
- Proveedor comercial
- Certificados de tu organización

Los certificados autofirmados solo son para desarrollo local.

