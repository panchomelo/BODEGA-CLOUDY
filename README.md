# BODEGA-CLOUDY
#  Sistema de Gestión de Bodega

Sistema de gestión de bodega construido con **Spring Boot 3**, **Java 21**, **MySQL 8** y **Docker Compose**.

## Microservicios y Puertos

| Servicio | Puerto | Base de Datos | Descripción |
| :--- | :--- | :--- | :--- |
| **`ms-usuario`** | `8082` | `identidad_db` | Autenticación (Login, Registro, JWT). |
| **`ms-categoria`** | `8081` | `catalogo_db` | Gestión de categorías. |
| **`ms-producto`** | `8083` | `catalogo_db` | Gestión de productos (valida categoría). |
| **`ms-inventario`** | `8084` | `operacion_db` | Control de stock (valida producto). |
| **`ms-movimiento`** | `8085` | `operacion_db` | Entradas y salidas de stock. |

---

##  Cómo Ejecutar

1. **Crear el archivo `.env`:**
   ```bash
   cp .env.example .env
   ```

2. **Levantar todo con Docker Compose:**
   ```bash
   docker compose up --build -d
   ```

3. **Verificar que los contenedores estén corriendo:**
   ```bash
   docker compose ps
   ```

---

## Pruebas Rápidas (Postman)

### 1. Iniciar Sesión (Obtener Token)
* **POST** `http://localhost:8082/auth/login`
* **Body:**
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
>  **Nota:** Usa el `accessToken` recibido en el header `Authorization: Bearer <TOKEN>` para las demás peticiones.

### 2. Crear Categoría
* **POST** `http://localhost:8081/categorias`
* **Body:** `{"nombre": "Abarrotes", "descripcion": "Productos no perecibles"}`

### 3. Crear Producto
* **POST** `http://localhost:8083/productos`
* **Body:** `{"nombre": "Arroz 1kg", "precio": 1500.0, "categoriaId": 1}`

### 4. Registrar Stock Inicial
* **POST** `http://localhost:8084/inventario`
* **Body:** `{"productoId": 1, "stockActual": 100, "stockMinimo": 10}`

### 5. Registrar Movimiento (Salida de Stock)
* **POST** `http://localhost:8085/movimientos`
* **Body:** `{"productoId": 1, "cantidad": 5, "tipo": "SALIDA"}`

---

## Detener el Sistema

```bash
# Detener contenedores
docker compose down

# Detener y borrar datos de MySQL
docker compose down -v
```