# API básica de recepción de telemetría

Servicio Spring Boot para guardar lecturas de sensores y administrar parcelas comunitarias en PostgreSQL.

## Parcelas

La API administra parcelas mediante `POST /api/v1/parcels`, `GET /api/v1/parcels` y `GET /api/v1/parcels/{id}`. El esquema de PostgreSQL está en [02-parcels.sql](./databases/init/02-parcels.sql). Hibernate valida el esquema al iniciar y no lo modifica.

`POST /api/v1/parcels` recibe nombre, ubicación GPS, tipo de suelo y responsable. Los tipos permitidos son `ARCILLOSO`, `LIMOSO` y `ARENOSO`.

```json
{
  "name": "Huerto comunitario norte",
  "location": {
    "latitude": 28.6353,
    "longitude": -106.0889
  },
  "soilType": "LIMOSO",
  "ownerOrLead": "Comunidad vecinal"
}
```

Los textos requeridos se validan y recortan en los extremos; las coordenadas deben estar dentro de los rangos GPS. Una parcela inexistente devuelve `404` con un mensaje de error.

## Ejecución local

La aplicación usa PostgreSQL. El esquema consta de [01-sensors.sql](./databases/init/01-sensors.sql) para sensores y lecturas, y [02-parcels.sql](./databases/init/02-parcels.sql) para parcelas. Hibernate valida el esquema al iniciar; no lo modifica. Al recibir una lectura, la API registra el sensor en `sensors` si aún no existe y guarda la medición asociada.

Si todavía no tienes PostgreSQL, puedes crear una instancia local con Docker. Ejecuta este comando desde la raíz del repositorio:

```powershell
docker run --name telemetria-postgres `
  -e POSTGRES_DB=telemetria `
  -e POSTGRES_USER=telemetria `
  -e POSTGRES_PASSWORD=telemetria `
  -p 5432:5432 `
  -v "${PWD}/databases/init:/docker-entrypoint-initdb.d:ro" `
  -d postgres:17
```

Los scripts de inicialización de Docker solo se aplican al crear el contenedor y la base por primera vez. Si la base ya existe, ejecuta ambos archivos SQL manualmente desde la raíz del repositorio:

```powershell
psql -h localhost -U telemetria -d telemetria -f databases/init/01-sensors.sql
psql -h localhost -U telemetria -d telemetria -f databases/init/02-parcels.sql
```

Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con los datos de tu PostgreSQL. Para la instancia Docker anterior, los valores predeterminados de la aplicación ya coinciden. Para un servidor distinto, define las variables antes de iniciar. En PowerShell:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/telemetria"
$env:DB_USERNAME = "telemetria"
$env:DB_PASSWORD = "telemetria"
```

Desde `basica/`, inicia la aplicación con `.\mvnw.cmd spring-boot:run`. Si la base no está accesible o el esquema no coincide, el arranque fallará; comprueba que PostgreSQL esté activo y que se haya aplicado el script.

## Épica 5: Sistema de Registro de Lecturas de Sensores IoT (Telemetría Básica)


![](screenshots\bd_creada.png)


### Pasos de la api (Adán)

La API implementa las historias de usuario de recepción y consulta de lecturas de la Épica 5:

1. **El dispositivo envía una lectura** a `POST /api/lecturas`, incluyendo el identificador del sensor, el tipo de medición (`TEMPERATURE` o `HUMIDITY`), el valor y la fecha de medición.

   ```json
   {
     "sensorId": "sensor-01",
     "metric": "TEMPERATURE",
     "value": 22.75,
     "measuredAt": "2026-10-02T18:30:00Z"
   }
   ```

![](screenshots\POST_lecturas.png)


2. **La API valida y almacena la lectura** en PostgreSQL, en la tabla `sensor_readings`. Si el sensor aún no está registrado, primero lo agrega a `sensors`. Al guardarla, responde `201 Created` con el identificador y la fecha de registro.

![](screenshots\Guardado.png)

3. **El operador consulta el historial** en `GET /api/sensores/{sensorId}/lecturas`. Puede limitar los resultados con los parámetros `from`, `to`, `page` y `size`. Por ejemplo:

   ```text
   GET /api/sensores/sensor-01/lecturas?from=2026-10-01T00:00:00Z&to=2026-10-03T00:00:00Z&page=0&size=20
   ```

![](screenshots\GET_lecturas)

### Alexa

![](screenshots\image.png)