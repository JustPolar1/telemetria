# Incidencias de sensores — HU-01

Microservicio independiente con Java 21, Spring Boot y MongoDB. `sensorId` es una referencia: no comprueba que el sensor exista en Telemetry. El CRUD no depende de los otros servicios.

## Ejecución local

Desde la raíz del repositorio, con Docker Desktop iniciado:

```powershell
docker compose -f incident-service/compose.yaml up --build -d
```

API: `http://localhost:8083/api/v1/incidents`. MongoDB: `localhost:27018`, base `incidents`. Se usa 27018 para no interferir con instalaciones locales de MongoDB en 27017. El Compose raíz también incluye estos servicios, pero requiere las variables de seguridad de los servicios anteriores. La configuración independiente publica ambos puertos solo en loopback y está destinada al laboratorio local, sin autenticación.

Para ejecutar Java fuera de Docker:

```powershell
docker compose -f incident-service/compose.yaml up -d incident-db
mvn -f incident-service/pom.xml spring-boot:run
```

Variables opcionales: `MONGODB_URI` y `SERVER_PORT` (8083). Los datos se conservan en un volumen. Detener sin borrar datos:

Swagger UI está disponible en `http://localhost:8083/swagger-ui/index.html` y el contrato OpenAPI en `/v3/api-docs`. Para cambiar el puerto publicado de MongoDB, define `INCIDENT_MONGO_PORT` en Compose; si ejecutas Java fuera de Docker, ajusta también `MONGODB_URI`.

```powershell
docker compose -f incident-service/compose.yaml down
```

## Contrato REST

| Método | Ruta | Respuesta |
|---|---|---|
| POST | `/api/v1/incidents` | 201 y cabecera Location |
| GET | `/api/v1/incidents?page=0&size=20` | 200, página con content y totalElements |
| GET | `/api/v1/incidents/{id}` | 200 o 404 |
| PUT | `/api/v1/incidents/{id}` | 200 o 404; reemplaza campos editables |
| DELETE | `/api/v1/incidents/{id}` | 204 o 404 |

Cuerpo para POST y PUT:

```json
{
  "sensorId": "sensor-01",
  "titulo": "Lecturas intermitentes",
  "descripcion": "El sensor dejó de reportar humedad",
  "estado": "ABIERTA"
}
```

Estados: `ABIERTA`, `EN_REVISION`, `RESUELTA`. Todos los campos son obligatorios. Sensor: hasta 100 caracteres alfanuméricos, guiones y guiones bajos. Título: hasta 150; descripción: hasta 2000. La paginación permite page >= 0 y size de 1 a 100. MongoDB genera el ID; el servicio genera un folio UUID único y fecha de creación, conservados al actualizar. El 409 protege la unicidad del folio generado; repetir un título o sensor está permitido.

Importa `postman/incidents.postman_collection.json` desde la raíz en Postman. Ejecuta Crear primero para guardar automáticamente `incidentId`, luego Listar, Consultar, Actualizar y Eliminar.

Errores: JSON `{"error":"mensaje"}`, con 400 para entradas inválidas, 404 para inexistentes, 409 para folio duplicado y 503 para fallo de persistencia sin exponer detalles internos.

## Pruebas y cobertura

```powershell
# Unitarias con Mockito y contrato web con MockMvc (sin Docker)
mvn -f incident-service/pom.xml test

# Incluye integración con MongoDB real en Testcontainers; requiere Docker
mvn -f incident-service/pom.xml clean verify
```

La integración crea un MongoDB desechable y comprueba CRUD, persistencia e índice único. No utiliza la base de desarrollo. Falla si Docker no está disponible; no se omite silenciosamente.

`verify` genera `target/site/jacoco/index.html` y `jacoco.xml` y exige al menos 80 % de líneas y ramas en el módulo completo, sin exclusiones. No confundir configuración con evidencia: el porcentaje válido es el de una ejecución terminada correctamente.

## SonarQube

Con un servidor SonarQube disponible y un token válido en `SONAR_TOKEN`:

```powershell
$env:SONAR_HOST_URL = "http://localhost:9000"
# Configura SONAR_TOKEN fuera del repositorio
mvn -f incident-service/pom.xml clean verify sonar:sonar "-Dsonar.qualitygate.wait=true"
```

El scanner importa el XML de JaCoCo. La aprobación depende del Quality Gate configurado en el servidor; no queda acreditada solo por tener el plugin.

## Evidencias pendientes para la entrega

Guardar capturas de POST, GET, PUT, DELETE y errores; ejecución satisfactoria de `verify`; reporte HTML con líneas y ramas; y panel de SonarQube con Quality Gate aprobado. Incorporarlas al reporte técnico PDF junto con portada, arquitectura y enlace al repositorio. No se incluyen resultados simulados.
