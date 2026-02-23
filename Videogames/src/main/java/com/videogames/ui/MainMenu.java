package com.videogames.ui;

import com.videogames.domain.Usuario;
import com.videogames.domain.Videojuego;
import com.videogames.mongo.MongoConfig;
import com.videogames.repository.JpaUtil;
import com.videogames.service.*;
import org.bson.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class MainMenu {

    private static final Scanner sc = new Scanner(System.in);
    private static String usuarioActual = "sistema";

    private static VideojuegoService videojuegoService;
    private static UsuarioService usuarioService;
    private static ResenaService resenaService;
    private static AuditService auditService;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     🎮 GESTIÓN DE VIDEOJUEGOS  🎮       ║");
        System.out.println("╚════════════════════════════════════════╝");

        inicializarServicios();
        cargarDatosDemoSiNecesario();

        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            int opcion = leerEntero("Elige una opción: ");
            switch (opcion) {
                case 1 -> menuVideojuegos();
                case 2 -> menuUsuarios();
                case 3 -> menuResenas();
                case 4 -> menuAuditoria();
                case 5 -> menuConsultasAvanzadas();
                case 6 -> menuJsonImportExport();
                case 0 -> salir = true;
                default -> System.out.println("⚠ Opción no válida.");
            }
        }

        System.out.println("\n👋 Cerrando conexiones...");
        JpaUtil.close();
        MongoConfig.close();
        System.out.println("¡Hasta luego!");
    }

    // ====================================================================
    // INICIALIZACIÓN
    // ====================================================================

    private static void inicializarServicios() {
        System.out.println("\n⏳ Conectando con bases de datos...");
        try {
            videojuegoService = new VideojuegoService(usuarioActual);
            usuarioService = new UsuarioService(usuarioActual);
            resenaService = new ResenaService(usuarioActual);
            auditService = new AuditService();
            System.out.println("✅ Conexión establecida con SQL (H2)");
            System.out.println("✅ Conexión establecida con MongoDB");
        } catch (Exception e) {
            System.out.println("⚠ No se pudo conectar a MongoDB: " + e.getMessage());
            System.out.println("   Continuando solo con SQL...");
        }
    }

    private static void cargarDatosDemoSiNecesario() {
        if (videojuegoService.listarTodos().isEmpty()) {
            System.out.println("\n📦 Cargando datos de demo...");
            try {
                // Usuarios
                Usuario admin = usuarioService.crear(new Usuario("admin", "admin@games.com", "Administrador", Usuario.Rol.ADMIN));
                Usuario user1 = usuarioService.crear(new Usuario("jugador01", "jugador@games.com", "Carlos García", Usuario.Rol.USUARIO));
                usuarioActual = "admin";
                videojuegoService.setUsuarioActual("admin");
                usuarioService.setUsuarioActual("admin");
                resenaService.setUsuarioActual("admin");

                // Videojuegos
                Videojuego v1 = videojuegoService.crear(new Videojuego(
                    "The Witcher 3", "RPG", "CD Projekt Red",
                    LocalDate.of(2015, 5, 19), 29.99, "RPG de mundo abierto con historia épica"));
                Videojuego v2 = videojuegoService.crear(new Videojuego(
                    "Elden Ring", "RPG", "FromSoftware",
                    LocalDate.of(2022, 2, 25), 59.99, "Action RPG de mundo abierto desafiante"));
                Videojuego v3 = videojuegoService.crear(new Videojuego(
                    "Minecraft", "Sandbox", "Mojang",
                    LocalDate.of(2011, 11, 18), 26.95, "Juego de construcción y supervivencia"));
                Videojuego v4 = videojuegoService.crear(new Videojuego(
                    "FIFA 24", "Deportes", "EA Sports",
                    LocalDate.of(2023, 9, 29), 49.99, "Simulador de fútbol"));

                // Reseñas (integración SQL + MongoDB)
                resenaService.crear(v1.getId(), admin.getId(), 10, "Obra maestra absoluta, el mejor RPG");
                resenaService.crear(v1.getId(), user1.getId(), 9, "Increíble historia y mundo abierto");
                resenaService.crear(v2.getId(), admin.getId(), 8, "Difícil pero muy gratificante");
                resenaService.crear(v3.getId(), user1.getId(), 7, "Clásico eterno, perfecto para creativos");

                System.out.println("✅ Datos de demo cargados correctamente");
            } catch (Exception e) {
                System.out.println("⚠ Error cargando datos demo: " + e.getMessage());
            }
        }
    }

    // ====================================================================
    // MENÚ PRINCIPAL
    // ====================================================================

    private static void mostrarMenuPrincipal() {
        System.out.println("\n╔═══════════════ MENÚ PRINCIPAL ═══════════════╗");
        System.out.printf("║  Usuario actual: %-28s║%n", usuarioActual);
        System.out.println("╠═══════════════════════════════════════════════╣");
        System.out.println("║  1. 🎮 Gestión de Videojuegos                 ║");
        System.out.println("║  2. 👤 Gestión de Usuarios                    ║");
        System.out.println("║  3. ⭐ Gestión de Reseñas                     ║");
        System.out.println("║  4. 📋 Auditoría (MongoDB)                    ║");
        System.out.println("║  5. 🔍 Consultas Avanzadas SQL                ║");
        System.out.println("║  6. 📦 Importar/Exportar JSON                 ║");
        System.out.println("║  0. 🚪 Salir                                  ║");
        System.out.println("╚═══════════════════════════════════════════════╝");
    }

    // ====================================================================
    // MENÚ VIDEOJUEGOS
    // ====================================================================

    private static void menuVideojuegos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── VIDEOJUEGOS ──────────────────────────────");
            System.out.println("  1. Listar todos");
            System.out.println("  2. Buscar por ID");
            System.out.println("  3. Buscar por título");
            System.out.println("  4. Crear nuevo");
            System.out.println("  5. Modificar");
            System.out.println("  6. Eliminar");
            System.out.println("  0. Volver");

            int op = leerEntero("Opción: ");
            switch (op) {
                case 1 -> listarVideojuegos();
                case 2 -> buscarVideojuegoPorId();
                case 3 -> buscarVideojuegoPorTitulo();
                case 4 -> crearVideojuego();
                case 5 -> modificarVideojuego();
                case 6 -> eliminarVideojuego();
                case 0 -> volver = true;
                default -> System.out.println("⚠ Opción no válida.");
            }
        }
    }

    private static void listarVideojuegos() {
        List<Videojuego> lista = videojuegoService.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay videojuegos registrados.");
            return;
        }
        System.out.println("\n📋 VIDEOJUEGOS (" + lista.size() + "):");
        System.out.printf("%-5s %-30s %-15s %-20s %8s%n", "ID", "Título", "Género", "Desarrollador", "Precio");
        System.out.println("-".repeat(85));
        for (Videojuego v : lista) {
            System.out.printf("%-5d %-30s %-15s %-20s %7.2f€%n",
                v.getId(), v.getTitulo(), v.getGenero(), v.getDesarrollador(), v.getPrecio());
        }
    }

    private static void buscarVideojuegoPorId() {
        Long id = leerLong("ID del videojuego: ");
        videojuegoService.buscarPorId(id).ifPresentOrElse(
            v -> {
                System.out.println("\n✅ Encontrado:");
                System.out.println("  ID:           " + v.getId());
                System.out.println("  Título:       " + v.getTitulo());
                System.out.println("  Género:       " + v.getGenero());
                System.out.println("  Desarrollador:" + v.getDesarrollador());
                System.out.println("  Precio:       " + v.getPrecio() + "€");
                System.out.println("  Lanzamiento:  " + v.getFechaLanzamiento());
                System.out.println("  Descripción:  " + v.getDescripcion());
            },
            () -> System.out.println("❌ No se encontró videojuego con ID: " + id)
        );
    }

    private static void buscarVideojuegoPorTitulo() {
        String titulo = leerTexto("Título a buscar: ");
        List<Videojuego> resultados = videojuegoService.buscarPorTitulo(titulo);
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron resultados para: " + titulo);
        } else {
            System.out.println("\n🔍 Resultados (" + resultados.size() + "):");
            resultados.forEach(System.out::println);
        }
    }

    private static void crearVideojuego() {
        System.out.println("\n── NUEVO VIDEOJUEGO ──");
        try {
            String titulo = leerTexto("Título: ");
            String genero = leerTexto("Género (RPG/Accion/Deportes/Sandbox/...): ");
            String desarrollador = leerTexto("Desarrollador: ");
            String fechaStr = leerTexto("Fecha lanzamiento (YYYY-MM-DD): ");
            LocalDate fecha = LocalDate.parse(fechaStr);
            double precio = leerDouble("Precio (€): ");
            String descripcion = leerTexto("Descripción: ");

            Videojuego v = new Videojuego(titulo, genero, desarrollador, fecha, precio, descripcion);
            Videojuego creado = videojuegoService.crear(v);
            System.out.println("✅ Videojuego creado con ID: " + creado.getId());
            System.out.println("   (Evento registrado en MongoDB audit_logs)");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void modificarVideojuego() {
        Long id = leerLong("ID del videojuego a modificar: ");
        Optional<Videojuego> opt = videojuegoService.buscarPorId(id);
        if (opt.isEmpty()) {
            System.out.println("❌ No encontrado.");
            return;
        }
        Videojuego v = opt.get();
        System.out.println("Videojuego actual: " + v);
        System.out.println("(Deja en blanco para mantener el valor actual)");

        try {
            String titulo = leerTextoOpcional("Nuevo título [" + v.getTitulo() + "]: ");
            if (!titulo.isBlank()) v.setTitulo(titulo);

            String genero = leerTextoOpcional("Nuevo género [" + v.getGenero() + "]: ");
            if (!genero.isBlank()) v.setGenero(genero);

            String precioStr = leerTextoOpcional("Nuevo precio [" + v.getPrecio() + "]: ");
            if (!precioStr.isBlank()) v.setPrecio(Double.parseDouble(precioStr));

            Videojuego actualizado = videojuegoService.actualizar(v);
            System.out.println("✅ Videojuego actualizado: " + actualizado);
            System.out.println("   (Cambio registrado en MongoDB audit_logs)");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void eliminarVideojuego() {
        Long id = leerLong("ID del videojuego a eliminar: ");
        System.out.print("¿Confirmar eliminación? (s/n): ");
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("s")) {
            System.out.println("Cancelado.");
            return;
        }
        if (videojuegoService.eliminar(id)) {
            System.out.println("✅ Videojuego eliminado.");
        } else {
            System.out.println("❌ No se encontró videojuego con ID: " + id);
        }
    }

    // ====================================================================
    // MENÚ USUARIOS
    // ====================================================================

    private static void menuUsuarios() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── USUARIOS ─────────────────────────────────");
            System.out.println("  1. Listar todos");
            System.out.println("  2. Buscar por ID");
            System.out.println("  3. Crear nuevo");
            System.out.println("  4. Modificar email/nombre");
            System.out.println("  5. Eliminar");
            System.out.println("  6. Cambiar usuario activo (sesión)");
            System.out.println("  0. Volver");

            int op = leerEntero("Opción: ");
            switch (op) {
                case 1 -> usuarioService.listarTodos().forEach(System.out::println);
                case 2 -> {
                    Long id = leerLong("ID: ");
                    usuarioService.buscarPorId(id).ifPresentOrElse(
                        System.out::println,
                        () -> System.out.println("❌ No encontrado.")
                    );
                }
                case 3 -> crearUsuario();
                case 4 -> modificarUsuario();
                case 5 -> {
                    Long id = leerLong("ID del usuario a eliminar: ");
                    System.out.println(usuarioService.eliminar(id) ? "✅ Eliminado." : "❌ No encontrado.");
                }
                case 6 -> cambiarUsuarioActivo();
                case 0 -> volver = true;
                default -> System.out.println("⚠ Opción no válida.");
            }
        }
    }

    private static void crearUsuario() {
        try {
            String username = leerTexto("Username: ");
            String email = leerTexto("Email: ");
            String nombre = leerTexto("Nombre completo: ");
            System.out.print("Rol (ADMIN/USUARIO) [USUARIO]: ");
            String rolStr = sc.nextLine().trim();
            Usuario.Rol rol = rolStr.equalsIgnoreCase("ADMIN") ? Usuario.Rol.ADMIN : Usuario.Rol.USUARIO;

            Usuario u = new Usuario(username, email, nombre, rol);
            Usuario creado = usuarioService.crear(u);
            System.out.println("✅ Usuario creado con ID: " + creado.getId());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void modificarUsuario() {
        Long id = leerLong("ID del usuario a modificar: ");
        Optional<Usuario> opt = usuarioService.buscarPorId(id);
        if (opt.isEmpty()) { System.out.println("❌ No encontrado."); return; }
        Usuario u = opt.get();
        try {
            String email = leerTextoOpcional("Nuevo email [" + u.getEmail() + "]: ");
            if (!email.isBlank()) u.setEmail(email);
            String nombre = leerTextoOpcional("Nuevo nombre [" + u.getNombre() + "]: ");
            if (!nombre.isBlank()) u.setNombre(nombre);
            usuarioService.actualizar(u);
            System.out.println("✅ Usuario actualizado.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void cambiarUsuarioActivo() {
        String username = leerTexto("Username para la sesión: ");
        Optional<Usuario> opt = usuarioService.buscarPorUsername(username);
        if (opt.isEmpty()) {
            System.out.println("❌ Usuario no encontrado.");
            return;
        }
        usuarioActual = username;
        videojuegoService.setUsuarioActual(username);
        usuarioService.setUsuarioActual(username);
        resenaService.setUsuarioActual(username);
        System.out.println("✅ Sesión cambiada a: " + username);
    }

    // ====================================================================
    // MENÚ RESEÑAS
    // ====================================================================

    private static void menuResenas() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── RESEÑAS ──────────────────────────────────");
            System.out.println("  1. Reseñas de un videojuego (SQL)");
            System.out.println("  2. Reseñas de un usuario (SQL)");
            System.out.println("  3. Crear reseña [SQL + MongoDB]");
            System.out.println("  4. Eliminar reseña");
            System.out.println("  5. Historial MongoDB de un juego");
            System.out.println("  6. Estadísticas MongoDB (agregación)");
            System.out.println("  0. Volver");

            int op = leerEntero("Opción: ");
            switch (op) {
                case 1 -> {
                    Long vid = leerLong("ID del videojuego: ");
                    List<?> resenas = resenaService.listarPorVideojuego(vid);
                    if (resenas.isEmpty()) System.out.println("Sin reseñas.");
                    else resenas.forEach(r -> System.out.println("  " + r));
                }
                case 2 -> {
                    Long uid = leerLong("ID del usuario: ");
                    List<?> resenas = resenaService.listarPorUsuario(uid);
                    if (resenas.isEmpty()) System.out.println("Sin reseñas.");
                    else resenas.forEach(r -> System.out.println("  " + r));
                }
                case 3 -> crearResena();
                case 4 -> {
                    Long id = leerLong("ID de la reseña a eliminar: ");
                    System.out.println(resenaService.eliminar(id) ? "✅ Eliminada." : "❌ No encontrada.");
                }
                case 5 -> {
                    Long vid = leerLong("ID del videojuego: ");
                    List<Document> hist = resenaService.historialResenasPorJuego(vid);
                    System.out.println("\n📋 Historial MongoDB (" + hist.size() + " reseñas):");
                    hist.forEach(d -> System.out.println("  " + formatDoc(d)));
                }
                case 6 -> {
                    System.out.println("\n📊 Estadísticas por juego (MongoDB):");
                    resenaService.estadisticasResenas().forEach(d ->
                        System.out.printf("  %-30s → Media: %.1f | Total: %d%n",
                            d.getString("_id"),
                            d.getDouble("media"),
                            d.getInteger("total"))
                    );
                }
                case 0 -> volver = true;
                default -> System.out.println("⚠ Opción no válida.");
            }
        }
    }

    private static void crearResena() {
        try {
            Long vid = leerLong("ID del videojuego: ");
            Long uid = leerLong("ID del usuario: ");
            int puntuacion = leerEntero("Puntuación (1-10): ");
            String comentario = leerTexto("Comentario: ");

            resenaService.crear(vid, uid, puntuacion, comentario);
            System.out.println("✅ Reseña creada y guardada en SQL + MongoDB.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ====================================================================
    // MENÚ AUDITORÍA (MongoDB)
    // ====================================================================

    private static void menuAuditoria() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── AUDITORÍA (MongoDB) ──────────────────────");
            System.out.println("  Total eventos: " + auditService.totalEventos());
            System.out.println("  1. Ver últimos 20 eventos");
            System.out.println("  2. Filtrar por tipo y entidad");
            System.out.println("  3. Filtrar por usuario (últimas 24h)");
            System.out.println("  4. Estadísticas por tipo (agregación)");
            System.out.println("  0. Volver");

            int op = leerEntero("Opción: ");
            switch (op) {
                case 1 -> {
                    System.out.println("\n📋 Últimos 20 eventos:");
                    auditService.listarRecientes(20).forEach(d -> System.out.println("  " + formatAudit(d)));
                }
                case 2 -> {
                    System.out.println("Tipos: CREATE, UPDATE, DELETE, READ");
                    String tipo = leerTexto("Tipo: ").toUpperCase();
                    System.out.println("Entidades: Videojuego, Usuario, Resena");
                    String entidad = leerTexto("Entidad: ");
                    List<Document> logs = auditService.buscarPorTipoYEntidad(tipo, entidad);
                    System.out.println("\n🔍 Resultados (" + logs.size() + "):");
                    logs.forEach(d -> System.out.println("  " + formatAudit(d)));
                }
                case 3 -> {
                    String user = leerTexto("Username: ");
                    List<Document> logs = auditService.buscarPorUsuarioYFecha(
                        user, LocalDateTime.now().minusHours(24), LocalDateTime.now()
                    );
                    System.out.println("\n🔍 Eventos de " + user + " (últimas 24h): " + logs.size());
                    logs.forEach(d -> System.out.println("  " + formatAudit(d)));
                }
                case 4 -> {
                    System.out.println("\n📊 Acciones por tipo:");
                    auditService.estadisticasPorTipo().forEach(d ->
                        System.out.printf("  %-10s → %d eventos%n",
                            d.getString("_id"), d.getInteger("total"))
                    );
                }
                case 0 -> volver = true;
                default -> System.out.println("⚠ Opción no válida.");
            }
        }
    }

    // ====================================================================
    // MENÚ CONSULTAS AVANZADAS SQL
    // ====================================================================

    private static void menuConsultasAvanzadas() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── CONSULTAS AVANZADAS SQL ──────────────────");
            System.out.println("  1. Juegos por género y rango de precio (paginado)");
            System.out.println("  2. Juegos mejor valorados (JOIN + GROUP BY)");
            System.out.println("  0. Volver");

            int op = leerEntero("Opción: ");
            switch (op) {
                case 1 -> {
                    System.out.println("(Deja género en blanco para todos los géneros)");
                    String genero = leerTextoOpcional("Género: ");
                    double min = leerDouble("Precio mínimo: ");
                    double max = leerDouble("Precio máximo: ");
                    int pagina = leerEntero("Página (0 = primera): ");
                    List<Videojuego> resultados = videojuegoService.buscarPorGeneroYPrecio(
                        genero.isBlank() ? null : genero, min, max, pagina, 5
                    );
                    System.out.println("\n🔍 Resultados (página " + pagina + ", máx 5):");
                    if (resultados.isEmpty()) System.out.println("  Sin resultados.");
                    else resultados.forEach(v -> System.out.printf("  %s | %.2f€%n", v.getTitulo(), v.getPrecio()));
                }
                case 2 -> {
                    double minPunt = leerDouble("Puntuación mínima (1.0-10.0): ");
                    List<Object[]> resultados = videojuegoService.juegosMejorValorados(minPunt);
                    System.out.println("\n🏆 Juegos con media ≥ " + minPunt + ":");
                    System.out.printf("%-30s %-15s %8s %8s%n", "Título", "Género", "Media", "Reseñas");
                    System.out.println("-".repeat(65));
                    for (Object[] row : resultados) {
                        System.out.printf("%-30s %-15s %8.1f %8d%n",
                            row[0], row[1], row[2], row[3]);
                    }
                }
                case 0 -> volver = true;
                default -> System.out.println("⚠ Opción no válida.");
            }
        }
    }

    // ====================================================================
    // MENÚ JSON IMPORT/EXPORT
    // ====================================================================

    private static void menuJsonImportExport() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── JSON IMPORT / EXPORT ─────────────────────");
            System.out.println("  1. Exportar videojuego a JSON");
            System.out.println("  2. Exportar todos los videojuegos a JSON");
            System.out.println("  3. Importar videojuego desde JSON");
            System.out.println("  0. Volver");

            int op = leerEntero("Opción: ");
            switch (op) {
                case 1 -> {
                    Long id = leerLong("ID del videojuego: ");
                    try {
                        String json = videojuegoService.exportarComoJson(id);
                        System.out.println("\n📄 JSON del videojuego:");
                        System.out.println(json);
                    } catch (Exception e) {
                        System.out.println("❌ Error: " + e.getMessage());
                    }
                }
                case 2 -> {
                    String json = videojuegoService.exportarTodosComoJson();
                    System.out.println("\n📄 JSON de todos los videojuegos:");
                    System.out.println(json);
                }
                case 3 -> {
                    System.out.println("Introduce el JSON del videojuego:");
                    System.out.println("Ejemplo: {\"titulo\":\"Hades\",\"genero\":\"Roguelike\",\"desarrollador\":\"Supergiant\",\"fechaLanzamiento\":\"2020-09-17\",\"precio\":24.99,\"descripcion\":\"Roguelike de acción\"}");
                    String json = leerTexto("JSON: ");
                    try {
                        Videojuego importado = videojuegoService.importarDesdeJson(json);
                        System.out.println("✅ Importado con ID: " + importado.getId());
                        System.out.println("   (Evento registrado en MongoDB)");
                    } catch (Exception e) {
                        System.out.println("❌ Error al importar: " + e.getMessage());
                    }
                }
                case 0 -> volver = true;
                default -> System.out.println("⚠ Opción no válida.");
            }
        }
    }

    // ====================================================================
    // UTILIDADES
    // ====================================================================

    private static String formatAudit(Document d) {
        return String.format("[%s] %s → %s #%d por '%s'",
            d.getDate("timestamp"),
            d.getString("type"),
            d.getString("entityType"),
            d.getLong("entityId") != null ? d.getLong("entityId") : 0,
            d.getString("user"));
    }

    private static String formatDoc(Document d) {
        return String.format("Reseña#%d | %s | %d/10 | %s | %s",
            d.getLong("resenaId") != null ? d.getLong("resenaId") : 0,
            d.getString("username"),
            d.getInteger("puntuacion"),
            d.getString("comentario"),
            d.getDate("fechaCreacion"));
    }

    private static int leerEntero(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String linea = sc.nextLine().trim();
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Introduce un número entero.");
            }
        }
    }

    private static Long leerLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠ Introduce un número válido.");
            }
        }
    }

    private static double leerDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(sc.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("⚠ Introduce un número decimal.");
            }
        }
    }

    private static String leerTexto(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = sc.nextLine().trim();
            if (input.isBlank()) System.out.println("⚠ El campo no puede estar vacío.");
        } while (input.isBlank());
        return input;
    }

    private static String leerTextoOpcional(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}
