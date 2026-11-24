# Capgemini-Test

## Prerrequisitos

Asegúrese de tener instalados los siguientes componentes antes de ejecutar el proyecto:

- Java 17
- Maven
- Git
- Docker

---

## Ejecución del Docker para tener la infraestructura necesaria

1. Clone el repositorio:
   bash
   git clone <url-del-repositorio>
   
2. Navegue a la carpeta docker:
   bash
   cd docker
   
3. Levante los contenedores con Docker Compose:
   bash
   docker-compose up
   

### Contenedores del Docker

- *PostgreSQL:* Base de datos.
- *Mock-server:* API expuesta en el puerto 1080.

---

## APIs Expuestas en el Mock-server

### 1. API check-dni
- *Método:* PATCH
- *URL:* http://localhost:1080/check-dni
- *Body de la petición:*
  json
  {
    "dni": "<dni>"
  }
  
- *Respuestas:*
  - *OK:* Código HTTP 200 para cualquier DNI.
  - *KO:* Código HTTP 409 si el DNI es 99999999w.

---

### 2. API notification
#### Email
- *Método:* POST
- *URL:* http://localhost:1080/email
- *Body de la petición:*
  json
  {
    "email": "<email>",
    "message": "<msg>"
  }
  
- *Respuesta:*
  - *OK:* Código HTTP 200.

#### SMS
- *Método:* POST
- *URL:* http://localhost:1080/sms
- *Body de la petición:*
  json
  {
    "phone": "<phone>",
    "message": "<msg>"
  }
  
- *Respuesta:*
  - *OK:* Código HTTP 200.

---

## Contexto

La aplicación debe gestionar salas y usuarios bajo las siguientes condiciones:

- Cada sala tiene un ID único (long incremental).
- Una sala puede contener N usuarios.
- Un usuario puede estar en una única sala.
- Al guardar el usuario en la sala, se valida su DNI contra una API externa.
- Almacena el usuario en la base de datos y notifica según su rol.
- Devuelve el ID del usuario almacenado.

---

## Requisitos

### Validaciones:
1. *Nombre:* No debe contener más de 6 caracteres.
2. *Email:* Debe contener un @ y un ..
3. *Rol:* Sólo puede ser admin o superadmin.
4. Si el usuario ya existe (por email), se debe lanzar una excepción.
5. Validar el DNI contra la API externa del mock-server.

### Notificaciones:
- *Admin:* Notificación por email con el mensaje: "usuario guardado".
- *Superadmin:* Notificación por SMS con el mensaje: "usuario guardado".

### Respuesta esperada:
- *Retornar el ID generado.*

---

## Métodos a Implementar

### 1. Crear Usuario
- *Método:* POST
- *Descripción:* Guarda un usuario en la sala 1 y retorna su ID.
- *Ejemplo de JSON a guardar:*
  json
  {
    "name": "pablo",
    "email": "email@email.com",
    "phone": "677998899",
    "rol": "admin",
    "dni": "23454234W"
  }
  
- *Respuestas:*
  - *OK:* 
    - Código HTTP 201 Created
    - Body:
      json
      {
        "id": "<id>"
      }
      
  - *KO:* 
    - Código HTTP 409 Conflict
    - Body:
      json
      {
        "code": 409,
        "message": "error validation <email | userName | dni>"
      }
      

### 2. Obtener Usuario
- *Método:* GET
- *Descripción:* Obtiene un usuario basado en su ID dentro de la sala 1.

---

## Condiciones Opcionales

1. *Escalabilidad:* La aplicación podrá escalar para manejar diferentes contextos (pagos, pedidos, etc.).
2. *Capacidad:* La aplicación podrá procesar desde una hasta millones de peticiones (no es necesario implementar Kubernetes).

---

## Cosas que se Valoran

1. *Pruebas:*
   - Unitarias.
   - De integración.
   - De aceptación.
2. *Arquitectura e implementación.*
3. *Uso de Spring y abstracción del framework.*
4. *Patrones de diseño.*

---

## Método de Entrega

- Subir el proyecto a un repositorio público personal.
- Todos los commits deben estar realizados en la rama main.

---

## Solución

### Cómo ejecutar

Requisitos: Java 17 y Docker. Maven es opcional: el proyecto incluye Maven Wrapper.

```bash
cd docker && docker compose up -d && cd ..
./mvnw spring-boot:run            # Windows: mvnw.cmd spring-boot:run
```

```bash
curl -i -X POST http://localhost:8080/users -H "Content-Type: application/json" \
  -d '{"name":"pablo","email":"email@email.com","phone":"677998899","rol":"admin","dni":"23454234W"}'
# 201 Created · Location: /users/3 · {"id":3}

curl http://localhost:8080/users/1
# {"id":1,"name":"Juan Pérez","email":"juan@example.com","phone":"600000001","rol":"admin","dni":"12345678Z"}
```

