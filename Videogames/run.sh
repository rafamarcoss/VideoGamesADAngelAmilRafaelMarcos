#!/bin/bash
# Script para compilar y ejecutar la aplicación

echo "🎮 Gestión de Videojuegos - Iniciando..."
echo ""

# Crear directorio para BD H2
mkdir -p data

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java no encontrado. Instala JDK 17+ desde https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "17" ] 2>/dev/null; then
    echo "⚠ Se requiere Java 17 o superior. Versión actual: $JAVA_VERSION"
fi

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven no encontrado. Instala Maven desde https://maven.apache.org/"
    exit 1
fi

# Compilar si no existe el JAR
JAR="target/videogames-app-1.0-SNAPSHOT-jar-with-dependencies.jar"
if [ ! -f "$JAR" ]; then
    echo "📦 Compilando proyecto..."
    mvn clean package -q
    if [ $? -ne 0 ]; then
        echo "❌ Error de compilación. Revisa los errores anteriores."
        exit 1
    fi
    echo "✅ Compilación exitosa"
fi

# Verificar MongoDB (opcional, avisa si no está)
if ! command -v mongod &> /dev/null; then
    echo "⚠ MongoDB no encontrado en PATH. Si no está en ejecución, la auditoría fallará."
    echo "  Instala MongoDB: https://www.mongodb.com/try/download/community"
fi

echo ""
echo "🚀 Ejecutando aplicación..."
echo ""
java -jar "$JAR"
