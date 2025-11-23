# Estrategia de Branching: GitFlow Adaptado

## NOTA: Rama actual es `dev` (NO develop)

## Ramas Principales

### master (producción)
- Código estable en producción
- Solo merge desde release branches
- Tags para releases (v1.0.0, v1.1.0)

### dev (desarrollo) ← **ACTUAL**
- Rama de integración principal
- Código listo para próximo release
- Merge desde feature branches
- **Esta es la rama que usamos, NO develop**

## Ramas de Soporte

### feature/* (nuevas funcionalidades)
**Patrón**: `feature/SPRINT-TAREA-descripcion`

**Ejemplos**:
- `feature/S1-01-sonarqube-integration`
- `feature/S1-02-trivy-security`
- `feature/S2-01-unit-tests-user-service`

**Flujo**:
```bash
# Crear feature DESDE dev (no develop)
git checkout dev
git pull origin dev
git checkout -b feature/S1-01-sonarqube-integration

# Trabajar...
git add .
git commit -m "feat(ci): integrate SonarQube analysis in pipeline"
git push origin feature/S1-01-sonarqube-integration

# En GitHub: Crear PR a dev (no develop)
# Después del merge: Borrar rama feature
```

### release/* (preparar release)
**Patrón**: `release/vX.Y.Z`

```bash
# Desde dev
git checkout -b release/v1.0.0
# Ajustes finales...
# Merge a master y dev
```

### hotfix/* (correcciones urgentes)
**Patrón**: `hotfix/descripcion-corta`

## Convención de Commits

**Formato**: `tipo(scope): descripción`

**Tipos**:
- `feat`: nueva funcionalidad
- `fix`: corrección de bug
- `docs`: documentación
- `test`: tests
- `refactor`: refactorización
- `ci`: CI/CD
- `chore`: mantenimiento

**Ejemplos**:
```
feat(ci): integrate SonarQube analysis in pipeline
fix(user-service): resolve NPE in credential lookup
docs(readme): update installation instructions
test(order-service): add integration tests for cart API
ci(workflows): add Trivy security scanning
```

## Estado Actual del Proyecto
- ✅ Rama principal: `dev` (confirmado 4 nov 2024)
- ✅ Workflows configurados para `dev`
- ✅ stage-ci.yml usa `dev`
- 📅 Primera release planificada: v1.0.0 (25 nov)

## Próximos Pasos
1. Mantener `dev` como rama de integración
2. NO crear `develop` para evitar confusión
3. Todos los features van a `dev`
4. Workflows ya están configurados correctamente
