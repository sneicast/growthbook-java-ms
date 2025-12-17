# 🚀 Feature Flags con GrowthBook

Microservicio Spring Boot integrado con **GrowthBook** para gestión de feature flags de manera dinámica y desacoplada.

---

## 📋 Tabla de Contenidos

- [Arquitectura](#-arquitectura)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Endpoints API](#-endpoints-api)
- [Ejemplos de Uso](#-ejemplos-de-uso)
- [Despliegue en Producción](#-despliegue-en-producción)

---

## 🏗 Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SPRING BOOT APP                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐    ┌──────────────────┐    ┌─────────────────────────┐    │
│  │  Controller │───▶│     Service      │───▶│    GrowthBookClient     │    │
│  │   (REST)    │    │ (Business Logic) │    │    (SDK Wrapper)        │    │
│  └─────────────┘    └──────────────────┘    └───────────┬─────────────┘    │
│                                                         │                   │
│  ┌─────────────────────────────────────────────────────┐│                   │
│  │                    CONFIG LAYER                     ││                   │
│  │  ┌─────────────────────┐  ┌──────────────────────┐  ││                   │
│  │  │ GrowthBookProperties│  │   GrowthBookConfig   │  ││                   │
│  │  │   (application.yaml)│  │   (Bean Factory)     │◀─┘│                   │
│  │  └─────────────────────┘  └──────────────────────┘   │                   │
│  └─────────────────────────────────────────────────────┘                    │
│                                                                             │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │
                                    ▼ HTTP
                    ┌───────────────────────────────┐
                    │         GROWTHBOOK API        │
                    │   /api/features/{client-key}  │
                    └───────────────────────────────┘
```

### Flujo de Datos

```
┌────────┐      ┌────────────┐      ┌─────────┐      ┌────────┐      ┌───────────┐
│ Client │─────▶│ Controller │─────▶│ Service │─────▶│ Client │─────▶│ GrowthBook│
│  HTTP  │      │            │      │         │      │        │      │    SDK    │
└────────┘      └────────────┘      └─────────┘      └────────┘      └───────────┘
    │                                                                      │
    │                         Response                                     │
    ◀──────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Estructura del Proyecto

```
src/main/java/dev/scastillo/feature_flags/
│
├── 📄 FeatureFlagsApplication.java      # Main Application
│
├── 📁 config/                           # Configuración
│   ├── GrowthBookProperties.java        # @ConfigurationProperties
│   └── GrowthBookConfig.java            # Bean Factory
│
├── 📁 client/                           # Cliente SDK
│   └── GrowthBookClient.java            # Wrapper del SDK GrowthBook
│
├── 📁 service/                          # Lógica de Negocio
│   └── FeatureFlagService.java          # Servicio principal
│
├── 📁 controller/                       # API REST
│   └── FeatureFlagController.java       # Endpoints
│
├── 📁 dto/                              # Data Transfer Objects
│   ├── request/
│   │   └── FeatureEvaluationRequest.java
│   └── response/
│       ├── FeatureResponse.java
│       └── FeatureValueResponse.java
│
└── 📁 exception/                        # Manejo de Errores
    └── FeatureFlagException.java
```

---

## 🛠 Instalación

### Prerrequisitos

- Java 21+
- Maven 3.8+
- GrowthBook Server (local o cloud)

### Dependencias (pom.xml)

```xml
<!-- Repositorio JitPack -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<!-- GrowthBook SDK -->
<dependency>
    <groupId>com.github.growthbook</groupId>
    <artifactId>growthbook-sdk-java</artifactId>
    <version>0.5.0</version>
</dependency>

<!-- Gson para JSON -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### Ejecutar

```bash
# Compilar
./mvnw compile

# Ejecutar
./mvnw spring-boot:run
```

---

## ⚙ Configuración

### application.yaml (Desarrollo)

```yaml
growthbook:
  enabled: true
  api-host: http://localhost:3100
  client-key: sdk-TU-CLIENT-KEY
  cache:
    enabled: true
    ttl-seconds: 60
```

### Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `GROWTHBOOK_ENABLED` | Habilitar/deshabilitar | `true` |
| `GROWTHBOOK_API_HOST` | URL del servidor GrowthBook | `http://localhost:3100` |
| `GROWTHBOOK_CLIENT_KEY` | Client Key del SDK | - |
| `GROWTHBOOK_CACHE_ENABLED` | Habilitar caché | `true` |
| `GROWTHBOOK_CACHE_TTL` | TTL del caché (segundos) | `60` |

---

## 🌐 Endpoints API

### Resumen

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/features/{key}` | Obtener estado de un feature |
| `POST` | `/api/features/{key}/evaluate` | Evaluar con atributos de usuario |
| `GET` | `/api/features/{key}/string` | Obtener valor como String |
| `GET` | `/api/features/{key}/boolean` | Obtener valor como Boolean |
| `GET` | `/api/features/{key}/integer` | Obtener valor como Integer |
| `GET` | `/api/features/{key}/double` | Obtener valor como Double |
| `POST` | `/api/features/refresh` | Refrescar features |

### Diagrama de Endpoints

```
                          /api/features
                               │
           ┌───────────────────┼───────────────────┐
           │                   │                   │
           ▼                   ▼                   ▼
      /{featureKey}       /refresh            /{featureKey}
           │               (POST)                  │
           │                                       │
     ┌─────┴─────┐                    ┌────────────┼────────────┐
     │           │                    │            │            │
     ▼           ▼                    ▼            ▼            ▼
   (GET)    /evaluate             /string      /boolean     /integer
             (POST)                (GET)         (GET)        (GET)
```

---

## 📝 Ejemplos de Uso

### 1. Obtener estado de un feature

```bash
curl -X GET 'http://localhost:8080/api/features/cliente'
```

**Response:**
```json
{
    "featureKey": "cliente",
    "enabled": true,
    "value": {
        "id": "aaa",
        "key": "sssssss",
        "active": true,
        "description": "mi feature"
    },
    "source": "growthbook"
}
```

### 2. Obtener valor string de un feature

```bash
curl -X GET 'http://localhost:8080/api/features/name/string?defaultValue=Unknown'
```

**Response:**
```json
{
    "featureKey": "name",
    "value": "Sneider",
    "defaultValue": "Unknown",
    "isDefaultValue": false
}
```

### 3. Evaluar feature con atributos de usuario

```bash
curl -X POST 'http://localhost:8080/api/features/cliente/evaluate' \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "user-123",
    "country": "CO",
    "deviceType": "mobile"
  }'
```

**Response:**
```json
{
    "featureKey": "cliente",
    "enabled": true,
    "value": true,
    "source": "growthbook"
}
```

### 4. Refrescar features desde GrowthBook

```bash
curl -X POST 'http://localhost:8080/api/features/refresh'
```

---

## 🚀 Despliegue en Producción

### Configuración por Ambiente

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   DESARROLLO    │     │     STAGING     │     │   PRODUCCIÓN    │
├─────────────────┤     ├─────────────────┤     ├─────────────────┤
│ application.yaml│     │ Variables ENV   │     │ Variables ENV   │
│ (valores local) │     │ (valores stage) │     │ (valores prod)  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### Docker

```bash
docker run -e GROWTHBOOK_API_HOST=https://gb.tudominio.com \
           -e GROWTHBOOK_CLIENT_KEY=sdk-prod-key \
           -e GROWTHBOOK_CACHE_TTL=300 \
           -p 8080:8080 \
           tu-imagen:latest
```

### Kubernetes ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: feature-flags-config
data:
  GROWTHBOOK_ENABLED: "true"
  GROWTHBOOK_API_HOST: "https://growthbook.tudominio.com"
  GROWTHBOOK_CACHE_ENABLED: "true"
  GROWTHBOOK_CACHE_TTL: "300"
---
apiVersion: v1
kind: Secret
metadata:
  name: feature-flags-secrets
type: Opaque
stringData:
  GROWTHBOOK_CLIENT_KEY: "sdk-tu-client-key-produccion"
```

---

## 🔧 Extensibilidad

### Agregar nuevos tipos de valores

El `GrowthBookClient` soporta múltiples tipos:

```java
// Boolean
client.getFeatureValueAsBoolean("mi-feature", false);

// String
client.getFeatureValue("mi-feature", "default");

// Integer
client.getFeatureValueAsInteger("mi-feature", 0);

// Double
client.getFeatureValueAsDouble("mi-feature", 0.0);

// Raw JSON
client.getFeatureValueRaw("mi-feature");
```

### Evaluar con atributos personalizados

```java
Map<String, Object> attributes = Map.of(
    "id", "user-123",
    "country", "CO",
    "plan", "premium"
);

boolean enabled = client.isFeatureEnabledForUser("mi-feature", attributes);
```

---

## 📊 Monitoreo

### Logs

```yaml
logging:
  level:
    dev.scastillo.feature_flags: DEBUG  # Desarrollo
    dev.scastillo.feature_flags: INFO   # Producción
```

### Health Check

La aplicación incluye Spring Boot Actuator para health checks:

```bash
curl http://localhost:8080/actuator/health
```

---

## 🤝 Contribuir

1. Fork el repositorio
2. Crea tu rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

---

<p align="center">
  Desarrollado con ❤️ usando Spring Boot + GrowthBook
</p>

