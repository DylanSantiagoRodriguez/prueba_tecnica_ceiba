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

Las 5 bicicletas de referencia ya están precargadas en `bikerental.db` — no se necesita ninguna configuración adicional de base de datos.

### Reiniciar la base de datos desde cero

```bash
rm bikerental.db
./mvnw spring-boot:run
```

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
2. **Separación de responsabilidades:** el dominio define *qué* necesita (puertos), la infraestructura decide *cómo* lo implementa (adapters). Cambiar de SQLite a PostgreSQL solo requiere tocar la capa de persistencia.
3. **Testabilidad por capas:** domain tests sin Spring, service tests con Mockito, controller tests con MockMvc — cada capa se prueba de forma independiente.

---

## Capas y responsabilidades

```
┌─────────────────────────────────────────────────────┐
│                   Infrastructure                     │
│                                                      │
│   ┌─────────────┐              ┌──────────────────┐  │
│   │  Controllers │              │  JPA / SQLite    │  │
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

Esta separación es la que permitió cambiar de H2 a SQLite sin modificar ninguna clase
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

**SQLite con archivo incluido en el repositorio.** Permite que el evaluador clone el
proyecto y lo ejecute directamente con los datos de referencia ya cargados, sin
instalar ni configurar ningún motor de base de datos externo. El `DataInitializer`
es idempotente (verifica existencia antes de insertar) para que la aplicación no
falle al reiniciarse con el archivo ya poblado.

**Flujo de una petición:**

```
HTTP Request → Controller → UseCase (port in) → RentalService → Repository (port out) → JPA Adapter → SQLite
```

---

## Tecnologías y dependencias

| Componente | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje base |
| Spring Boot | 3.2.5 | Framework principal (web, DI, test) |
| Spring Data JPA | incluida | Abstracción de persistencia |
| Spring Validation | incluida | Validación de DTOs con Bean Validation |
| SQLite JDBC | 3.45.3.0 | Driver de base de datos |
| Hibernate Community Dialects | incluida | Dialecto SQLite para Hibernate 6 |
| JUnit 5 | incluida en starter-test | Tests unitarios e integración |
| Mockito | incluida en starter-test | Mocks en tests de servicio |
| MockMvc | incluida en starter-test | Tests de capa web |

### Por qué SQLite

El archivo `bikerental.db` se incluye en el repositorio con los datos de referencia precargados. El evaluador clona el proyecto y ejecuta `./mvnw spring-boot:run` — sin instalar ni configurar ningún motor de base de datos externo.

---

## Endpoints

| Método | URL | Descripción | Código |
|---|---|---|---|
| `POST` | `/api/bikes` | Registrar bicicleta | 201 |
| `GET` | `/api/bikes` | Listar todas (`?status=DISPONIBLE\|ALQUILADA\|EN_MANTENIMIENTO`) | 200 |
| `GET` | `/api/bikes/available` | Solo disponibles (filtro opcional `?type=URBANA\|MONTANA\|ELECTRICA`) | 200 |
| `GET` | `/api/bikes/{code}/history` | Historial de alquileres por bicicleta | 200 |
| `POST` | `/api/rentals` | Iniciar alquiler | 201 |
| `GET` | `/api/rentals` | Ver alquileres activos | 200 |
| `PATCH` | `/api/rentals/{id}/finish` | Finalizar alquiler  | 200 |

### Ejemplos con curl

```bash
# Registrar bicicleta
curl -X POST http://localhost:8080/api/bikes \
  -H "Content-Type: application/json" \
  -d '{"code":"BIC-010","type":"URBANA","status":"DISPONIBLE"}'

# Ver todas las bicicletas
curl http://localhost:8080/api/bikes

# Ver solo disponibles
curl http://localhost:8080/api/bikes/available

# Filtrar disponibles por tipo
curl "http://localhost:8080/api/bikes/available?type=MONTANA"

# Iniciar alquiler (estimatedMinutes en minutos)
curl -X POST http://localhost:8080/api/rentals \
  -H "Content-Type: application/json" \
  -d '{"bikeCode":"BIC-001","customerName":"Ana García","estimatedMinutes":90}'

# Ver alquileres activos
curl http://localhost:8080/api/rentals

# Finalizar alquiler — sin body, el tiempo se captura automáticamente
curl -X PATCH http://localhost:8080/api/rentals/1/finish

# Historial de una bicicleta
curl http://localhost:8080/api/bikes/BIC-001/history
```


---

## Reglas de negocio

| Regla | Descripción |
|---|---|
| RN-01 | Tarifas por hora: URBANA $3.500, MONTANA $5.000, ELECTRICA $7.500 |
| RN-02 | Costo base redondeado al alza (ceil) por hora |
| RN-03 | Retraso cobra 50% de la tarifa/hora (ceil), calculado desde la hora estimada de devolución |
| RN-04 | Iniciar alquiler en bicicleta no disponible → HTTP 409 |
| RN-05 | Finalizar alquiler inexistente → 404 · ya finalizado → 409 |

## Supuestos tomados

- **Enums sin tildes:** `MONTANA` y `ELECTRICA` en lugar de "MONTAÑA"/"ELÉCTRICA" para evitar problemas de encoding en Java enums.
- **Tiempo estimado en minutos:** `estimatedMinutes` permite rentas de cualquier duración (ej. 45 min, 90 min), no solo horas completas.
- **`returnTime` automático:** Al finalizar un alquiler se usa `LocalDateTime.now()` — no requiere body en el PATCH.
- **Retraso desde hora estimada:** el cálculo de multa parte de `startTime + estimatedMinutes`, no de `startTime`.
- **No se implementa autenticación:** fuera del alcance; Spring Security es el punto de extensión natural.
