# ms-andesstay-bff

BFF **Spring Boot** del caso **AndesStay** (EP1 DSY1107). Va detrás de AWS API Gateway y valida el JWT de **Microsoft Entra ID** (issuer + audience + firma/vigencia), además de autorizar por rol.

## Perfil EP1

Alcance mínimo evaluado:

- Login con Entra ID en el frontend Angular (`frontend-andesstay`)
- BFF que valida el token y comprueba el rol en cada endpoint

Los payloads de reservas/catálogo/auditoría/reportes son **mock** para demostrar el flujo. En evaluaciones siguientes se enrutarán a `ms-andesstay-reservations`, `catalog`, `notify`, `audit` y `report`.

## Roles del caso

| Rol | Acceso en este BFF |
|-----|-------------------|
| Admin | Dashboard, reservas, catálogo, reportería, auditoría |
| Operador | Dashboard, reservas, catálogo |
| Cliente | Dashboard, reservas |
| Auditor | Dashboard, auditoría |

Los roles se leen del claim `roles` (o `groups`) del JWT y se mapean a `ROLE_<rol>`.

## Endpoints

| Método | Ruta | Autorización |
|--------|------|----------------|
| GET | `/actuator/health` | público |
| GET | `/api/bff/ping` | autenticado |
| GET | `/api/bff/me` | autenticado |
| GET | `/api/bff/dashboard` | autenticado (respuesta varía por rol) |
| GET | `/api/bff/reservations` | Admin, Operador, Cliente |
| GET | `/api/bff/catalog/units` | Admin, Operador |
| GET | `/api/bff/audit/timeline` | Admin, Auditor |
| GET | `/api/bff/report/kpis` | Admin |

Errores JSON:

- `401` token ausente/inválido (firma, issuer, audience, expiración)
- `403` token válido pero rol insuficiente

## Requisitos

- Java 21+
- Maven 3.9+
- Tenant Entra ID con API expuesta y roles asignados

## Configuración

Crea `.env.andesstay` (o exporta las variables):

```properties
AZURE_ISSUER_URI=https://login.microsoftonline.com/<TENANT_ID>/v2.0
AZURE_AUDIENCE=api://<API_CLIENT_ID>
ANDESSTAY_CORS_ORIGINS=http://localhost:4200
```

Ver `.env.example`.

## Ejecutar

### Perfil `azure` (por defecto, validación JWT)

```powershell
$env:ENV_FILE = ".env.andesstay"
mvn spring-boot:run
```

### Perfil `local` (sin Entra, para desarrollo)

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Con `local` el BFF responde identidad simulada y no valida tokens.

### Pruebas

```powershell
mvn clean test
```

## Flujo

```text
JWT (Entra ID) → API Gateway (JWT Authorizer) → ms-andesstay-bff → dominio
```

En local se omite el Gateway:

```text
Angular MSAL → Authorization: Bearer → ms-andesstay-bff
```

## Docker

```powershell
mvn -DskipTests package
docker build -t ms-andesstay-bff .
docker run -d --name ms-andesstay-bff -p 8080:8080 `
  -e AZURE_ISSUER_URI="<issuer>" `
  -e AZURE_AUDIENCE="api://<api-client-id>" `
  -e ANDESSTAY_CORS_ORIGINS="http://localhost:4200" `
  ms-andesstay-bff
```

## Validación de seguridad (rubro BFF 40%)

1. **Issuer**: `https://login.microsoftonline.com/<TENANT_ID>/v2.0`
2. **Audience**: `api://<API_CLIENT_ID>` (acepta también el GUID sin prefijo)
3. **Firma y expiración**: Spring OAuth2 Resource Server + JWKS del tenant
4. **Roles**: `@PreAuthorize` por endpoint
5. **Códigos**: 401 vs 403 con JSON de error

## Entrega

Repositorio GitHub sugerido: `ms-andesstay-bff`. No subir `target/` ni `.env`. Enviar el enlace al docente según el encargo EP1.
