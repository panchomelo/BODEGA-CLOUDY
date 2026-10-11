# Guía de pruebas de API en Postman — BODEGA-CLOUDY

> **Alcance y honestidad técnica:** esta guía se elaboró **únicamente por lectura del código** (`README.md`, controladores `@RestController`, DTOs con validaciones `jakarta.validation`, clases `SecurityConfig`, archivos `application.yml` y `compose.yml`).
> **No se ejecutaron pruebas HTTP** (no se levantaron los microservicios ni se enviaron solicitudes reales). Por tanto, los ejemplos de respuesta son **ilustrativos** y derivan de la forma de los DTOs de salida, no de una ejecución observada.
> No se realizó ninguna operación de escritura, actualización o borrado contra una base de datos.

---

## 1. Prerrequisitos

1. Microservicios levantados, por ejemplo con:
   ```bash
   docker compose up --build
   ```
2. MySQL disponible en `3306` y microservicios en los puertos verificados:
   | Microservicio | Puerto verificado (`application.yml` / `compose.yml`) | Base de datos |
   |---|---:|---|
   | `ms-categoria` | `8081` | `catalogo_db` |
   | `ms-usuario` | `8082` | `identidad_db` |
   | `ms-producto` | `8083` | `catalogo_db` |
   | `ms-inventario` | `8084` | `operacion_db` |
   | `ms-movimiento` | `8085` | `operacion_db` |
3. Postman con una colección y una variable de entorno, por ejemplo:
   - `{{accessToken}}`: token JWT devuelto por el login.
   - `{{refreshToken}}`: token de refresco.
4. Dependencias entre servicios verificadas en `application.yml` / `compose.yml`:
   - `ms-producto` valida categorías contra `ms-categoria` (`MS_CATEGORIA_URL`).
   - `ms-inventario` referencia a `ms-producto` (`MS_PRODUCTO_URL`).
   - `ms-movimiento` referencia a `ms-producto` y `ms-inventario`.

---

## 2. Autenticación y encabezados comunes

### 2.1 Seguridad verificada

- `ms-producto`, `ms-categoria`, `ms-inventario` y `ms-movimiento`: `SecurityConfig` exige `anyRequest().authenticated()` en modo stateless JWT.
  - Sin `Authorization`, responden `401` con cuerpo:
    ```json
    {"error": "No autenticado"}
    ```
- `ms-usuario`: solo son públicos `POST /usuarios` y `POST /auth/**`. Todo lo demás exige JWT.
- No hay roles: cualquier token válido puede acceder a cualquier endpoint protegido.

### 2.2 Encabezados para Postman

Para todo endpoint protegido:

| Header | Valor |
|---|---|
| `Authorization` | `Bearer {{accessToken}}` |
| `Content-Type` | `application/json` (solo en `POST` / `PUT` con body) |
| `Accept` | `application/json` (opcional) |

En Postman se recomienda usar la pestaña **Authorization → Type: Bearer Token** y pegar `{{accessToken}}`.

### 2.3 Paso 0 — Obtener token

#### `POST http://localhost:8082/auth/login` (público)

Verificado en `AuthController.java:21-24`. DTO de entrada `AuthRequest.java`: `username` y `password` obligatorios, no nulos/vacíos.

**Request:**
```http
POST http://localhost:8082/auth/login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "admin123"
}
```

> Los usuarios semilla (`admin` / `admin123`) se mencionan en `README.md:66,79-83`. No se verificó el SQL semilla en este documento; si esas credenciales no existen en tu entorno, usa un usuario creado por `POST /usuarios`.

**Respuesta esperada (derivada de `AuthResponse.java`, no observada en ejecución):**
- `200 OK`, ejemplo ilustrativo:
  ```json
  {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "tokenType": "Bearer"
  }
  ```
- Guarda `accessToken` en la variable `{{accessToken}}`.

**Errores posibles:**
- `400 Bad Request`: falta `username` o `password`, o están vacíos:
  ```json
  {
    "username": "El username no puede estar vacío"
  }
  ```
- `401 Unauthorized` / `400` por credenciales inválidas, según `UsuarioService` (mensaje exacto no verificado por ejecución; no se inventa aquí el texto literal).

