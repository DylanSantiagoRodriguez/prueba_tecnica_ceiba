# Bike Rental API

REST API para gestión de alquiler de bicicletas. Prueba técnica Practicante Java.

---

## Cómo ejecutar localmente

### Requisitos previos

- Java 17+
- Maven 3.8+ (o usar el wrapper incluido `./mvnw`)

### Pasos

```bash
git clone <url-del-repositorio>
cd bike-rental-api
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

Las 5 bicicletas de referencia se cargan automáticamente al iniciar (`DataInitializer` es idempotente).

### Ejecutar los tests

```bash
# Todos los tests
./mvnw test

# Solo dominio (sin Spring context)
./mvnw test -Dtest=RentalDomainTest

# Solo servicio
./mvnw test -Dtest=RentalServiceTest

# Solo controladores
./mvnw test -Dtest="BikeControllerTest,RentalControllerTest"
```

---

## Arquitectura

Se eligió **Arquitectura Hexagonal (Ports & Adapters)** por tres razones concretas:

1. **Aislamiento del dominio:** las reglas de negocio (`Rental`, `Bike`, `RentalService`) no tienen ninguna dependencia de Spring, JPA ni HTTP. Se pueden probar con JUnit puro sin levantar contexto.
2. **Separación de responsabilidades:** el dominio define *qué* necesita (puertos), la infraestructura decide *cómo* lo implementa (adapters). Cambiar el motor de base de datos solo requiere tocar la capa de persistencia.
3. **Testabilidad por capas:** domain tests sin Spring, service tests con Mockito, controller tests con MockMvc — cada capa se prueba de forma independiente.

---

## Capas y responsabilidades

```
┌─────────────────────────────────────────────────────┐
│                   Infrastructure                     │
│                                                      │
│   ┌─────────────┐              ┌──────────────────┐  │
│   │  Controllers │              │  JPA / PostgreSQL│  │
│   │  (HTTP/JSON) │              │  Adapters        │  │
│   └──────┬──────┘              └────────┬─────────┘  │
│          │                              │             │
│     Puertos IN                     Puertos OUT        │
│  (interfaces que                (interfaces que       │
│   el dominio expone)             el dominio exige)    │
│          │                              │             │
│          └──────────┐   ┌──────────────┘             │
│                     ▼   ▼                             │
│              ┌─────────────────┐                     │
│              │     Dominio     │  ← cero imports      │
│              │                 │     de Spring,       │
│              │  Bike, Rental   │     JPA o HTTP       │
│              │  RentalService  │                      │
│              └─────────────────┘                     │
└─────────────────────────────────────────────────────┘
```

---

## Las tres capas en detalle

### Dominio

Contiene las entidades `Bike` y `Rental`, los enums `BikeType` y `BikeStatus`, y el
servicio `RentalService`. No tiene ningún import de `org.springframework` ni de
`jakarta.persistence`. Toda la lógica de negocio vive aquí: el cálculo de costo con
redondeo al alza, la multa por devolución tardía, y las validaciones de estado.

`Rental.finish()` es el método más importante del sistema. Recibe la hora de
devolución y la tarifa, y calcula el costo total aplicando las reglas RN-02 y RN-03.
Que ese método sea un método de instancia de `Rental` y no una función estática en un
servicio es una decisión deliberada: la entidad conoce sus propias reglas de negocio
(modelo de dominio rico), lo que facilita el testing y hace el código más cohesivo.

### Puertos

Son interfaces Java puras definidas dentro del dominio. Los puertos de entrada
(`port/in`) son los casos de uso que el dominio expone al exterior: `StartRentalUseCase`,
`FinishRentalUseCase`, etc. Los puertos de salida (`port/out`) son las dependencias
que el dominio necesita del exterior: `BikeRepository`, `RentalRepository`.

Tener un caso de uso por interfaz aplica el principio de segregación de interfaces:
el `BikeController` solo inyecta `GetAvailableBikesUseCase`, no tiene acceso a
`FinishRentalUseCase`. Cada componente ve únicamente lo que necesita.

### Infraestructura

Contiene todo lo que depende de tecnologías concretas. Los adapters implementan los
puertos de salida del dominio: `BikeRepositoryAdapter` implementa `BikeRepository`
usando JPA por debajo. Los controllers implementan HTTP usando los puertos de entrada.

Esta separación es la que permitió cambiar de H2 a PostgreSQL sin modificar ninguna clase
del dominio ni ningún test de dominio. Solo cambiaron `pom.xml` y
`application.properties`.


---

## Decisiones de diseño secundarias

**`BigDecimal` para valores monetarios.** El tipo `double` tiene errores de
representación en punto flotante que son inaceptables en cálculos de facturación.
`BigDecimal` garantiza aritmética exacta.

**Tarifas embebidas en el enum `BikeType`.** La tarifa es una propiedad intrínseca
del tipo de bicicleta, no un dato de configuración externa. Tenerla en el enum
garantiza que hay un único lugar donde cambiarla y elimina la posibilidad de que
exista un tipo sin tarifa definida.

**`returnTime` capturado automáticamente.** Al finalizar un alquiler, el servicio captura `LocalDateTime.now()` internamente. El endpoint `PATCH /api/rentals/{id}/finish` no requiere body. Los tests de dominio pasan la hora de devolución directamente al método `Rental.finish()`, lo que los mantiene determinísticos sin necesidad de mockear el reloj del sistema.

**Mappers manuales en lugar de MapStruct.** Para el alcance del proyecto, los mappers
manuales con métodos estáticos son más explícitos y no requieren procesamiento de
anotaciones en tiempo de compilación. MapStruct es la elección correcta en proyectos
más grandes donde la cantidad de conversiones justifica el setup.

**PostgreSQL hosteado en Railway.** La base de datos vive en un servicio externo; las credenciales se inyectan como variables de entorno (`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`). Para desarrollo local basta con un archivo `.env` en la raíz del proyecto — `spring-dotenv` lo carga automáticamente. El `DataInitializer` es idempotente (verifica existencia antes de insertar) para que el arranque sea seguro en cualquier entorno.

**Flujo de una petición:**

```
HTTP Request → Controller → UseCase (port in) → RentalService → Repository (port out) → JPA Adapter → PostgreSQL
```

---

## Tecnologías y dependencias

| Componente | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje base |
| Spring Boot | 3.2.5 | Framework principal (web, DI, test) |
| Spring Data JPA | incluida | Abstracción de persistencia |
| Spring Validation | incluida | Validación de DTOs con Bean Validation |
| PostgreSQL JDBC | incluida en Boot | Driver de base de datos |
| spring-dotenv | 3.0.0 | Carga variables de entorno desde `.env` en local |
| JUnit 5 | incluida en starter-test | Tests unitarios e integración |
| Mockito | incluida en starter-test | Mocks en tests de servicio |
| MockMvc | incluida en starter-test | Tests de capa web |

### PostgreSQL en Railway

Base de datos gestionada, sin configuración local. El evaluador solo necesita un archivo `.env` con las variables `PG*` para conectarse. En producción (Railway), las variables se inyectan automáticamente.

---

## Endpoints

Base URL: `http://localhost:8080`

