# Estrategia de Branching

## Ramas Principales

### `main`
- Rama principal de producción
- Contiene el código estable y listo para producción
- Protegida contra push directo
- Requiere Pull Request y aprobación de revisores
- Solo se actualiza mediante merge desde `release` o hotfixes

### `develop`
- Rama de desarrollo principal
- Contiene el código más reciente para el siguiente release
- Base para todas las ramas de feature
- Se actualiza mediante merge desde ramas de feature

## Ramas de Soporte

### `feature/*`
- Nomenclatura: `feature/nombre-del-feature`
- Se crean desde `develop`
- Se mergean de vuelta a `develop`
- Ejemplos:
  - `feature/user-authentication`
  - `feature/payment-integration`
  - `feature/order-management`

### `release/*`
- Nomenclatura: `release/vX.Y.Z`
- Se crean desde `develop`
- Se mergean a `main` y `develop`
- Usadas para preparar releases
- Ejemplo: `release/v1.2.0`

### `hotfix/*`
- Nomenclatura: `hotfix/descripcion-corta`
- Se crean desde `main`
- Se mergean a `main` y `develop`
- Para correcciones urgentes en producción
- Ejemplo: `hotfix/security-patch`

## Flujo de Trabajo

1. **Nuevo Feature**:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/nombre-feature
   # Desarrollo del feature
   git push origin feature/nombre-feature
   # Crear Pull Request a develop
   ```

2. **Preparar Release**:
   ```bash
   git checkout develop
   git checkout -b release/vX.Y.Z
   # Ajustes finales y versionado
   git push origin release/vX.Y.Z
   # Crear Pull Request a main y develop
   ```

3. **Hotfix**:
   ```bash
   git checkout main
   git checkout -b hotfix/descripcion
   # Corrección
   git push origin hotfix/descripcion
   # Crear Pull Request a main y develop
   ```

## Reglas y Convenciones

1. **Commits**:
   - Usar mensajes descriptivos
   - Formato: `tipo(scope): descripción`
   - Tipos: feat, fix, docs, style, refactor, test, chore

2. **Pull Requests**:
   - Descripción clara del cambio
   - Referencia a issues relacionados
   - Revisión de código requerida
   - Tests pasando

3. **Protección de Ramas**:
   - `main` y `develop` protegidas
   - Requiere revisión de código
   - Requiere tests pasando
   - No permite push directo

4. **Versionado**:
   - Seguir Semantic Versioning (MAJOR.MINOR.PATCH)
   - Actualizar versiones en release branches
   - Documentar cambios en CHANGELOG.md

## Integración con CI/CD

- Cada push a cualquier rama ejecuta pipeline de CI
- Pull Requests requieren pipeline exitoso
- Deploy a producción solo desde `main`
- Deploy a staging desde `develop`
- Deploy a desarrollo desde ramas de feature 