#### `POST http://localhost:8082/auth/refresh` (público)

Verificado en `AuthController.java:26-29`. Entrada `RefreshRequest.java`: campo `refreshToken` obligatorio.

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

Respuesta esperada: `200 OK` con un nuevo par `accessToken` / `refreshToken` (forma `AuthResponse`).

#### `POST http://localhost:8082/auth/logout` (público)

Verificado en `AuthController.java:31-35`.

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

Respuesta esperada: `204 No Content`.

---

## 3. ms-producto (`http://localhost:8083`)

Base verificada: `@RequestMapping("/productos")` en `ProductoController.java:14`.

### 3.1 `GET /productos` — Listar productos

Verificado en `ProductoController.java:20-23`. Retorna `List<ProductoResponseDTO>`.

```http
GET http://localhost:8083/productos
Authorization: Bearer {{accessToken}}
Accept: application/json
```

**Respuesta esperada:**
- `200 OK`, ejemplo ilustrativo según `ProductoResponseDTO.java` (`id`, `nombre`, `precio`, `categoriaId`):
  ```json
  [
    {
      "id": 1,
      "nombre": "Teclado Mecánico",
      "precio": 45990.0,
      "categoriaId": 1
    }
  ]
  ```
- Lista vacía `[]` si aún no hay productos.

**Errores:**
- `401 {"error":"No autenticado"}` si falta el token o expiró.

### 3.2 `GET /productos/{id}` — Consultar por ID

Verificado en `ProductoController.java:25-28`.

```http
GET http://localhost:8083/productos/1
Authorization: Bearer {{accessToken}}
```

**Respuesta esperada:**
- `200 OK`:
  ```json
  {
    "id": 1,
    "nombre": "Teclado Mecánico",
    "precio": 45990.0,
    "categoriaId": 1
  }
  ```

**Errores:**
- `401` sin token.
- `404 Not Found` si no existe, vía `ProductoNotFoundException` (`GlobalExceptionHandler.java:23-28`), forma:
  ```json
  {"error": "<mensaje del servidor>"}
  ```
  El texto exacto no se afirma porque no se ejecutó la petición.

### 3.3 Endpoints GET adicionales verificados

| Método y ruta | Verificación | Ejemplo Postman |
|---|---|---|
| `GET /productos/categoria/{catId}` | `ProductoController.java:31-34` | `GET http://localhost:8083/productos/categoria/1` + Bearer |
| `GET /productos/buscar?nombre=...` | `ProductoController.java:37-40`, `@RequestParam String nombre` obligatorio | `GET http://localhost:8083/productos/buscar?nombre=teclado` + Bearer |

Responden `200 OK` con arreglo de `ProductoResponseDTO`. Si falta el query param `nombre`, Spring responde `400` por parámetro requerido.

### 3.4 `POST /productos` — Crear producto

Verificado en `ProductoController.java:42-46`. Entrada `ProductoRequestDTO.java:20-31`.

```http
POST http://localhost:8083/productos
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "nombre": "Teclado Mecánico",
  "precio": 45990.0,
  "categoriaId": 1
}
```

Validaciones reales:
- `nombre`: obligatorio, no vacío, máximo 100 caracteres.
- `precio`: obligatorio, no negativo (`@DecimalMin("0.0")`).
- `categoriaId`: obligatorio. Además el servicio valida la categoría contra `ms-categoria` vía WebClient (requiere que `ms-categoria` esté levantado).

**Respuesta esperada:**
- `201 Created` con el `ProductoResponseDTO` creado (incluye `id`).

**Errores:**
- `400` validación, ejemplo ilustrativo según `GlobalExceptionHandler.java:15-19`:
  ```json
  {
    "nombre": "El nombre no puede estar vacío",
    "precio": "El precio es obligatorio",
    "categoriaId": "El categoriaId es obligatorio"
  }
  ```
  Solo aparecen los campos que fallen.
- `400 {"error":"..."}` si `categoriaId` no existe en `ms-categoria` (vía `RuntimeException`, `GlobalExceptionHandler.java:31-36`). Texto exacto no verificado por ejecución.
- `401` sin token.

