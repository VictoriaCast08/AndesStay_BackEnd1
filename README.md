# AndesStay — Backend (EP1)

Monorepo Spring Boot del caso **AndesStay** (DSY1107). Flujo seguro del encargo:

```text
JWT (Microsoft Entra ID) → AWS API Gateway (JWT Authorizer) → ms-andesstay-bff → microservicios de dominio
```

## Estructura

| Carpeta | Servicio | Puerto local | Rol |
|---------|----------|--------------|-----|
| `bff/` | ms-andesstay-bff | 8080 | Valida JWT (issuer, audience, firma, roles) |
| `ms-andesstay-reservations/` | Reservas | 8082 | CRUD y estados de reserva |
| `ms-andesstay-catalog/` | Catálogo | 8083 | Unidades y disponibilidad |
| `ms-andesstay-notify/` | Notificaciones | 8084 | Consumer RabbitMQ (esqueleto) |
| `ms-andesstay-audit/` | Auditoría | 8085 | Timeline (lectura) |
| `ms-andesstay-report/` | Reportería | 8086 | KPIs |
| `docs/` | Guías | — | Entra ID + API Gateway |
| `infra/` | Compose | — | Despliegue EC2 (plantilla) |

## Requisitos

- Java 21+
- Maven 3.9+
- Node no aplica (frontend en otro repo)

## Perfiles comunes

Cada servicio Spring Boot:

- **`azure`** (por defecto): valida JWT de Entra ID (`AZURE_ISSUER_URI`, `AZURE_AUDIENCE`)
- **`local`**: sin validación JWT, BD H2 en memoria — para desarrollo y pruebas

## BD

- Perfil **local**: H2 en memoria con datos de ejemplo (reservas, unidades, eventos)
- Perfil **azure**: properties Oracle Autonomous rellenables en `.env` (ver `docs/entra-id-setup.md`)

## Ejecutar (desarrollo local)

```powershell
# BFF
cd bff
mvn spring-boot:run "-Dspring-boot.run.profiles=local"

# Reservas
cd ms-andesstay-reservations
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Repite para catalog / notify / audit / report.

## Pruebas

```powershell
cd bff && mvn test
cd ms-andesstay-reservations && mvn test
# ...
```

## Entrega

Repositorio: `https://github.com/DsDekoV02/AndesStay_BackEnd`  
Frontend: `https://github.com/DsDekoV02/AndesStay_FrontEnd`

Pegar ambos enlaces en AVA y copia al correo del docente según el encargo EP1.

## Pendiente en Azure / AWS

1. App Registration (SPA + API + App Roles) → ver `docs/entra-id-setup.md`
2. API Gateway HTTP API + JWT Authorizer → ver `docs/api-gateway.md`
3. Rellenar `.env` / `environment.ts` del frontend con los IDs reales
4. `environment.demo = false` en el frontend antes de la demo con Entra
