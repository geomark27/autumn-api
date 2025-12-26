.PHONY: help setup build test run clean docker-up docker-down docker-logs db-reset compile package install

# Variables
COMPOSE=docker-compose --env-file .env.local
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
	@echo "$(GREEN)      AUTUMN BANKING SYSTEM - Comandos Make           $(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(GREEN)📦 PRIMERA VEZ (Setup Inicial):$(NC)"
	@echo "  $(YELLOW)make setup$(NC)              - Setup completo del proyecto"
	@echo "  $(YELLOW)make docker-up$(NC)          - Levantar infraestructura (PostgreSQL + Redis)"
	@echo "  $(YELLOW)make run$(NC)                - Ejecutar aplicación"
	@echo ""
	@echo "$(GREEN)� RÁPIDO (Una sola línea):$(NC)"
	@echo "  $(YELLOW)make start$(NC)              - Docker up + Compilar + Ejecutar (TODO en uno)"
	@echo ""
	@echo "$(GREEN)�🔄 DESARROLLO DIARIO:$(NC)"
	@echo "  $(YELLOW)make quick-start$(NC)        - Compilar + Levantar todo + Ejecutar"
	@echo "  $(YELLOW)make compile$(NC)            - Solo compilar cambios"
	@echo "  $(YELLOW)make test$(NC)               - Ejecutar tests"
	@echo ""
	@echo "$(GREEN)🐳 DOCKER:$(NC)"
	@echo "  $(YELLOW)make docker-up$(NC)          - Levantar PostgreSQL + Redis"
	@echo "  $(YELLOW)make docker-down$(NC)        - Detener servicios"
	@echo "  $(YELLOW)make docker-clean$(NC)       - Eliminar contenedores y volúmenes"
	@echo "  $(YELLOW)make docker-logs$(NC)        - Ver logs en tiempo real"
	@echo ""
	@echo "$(GREEN)🗄️  BASE DE DATOS:$(NC)"
	@echo "  $(YELLOW)make db-connect$(NC)         - Conectar a PostgreSQL (psql)"
	@echo "  $(YELLOW)make db-reset$(NC)           - Resetear base de datos"
	@echo "  $(YELLOW)make redis-cli$(NC)          - Conectar a Redis"
	@echo ""
	@echo "$(GREEN)🧹 LIMPIEZA:$(NC)"
	@echo "  $(YELLOW)make clean$(NC)              - Limpiar archivos generados"
	@echo "  $(YELLOW)make clean-all$(NC)          - Limpieza profunda (Maven + caché)"
	@echo "  $(YELLOW)make fresh-install$(NC)      - Reinstalar desde cero"
	@echo ""
	@echo "$(GREEN)📊 UTILIDADES:$(NC)"
	@echo "  $(YELLOW)make status$(NC)             - Ver estado de servicios"
	@echo "  $(YELLOW)make dependencies$(NC)       - Ver árbol de dependencias"
	@echo ""
	@echo "$(GREEN)📦 GIT (Control de versiones):$(NC)"
	@echo "  $(YELLOW)make push m='mensaje'$(NC)   - Add + Commit + Push"
	@echo "  $(YELLOW)make pull$(NC)                - Pull desde origin"
	@echo "  $(YELLOW)make git-status$(NC)         - Ver estado de git"
	@echo "  $(YELLOW)make sync m='mensaje'$(NC)   - Pull + Push (sincronizar)"
	@echo ""
	@echo "$(BLUE)Usa 'make <comando>' para ejecutar$(NC)"
	@echo ""

