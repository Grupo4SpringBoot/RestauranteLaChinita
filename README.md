# Sistema de Reservas y Pedidos de Restaurante

## 👥 Integrantes

- Edgar
- Cristian
- David

---

## 📝 Descripción del Proyecto

Este proyecto consiste en el diseño y desarrollo de una aplicación web monolítica basada en una base de datos relacional para gestionar clientes, empleados, reservas y pedidos de un restaurante. 

Está enfocado como un proyecto académico e integrado dentro de una formación de especialización de 100 horas, sirviendo como pieza central de nuestro portfolio profesional. El objetivo es demostrar un desarrollo limpio, estructurado por capas y con un flujo de despliegue automatizado moderno.

### 🎯 Objetivos y Alcance (MVP)

- **Gestión de Clientes:** Registro, almacenamiento seguro de credenciales e inicio de sesión.
- **Control de Reservas:** Registro de mesas, fechas, horas y comensales asociados a un empleado responsable.
- **Pedidos y Menú:** Gestión de comandas (pedidos) y desglose de platos con cantidades y precios históricos.
- **Alcance Realista:** Se prioriza una arquitectura monolítica robusta.

---

## 🛠️ Stack Tecnológico

La aplicación integra de forma progresiva las siguientes tecnologías a lo largo de su ciclo de vida:

- **Lenguaje y Gestión:** Java & Maven
- **Framework Principal:** Spring Boot (Spring MVC, Spring Data JPA)
- **Base de Datos:** Supabase (PostgreSQL)
- **Interfaz de Usuario:** Thymeleaf (HTML5 / CSS3)
- **Automatización CI/CD:** Jenkins
- **Contenedores e Infraestructura:** Docker & Kubernetes

---

## 📊 Modelo de Datos (E-R)

### Entidades y Atributos Principal

#### 1. CLIENTE

| Campo           | Tipo                 | Descripción                                       |
| --------------- | -------------------- | ------------------------------------------------- |
| `id`            | UUID, PK             | Identificador único del cliente.                  |
| `nombre`        | VARCHAR(50)          | Nombre obligatorio.                               |
| `apellido`      | VARCHAR(50)          | Apellido obligatorio.                             |
| `telefono`      | VARCHAR(20)          | Teléfono de contacto.                             |
| `email`         | VARCHAR(100), UNIQUE | Correo para identificación e inicio de sesión.    |
| `password_hash` | VARCHAR(255)         | Hash seguro (Bcrypt/Argon2id); nunca texto plano. |

#### 2. EMPLEADO

| Campo      | Tipo        | Descripción                       |
| ---------- | ----------- | --------------------------------- |
| `id`       | UUID, PK    | Identificador único del empleado. |
| `nombre`   | VARCHAR(50) | Nombre obligatorio.               |
| `apellido` | VARCHAR(50) | Apellido obligatorio.             |
| `puesto`   | VARCHAR(50) | Puesto desempeñado en el local.   |

#### 3. RESERVA

| Campo               | Tipo                | Descripción                         |
| ------------------- | ------------------- | ----------------------------------- |
| `id`                | UUID, PK            | Identificador único de la reserva.  |
| `numero_reserva`    | VARCHAR(20), UNIQUE | Código alfanumérico de reserva.     |
| `numero_mesa`       | INT                 | Mesa física asignada.               |
| `cantidad_personas` | INT                 | Número de comensales (Mayor a 0).   |
| `fecha_hora`        | TIMESTAMP           | Fecha y hora programada.            |
| `cliente_id`        | UUID, FK            | Enlace con la entidad CLIENTE.      |
| `empleado_id`       | UUID, FK            | Enlace con el EMPLEADO responsable. |

#### 4. PEDIDO

| Campo        | Tipo        | Descripción                                        |
| ------------ | ----------- | -------------------------------------------------- |
| `id`         | UUID, PK    | Identificador único del pedido.                    |
| `reserva_id` | UUID, FK    | Enlace con la RESERVA de origen.                   |
| `fecha_hora` | TIMESTAMP   | Fecha y hora de creación de la comanda.            |
| `estado`     | VARCHAR(30) | Estados válidos: `PENDIENTE`, `SERVIDO`, `PAGADO`. |

#### 5. MENU (Platos)

| Campo         | Tipo          | Descripción                                      |
| ------------- | ------------- | ------------------------------------------------ |
| `id`          | UUID, PK      | Identificador único del plato.                   |
| `nombre`      | VARCHAR(100)  | Nombre del plato o bebida.                       |
| `precio`      | DECIMAL(10,2) | Precio actual en carta.                          |
| `tipo_comida` | VARCHAR(50)   | Categoría (Entrante, Principal, Postre, Bebida). |

#### 6. DETALLE_PEDIDO

| Campo             | Tipo          | Descripción                                         |
| ----------------- | ------------- | --------------------------------------------------- |
| `id`              | UUID, PK      | Identificador de la línea de detalle.               |
| `pedido_id`       | UUID, FK      | Enlace con el PEDIDO.                               |
| `menu_id`         | UUID, FK      | Enlace con el producto del MENU.                    |
| `cantidad`        | INT           | Unidades solicitadas (Mayor a 0).                   |
| `precio_unitario` | DECIMAL(10,2) | Precio histórico guardado en el momento del pedido. |

### Relaciones del Sistema

