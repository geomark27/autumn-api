.PHONY: help setup build test run clean docker-up docker-down docker-logs db-reset compile package install

# Variables
COMPOSE=docker-compose
DB_NAME=autumn_dev
DB_USER=autumn_user
DB_PASS=autumn_pass

# Color output
BLUE=\033[0;34m
GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
NC=\033[0m # No Color

## help: Muestra esta ayuda
help:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN) Autumn Banking System - Comandos Disponibles$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@grep -E '^## ' Makefile | sed 's/## /  $(YELLOW)make /g' | column -t -s ':'
	@echo ""

## setup: Configuración inicial del proyecto (instala dependencias)
setup:
	@echo "$(GREEN)🔧 Configurando proyecto...$(NC)"
	mvn clean install -DskipTests
	@echo "$(GREEN)✅ Proyecto configurado exitosamente$(NC)"

## docker-up: Levanta toda la infraestructura (PostgreSQL, Redis)
docker-up:
	@echo "$(GREEN)🐳 Levantando infraestructura con Docker Compose...$(NC)"
	$(COMPOSE) up -d postgres redis
	@echo "$(GREEN)✅ Servicios iniciados$(NC)"
	@echo "$(BLUE)   PostgreSQL: localhost:5432 (DB: $(DB_NAME))$(NC)"
	@echo "$(BLUE)   Redis: localhost:6379$(NC)"

## docker-up-all: Levanta TODO (PostgreSQL, Redis y aplicación)
docker-up-all:
	@echo "$(GREEN)🐳 Levantando toda la stack...$(NC)"
	$(COMPOSE) up -d
	@echo "$(GREEN)✅ Stack completa iniciada$(NC)"

## docker-down: Detiene todos los servicios
docker-down:
	@echo "$(YELLOW)⏸️  Deteniendo servicios...$(NC)"
	$(COMPOSE) down
	@echo "$(GREEN)✅ Servicios detenidos$(NC)"

## docker-clean: Elimina contenedores y volúmenes (CUIDADO: borra datos)
docker-clean:
	@echo "$(RED)🗑️  Eliminando contenedores y volúmenes...$(NC)"
	$(COMPOSE) down -v
	@echo "$(GREEN)✅ Limpieza completada$(NC)"

## docker-logs: Muestra logs de todos los servicios
docker-logs:
	@echo "$(BLUE)📋 Logs en tiempo real (Ctrl+C para salir):$(NC)"
	$(COMPOSE) logs -f

## docker-logs-app: Logs solo de la aplicación
docker-logs-app:
	@echo "$(BLUE)📋 Logs de la aplicación:$(NC)"
	$(COMPOSE) logs -f app

## compile: Compila el proyecto sin ejecutar tests
compile:
	@echo "$(GREEN)🔨 Compilando proyecto...$(NC)"
	mvn clean compile
	@echo "$(GREEN)✅ Compilación exitosa$(NC)"

## package: Empaqueta el proyecto en JAR
package:
	@echo "$(GREEN)📦 Empaquetando proyecto...$(NC)"
	mvn clean package -DskipTests
	@echo "$(GREEN)✅ JAR generado en target/$(NC)"

## install: Instala el proyecto en el repositorio local de Maven
install:
	@echo "$(GREEN)📥 Instalando proyecto...$(NC)"
	mvn clean install
	@echo "$(GREEN)✅ Proyecto instalado$(NC)"

## test: Ejecuta todos los tests
test:
	@echo "$(GREEN)🧪 Ejecutando tests...$(NC)"
	mvn test

## test-unit: Ejecuta solo tests unitarios
test-unit:
	@echo "$(GREEN)🧪 Ejecutando tests unitarios...$(NC)"
	mvn test -Dtest="**/*Test"

## test-integration: Ejecuta solo tests de integración
test-integration:
	@echo "$(GREEN)🧪 Ejecutando tests de integración...$(NC)"
	mvn test -Dtest="**/*IT"

## run: Ejecuta la aplicación en modo desarrollo
run:
	@echo "$(GREEN)🚀 Iniciando aplicación...$(NC)"
	mvn spring-boot:run

## run-prod: Ejecuta la aplicación con perfil de producción
run-prod:
	@echo "$(GREEN)🚀 Iniciando aplicación (producción)...$(NC)"
	mvn spring-boot:run -Dspring-boot.run.profiles=prod

## run-jar: Ejecuta el JAR generado
run-jar: package
	@echo "$(GREEN)🚀 Ejecutando JAR...$(NC)"
	java -jar target/autumn-0.0.1-SNAPSHOT.jar

## clean: Limpia archivos generados
clean:
	@echo "$(YELLOW)🧹 Limpiando archivos generados...$(NC)"
	mvn clean
	@echo "$(GREEN)✅ Limpieza completada$(NC)"

## docker-build: Construye la imagen de la aplicación
docker-build:
	@echo "$(GREEN)🏗️  Construyendo imagen Docker...$(NC)"
	$(COMPOSE) build app
	@echo "$(GREEN)✅ Imagen construida$(NC)"

## docker-rebuild: Reconstruye la imagen forzando (sin cache)
docker-rebuild:
	@echo "$(GREEN)🏗️  Reconstruyendo imagen (sin cache)...$(NC)"
	$(COMPOSE) build --no-cache app
	@echo "$(GREEN)✅ Imagen reconstruida$(NC)"

## db-reset: Resetea la base de datos (CUIDADO: elimina todos los datos)
db-reset:
	@echo "$(YELLOW)⚠️  Reseteando base de datos...$(NC)"
	$(COMPOSE) down -v
	$(COMPOSE) up -d postgres redis
	@sleep 3
	@echo "$(GREEN)✅ Base de datos reseteada$(NC)"

## db-connect: Conecta a PostgreSQL via psql
db-connect:
	@echo "$(BLUE)🔌 Conectando a PostgreSQL...$(NC)"
	$(COMPOSE) exec postgres psql -U $(DB_USER) -d $(DB_NAME)

## redis-cli: Abre Redis CLI
redis-cli:
	@echo "$(BLUE)🔌 Conectando a Redis...$(NC)"
	$(COMPOSE) exec redis redis-cli

## logs: Muestra logs de la aplicación Spring Boot
logs:
	@tail -f logs/spring.log 2>/dev/null || echo "$(RED)No se encontraron logs$(NC)"

## format: Formatea el código con el plugin de Maven
format:
	@echo "$(GREEN)✨ Formateando código...$(NC)"
	mvn spring-javaformat:apply

## dependencies: Muestra el árbol de dependencias
dependencies:
	@echo "$(BLUE)📦 Árbol de dependencias:$(NC)"
	mvn dependency:tree

## update: Actualiza las dependencias del proyecto
update:
	@echo "$(GREEN)🔄 Actualizando dependencias...$(NC)"
	mvn versions:display-dependency-updates

## dev: Setup completo para desarrollo local (docker + compile + run)
dev: docker-up compile run

## dev-docker: Desarrollo completo en Docker (todo containerizado)
dev-docker: docker-build docker-up-all
	@echo "$(GREEN)✅ Stack completa en ejecución$(NC)"

## ci: Flujo completo de CI (compile + test + package)
ci: compile test package
	@echo "$(GREEN)✅ Build de CI exitoso$(NC)"

## all: Ejecuta todo el flujo (setup + dev)
all: setup dev

## status: Muestra el estado de los servicios
status:
	@echo "$(BLUE)📊 Estado de servicios:$(NC)"
	@echo ""
	$(COMPOSE) ps
	@echo ""
	@echo "$(YELLOW)Health Status:$(NC)"
	@curl -s http://localhost:8080/actuator/health 2>/dev/null && echo "  ✅ Spring Boot: OK" || echo "  ❌ Spring Boot: DOWN"

## restart: Reinicia todos los servicios
restart:
	@echo "$(YELLOW)🔄 Reiniciando servicios...$(NC)"
	$(COMPOSE) restart
	@echo "$(GREEN)✅ Servicios reiniciados$(NC)"

## restart-app: Reinicia solo la aplicación
restart-app:
	@echo "$(YELLOW)🔄 Reiniciando aplicación...$(NC)"
	$(COMPOSE) restart app
	@echo "$(GREEN)✅ Aplicación reiniciada$(NC)"
