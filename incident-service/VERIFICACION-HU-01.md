# Verificación de CRUD, integración y cobertura — HU-01

Fecha: 12 de noviembre de 2026. Rama: `feature/incidents-mongodb-hu01`.

## Ejecución

```powershell
mvn -B -f incident-service/pom.xml clean verify
```

Resultado: **BUILD SUCCESS**, sin opciones para omitir pruebas.

| Grupo | Pruebas | Fallos | Errores | Omitidas |
|---|---:|---:|---:|---:|
| IncidentServiceTests | 7 | 0 | 0 | 0 |
| IncidentControllerTests | 4 | 0 | 0 | 0 |
| IncidentMongoIT | 1 | 0 | 0 | 0 |
| Total | 12 | 0 | 0 | 0 |

La integración utilizó MongoDB 8.0 real en un contenedor temporal creado por Testcontainers. Comprobó creación por endpoint y persistencia, consulta por ID, listado, actualización de estado persistida, eliminación y respuesta 404 posterior. También comprobó el rechazo de un folio duplicado por el índice único de MongoDB. El contenedor temporal se eliminó al finalizar.

## Cobertura

JaCoCo registró **37 líneas cubiertas de 39: 94.87 %**. La compilación aprobó las reglas configuradas. No hubo ramas medibles en el reporte; no se interpreta como 100 % de ramas.

Archivos generados localmente (ignorados por Git y regenerables con el comando anterior):

- `target/site/jacoco/index.html`
- `target/site/jacoco/jacoco.xml`
- `target/surefire-reports/`
- `target/failsafe-reports/com.telemetria.incidents.IncidentMongoIT.txt`

Esta ejecución acredita la integración automatizada y la cobertura; todavía deben recopilarse capturas del cliente HTTP y de los reportes para el PDF. El análisis SonarQube y su Quality Gate siguen pendientes.
