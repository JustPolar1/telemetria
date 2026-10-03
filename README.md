# Épicas de Proyectos para Java y Spring Boot (Consultas e Inserciones sin Transacciones)

## Épica 5: Sistema de Registro de Lecturas de Sensores IoT (Telemetría Básica)
* **Descripción:** Una API backend diseñada para recibir flujos de datos enviados por sensores ambientales (temperatura, humedad) y almacenarlos, permitiendo consultar lecturas pasadas y estadísticas básicas de consulta.
* **Tecnologías:** Spring Boot, Spring Data JPA, PostgreSQL.
* **Historias de Usuario Sugeridas:**
  1. Como dispositivo IoT, quiero enviar e insertar un registro de telemetría con el ID del sensor, valor medido y marca de tiempo.
  2. Como operador, quiero consultar las lecturas registradas de un sensor en particular.