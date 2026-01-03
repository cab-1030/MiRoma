# Configuración de HTTPS para Mi Roma

## Requisitos

Para que el sistema funcione con HTTPS, necesitas:
1. Un certificado SSL/TLS
2. Configuración en Spring Boot
3. Configuración en el frontend

## Generar Certificado SSL para Desarrollo

### Opción 1: Usando keytool (incluido con Java)

```bash
# Generar un keystore con certificado autofirmado
keytool -genkeypair \
  -alias miroma \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore src/main/resources/keystore.p12 \
  -validity 365 \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=localhost, OU=Development, O=MiRoma, L=City, ST=State, C=CO"
```

**Nota:** El password por defecto es `changeit`. En producción, usa un password seguro y configúralo como variable de entorno.

### Opción 2: Usando OpenSSL

```bash
# Generar clave privada
openssl genrsa -out miroma-key.pem 2048

# Generar certificado autofirmado
openssl req -new -x509 -key miroma-key.pem -out miroma-cert.pem -days 365 \
  -subj "/CN=localhost/O=MiRoma/C=CO"

# Convertir a PKCS12 para Java
openssl pkcs12 -export \
  -in miroma-cert.pem \
  -inkey miroma-key.pem \
  -out src/main/resources/keystore.p12 \
  -name miroma \
  -password pass:changeit
```

## Configuración en Spring Boot

La configuración ya está en `application.properties`:

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD:changeit}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=miroma
server.ssl.protocol=TLS
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
```

### Variables de Entorno

Para producción, configura las siguientes variables de entorno:

```bash
export SSL_KEYSTORE_PASSWORD=tu_password_seguro
```

## Certificados para Producción

### Opción 1: Let's Encrypt (Gratuito)

1. **Instalar Certbot:**
```bash
sudo apt-get update
sudo apt-get install certbot
```

2. **Obtener certificado:**
```bash
sudo certbot certonly --standalone -d tu-dominio.com
```

3. **Convertir a PKCS12:**
```bash
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/tu-dominio.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/tu-dominio.com/privkey.pem \
  -out keystore.p12 \
  -name miroma \
  -password pass:tu_password_seguro
```

4. **Mover al proyecto:**
```bash
mv keystore.p12 src/main/resources/
```

### Opción 2: Certificado Comercial

Si tienes un certificado comercial (.crt y .key):

```bash
openssl pkcs12 -export \
  -in tu-certificado.crt \
  -inkey tu-clave-privada.key \
  -out src/main/resources/keystore.p12 \
  -name miroma \
  -password pass:tu_password_seguro
```

## Configuración del Frontend

El frontend ya está configurado para usar HTTPS. Las URLs se centralizan en `src/config/api.js`.

### Variables de Entorno (Frontend)

Crea un archivo `.env` en la raíz del proyecto frontend:

```env
# .env.development
VITE_API_BASE_URL=https://localhost:8443

# .env.production
VITE_API_BASE_URL=https://api.miroma.com
```

## Manejo de Certificados Autofirmados en Desarrollo

Los navegadores mostrarán una advertencia con certificados autofirmados. Para desarrollo:

### Chrome/Edge
1. Ir a `chrome://flags/#allow-insecure-localhost`
2. Habilitar "Allow invalid certificates for resources loaded from localhost"
3. Reiniciar el navegador

### Firefox
1. Ir a `about:config`
2. Buscar `security.tls.insecure_fallback_hosts`
3. Agregar `localhost`

### Alternativa: Aceptar el certificado manualmente
1. Navegar a `https://localhost:8443`
2. Hacer clic en "Avanzado"
3. Hacer clic en "Continuar a localhost (no seguro)"

## Verificar Configuración

### Backend
```bash
# Verificar que el servidor inicia con HTTPS
curl -k https://localhost:8443/api/auth/test
```

### Frontend
Abre la consola del navegador y verifica que todas las peticiones usen HTTPS.

## Seguridad Adicional

### HSTS (HTTP Strict Transport Security)

Agregar en `WebConfig.java`:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Bean
    public FilterRegistrationBean<HstsFilter> hstsFilter() {
        FilterRegistrationBean<HstsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HstsFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
```

### Redirección HTTP a HTTPS

Si necesitas mantener el puerto 8080 para redirección:

```java
@Configuration
public class HttpsRedirectConfig {
    
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }
    
    private Connector redirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}
```

## Troubleshooting

### Error: "keystore.p12 not found"
- Verifica que el archivo esté en `src/main/resources/`
- Verifica que el nombre del archivo coincida con la configuración

### Error: "Password incorrect"
- Verifica la variable de entorno `SSL_KEYSTORE_PASSWORD`
- Verifica que el password coincida con el usado al generar el keystore

### Error: "Certificate chain not found"
- Asegúrate de incluir la cadena completa de certificados
- Para Let's Encrypt, usa `fullchain.pem`

### Frontend: "Mixed Content" error
- Verifica que todas las URLs usen HTTPS
- Verifica la configuración en `src/config/api.js`

## Producción

### Checklist de Producción

- [ ] Certificado válido (no autofirmado)
- [ ] Password seguro configurado como variable de entorno
- [ ] HSTS habilitado
- [ ] Redirección HTTP a HTTPS configurada
- [ ] Renovación automática de certificados (Let's Encrypt)
- [ ] Monitoreo de expiración de certificados
- [ ] Backup del keystore en lugar seguro

### Renovación Automática (Let's Encrypt)

Configurar cron job para renovación:

```bash
# Agregar a crontab
0 0 * * * certbot renew --quiet && systemctl reload miroma
```

