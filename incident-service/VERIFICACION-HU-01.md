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

## Verificación final con Swagger y SonarQube (13 de noviembre de 2026)

Después de incorporar Swagger UI, `clean verify` volvió a terminar con BUILD SUCCESS: 12 pruebas aprobadas, sin fallos, errores ni omisiones. Se comprobó además que `/v3/api-docs` expone el contrato del CRUD.

Docker Compose construyó e inició el microservicio y MongoDB de desarrollo. Se configuró el puerto local 27018 para evitar el conflicto con una instalación existente en 27017. Desde Swagger se ejecutaron POST 201, GET individual 200, GET paginado 200, PUT 200, DELETE 204, GET posterior 404 y POST inválido 400. Las respuestas completas y capturas están en `evidence/`.

SonarQube Community Build 26.11.0.134175 analizó la revisión `82b64df7b9b7837e342b5b91aa9d9d344b497ce9`. El proyecto `telemetria-incidents` aprobó el Quality Gate **HU-01 Incidencias** con condiciones globales explícitas: cobertura 94.9 % (mínimo 80 %), bugs 0, vulnerabilidades 0 y Code Smells 0. Se importaron las 12 pruebas, con 0 errores, fallos u omisiones; también se registraron 0 Security Hotspots y 0.0 % de duplicación.

El análisis empleó tokens temporales, revocados al finalizar. Los accesos temporales usados para capturar el panel fueron desactivados y anonimizados. No se modificó la contraseña de la cuenta existente.

Se conservan el registro Maven, registro SonarQube, JSON de métricas/condiciones, HTML/XML de JaCoCo y capturas reales. El reporte técnico está en `output/pdf/HU-01_Reporte_tecnico.pdf` desde la raíz del repositorio.
