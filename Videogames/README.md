# 🎮 Gestión de Videojuegos — Proyecto Acceso a Datos

Aplicación Java para gestión de videojuegos que combina persistencia relacional con Hibernate/JPA y persistencia documental con MongoDB.

---

## 📌 Tema y Reglas del Negocio

Sistema para gestionar una colección de videojuegos donde:
- Los **videojuegos** tienen título, género, desarrollador, precio y fecha de lanzamiento.
- Los **usuarios** pueden escribir **reseñas** (puntuación 1-10 + comentario) sobre videojuegos.
- Todas las operaciones quedan **auditadas en MongoDB** (quién hizo qué y cuándo).
- Las reseñas se guardan **tanto en SQL como en MongoDB** (snapshot JSON para historial).

---

## 🗂 Modelo de Datos

### Modelo Relacional (SQL / Hibernate)

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   VIDEOJUEGOS   │       │     RESENAS     │       │    USUARIOS     │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │◄──┐   │ id (PK)         │   ┌──►│ id (PK)         │
│ titulo          │   └───│ videojuego_id   │   │   │ username        │
│ genero          │       │ usuario_id      │───┘   │ email           │
│ desarrollador   │       │ puntuacion      │       │ nombre          │
│ fecha_lanzam.   │       │ comentario      │       │ fecha_registro  │
│ precio          │       │ fecha_resena    │       │ rol             │
│ descripcion     │       └─────────────────┘       └─────────────────┘
└─────────────────┘
```

**Relaciones:**
- `Videojuego` → `Resena`: `@OneToMany` / `@ManyToOne`
- `Usuario` → `Resena`: `@OneToMany` / `@ManyToOne`

### Modelo Documental (MongoDB)

#### Colección `audit_logs` — Auditoría de acciones
```json
{
  "timestamp": "2024-03-15T10:30:00",
  "type": "CREATE",
  "user": "admin",
  "entityType": "Videojuego",
  "entityId": 5,
  "payload": {
    "titulo": "Elden Ring",
    "genero": "RPG",
    "desarrollador": "FromSoftware",
    "precio": 59.99
  }
}
```

#### Colección `resenas_historial` — Snapshot de reseñas
```json
{
  "resenaId": 12,
  "videojuegoId": 1,
  "tituloJuego": "The Witcher 3",
  "usuarioId": 2,
  "username": "jugador01",
  "puntuacion": 10,
  "comentario": "Obra maestra absoluta",
  "fechaCreacion": "2024-03-15T12:00:00"
}
```

---

## 🔗 Integración SQL ↔ MongoDB

| Flujo | SQL (Hibernate) | MongoDB |
|-------|----------------|---------|
| Crear/editar/borrar videojuego | Persist en `videojuegos` | Evento en `audit_logs` |
| Crear reseña | Persist en `resenas` | Snapshot en `resenas_historial` + evento en `audit_logs` |
| Importar JSON | Deserializa con Jackson → Persist en SQL | Evento en `audit_logs` |

**¿Por qué esta división?**
- **SQL** → Datos estructurados con relaciones fuertes (videojuegos, usuarios, reseñas).
- **MongoDB** → Datos dinámicos sin esquema fijo: logs de auditoría (payload variable según entidad) e historial de reseñas enriquecido con datos desnormalizados para consulta rápida sin JOINs.

---

## ⚙️ Requisitos Técnicos

| Software | Versión mínima | Para qué |
|----------|---------------|---------|
| Java JDK | 17+ | Compilar y ejecutar |
| Maven | 3.8+ | Gestión de dependencias |
| MongoDB | 6.0+ | Base de datos documental |
| H2 (embebido) | — | BD relacional (sin instalar nada extra) |

> **Nota:** La BD relacional usa **H2 embebido** por defecto, por lo que NO necesitas instalar MySQL. Los datos se guardan en `./data/videogames.mv.db`. Si prefieres MySQL, descomenta la configuración en `persistence.xml`.

---

## 🚀 Cómo Ejecutar

### 1. Instalar MongoDB

**Windows:**
1. Descarga desde https://www.mongodb.com/try/download/community
2. Instala con todas las opciones por defecto
3. MongoDB se inicia automáticamente como servicio

**macOS:**
```bash
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install gnupg curl
curl -fsSL https://www.mongodb.org/static/pgp/server-7.0.asc | sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg --dearmor
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
sudo apt-get update && sudo apt-get install -y mongodb-org
sudo systemctl start mongod
```

### 2. Instalar Java y Maven

**Windows/macOS:** Descarga JDK 17+ desde https://adoptium.net/

**Linux:**
```bash
sudo apt install openjdk-17-jdk maven
```

### 3. Compilar el proyecto

```bash
cd videogames-app
mvn clean package -q
```

Genera `target/videogames-app-1.0-SNAPSHOT-jar-with-dependencies.jar`

### 4. Ejecutar

```bash
java -jar target/videogames-app-1.0-SNAPSHOT-jar-with-dependencies.jar
```

O usar el script:
```bash
# Linux/macOS
./run.sh

