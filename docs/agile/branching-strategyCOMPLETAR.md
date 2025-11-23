# Estrategia de Branching: GitFlow

## Ramas Principales

### master (producción)
- Código estable en producción
- Solo merge desde release branches
- Tags para releases (v1.0.0, v1.1.0)

### develop (desarrollo)
- Rama de integración
- Código listo para próximo release
- Merge desde feature branches

## Ramas de Soporte

### feature/* (nuevas funcionalidades)
**Patrón**: `feature/SPRINT-TAREA-descripcion`

Ejemplos:
- `feature/S1-01-sonarqube-integration`
- `feature/S1-02-trivy-security`
- `feature/S2-01-unit-tests-user-service`

**Flujo**:
```bash
# Crear feature
git checkout develop
git pull origin develop
git checkout -b feature/S1-01-sonarqube-integration

# Trabajar...
git add .
git commit -m "feat(ci): integrate SonarQube analysis in pipeline"
git push origin feature/S1-01-sonarqube-integration

# En GitHub: Crear PR a develop
# Después del merge: Borrar rama
release/* (preparar release)
Patrón: release/vX.Y.Z
hotfix/* (correcciones urgentes)
Patrón: hotfix/descripcion-corta
Convención de Commits
Formato: tipo(scope): descripción Tipos:
feat: nueva funcionalidad
fix: corrección de bug
docs: documentación
test: tests
refactor: refactorización
ci: CI/CD
chore: mantenimiento
Ejemplos:
feat(ci): integrate SonarQube analysis in pipeline
fix(user-service): resolve NPE in credential lookup
docs(readme): update installation instructions
test(order-service): add integration tests for cart API
ci(workflows): add Trivy security scanning
Estado Actual
Rama principal actual: dev → migrar a develop
Adopción GitFlow: 4 noviembre 2024
Primera release planificada: v1.0.0 (25 nov) EOF