### 3.5 `PUT /productos/{id}` — Actualizar producto

Verificado en `ProductoController.java:48-53`. Mismo body que la creación, con `@Valid`.

```http
PUT http://localhost:8083/productos/1
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "nombre": "Teclado Mecánico RGB",
  "precio": 49990.0,
  "categoriaId": 1
}
```

**Respuesta esperada:**
- `200 OK` con el producto actualizado.

**Errores:** `400` validación, `400` categoría inexistente, `401`, `404` si el producto no existe.

### 3.6 `DELETE /productos/{id}` — Eliminar producto

Verificado en `ProductoController.java:55-59`.

```http
DELETE http://localhost:8083/productos/1
Authorization: Bearer {{accessToken}}
```

**Respuesta esperada:**
- `204 No Content`, sin cuerpo.

**Errores:** `401`, `404` si no existe.

> **Nota de seguridad para la práctica:** `POST`, `PUT` y `DELETE` crean, modifican o eliminan registros. Úsalos en Postman solo contra un entorno de prueba y con datos desechables.

---

## 4. ms-categoria (`http://localhost:8081`)

Base verificada: `@RequestMapping("/categorias")` en `CategoriaController.java:17`. Todos los endpoints requieren JWT (misma política stateless que producto).

### 4.1 `GET /categorias` — Listar categorías

Verificado en `CategoriaController.java:25-29`.

```http
GET http://localhost:8081/categorias
Authorization: Bearer {{accessToken}}
```

Respuesta `200 OK`, ejemplo ilustrativo según `CategoriaResponseDTO.java`:
```json
[
  {
    "id": 1,
    "nombre": "Tecnología",
    "descripcion": "Dispositivos y accesorios"
  }
]
```

### 4.2 `GET /categorias/{id}` — Consultar por ID

Verificado en `CategoriaController.java:33-37`.

```http
GET http://localhost:8081/categorias/1
Authorization: Bearer {{accessToken}}
```

Respuesta `200 OK` con una categoría. `404 {"error":"..."}` si no existe (comentario en el controlador indica `CategoriaNotFoundException → 404`).

### 4.3 `GET /categorias/buscar?nombre=...`

Verificado en `CategoriaController.java:41-43`. Ejemplo:
```http
GET http://localhost:8081/categorias/buscar?nombre=tec
Authorization: Bearer {{accessToken}}
```

### 4.4 `POST /categorias` — Crear categoría

Verificado en `CategoriaController.java:46-51`. Entrada `CategoriaRequestDTO.java`:
- `nombre`: obligatorio, máximo 100.
- `descripcion`: opcional, máximo 255.

```http
POST http://localhost:8081/categorias
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "nombre": "Tecnología",
  "descripcion": "Dispositivos y accesorios"
}
```

Respuesta `201 Created` con la categoría creada.

### 4.5 `PUT /categorias/{id}` — Actualizar categoría

Verificado en `CategoriaController.java:53-59`. Mismo JSON que la creación.

```http
PUT http://localhost:8081/categorias/1
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "nombre": "Tecnología y Oficina",
  "descripcion": "Dispositivos, accesorios y papelería"
}
```

Respuesta `200 OK`.

### 4.6 `DELETE /categorias/{id}` — Eliminar categoría

Verificado en `CategoriaController.java:61-65`.

```http
DELETE http://localhost:8081/categorias/1
Authorization: Bearer {{accessToken}}
```

Respuesta `204 No Content`.

**Errores comunes de categoría:** `400` por validación (`nombre` vacío o demasiado largo), `401` sin token, `404` por ID inexistente.

---

## 5. ms-inventario (`http://localhost:8084`)

Base verificada: `@RequestMapping("/inventario")` en `InventarioController.java:15`. Todos los endpoints requieren JWT.

### 5.1 Diferencia expresa entre `README.md` e `InventarioRequestDTO.java`