Todos los errores devuelven el mismo formato:
```json
{ "status": 404, "error": "NOT_FOUND", "message": "Bicicleta BIC-999 no encontrada" }
```

---

### Bicicletas

#### `POST /api/bikes` — Registrar bicicleta

**Body (JSON):**
```json
{
  "code": "BIC-010",
  "type": "URBANA",
  "status": "DISPONIBLE"
}
```
- `type`: `URBANA` · `MONTANA` · `ELECTRICA`
- `status`: `DISPONIBLE` · `ALQUILADA` · `EN_MANTENIMIENTO`

**Respuesta `201`:**
```json
{ "code": "BIC-010", "type": "URBANA", "status": "DISPONIBLE" }
```

**Errores:** `400` si falta `code`, `type` o `status`.

---

#### `GET /api/bikes` — Listar bicicletas

**Query params (todos opcionales, combinables):**

| Parámetro | Valores | Descripción |
|---|---|---|
| `status` | `DISPONIBLE` · `ALQUILADA` · `EN_MANTENIMIENTO` | Filtra por estado |
| `type` | `URBANA` · `MONTANA` · `ELECTRICA` | Filtra por tipo |

**Respuesta `200`:**
```json
[
  { "code": "BIC-001", "type": "URBANA", "status": "DISPONIBLE" },
  { "code": "BIC-002", "type": "MONTANA", "status": "ALQUILADA" }
]
```

```bash
GET /api/bikes                              # todas
GET /api/bikes?status=DISPONIBLE            # por estado
GET /api/bikes?type=MONTANA                 # por tipo
GET /api/bikes?status=DISPONIBLE&type=URBANA  # combinables
```

---

#### `GET /api/bikes/available` — Bicicletas disponibles

Equivale a `GET /api/bikes?status=DISPONIBLE` con filtro adicional por tipo.