La colección de `postman/` incluye estas peticiones y las del mock-server.

### Tests

| Comando | Qué ejecuta | Requisitos |
|---|---|---|
| `./mvnw test` | Unitarias (dominio, casos de uso, adaptadores) y test del controlador (`@WebMvcTest`) | Ninguno |
| `./mvnw verify` | Lo anterior + integración y aceptación (`*IT`) | Docker en ejecución |

Los tests de integración y aceptación levantan sus propios contenedores con Testcontainers
(PostgreSQL 16 y MockServer cargando el mismo `docker/mockserver-config/init.json`), por lo que
no necesitan `docker compose up`.

- **Integración:** adaptador JPA contra PostgreSQL real y clientes Feign contra el mock-server.
- **Aceptación (`UserAcceptanceIT`):** los escenarios de este README por HTTP, verificando además
  que el mock-server recibe el email o el SMS.

### Arquitectura

Hexagonal (puertos y adaptadores), organizada por contexto:

```
user/
├── domain/              User, Role y excepciones de dominio (sin Spring)
├── application/
│   ├── port/in/         CreateUserUseCase, GetUserUseCase
│   ├── port/out/        UserRepository, DniValidator, UserNotifier
│   └── service/         Casos de uso en Java plano
└── infrastructure/
    ├── rest/            Controlador, DTOs y manejador global de errores
    ├── persistence/     Adaptador JPA
    ├── dni/             Cliente Feign de check-dni
    ├── notification/    Clientes Feign de email y SMS
    └── config/          Único punto donde se conectan puertos y adaptadores
```

- **Puertos y adaptadores:** dominio y casos de uso no dependen de Spring, JPA ni Feign.
- **Strategy:** notificación por rol (`admin` → email, `superadmin` → SMS) mediante un `EnumMap` en `UserConfig`.
- **Factory method:** `User.create(...)` valida los usuarios nuevos; los ya guardados se reconstruyen sin revalidar.
- **Adapter:** JPA y Feign detrás de interfaces propias; sus errores técnicos se traducen a errores de dominio.

Flujo de alta: validación de campos → email duplicado → DNI (API externa) → guardado en la sala 1 →
notificación. Las comprobaciones baratas van primero para no llamar a la API externa con peticiones inválidas.

### Decisiones y supuestos

- **Formato de error** `{"code", "message"}` en todas las respuestas:

  | Código | Mensaje |
  |---|---|
  | 409 | `error validation <userName \| email \| rol \| phone \| dni>` |
  | 404 | `user not found` (también si el usuario está en otra sala) |
  | 400 | `malformed request` |
  | 502 | `dni validation unavailable` (la API de DNI falla o no responde) |
  | 500 | `internal error` (sin detalles internos) |

- **`rol` y `phone`:** el enunciado no define su mensaje; se usa el nombre del campo. `rol` se acepta sin distinguir mayúsculas.
- **`id`** se devuelve como número (`{"id": 3}`), al ser un long incremental.
- **Unicidad:** DNI y teléfono también son `UNIQUE` en la base de datos. Los duplicados devuelven 409 a partir
  de esas restricciones, lo que además cubre dos altas simultáneas con el mismo email.
- **Notificación fallida:** el usuario ya está guardado, así que se registra el error y se devuelve 201.
  Fallar provocaría reintentos que chocarían con el email duplicado.
- **DNI en el mock-server:** sólo `99999999w` en minúsculas devuelve 409; `99999999W` responde 200.
  El DNI se envía tal cual, sin normalizar.
- **Proyecto base:** no compilaba (faltaba `feign-okhttp`) y la migración V1 fallaba (usuarios semilla sin
  `dni`/`phone` y con un rol inválido). V1 se corrigió directamente porque nunca llegó a aplicarse, y los ids
  pasan a `BIGSERIAL` según el enunciado. Las imágenes Docker se fijan a versiones concretas.
- **Testcontainers 1.21.4:** la versión que gestiona Spring Boot 3.2.0 no es compatible con Docker Engine 29+.

### Escalabilidad

Ya preparado:
- Aplicación sin estado: escala horizontalmente con varias instancias tras un balanceador.
- Pool de conexiones HTTP (OkHttp) y timeouts cortos (2 s / 5 s) hacia las APIs externas.
- La unicidad la garantiza la base de datos, por lo que es segura entre instancias concurrentes.
- Nuevos contextos (pagos, pedidos…) se añaden como paquetes hermanos de `user/` con la misma estructura.

Siguientes pasos para alto volumen:
- Notificaciones asíncronas (cola de mensajes o patrón outbox), fuera del tiempo de respuesta.
- Circuit breaker y reintentos (p. ej. Resilience4j) sobre la API de DNI.
- Métricas y trazas (Actuator, Micrometer) y réplicas de lectura para las consultas.