> **Advertencia verificada:** `README.md:109-119` (Paso D) documenta:
> ```json
> {
>   "productoId": 1,
>   "stockActual": 50,
>   "stockMinimo": 5
> }
> ```
> Sin embargo, el código real vigente `ms-inventario/.../dto/InventarioRequestDTO.java:14-20` **solo define**:
> ```java
> private Long productoId;  // @NotNull
> private Integer stock;    // @NotNull
> ```
> Por tanto, **en esta guía se usa la estructura real del código** (`productoId` + `stock`). No envíes `stockActual` ni `stockMinimo` porque no existen en el DTO verificado y serían ignorados o causarían un uso incorrecto. Si el backend evoluciona, actualiza esta guía tras verificar de nuevo el DTO.

### 5.2 `GET /inventario` — Listar inventario

Verificado en `InventarioController.java:21-24`.

```http
GET http://localhost:8084/inventario
Authorization: Bearer {{accessToken}}
```

Respuesta `200 OK`, ejemplo ilustrativo según `InventarioResponseDTO.java` (`id`, `productoId`, `stock`):
```json
[
  {
    "id": 1,
    "productoId": 1,
    "stock": 50
  }
]
```

### 5.3 `GET /inventario/{id}` — Consultar por ID

Verificado en `InventarioController.java:29-32` con patrón `@GetMapping("/{id:\\d+}")`.

```http
GET http://localhost:8084/inventario/1
Authorization: Bearer {{accessToken}}
```

Notas:
- El `{id}` debe ser numérico. El comentario del código (`InventarioController.java:26-28`) explica que el constraint `\\d+` evita que `stock` se interprete como un ID.
- `404` si no existe; `401` sin token.

### 5.4 `POST /inventario` — Crear registro de inventario

Verificado en `InventarioController.java:34-38`. Usa la estructura real del DTO.

```http
POST http://localhost:8084/inventario
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "productoId": 1,
  "stock": 50
}
```

Respuesta `201 Created` con el `InventarioResponseDTO` creado. Errores: `400` si falta `productoId` o `stock`, `401` sin token.

### 5.5 `PUT /inventario/{id}` — Actualizar registro

Verificado en `InventarioController.java:40-45`, también con `{id:\d+}` y `@Valid`.

```http
PUT http://localhost:8084/inventario/1
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "productoId": 1,
  "stock": 45
}
```

Respuesta `200 OK`. Errores: `400`, `401`, `404`.

### 5.6 Ajustes de stock verificados (dos variantes)

Ambas están implementadas; elige según el caso:

**a) `PUT /inventario/stock?productoId=1&cantidad=10` (query params)**

Verificado en `InventarioController.java:49-54` con `params = {"productoId", "cantidad"}`. Comentario del código: cantidad negativa = salida/venta; es la versión por compatibilidad.

```http
PUT http://localhost:8084/inventario/stock?productoId=1&cantidad=10
Authorization: Bearer {{accessToken}}
```
Sin body. Respuesta `200 OK` con el inventario actualizado.

**b) `PUT /inventario/stock/ajuste` (body DTO, usado por `ms-movimiento`)**

Verificado en `InventarioController.java:58-63`. Entrada `InventarioStockDTO.java`: `productoId` y `cantidad` obligatorios.

```http
PUT http://localhost:8084/inventario/stock/ajuste
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "productoId": 1,
  "cantidad": -2
}
```

Respuesta `200 OK`.

### 5.7 `DELETE /inventario/{id}` — Eliminar registro

Verificado en `InventarioController.java:65-69` (`/{id:\d+}`).

```http
DELETE http://localhost:8084/inventario/1
Authorization: Bearer {{accessToken}}
```

Respuesta `204 No Content`.

---

## 6. ms-movimiento (`http://localhost:8085`)

Base verificada: `@RequestMapping("/movimientos")` en `MovimientoController.java:14`. Requiere JWT.

### 6.1 `GET /movimientos` — Listar movimientos

Verificado en `MovimientoController.java:27-30`.

```http
GET http://localhost:8085/movimientos
Authorization: Bearer {{accessToken}}
```

Respuesta `200 OK`, ejemplo ilustrativo según `MovimientoResponseDTO.java`:
```json
[
  {
    "id": 1,
    "productoId": 1,
    "cantidad": 2,
    "tipo": "SALIDA",
    "fecha": "2026-01-01T10:00:00"
  }
]
```