- **CLIENTE (1) — (N) RESERVA:** Un cliente gestiona N reservas.
- **EMPLEADO (1) — (N) RESERVA:** Un empleado supervisa N reservas.
- **RESERVA (1) — (N) PEDIDO:** Una mesa reservada puede abrir múltiples comandas/pedidos a lo largo de su estancia.
- **PEDIDO (1) — (N) DETALLE_PEDIDO — (N) MENU:** Relación de muchos a muchos resuelta mediante una tabla intermedia con atributos compuestos (`cantidad` y `precio_unitario` histórico).

---

## 💻 Evidencias e Integración del Stack (Paso a Paso)

A continuación se detalla cómo se ha estructurado e integrado el contenido formativo del curso en este repositorio:

### 1. Gestión de Dependencias (Maven)

- Implementación de un archivo `pom.xml` limpio y estructurado.
- Modularización de dependencias de Spring Boot, PostgreSQL Driver y Lombok.

### 2. Persistencia y Datos (JPA / Hibernate & Supabase)

- Mapeo objeto-relacional completo utilizando anotaciones JPA (`@Entity`, `@Table`, `@ManyToOne`, `@OneToMany`).
- Conexión nativa a la base de datos **PostgreSQL** alojada en la nube con **Supabase**.

### 3. Interfaz Web (Thymeleaf)

Vistas web interactivas renderizadas en el servidor para uso del personal y los clientes:

- **`/clientes`**: Panel de alta y listado de clientes.
- **`/reservas`**: Formulario dinámico para la asignación de mesas y control de comensales.
- **`/pedidos/nuevo`**: Interfaz de comandas para los empleados en tiempo real.

### 4. API REST Propia

Exposición de endpoints en formato JSON creados con controladores Spring (`@RestController`) para demostrar competencias de backend:

| Método   | Endpoint                      | Descripción                                                  |
| -------- | ----------------------------- | ------------------------------------------------------------ |
| **GET**  | `/api/v1/menu`                | Obtiene la lista completa de platos activos.                 |
| **POST** | `/api/v1/pedidos`             | Crea un nuevo pedido con múltiples líneas de detalle.        |
| **GET**  | `/api/v1/reservas/{id}`       | Recupera la información detallada de una reserva específica. |
| **PUT**  | `/api/v1/pedidos/{id}/estado` | Cambia el estado del pedido (`PENDIENTE` -> `SERVIDO`).      |

---

## 🚀 Instalación y Despliegue Local

### Requisitos Previos

- Java 17 o superior.
- Maven 3.8+.
- Cuenta o credenciales de Supabase (PostgreSQL).
- Docker Desktop (opcional para contenedores).

### Pasos para Ejecutar la Aplicación

#### Opción 1: Ejecución Tradicional (Spring Boot)

1. Clona el repositorio:
   
   ```bash
   git clone https://github.com
   cd restaurante-reservas
   ```

2. Configura tus variables de entorno locales (o un archivo `application.properties` excluido del control de versiones) para conectar con Supabase:
   
   ```env
   DB_URL=jdbc:postgresql://db.supabase.co:5432/postgres
   DB_USER=
   DB_PASSWORD=${PASSWORD}
   ```

3. Compila y ejecuta el proyecto con Maven:
   
   ```bash
   mvn clean spring-boot:run
   ```

#### Opción 2: Despliegue Automático (CI/CD con Jenkins)

El repositorio incorpora un archivo `Jenkinsfile` que automatiza el ciclo de vida del código:

- **Fase de Compilación:** `mvn clean compile`
- **Fase de Test:** Ejecución de pruebas unitarias con JUnit.
- **Fase de Empaquetado:** Generación del archivo `.jar` ejecutable.

#### Opción 3: Contenedores (Docker)

Para construir y correr la aplicación de manera aislada utilizando el `Dockerfile` incluido:

1. Construye la imagen de la aplicación:
   
   ```bash
   docker build -t restaurante-app:latest .
   ```

2. Ejecuta el contenedor inyectando las variables de entorno de la base de datos:
   
   ```bash
   docker run -d -p 8080:8080 --name restaurante-container \
     -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-supabase-url:5432/postgres \
     -e SPRING_DATASOURCE_USERNAME=${USER}
     -e SPRING_DATASOURCE_PASSWORD=${PASSWORD}
     restaurante-app:latest
   ```

#### Opción 4: Orquestación (Kubernetes)

Despliegue local de aprendizaje simulado mediante manifiestos de Kubernetes incluidos en la carpeta `/k8s`:

```bash
kubectl apply -f k8s/restaurante-deployment.yaml
kubectl apply -f k8s/restaurante-service.yaml
```

---

## 🧠 Competencias Demostradas en este Proyecto

Este proyecto sirve como evidencia técnica ante un entrevistador o evaluador para comprobar las siguientes habilidades:

- **Diseño de Base de Datos Eficiente:** Resolución correcta de relaciones muchos a muchos resguardando datos históricos (precios de venta).
- **Seguridad en Backend:** Gestión de autenticación mediante el uso correcto de hashes de contraseñas de un solo sentido.
- **Arquitectura de Software:** Código limpio estructurado en capas (Controladores, Servicios, Repositorios, Entidades).
- **Conocimientos de DevOps:** Automatización de compilaciones con Jenkins, creación de entornos aislados con Docker y principios de orquestación con Kubernetes.
