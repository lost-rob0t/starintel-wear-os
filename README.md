# StarIntel Wear

Native standalone Wear OS client for StarIntel.

## What is included

- Modern Wear Compose Material 3 app.
- Dashboard:
  - total documents;
  - total people;
  - dynamic `dtype` totals;
  - dtype-specific icons.
- Search from the watch using Wear OS voice/keyboard input.
- Create StarIntel targets from the watch.
- Settings:
  - StarIntel HTTP/HTTPS endpoint;
  - auth mode;
  - bearer token.
- API token encrypted with an Android Keystore AES-256-GCM key.
- Wear OS 7 Widgets:
  - Overview;
  - Documents;
  - People;
  - Top dtype.
- Wear OS Tile fallback for older Wear OS devices.
- Watch-face complications:
  - Documents;
  - People.
- 15-minute cached stats refresh via WorkManager.
- GitHub Actions build that uploads an installable debug APK.

## Transport policy

The endpoint setting accepts both `http://` and `https://`. HTTPS is not enforced. This is intentional so the watch can talk to LAN IPs, local hostnames, lab servers, and public HTTP deployments. Android cleartext traffic is enabled for this purpose.

Bearer auth is allowed over either transport. When using HTTP, credentials and response contents are not protected by transport encryption.

## Expected StarIntel API contract

This starter intentionally isolates all server-specific behavior in:

`app/src/main/java/actor/starintel/wear/data/StarIntelApi.kt`

Default requests:

### Stats

`GET /api/v1/stats`

Accepted response shape:

```json
{
  "total_documents": 1200345,
  "total_people": 44321,
  "by_dtype": {
    "document": 900000,
    "person": 44321,
    "organization": 13002,
    "domain": 9133
  }
}
```

For compatibility, `documents`, `people`, and `dtypes` are also accepted aliases.

### Search

`GET /api/v1/search?q=<query>&limit=10`

Accepted response:

```json
{
  "results": [
    {
      "id": "doc:123",
      "title": "Example",
      "dtype": "document",
      "summary": "Short result summary",
      "url": "https://starintel.actor/document/123"
    }
  ]
}
```

A top-level JSON array is also accepted.

### Create target

`POST /api/v1/targets`

```json
{
  "name": "John Smith",
  "dtype": "person"
}
```

Expected response:

```json
{
  "id": "target:123",
  "name": "John Smith"
}
```

## Build

Requires JDK 17 and Android SDK 37.

```bash
./gradlew testDebugUnitTest assembleDebug
```

The bootstrap `gradlew` script downloads Gradle 9.5.0 and verifies its upstream SHA-256 checksum before executing it.

APK:

`app/build/outputs/apk/debug/app-debug.apk`

## Install on a watch

Enable developer options and wireless debugging on the watch, pair ADB, then:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or install the APK artifact produced by GitHub Actions.

## Server integration note

The watch client is ready, but the exact StarIntel server API names/schema still need to be reconciled against the server repo. Once the repo is supplied, update `StarIntelApi.kt` and add contract tests against the real server API before calling the build release-ready.

## Why native Wear OS instead of Agent Zero on-watch?

Agent Zero is a better *server-side agent/orchestration backend* if you want richer reasoning, natural-language command routing, or autonomous workflows.

It is not a better watch runtime. The watch should stay a thin native client:
- fast UI;
- low battery use;
- direct HTTP or HTTPS;
- Wear Tiles/Widgets/complications;
- voice/keyboard input;
- explicit authenticated actions.

A clean future architecture is:

`Wear OS -> StarIntel public API -> authenticated StarIntel actions -> optional Agent Zero / Auto-Dig`

That keeps privileged reasoning off the watch without losing access to it.