### 6.2 `GET /movimientos/{id}` — Consultar por ID

Verificado en `MovimientoController.java:32-35`.

```http
GET http://localhost:8085/movimientos/1
Authorization: Bearer {{accessToken}}
```

Respuesta `200 OK` o `404` si no existe; `401` sin token.

### 6.3 `POST /movimientos` — Registrar movimiento

Verificado en `MovimientoController.java:20-25`. Entrada `MovimientoRequestDTO.java:16-26`:
- `productoId`: obligatorio.
- `cantidad`: obligatoria y positiva (`@Positive`).
- `tipo`: debe cumplir `@Pattern("ENTRADA|SALIDA")`.

```http
POST http://localhost:8085/movimientos
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "productoId": 1,
  "cantidad": 2,
  "tipo": "SALIDA"
}
```

Respuesta `201 Created` con el movimiento registrado (incluye `id` y `fecha` generados por el servidor).

Errores: `400` por validación (cantidad no positiva, tipo distinto de `ENTRADA|SALIDA`, campos nulos), `401` sin token. Internamente descuenta/carga stock vía `ms-inventario`, por lo que requiere que `ms-producto` y `ms-inventario` estén disponibles.

### 6.4aclaración sobre `PUT` y `DELETE` en movimientos

> **No verificados / no existentes:** se revisó `MovimientoController.java` completo y **no contiene** `@PutMapping` ni `@DeleteMapping`. Por tanto, **no se documentan** ejemplos de `PUT` ni `DELETE` para `/movimientos`. No intentes rutas como `PUT /movimientos/{id}` porque no están implementadas en el código revisado.

---

## 7. Tabla rápida de errores

| Código | Cuándo ocurre (verificado) | Ejemplo de cuerpo |
|---|---|---|
| `200 OK` | `GET` y `PUT` exitosos | Recurso o lista solicitada |
| `201 Created` | `POST` exitoso | DTO de respuesta con `id` |
| `204 No Content` | `DELETE` y `POST /auth/logout` exitosos | Sin cuerpo |
| `400 Bad Request` | Validación `@Valid` fallida; query param requerido ausente; `categoriaId` / `productoId` inválido (`RuntimeException`) | `{"nombre":"El nombre no puede estar vacío"}` o `{"error":"..."}` |
| `401 Unauthorized` | Falta `Authorization: Bearer`, token inválido o expirado | `{"error":"No autenticado"}` |
| `404 Not Found` | ID inexistente (`*NotFoundException`) | `{"error":"..."}` (texto exacto no afirmado sin ejecución) |

---

## 8. Orden sugerido en Postman

1. `POST /auth/login` → guarda `{{accessToken}}`.
2. `GET /categorias` y `GET /categorias/1` (solo lectura).
3. `GET /productos`, `GET /productos/1`, `GET /productos/categoria/1`, `GET /productos/buscar?nombre=...` (solo lectura).
4. `GET /inventario`, `GET /inventario/1`, `GET /movimientos`, `GET /movimientos/1` (solo lectura).
5. Solo en entorno de prueba y si lo necesitas: `POST /categorias` → `POST /productos` → `POST /inventario` → `POST /movimientos`; luego `PUT` y finalmente `DELETE`.
6. Recuerda: `ms-producto` necesita a `ms-categoria` en línea; `ms-movimiento` necesita a `ms-producto` y `ms-inventario`.

---

## 9. Qué no está cubierto (para no inventar rutas)

- No se documentan `PUT /movimientos/{id}` ni `DELETE /movimientos/{id}` porque no existen en `MovimientoController.java`.
- No se documentan campos `stockActual` / `stockMinimo` porque no existen en `InventarioRequestDTO.java` vigente.
- No se afirman mensajes literales de `404` / errores de negocio internos porque no se ejecutó ninguna petición; solo se describe su forma (`{"error":"..."}`) según los `GlobalExceptionHandler`.
- La gestión de usuarios (`GET/POST/PUT/DELETE /usuarios`) existe en `UsuarioController.java`, pero queda fuera del foco pedido (productos, categorías e inventario); se menciona aquí únicamente para trazabilidad.
