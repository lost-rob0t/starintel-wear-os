# StarIntel Wear API Contract

The watch distinguishes public and privileged operations.

## Public-capable

### `GET /api/v1/stats`

Returns aggregate counts safe to display on a public watch surface.

Required fields:

- `total_documents: integer`
- `total_people: integer`
- `by_dtype: object<string, integer>`

Recommended additions:

- `updated_at`
- `server_version`
- `ingest_rate_1h`
- `documents_24h`
- `active_ingestors`
- `search_latency_p50_ms`

### `GET /api/v1/search`

Query parameters:

- `q`
- `limit`

The watch only needs compact result cards. Avoid returning full document bodies.

## Authenticated

### `POST /api/v1/targets`

Bearer authorization required.

Request:

```json
{
  "name": "John Smith",
  "dtype": "person"
}
```

The server is authoritative for:
- authorization;
- validation;
- target normalization;
- duplicate handling;
- audit events;
- rate limits.

The watch must not emulate those policies locally.

## Auth

Initial client support is Bearer auth:

`Authorization: Bearer <token>`

Public stats/search can run with auth mode `none` if the server permits them.

Privileged operations fail closed when auth is not configured.

## Transport policy

The client accepts both `http://...` and `https://...`.

There is intentionally **no HTTPS-only enforcement**. This supports LAN IPs, local hostnames, development servers, and public HTTP deployments. Android cleartext traffic is enabled in the application manifest.

Bearer authentication is allowed over HTTP as well as HTTPS. When HTTP is selected, transport encryption is absent and credentials/response contents can be observed by the network.
