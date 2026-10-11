# BODEGA-CLOUDY

Sistema integral de gestión de bodega y control de inventario basado en una arquitectura de microservicios con Spring Boot 3, Java 21, Spring Security (JWT), bases de datos MySQL aisladas y orquestación con Docker Compose.

---

## 1. Requisitos del Sistema
Para ejecutar y desarrollar el proyecto de manera local, asegúrate de contar con:
* **JDK 21** instalado y configurado en el sistema.
* **Maven 3.9+** para la gestión de dependencias y empaquetado.
* **Docker** y **Docker Compose** para la contenerización de las bases de datos y microservicios.
* **Puertos libres** en tu máquina: `3306` (MySQL) y del `8081` al `8085` (Microservicios).

---

## 2. Clonar el Repositorio
Abre tu terminal y ejecuta los siguientes comandos para clonar el proyecto y acceder a la carpeta raíz:

```bash
git clone https://github.com/panchomelo/BODEGA-CLOUDY.git
cd BODEGA-CLOUDY
```

---

## 3. Configuración de Variables de Entorno
El proyecto utiliza un archivo de plantilla para las credenciales y el secreto de seguridad JWT.

1. Copia el archivo de ejemplo para crear tu entorno local:
   ```bash
   cp .env.example .env
   ```
2. Abre el archivo `.env` recién creado y configura las credenciales para los usuarios de base de datos y la clave secreta `JWT_SECRET`.

---

## 4. Levantar el Sistema con Docker Compose
Puedes arrancar todo el ecosistema (MySQL + 5 microservicios) con un solo comando:

* **Primera vez (construyendo imágenes):**
  ```bash
  docker compose up --build
  ```
* **Ejecución normal en segundo plano:**
  ```bash
  docker compose up -d
  ```
* **Detener los servicios (conservando los datos de las bases de datos):**
  ```bash
  docker compose down
  ```
  >  *Nota:* **Nunca** uses el flag `-v` a menos que quieras borrar por completo los volúmenes y resetear las bases de datos desde cero.

---

## 5. Tabla de Microservicios, Puertos y Bases de Datos

| Microservicio | Puerto | Base de Datos | Descripción Principal |
| :--- | :--- | :--- | :--- |
| **`ms-usuario`** | `8082` | `identidad_db` | Autenticación, emisión y validación de tokens JWT. |
| **`ms-categoria`** | `8081` | `catalogo_db` | Administración y consulta de categorías de productos. |
| **`ms-producto`** | `8083` | `catalogo_db` | Gestión de productos (valida categorías vía WebClient). |
| **`ms-inventario`** | `8084` | `operacion_db` | Control de stock y existencias iniciales. |
| **`ms-movimiento`** | `8085` | `operacion_db` | Registro de entradas y salidas de inventario. |

* **Seeds automáticos:** Al arrancar por primera vez, el sistema carga automáticamente usuarios iniciales en `ms-usuario` mediante componentes de inicialización con contraseñas cifradas en BCrypt.

---

## 6. Pruebas Rápidas y Flujo JWT (Postman / cURL)

La seguridad es estricta (Stateless JWT). La mayoría de los endpoints protegidos requieren enviar el token de acceso en la cabecera HTTP:
`Authorization: Bearer <accessToken>`

### Paso A: Iniciar Sesión (Obtener Token)
* **POST** `http://localhost:8082/auth/login`
* **Body (JSON):**
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
* *Copia el `accessToken` devuelto en la respuesta.*

### Paso B: Crear Categoría (`ms-categoria`)
* **POST** `http://localhost:8081/categorias`
* **Header:** `Authorization: Bearer <tu_token>`
* **Body:**
  ```json
  {
    "nombre": "Tecnología",
    "descripcion": "Dispositivos y accesorios"
  }
  ```

### Paso C: Crear Producto (`ms-producto`)
* **POST** `http://localhost:8083/productos`
* **Header:** `Authorization: Bearer <tu_token>`
* **Body:**
  ```json
  {
    "nombre": "Teclado Mecánico",
    "precio": 45990.0,
    "categoriaId": 1
  }
  ```

### Paso D: Registrar Inventario (`ms-inventario`)
* **POST** `http://localhost:8084/inventario`
* **Header:** `Authorization: Bearer <tu_token>`
* **Body:**
  ```json
  {
    "productoId": 1,
    "stockActual": 50,
    "stockMinimo": 5
  }
  ```

### Paso E: Registrar Movimiento de Salida (`ms-movimiento`)
* **POST** `http://localhost:8085/movimientos`
* **Header:** `Authorization: Bearer <tu_token>`
* **Body:**
  ```json
  {
    "productoId": 1,
    "cantidad": 2,
    "tipo": "SALIDA"
  }
  ```

---

## 7. Desarrollo Local (Ejecución individual en IntelliJ / Maven)
Si prefieres levantar un microservicio de forma nativa en tu IDE sin Docker (dejando MySQL corriendo en Docker o en local):

1. Configura las variables de entorno de ejecución en tu IDE (ej. en IntelliJ IDEA -> *Run Configurations* -> *Environment variables*):
   ```text
   DB_HOST=localhost;DB_USER=catalogo_user;DB_PASSWORD=catalogo123;JWT_SECRET=tu_secreto_compartido
   ```
2. Ejecuta la clase principal del microservicio respectivo (por ejemplo, `MsCategoriaApplication`).

---

## 8. Solución de Problemas Comunes (Troubleshooting)
* **Error `401 Unauthorized`:** Asegúrate de estar enviando correctamente la cabecera `Authorization: Bearer <token>` y de que el token no haya expirado.
* **Error de conexión a MySQL al arrancar:** Los microservicios dependen de un healthcheck en Docker (`compose.yml`) para asegurar que MySQL esté completamente listo antes de iniciar. Si algún servicio falla al arrancar por base de datos no disponible, verifica el estado con `docker compose ps`.
* **Reseteo total de datos:** Si modificaste scripts de bases de datos y necesitas limpiar tablas e historiales, ejecuta:
  ```bash
  docker compose down -v
  docker compose up --build
  ```