# Windows
run.bat
```

---

## 🧭 Guía de Demo

1. **Arrancar** → Se cargan datos de demo automáticamente (4 juegos, 2 usuarios, 4 reseñas)
2. **Menú 1** → Ver juegos, crear uno nuevo, modificar precio → observar que en Menú 4 aparece el log
3. **Menú 3** → Crear una reseña → aparece en SQL Y en MongoDB (historial)
4. **Menú 4** → Ver auditoría: filtrar por tipo CREATE, entidad Videojuego
5. **Menú 5** → Consulta avanzada: juegos por género/precio paginados; mejores valorados
6. **Menú 6** → Exportar a JSON con Jackson; importar desde JSON

---

## 📋 Lista de Verificación

- [x] 3 entidades JPA: `Videojuego`, `Usuario`, `Resena`
- [x] 2 relaciones: `@OneToMany` + `@ManyToOne` (doble)
- [x] CRUD completo en `Videojuego` y `Usuario`
- [x] 2 consultas SQL avanzadas: filtro+paginación y GROUP BY+HAVING
- [x] MongoDB guarda con sentido: auditoría y snapshots de reseñas
- [x] 2 filtros MongoDB (por tipo/entidad; por usuario/fecha)
- [x] 1 agregación MongoDB (count por tipo; media por juego)
- [x] Integración real SQL→Mongo en varios flujos
- [x] Serialización/deserialización JSON con Jackson
- [x] Arquitectura por capas (domain/repository/service/mongo/ui)
- [x] Validaciones y manejo de errores
- [x] README completo

---

## 🏗 Arquitectura del Proyecto

```
src/main/java/com/videogames/
├── domain/          → Entidades JPA + DTOs (sin lógica de negocio)
│   ├── Videojuego.java
│   ├── Usuario.java
│   ├── Resena.java
│   └── VideojuegoDTO.java
├── repository/      → DAOs JPA (solo acceso a BD)
│   ├── JpaUtil.java
│   ├── VideojuegoRepository.java
│   ├── UsuarioRepository.java
│   └── ResenaRepository.java
├── service/         → Casos de uso (lógica, validaciones, coordinación)
│   ├── VideojuegoService.java
│   ├── UsuarioService.java
│   ├── ResenaService.java
│   ├── AuditService.java
│   └── JsonUtil.java
├── mongo/           → Cliente MongoDB + repositorios Mongo
│   ├── MongoConfig.java
│   ├── AuditLogRepository.java
│   └── ResenaMongoRepository.java
└── ui/              → Consola (solo llama a services)
    └── MainMenu.java
```

**Regla de oro:** La UI nunca toca directamente Hibernate ni MongoDB, siempre pasa por la capa `service`.
