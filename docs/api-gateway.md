# AWS API Gateway (HTTP API) + JWT Authorizer — AndesStay

El flujo del caso es siempre:

```text
SPA (MSAL) → Bearer JWT → API Gateway → ms-andesstay-bff → microservicios
```

## 1. Crear el HTTP API

1. AWS Console → **API Gateway** → **Create API** → **HTTP API**
2. Name: `andesstay-gateway`
3. Create

## 2. Integración con el BFF (EC2 / Docker)

1. **Integrations** → **Add integration** → **HTTP**
2. URI: `http://<EC2-public-or-internal>/` (el BFF publica 8080→80)
3. **Routes**:
   - `ANY /{proxy+}` → integración BFF
4. **Stages** → `$default` (o stage `prod`)

## 3. JWT Authorizer (Entra ID)

1. **Authorization** → **Create Authorizer** → **JWT**
2. Name: `andesstay-entra`
3. Identity source: `$request.header.Authorization`
4. Issuer URL:

   ```text
   https://login.microsoftonline.com/<TENANT_ID>/v2.0
   ```

5. Audience:

   ```text
   api://<API_CLIENT_ID>
   ```

   (el Application ID URI de AndesStay-API)

6. Asignar el authorizer a la ruta `ANY /{proxy+}`

## 4. Security Groups (EC2)

| Instancia | Abrir hacia | Puertos |
|-----------|-------------|---------|
| ec2-apps (BFF + servicios) | Subred / API Gateway | 80 (o 8080 interno) |
| ec2-apps | Estudiantes / docente (demo) | 22 SSH restringido |
| ec2-mq (futuro) | Solo ec2-apps | 5672 AMQP, 15672 mgmt |
| ec2-kafka (futuro) | Solo ec2-apps | 9092 |

No exponer bases de datos Oracle ni Kafka al público.

## 5. Variables de entorno del BFF en EC2

```bash
AZURE_ISSUER_URI=https://login.microsoftonline.com/<TENANT_ID>/v2.0
AZURE_AUDIENCE=api://<API_CLIENT_ID>
ANDESSTAY_CORS_ORIGINS=http://localhost:4200
```

## 6. Comprobaciones del rubro (40% BFF)

| Prueba | Esperado |
|--------|----------|
| Request sin `Authorization` | 401 desde Gateway o BFF |
| Token expirado / firma inválida | 401 |
| Audience incorrecta | 401 |
| Token válido, rol insuficiente | 403 |
| Token + rol correcto | 200 |

## 7. Plantilla compose (EC2 apps)

Ver `infra/apps/compose.yml`. Para EP1 basta con el BFF; los microservicios se levantan en el mismo host con puertos internos.
