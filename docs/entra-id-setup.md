# Configuración Microsoft Entra ID (AndesStay)

Guía para crear el App Registration que usa el frontend Angular y el backend BFF.

## 1. Crear la aplicación SPA (frontend)

1. Azure Portal → **Microsoft Entra ID** → **App registrations** → **New registration**
2. Name: `AndesStay-SPA` (o `BarrioDigital-SAP` si el docente exige ese nombre)
3. Supported account types: cuentas de tu organización / tenant DUOC
4. Redirect URI:
   - Platform: **Single-page application (SPA)**
   - URI: `http://localhost:4200`
5. Registrar y anotar:
   - **Application (client) ID** → `environment.azure.clientId`
   - **Directory (tenant) ID** → `environment.azure.tenantId`

## 2. Crear / configurar la API (audience del BFF)

1. **New registration** → Name: `AndesStay-API`
2. Anotar su **Application (client) ID** → `environment.api.clientId` y `AZURE_AUDIENCE`
3. **Expose an API** → Set Application ID URI:
   - `api://<API_CLIENT_ID>`
4. **Add a scope**:
   - Name: `access_as_user`
   - Who can consent: Admins and users
   - Admin consent display: “Acceder a AndesStay API”
5. El scope completo es: `api://<API_CLIENT_ID>/access_as_user`

## 3. App Roles (autorización del caso)

En la aplicación **API** (AndesStay-API) → **App roles**:

| Display name | Value | Allowed member types |
|--------------|-------|----------------------|
| Admin | `Admin` | Users/Groups |
| Operador | `Operador` | Users/Groups |
| Cliente | `Cliente` | Users/Groups |
| Auditor | `Auditor` | Users/Groups |

En **Enterprise applications** → AndesStay-API → **Users and groups** → asignar el rol a cada cuenta de prueba.

El claim `roles` llega al SPA y el BFF lo convierte a `ROLE_Admin`, etc.

## 4. Permiso de la SPA sobre la API

En **AndesStay-SPA** → **API permissions** → Add a permission → **My APIs** → AndesStay-API → `access_as_user` (Delegated).

## 5. Valores que debes rellenar

### Frontend — `src/environments/environment.ts`

```ts
export const environment = {
  production: false,
  demo: false, // false en entrega con Entra real
  azure: {
    clientId: '<SPA-client-id>',
    tenantId: '<tenant-id>',
    redirectUri: 'http://localhost:4200',
    postLogoutRedirectUri: 'http://localhost:4200',
  },
  api: {
    baseUrl: 'http://localhost:8080',
    clientId: '<API-client-id>',
    scopeName: 'access_as_user',
  },
};
```

### Backend — `bff/.env.andesstay`

```properties
AZURE_ISSUER_URI=https://login.microsoftonline.com/<TENANT_ID>/v2.0
AZURE_AUDIENCE=api://<API_CLIENT_ID>
ANDESSTAY_CORS_ORIGINS=http://localhost:4200
```

Los mismos valores aplican a cada microservicio de dominio (perfil `azure`).

## 6. Oracle Autonomous (properties de conexión)

Cuando tengas la BD cloud, exporta en cada servicio (o en el `.env` del host EC2):

```properties
ORACLE_URL=jdbc:oracle:thin:@<host>:1521/<service_name>
ORACLE_USER=<user>
ORACLE_PASSWORD=<password>
```

Los `application.yml` ya leen estas variables en el perfil `azure`.

## 7. Checklist de demo

- [ ] Login con Microsoft funciona en `http://localhost:4200`
- [ ] Header muestra roles (Admin / Operador / …)
- [ ] Llamada a `GET http://localhost:8080/api/bff/me` con token devuelve roles
- [ ] Sin token → **401**
- [ ] Token sin rol Admin en `/api/bff/report/kpis` → **403**
- [ ] `environment.demo = false` en el frontend
