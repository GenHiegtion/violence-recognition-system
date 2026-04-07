# VRS Frontend (React + Vite)

Frontend for:
- Register/Login
- Session-based access via bearer token
- Violence pattern management (create/list/update/delete)
- Active threshold visualization

## Run

```bash
npm install
npm run dev
```

Default app URL: `http://localhost:5173`

## Backend integration

The frontend calls API paths under `/api/*`.
In development, Vite proxies these calls to the gateway at:

- `http://localhost:8080` by default
- or `VITE_PROXY_TARGET` if provided

Optional environment values:

```bash
VITE_PROXY_TARGET=http://localhost:8080
VITE_API_BASE_URL=
```

`VITE_API_BASE_URL` is optional. Leave it empty for relative API paths with proxy.

## Payload mapping

Auth APIs:
- `POST /api/auth/register`
  - Request: `{ username, fullName, password }`
  - Response: `{ id, username, fullName, role, createdAt }`
- `POST /api/auth/login`
  - Request: `{ username, password }`
  - Response: `{ token, tokenType, issuedAt, user }`
- `POST /api/auth/logout`
  - Response: `{ loggedOut: boolean }`

User APIs:
- `GET /api/users/me` → current user profile

Pattern APIs:
- `GET /api/patterns` → `VioPatternResponse[]`
- `POST /api/patterns` with `VioPatternRequest`
- `PUT /api/patterns/{id}` with `VioPatternRequest`
- `DELETE /api/patterns/{id}`
- `GET /api/patterns/thresholds` → `Record<string, number>`

`VioPatternRequest` shape:

```json
{
  "name": "Fight",
  "sevLevel": 2,
  "threshold": 0.65,
  "file": "weights/fight.pt"
}
```

## Build checks

```bash
npm run lint
npm run build
```