**Query params (opcional):**

| Parámetro | Valores | Descripción |
|---|---|---|
| `type` | `URBANA` · `MONTANA` · `ELECTRICA` | Filtra por tipo dentro de las disponibles |

**Respuesta `200`:** igual que `/api/bikes`.

```bash
GET /api/bikes/available              # todas las disponibles
GET /api/bikes/available?type=MONTANA # disponibles de tipo MONTANA
```

---

#### `DELETE /api/bikes/{code}` — Eliminar bicicleta

**Path param:** `code` — código de la bicicleta.

**Respuesta `204`:** sin body.

**Errores:**
- `404` — bicicleta no encontrada.
- `409` — la bicicleta tiene un alquiler activo; no se puede eliminar.

```bash
curl -X DELETE http://localhost:8080/api/bikes/BIC-010
```

---

#### `GET /api/bikes/{code}/history` — Historial de alquileres

**Path param:** `code` — código de la bicicleta.

**Respuesta `200`:**
```json
[
  {
    "id": 1,
    "bikeCode": "BIC-001",
    "customerName": "Ana García",
    "startTime": "2026-05-18T10:00:00",
    "endTime": "2026-05-18T12:15:00",
    "realDurationMinutes": 135,
    "totalCost": 10500.00,
    "hasPenalty": true,
    "finished": true
  }
]
```

- `endTime` / `realDurationMinutes` / `totalCost` son `null` si el alquiler está activo.
- `hasPenalty: true` indica que se aplicó el 50% de recargo por devolución tardía.

**Errores:** `404` — bicicleta no encontrada.

```bash
curl http://localhost:8080/api/bikes/BIC-001/history
```

---

### Alquileres

#### `POST /api/rentals` — Iniciar alquiler

**Body (JSON):**
```json
{
  "bikeCode": "BIC-001",
  "customerName": "Ana García",
  "estimatedMinutes": 90
}
```
- `estimatedMinutes`: duración estimada en minutos (entero positivo). Se usa para calcular la multa si hay retraso.

**Respuesta `201`:**
```json
{
  "id": 1,
  "bikeCode": "BIC-001",
  "customerName": "Ana García",
  "startTime": "2026-05-18T10:00:00",
  "endTime": null,
  "realDurationMinutes": null,
  "totalCost": null,
  "hasPenalty": false,
  "finished": false
}
```

**Errores:**
- `404` — bicicleta no encontrada.
- `409` — bicicleta no disponible (`ALQUILADA` o `EN_MANTENIMIENTO`).

---

#### `GET /api/rentals` — Consultar alquileres

**Query params (opcional):**

| Parámetro | Valores | Descripción |
|---|---|---|
| `finished` | `true` · `false` | Filtra por estado del alquiler. Sin parámetro devuelve todos. |

**Respuesta `200`:** lista de alquileres con el mismo formato que `POST /api/rentals`.

```bash
GET /api/rentals                # historial completo
GET /api/rentals?finished=false # solo activos
GET /api/rentals?finished=true  # solo finalizados
```

---

#### `PATCH /api/rentals/{id}/finish` — Finalizar alquiler

**Path param:** `id` — ID del alquiler.

No requiere body. La hora de devolución se captura automáticamente con `LocalDateTime.now()`.

**Respuesta `200`:**
```json
{
  "id": 1,
  "bikeCode": "BIC-001",
  "customerName": "Ana García",
  "startTime": "2026-05-18T10:00:00",
  "endTime": "2026-05-18T12:15:00",
  "realDurationMinutes": 135,
  "totalCost": 10500.00,
  "hasPenalty": true,
  "finished": true
}
```

**Errores:**
- `404` — alquiler no encontrado.
- `409` — alquiler ya finalizado.

```bash
curl -X PATCH http://localhost:8080/api/rentals/1/finish
```


---



## Supuestos tomados

- **Enums sin tildes:** `MONTANA` y `ELECTRICA` en lugar de "MONTAÑA"/"ELÉCTRICA" para evitar problemas de encoding en Java enums.
- **Tiempo estimado en minutos:** `estimatedMinutes` permite rentas de cualquier duración (ej. 45 min, 90 min), no solo horas completas.
- **`returnTime` automático:** Al finalizar un alquiler se usa `LocalDateTime.now()` — no requiere body en el PATCH.
- **Retraso desde hora estimada:** el cálculo de multa parte de `startTime + estimatedMinutes`, no de `startTime`.
- **No se implementa autenticación:** fuera del alcance; Spring Security es el punto de extensión natural.
