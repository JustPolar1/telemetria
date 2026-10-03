# API básica de recepción de telemetría

Servicio Spring Boot para guardar lecturas de temperatura y humedad en PostgreSQL y consultarlas por sensor.

## Ejecución local

Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con las credenciales de PostgreSQL. Los valores predeterminados apuntan a `telemetria` en localhost con usuario y contraseña `telemetria`; crea previamente esa base y ese usuario, o sustituye las variables por tus valores. Hibernate actualiza el esquema al iniciar.

Desde `basica/`, inicia la aplicación con `.\mvnw.cmd spring-boot:run`.

## API

`POST /api/lecturas` recibe un objeto JSON. `sensorId`, `metric`, `value` y `measuredAt` son obligatorios. `metric` acepta `TEMPERATURE` o `HUMIDITY`; `measuredAt` debe ser una fecha ISO 8601 con zona horaria.

```json
{
  "sensorId": "sensor-01",
  "metric": "TEMPERATURE",
  "value": 22.75,
  "measuredAt": "2026-10-02T18:30:00Z"
}
```

Al guardar responde `201 Created` con la lectura, incluyendo su identificador y la fecha de recepción (`receivedAt`). Datos inválidos responden `400 Bad Request`.

`GET /api/sensores/{sensorId}/lecturas` consulta las lecturas más recientes primero. Admite filtros opcionales `from` y `to` (ISO 8601 con zona horaria) y paginación con `page` (desde 0) y `size` (1–100; predeterminado 20). Por ejemplo:

```text
GET /api/sensores/sensor-01/lecturas?from=2026-10-01T00:00:00Z&to=2026-10-03T00:00:00Z&page=0&size=20
```

Ejecuta las pruebas desde `basica/` con `.\mvnw.cmd test`. Utilizan una base H2 en memoria y no requieren PostgreSQL.
## Épica 5: Sistema de Registro de Lecturas de Sensores IoT (Telemetría Básica)

### Alexa.

![](screenshots\image.png)
