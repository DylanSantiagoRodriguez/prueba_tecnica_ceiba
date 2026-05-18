# Bike Rental API

REST API para gestión de alquiler de bicicletas. Prueba técnica Practicante Java.

---

## Arquitectura

Se usa **arquitectura hexagonal (Ports & Adapters)** para aislar completamente las reglas de negocio del framework y la base de datos, facilitando pruebas unitarias puras y separación de responsabilidades.

```
src/main/java/com/bikerental/
├── domain/                     ← Núcleo puro, CERO dependencias externas
│   ├── model/                  (Bike, Rental, BikeType, BikeStatus)
│   ├── exception/              (excepciones de dominio)
│   ├── port/
│   │   ├── in/                 (interfaces de casos de uso)
│   │   └── out/                (interfaces de repositorios)
│   └── service/                (RentalService — implementa todos los casos de uso)
├── application/dto/            ← DTOs de request y response
└── infrastructure/
    ├── persistence/            (JPA entities, Spring Data repos, mappers, adapters)
    ├── web/                    (controllers, GlobalExceptionHandler)
    └── config/                 (DataInitializer)
```

---

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Data JPA | (incluida en Spring Boot) |
| SQLite JDBC | 3.45.3.0 |
| Hibernate Community Dialects | (incluida en Spring Boot) |
| JUnit 5 + Mockito | (incluidos en spring-boot-starter-test) |

---

## Base de datos

Se usa **SQLite** con archivo local `bikerental.db` incluido en el repositorio. Esto permite que el evaluador clone el proyecto y los datos de referencia ya estén precargados — cero configuración adicional.

Para reiniciar la base de datos desde cero:

```bash
rm bikerental.db
./mvnw spring-boot:run
```

Los archivos `-shm` y `-wal` son temporales del modo WAL de SQLite y están excluidos por `.gitignore`.

---

## Supuestos tomados

- **Enums sin tildes:** Se usan `MONTANA` y `ELECTRICA` (sin tilde) para evitar problemas de encoding en Java enums. Los datos de referencia del enunciado mencionan "MONTAÑA" y "ELÉCTRICA".
- **`returnTime` recibido en el request:** En lugar de usar `LocalDateTime.now()` al finalizar, el tiempo de devolución viene en el body. Esto hace el código determinístico y fácilmente testeable.
- **Retraso calculado desde hora estimada:** La multa se calcula desde `startTime + estimatedHours`, no desde `startTime`. Ejemplo: estimada 2h, devuelta a los 3h20m → retraso = 1h20m (no 3h20m).
- **No se implementa autenticación:** Fuera del alcance para practicante. Spring Security es el punto de extensión natural.
- **DataInitializer idempotente:** Verifica existencia antes de insertar para evitar errores de constraint único al reiniciar con el archivo SQLite ya existente.

---

## Cómo ejecutar localmente

```bash
git clone <url>
cd bike-rental-api
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Las 5 bicicletas de referencia ya están en `bikerental.db`.

---

## Endpoints

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/bikes` | Registrar bicicleta |
| GET | `/api/bikes/available` | Consultar disponibles (filtro opcional `?type=URBANA`) |
| GET | `/api/bikes/{code}/history` | Historial de alquileres por código |
| POST | `/api/rentals` | Iniciar alquiler |
| PATCH | `/api/rentals/{id}/finish` | Finalizar alquiler |

### Ejemplos con curl

```bash
# Registrar bicicleta
curl -X POST http://localhost:8080/api/bikes \
  -H "Content-Type: application/json" \
  -d '{"code":"BIC-010","type":"URBANA","status":"DISPONIBLE"}'

# Consultar disponibles
curl http://localhost:8080/api/bikes/available

# Consultar disponibles por tipo
curl "http://localhost:8080/api/bikes/available?type=MONTANA"

# Iniciar alquiler
curl -X POST http://localhost:8080/api/rentals \
  -H "Content-Type: application/json" \
  -d '{"bikeCode":"BIC-001","customerName":"Ana García","estimatedHours":2}'

# Finalizar alquiler (id=1)
curl -X PATCH http://localhost:8080/api/rentals/1/finish \
  -H "Content-Type: application/json" \
  -d '{"returnTime":"2026-05-18T12:30:00"}'

# Historial de una bicicleta
curl http://localhost:8080/api/bikes/BIC-001/history
```

### Formato de error

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Bicicleta con código BIC-999 no encontrada",
  "timestamp": "2026-05-18T10:30:00"
}
```

---

## Cómo ejecutar los tests

```bash
# Todos los tests
./mvnw test

# Solo pruebas de dominio (sin Spring)
./mvnw test -Dtest=RentalDomainTest

# Solo pruebas del servicio
./mvnw test -Dtest=RentalServiceTest

# Solo pruebas de controladores
./mvnw test -Dtest="BikeControllerTest,RentalControllerTest"
```

---

## Reglas de negocio implementadas

| Regla | Descripción |
|---|---|
| RN-01 | Tarifas: URBANA=$3500/h, MONTANA=$5000/h, ELECTRICA=$7500/h |
| RN-02 | Costo base redondeado al alza (ceil) por hora |
| RN-03 | Multa del 50% por hora de retraso (ceil), calculada desde hora estimada |
| RN-04 | Rechazar alquiler si la bicicleta no está DISPONIBLE (HTTP 409) |
| RN-05 | Rechazar finalización si el alquiler no existe (404) o ya terminó (409) |
