# firepractica 

App Android nativa (Java, minSdk 34) con Firebase Authentication y Realtime Database para registrar y administrar pronósticos de partidos del Mundial.


Estructura en Firebase: `pronosticos/{uid}/{id}` con `seleccionA`, `seleccionB`, `fechaPartido` (ms), `golesA`, `golesB`, `estado`, `creadoEn`.

> **Documentación** (en `docs/`):
> - **[`00-empezar-en-cualquier-pc.md`](docs/00-empezar-en-cualquier-pc.md)** — instalar, clonar y sacar el SHA-1 y el hash de clave en cualquier PC (incluye un script).
> - [`guia-adaptar-y-conectar.md`](docs/guia-adaptar-y-conectar.md) — adaptar el proyecto a otro nombre/tema y crear Firebase desde cero.

> **Credenciales fuera de Git:** `app/google-services.json` y `app/src/main/res/values/facebook.xml`
> están en `.gitignore`. Si clonas el proyecto, créalos con las plantillas de `docs/plantillas/`
> (detalle en la sección 12 de la guía).

## Configurar Firebase (una sola vez)

El archivo `app/google-services.json` incluido es un **marcador**. Hay que reemplazarlo por el de tu proyecto:

1. En https://console.firebase.google.com crea un proyecto.
2. Agrega una app Android con el paquete **`com.pucp.firepractica`** y el **SHA-1** de tu llave de depuración
   (`./gradlew signingReport`, variante `debug`).
3. **Authentication → Sign-in method**: habilita **Correo/contraseña**, **Google**, **GitHub** y **Facebook**.
   - GitHub: crea una *OAuth App* en GitHub y pega *Client ID/Secret* en Firebase; copia la *callback URL* de Firebase en GitHub.
   - Facebook: crea una app en Meta for Developers y pega *App ID/Secret* en Firebase; copia la *OAuth redirect URI* de Firebase en Facebook.
4. **Realtime Database → Crear base de datos**, y en la pestaña **Reglas** pega el contenido de `database.rules.json` y publica.
5. Descarga el `google-services.json` **después** de habilitar Google (así incluye el cliente web) y reemplaza `app/google-services.json`.
6. Abre la carpeta en Android Studio (Gradle JDK 17 o 21) y ejecuta.

## Probar sin credenciales (Firebase Emulator Suite)

Sirve para revisar correo/contraseña y la base de datos sin crear un proyecto (Google/GitHub/Facebook no funcionan en el emulador).

```bash
npm install -g firebase-tools
firebase emulators:start --only auth,database --project demo-firepractica
./gradlew assembleDebug -PuseEmulator=true
```

El emulador de Android alcanza el equipo anfitrión en `10.0.2.2`.
# lab6_20221747
