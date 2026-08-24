# Galaxy Watch5 Pro build

The `compat` flavor is the installable build for Galaxy Watch5 Pro / Wear OS 6.

It deliberately excludes the Wear OS 7 Remote Compose Widget runtime while retaining:

- the StarIntel Wear app;
- HTTP and HTTPS endpoints;
- stats;
- search;
- target creation;
- authentication/settings;
- Tiles;
- document and people complications.

Build:

```bash
./gradlew assembleCompatDebug
```

APK:

```text
app/build/outputs/apk/compat/debug/app-compat-debug.apk
```

The separate `wear7` flavor owns the experimental Wear OS 7 Widget implementation so those large dependencies do not inflate the Watch5 Pro package.
