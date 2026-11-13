# Checklist HU-01

Alcance: microservicio `incident-service`. Una casilla pendiente puede tener código implementado, pero todavía carecer de evidencia de ejecución. Estado revisado el 12 de noviembre de 2026.

## 1. Microservicio y base NoSQL

- [x] Implementar un microservicio con un framework moderno: Spring Boot.
- [ ] Verificar conexión correcta a MongoDB y operaciones CRUD completas sobre incidencias. Los endpoints y repositorio están implementados; falta ejecutarlos contra MongoDB real.
- [x] Aplicar validaciones de campos obligatorios, tamaños, formato del sensor y estado permitido.

## 2. Errores y excepciones

- [x] Implementar excepciones de dominio: `IncidentNotFoundException` y `DuplicateIncidentException`.
- [x] Implementar manejador global con `@RestControllerAdvice` para excepciones de negocio y validación.
- [x] Devolver errores JSON con mensajes descriptivos y estados HTTP adecuados. Pruebas web aprobadas para 400, 404, 409 y 503.

## 3. Pruebas

- [x] Desarrollar y ejecutar pruebas unitarias de servicios con JUnit y Mockito, incluyendo éxito y error: 7 aprobadas.
- [ ] Ejecutar satisfactoriamente la integración del flujo completo de endpoints con MongoDB, serialización y respuestas HTTP. `IncidentMongoIT` está implementada; falta ejecutarla. Las 4 pruebas de contrato web aprobadas usan un servicio simulado y no acreditan persistencia real.

## 4. JaCoCo

- [x] Configurar JaCoCo para generar HTML/XML y exigir un mínimo de 80 % de líneas y ramas.
- [ ] Generar y conservar el reporte de una compilación con todas las pruebas, incluida integración. La ejecución parcial con `-DskipITs=true` produjo 94.87 % de líneas; no detectó ramas medibles (no equivale a 100 % de ramas).

## 5. SonarQube

- [ ] Ejecutar el análisis del código fuente en SonarQube. El plugin y la ruta al reporte de cobertura están configurados; no hay evidencia de análisis realizado.
- [ ] Aprobar el Quality Gate y verificar ausencia de bugs críticos y vulnerabilidades, con control de Code Smells. Corregir lo que indique el análisis y volver a ejecutarlo si es necesario.

## 6. Entregables

- [ ] Entregar enlace al repositorio público o institucional con los cambios accesibles, commits estructurados y README de ejecución. README y commits locales están preparados; falta publicar los cambios y comprobar el acceso al enlace de entrega.
- [ ] Entregar reporte técnico PDF formal con portada y las siguientes evidencias:
  - Endpoints funcionando en Postman o Swagger.
  - Ejecución de pruebas unitarias y de integración.
  - Reporte JaCoCo con líneas y ramas.
  - Panel SonarQube y Quality Gate.

## Siguiente verificación

Con Docker disponible, desde la raíz del repositorio:

```powershell
docker compose -f incident-service/compose.yaml up --build -d
mvn -f incident-service/pom.xml clean verify
```

Después, probar los endpoints con la colección de Postman, ejecutar SonarQube y recopilar las evidencias para el PDF. Al revisar este checklist, Docker no estaba disponible y no existía un reporte de ejecución de la prueba de integración.