## setup: Configuración inicial del proyecto (primera vez)
setup:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)        🎯 SETUP INICIAL - AUTUMN BANKING SYSTEM        $(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 1/4:$(NC) Verificando archivo .env.local..."
	@test -f .env.local || (echo "$(RED)ERROR: .env.local no encontrado$(NC)" && echo "$(YELLOW)Copia .env.example a .env.local y configúralo$(NC)" && exit 1)
	@echo "$(GREEN)  ✅ .env.local existe$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 2/4:$(NC) Descargando dependencias de Maven..."
	@mvn dependency:resolve dependency:resolve-plugins -q
	@echo "$(GREEN)  ✅ Dependencias descargadas$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 3/4:$(NC) Compilando proyecto (generando mappers)..."
	@mvn clean compile -q
	@echo "$(GREEN)  ✅ Proyecto compilado$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 4/4:$(NC) Instalando en repositorio Maven local..."
	@mvn install -DskipTests -q
	@echo "$(GREEN)  ✅ Proyecto instalado$(NC)"
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)        ✅ SETUP COMPLETADO EXITOSAMENTE                $(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(BLUE)Próximos pasos:$(NC)"
	@echo "  $(YELLOW)make docker-up$(NC)   → Levantar infraestructura (PostgreSQL + Redis)"
	@echo "  $(YELLOW)make run$(NC)         → Ejecutar aplicación en desarrollo"
	@echo "  $(YELLOW)make help$(NC)        → Ver todos los comandos disponibles"
	@echo ""

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

## install: Instala el proyecto en el repositorio local de Maven (sin tests)
install:
	@echo "$(GREEN)📥 Instalando proyecto...$(NC)"
	mvn clean install -DskipTests
	@echo "$(GREEN)✅ Proyecto instalado (tests omitidos)$(NC)"

## install-with-tests: Instala el proyecto ejecutando todos los tests
install-with-tests:
	@echo "$(GREEN)📥 Instalando proyecto con tests...$(NC)"
	mvn clean install
	@echo "$(GREEN)✅ Proyecto instalado con tests$(NC)"

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

## start: Comando COMPLETO - Docker up + Compilar + Ejecutar
start: docker-up compile run
	@echo "$(GREEN)✅ Aplicación iniciada correctamente$(NC)"

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

## clean: Limpia archivos generados por Maven
clean:
	@echo "$(YELLOW)🧹 Limpiando archivos generados...$(NC)"
	mvn clean
	@echo "$(GREEN)✅ Limpieza completada$(NC)"

## clean-all: Limpieza profunda (Maven + dependencias + caché IDE)
clean-all:
	@echo "$(RED)🗑️  Limpieza profunda del proyecto...$(NC)"
	@echo "$(YELLOW)  → Limpiando Maven...$(NC)"
	mvn clean
	@echo "$(YELLOW)  → Eliminando dependencias descargadas...$(NC)"
	rm -rf ~/.m2/repository/sys/azentic/autumn
	@echo "$(YELLOW)  → Limpiando caché de IDE...$(NC)"
	rm -rf .vscode/.factorypath .classpath .project .settings
	rm -rf target/
	@echo "$(GREEN)✅ Limpieza profunda completada$(NC)"

## fresh-install: Reinstala TODO desde cero (limpieza + descarga dependencias)
fresh-install: clean-all
	@echo "$(GREEN)🆕 Instalación desde cero...$(NC)"
	@echo "$(YELLOW)  → Descargando dependencias...$(NC)"
	mvn dependency:resolve dependency:resolve-plugins
	@echo "$(YELLOW)  → Compilando y generando código...$(NC)"
	mvn clean compile
	@echo "$(YELLOW)  → Instalando en repositorio local...$(NC)"
	mvn install -DskipTests
	@echo "$(GREEN)✅ Instalación fresca completada$(NC)"

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

## quick-start: Inicio rápido para desarrollo (infraestructura + app)
quick-start:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)           🚀 INICIO RÁPIDO DE DESARROLLO              $(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 1/3:$(NC) Levantando infraestructura..."
	@$(MAKE) docker-up
	@echo ""
	@echo "$(YELLOW)Paso 2/3:$(NC) Compilando cambios..."
	@mvn compile -q
	@echo "$(GREEN)  ✅ Compilación exitosa$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 3/3:$(NC) Iniciando aplicación..."
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@mvn spring-boot:run

## dev: Alias de quick-start
dev: quick-start

## dev-docker: Desarrollo completo en Docker (todo containerizado)
dev-docker: docker-build docker-up-all
	@echo "$(GREEN)✅ Stack completa en ejecución$(NC)"
	@echo "$(BLUE)Accede a: http://localhost:8080$(NC)"

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

# ============================================
# COMANDOS GIT
# ============================================

# Variables para Git
BRANCH := $(shell git branch --show-current)

## push: Push rápido con mensaje - Uso: make push m="tu mensaje"
push:
	@if [ -z "$(m)" ]; then \
		echo "$(RED)❌ Error: Debes proporcionar un mensaje$(NC)"; \
		echo "$(YELLOW)   Uso: make push m='tu mensaje de commit'$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)📦 Agregando archivos...$(NC)"
	@git add .
	@echo "$(GREEN)✍️  Commiteando: $(m)$(NC)"
	@git commit -m "$(m)"
	@echo "$(GREEN)🚀 Pusheando a origin/$(BRANCH)...$(NC)"
	@git push origin $(BRANCH)
	@echo "$(GREEN)✅ Push completado exitosamente!$(NC)"

## pull: Pull desde origin
pull:
	@echo "$(GREEN)⬇️  Pulling desde origin/$(BRANCH)...$(NC)"
	@git pull origin $(BRANCH)
	@echo "$(GREEN)✅ Pull completado!$(NC)"

## git-status: Ver estado de git
git-status:
	@echo "$(BLUE)📊 Estado de Git (rama: $(BRANCH)):$(NC)"
	@echo ""
	@git status

## sync: Sincronizar (pull + push) - Uso: make sync m="tu mensaje"
sync:
	@if [ -z "$(m)" ]; then \
		echo "$(RED)❌ Error: Debes proporcionar un mensaje$(NC)"; \
		echo "$(YELLOW)   Uso: make sync m='tu mensaje de commit'$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)⬇️  Pulling cambios...$(NC)"
	@git pull origin $(BRANCH)
	@echo "$(GREEN)📦 Agregando archivos...$(NC)"
	@git add .
	@echo "$(GREEN)✍️  Commiteando: $(m)$(NC)"
	@git commit -m "$(m)"
	@echo "$(GREEN)🚀 Pusheando a origin/$(BRANCH)...$(NC)"
	@git push origin $(BRANCH)
	@echo "$(GREEN)✅ Sincronización completada!$(NC)"
