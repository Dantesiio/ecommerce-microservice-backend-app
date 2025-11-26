#!/bin/bash

# Script de inicio rápido para Ecommerce Microservices
# Uso: ./start.sh [build|up|down|logs|status]

set -e

COMPOSE_FILE="compose.yml"
PROJECT_NAME="ecommerce-microservice-backend-app"

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker no está instalado. Por favor instálalo primero."
        exit 1
    fi

    if ! docker ps &> /dev/null; then
        print_error "Docker no está corriendo. Por favor inicia Docker Desktop."
        exit 1
    fi

    print_info "Docker está instalado y corriendo ✓"
}

check_maven() {
    if [ ! -f "./mvnw" ]; then
        print_error "Maven wrapper (mvnw) no encontrado. Asegúrate de estar en el directorio raíz del proyecto."
        exit 1
    fi
    print_info "Maven wrapper encontrado ✓"
}

build_project() {
    print_info "Construyendo el proyecto..."
    
    # Limpiar target problemático de product-service si existe
    if [ -d "product-service/target" ]; then
        print_warn "Limpiando target de product-service..."
        rm -rf product-service/target/
    fi
    
    ./mvnw clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_info "Build completado exitosamente ✓"
    else
        print_error "Build falló. Revisa los errores arriba."
        exit 1
    fi
}

start_services() {
    print_info "Iniciando todos los servicios con Docker Compose..."
    
    docker compose -f $COMPOSE_FILE up -d
    
    if [ $? -eq 0 ]; then
        print_info "Servicios iniciados ✓"
        print_info "Esperando a que los servicios se registren en Eureka..."
        sleep 10
        
        print_info "Verificando estado de los servicios..."
        docker compose -f $COMPOSE_FILE ps
        
        echo ""
        print_info "Servicios disponibles en:"
        echo "  - Eureka Dashboard: http://localhost:8761"
        echo "  - API Gateway: http://localhost:8080"
        echo "  - Proxy Client: https://localhost:8900"
        echo "  - Zipkin: http://localhost:9411"
        echo ""
        print_warn "Nota: Los servicios pueden tardar 1-2 minutos en registrarse completamente."
        print_info "Usa './start.sh logs' para ver los logs o './start.sh status' para ver el estado."
    else
        print_error "Error al iniciar los servicios."
        exit 1
    fi
}

stop_services() {
    print_info "Deteniendo todos los servicios..."
    docker compose -f $COMPOSE_FILE down
    
    if [ $? -eq 0 ]; then
        print_info "Servicios detenidos ✓"
    else
        print_error "Error al detener los servicios."
        exit 1
    fi
}

show_logs() {
    print_info "Mostrando logs de todos los servicios (Ctrl+C para salir)..."
    docker compose -f $COMPOSE_FILE logs -f
}

show_status() {
    print_info "Estado de los servicios:"
    docker compose -f $COMPOSE_FILE ps
    
    echo ""
    print_info "Verificando Eureka (Service Discovery)..."
    if curl -s http://localhost:8761 > /dev/null 2>&1; then
        print_info "Eureka está accesible en http://localhost:8761 ✓"
    else
        print_warn "Eureka no está accesible aún. Espera unos segundos más."
    fi
}

show_help() {
    echo "Uso: ./start.sh [comando]"
    echo ""
    echo "Comandos disponibles:"
    echo "  build   - Construir el proyecto (Maven)"
    echo "  up      - Iniciar todos los servicios"
    echo "  down    - Detener todos los servicios"
    echo "  logs    - Mostrar logs de todos los servicios"
    echo "  status  - Mostrar estado de los servicios"
    echo "  help    - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  ./start.sh build    # Construir el proyecto"
    echo "  ./start.sh up       # Iniciar servicios"
    echo "  ./start.sh logs     # Ver logs"
    echo "  ./start.sh down     # Detener servicios"
}

# Main
case "${1:-help}" in
    build)
        check_docker
        check_maven
        build_project
        ;;
    up)
        check_docker
        start_services
        ;;
    down)
        check_docker
        stop_services
        ;;
    logs)
        check_docker
        show_logs
        ;;
    status)
        check_docker
        show_status
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Comando desconocido: $1"
        echo ""
        show_help
        exit 1
        ;;
esac


