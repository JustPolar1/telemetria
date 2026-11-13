# Checklist HU-01

Alcance: microservicio `incident-service`. Estado revisado el 13 de noviembre de 2026. Las evidencias se conservan en `evidence/` y en el reporte técnico PDF.

## 1. Microservicio y base NoSQL

- [x] Implementar un microservicio con un framework moderno: Spring Boot.
- [x] Verificar conexión correcta a MongoDB y operaciones CRUD completas sobre incidencias. `IncidentMongoIT` aprobada con MongoDB 8.0 real en Testcontainers.
- [x] Aplicar validaciones de campos obligatorios, tamaños, formato del sensor y estado permitido.

## 2. Errores y excepciones

- [x] Implementar excepciones de dominio: `IncidentNotFoundException` y `DuplicateIncidentException`.
- [x] Implementar manejador global con `@RestControllerAdvice` para excepciones de negocio y validación.
- [x] Devolver errores JSON con mensajes descriptivos y estados HTTP adecuados. Pruebas web aprobadas para 400, 404, 409 y 503.

## 3. Pruebas

- [x] Desarrollar y ejecutar pruebas unitarias de servicios con JUnit y Mockito, incluyendo éxito y error: 7 aprobadas.
- [x] Ejecutar satisfactoriamente la integración del flujo completo de endpoints con MongoDB, serialización y respuestas HTTP. `IncidentMongoIT` aprobada sin omisiones; también se aprobaron las 4 pruebas de contrato web.

## 4. JaCoCo

- [x] Configurar JaCoCo para generar HTML/XML y exigir un mínimo de 80 % de líneas y ramas.
- [x] Generar y conservar el reporte de una compilación con todas las pruebas, incluida integración. `clean verify` aprobó sin omitir pruebas: 94.87 % de líneas (37 de 39). No se detectaron ramas medibles (no equivale a 100 % de ramas). HTML portable en `evidence/jacoco/index.html` y captura incorporada al PDF.

## 5. SonarQube

- [x] Ejecutar el análisis del código fuente en SonarQube, importando cobertura y resultados de las 12 pruebas. Evidencias en `evidence/sonar.txt` y `evidence/sonar-results.json`.
- [x] Aprobar el Quality Gate: cobertura global >= 80 %, 0 bugs, 0 vulnerabilidades y 0 Code Smells. Todas las condiciones globales evaluadas aprobaron; cobertura importada 94.9 %. Se registraron además 0 Security Hotspots.

## 6. Entregables

- [x] Entregar enlace al repositorio público con commits estructurados y README de ejecución: https://github.com/JustPolar1/telemetria/tree/feature/incidents-mongodb-hu01 . Rama publicada y acceso al repositorio comprobado.
- [x] Entregar reporte técnico PDF formal: `output/pdf/HU-01_Reporte_tecnico.pdf` desde la raíz, con portada y las siguientes evidencias:
  - Endpoints funcionando en Postman o Swagger.
  - Ejecución de pruebas unitarias y de integración.
  - Reporte JaCoCo con líneas y ramas.
  - Panel SonarQube y Quality Gate.

## Verificación realizada

Con Docker disponible, desde la raíz del repositorio:

```powershell
mvn -f incident-service/pom.xml clean verify
```

Ejecutado satisfactoriamente el 13 de noviembre de 2026: 7 pruebas de servicio, 4 de contrato web y 1 de integración con MongoDB real; sin fallos, errores ni omisiones. Se verificó también Swagger UI y se capturaron respuestas reales 201, 200, 204, 400 y 404. Detalles en `VERIFICACION-HU-01.md`.

SonarQube y su Quality Gate también se verificaron satisfactoriamente. El PDF y los archivos de evidencia acompañan a la entrega.
