# Empezar en cualquier PC: instalar, clonar y sacar SHA-1 y hash de clave

Guía principal para abrir este proyecto en una computadora donde no hay nada instalado, y dejar
funcionando **todos** los inicios de sesión (correo, Google, GitHub y Facebook).

> **La idea clave:** el SHA-1 (para Google) y el hash de clave (para Facebook) salen de la **llave de
> depuración de cada PC**. Cada computadora tiene la suya, así que **en una PC nueva hay que sacarlos y
> registrarlos otra vez**. Correo, GitHub y la base de datos funcionan sin esto.

Para adaptar el proyecto a otro nombre o tema, y crear Firebase desde cero, ver
[`guia-adaptar-y-conectar.md`](guia-adaptar-y-conectar.md).

---

## Resumen en 10 pasos

1. [Instalar Git y Android Studio](#1-instalar-lo-necesario)
2. [Crear el emulador con Google Play](#2-crear-el-emulador)
3. [Clonar el repositorio](#3-clonar-el-repositorio)
4. [Poner los dos archivos de credenciales](#4-poner-los-archivos-de-credenciales)
5. [Abrir y ejecutar el proyecto](#5-abrir-y-ejecutar)
6. [Sacar el SHA-1 y el hash de clave](#6-sacar-el-sha-1-y-el-hash-de-clave)
7. [Registrar el SHA-1 en Firebase](#7-registrar-el-sha-1-en-firebase)
8. [Registrar el hash en Meta (Facebook)](#8-registrar-el-hash-en-meta-facebook)
9. [Probar los cuatro inicios de sesión](#9-probar)
10. Si algo falla: [errores frecuentes](#10-errores-frecuentes)

---

## 1. Instalar lo necesario

| Programa | Para qué | Dónde |
|---|---|---|
| **Git para Windows** | Clonar el repositorio (trae Git Bash) | https://git-scm.com/download/win |
| **Android Studio** | Editor, SDK, emulador y un JDK incluido | https://developer.android.com/studio |
| **Google Chrome** | Lo usan GitHub y Facebook al iniciar sesión | https://www.google.com/chrome |

No instales un JDK aparte: Android Studio ya trae uno.

Al abrir Android Studio por primera vez, deja que descargue el SDK. Luego: **More Actions → SDK Manager**
→ pestaña **SDK Platforms** → marca **Android 15.0 (API 35)** → **Apply**.

## 2. Crear el emulador

**More Actions → Virtual Device Manager → Create Device**.
- Dispositivo: **Pixel 8 Pro** (o similar).
- Imagen del sistema: una que tenga el **ícono de Google Play** en la columna *Play Store*. Sin Google
  Play no funciona "Continuar con Google".
- Una vez creado, ábrelo y entra a una **cuenta de Google** (Ajustes → Contraseñas y cuentas) para que el
  inicio de sesión con Google tenga una cuenta que usar.

## 3. Clonar el repositorio

En Git Bash:
```bash
cd C:/Users/TU_USUARIO/Desktop
git clone https://github.com/Markernan/firepractica.git
```

## 4. Poner los archivos de credenciales

> **Cómo se conecta la app con Firebase:** el `google-services.json` es el archivo que une la app con
> tu proyecto de Firebase. Dentro trae tu `project_id`, el paquete de la app y `firebase_url`, que es la
> **dirección de tu base de datos** (`https://TU-PROYECTO-default-rtdb.firebaseio.com`). No hay que
> configurar la base de datos en el código: con ese archivo en `app/` la app ya la encuentra.
>
> **Si el proyecto de Firebase ya existe** (los métodos de acceso, la base de datos y las reglas ya
> están creados), en una PC nueva **no repitas nada en la consola**: solo descarga este JSON y colócalo.
> Si el proyecto de Firebase todavía **no existe**, créalo primero con la sección 5 de
> [`guia-adaptar-y-conectar.md`](guia-adaptar-y-conectar.md).
>
> **No confundas los dos "JSON":**
>
> | Archivo | Para qué | Dónde se usa |
> |---|---|---|
> | `google-services.json` | Conecta la app con Firebase | Se copia a `app/` |
> | `database.rules.json` | Reglas de seguridad de la base de datos | Se **pega en la consola de Firebase**: Realtime Database → pestaña **Reglas** → **Publicar** |
>
> Las reglas se pegan **una sola vez por proyecto de Firebase** (paso 6 de la sección 5 de la guía de
> adaptación). Viven en Firebase, no en la PC: en una PC nueva con el mismo Firebase **no hay que volver
> a pegarlas**.

Dos archivos **no están en GitHub** (están en `.gitignore`) y hay que crearlos a mano:

| Archivo | Ruta dentro del proyecto |
|---|---|
| `google-services.json` | `app/google-services.json` |
| `facebook.xml` | `app/src/main/res/values/facebook.xml` |

**De dónde sacarlos (elige una forma):**

- **A. Desde el zip o pendrive de la otra PC:** copia los dos archivos a esas rutas.
- **B. Desde las consolas (lo más seguro):**
  - `google-services.json`: https://console.firebase.google.com → abre tu proyecto → pulsa el **engranaje ⚙**
    (arriba a la izquierda, junto a "Descripción general del proyecto") → **Configuración del proyecto**
    → baja a **Tus apps**, elige tu app Android → pulsa **google-services.json**. Se descarga en
    *Descargas*; cópialo a `app/` y, si ya había uno, reemplázalo.
    Para comprobarlo, ábrelo con el Bloc de notas: debe decir tu `project_id`, tu `package_name` y una
    línea `firebase_url` con `firebaseio.com`.
  - `facebook.xml`: copia `docs/plantillas/facebook.xml.example` a `app/src/main/res/values/facebook.xml`
    y reemplaza los tres valores:
    - `facebook_app_id`: Meta → tu app → **Configuración de la app → Básica → ID de la app**
    - `fb_login_protocol_scheme`: la letra `fb` seguida del ID, sin espacios
    - `facebook_client_token`: Meta → **Configuración de la app → Avanzada → Seguridad → Token de cliente**

> Si no hay Firebase todavía, mira la sección 5 de `guia-adaptar-y-conectar.md`.

## 5. Abrir y ejecutar

1. Android Studio → **Open** → elige la carpeta `firepractica` (la raíz, no `app`).
2. Espera la **sincronización de Gradle**: la primera vez descarga Gradle y tarda unos minutos.
   Android Studio crea solo `local.properties` con la ruta del SDK; no lo copies de otra PC.
3. Si aparece un error de JDK: **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
   y en **Gradle JDK** elige el que trae Android Studio (jbr 17 o 21).
4. Elige tu emulador y pulsa **Run ▶**.
5. **Ejecutar la app una vez crea la llave de depuración** de esta PC, que necesitas en el paso 6.

Ya puedes probar **correo y GitHub**. Para Google y Facebook sigue.

---

## 6. Sacar el SHA-1 y el hash de clave

### Forma rápida: el script (recomendada)

En la carpeta del proyecto hay un script que hace todo.

- **Doble clic** en `docs/scripts/obtener-huellas.bat`, o
- desde PowerShell, dentro de la carpeta del proyecto:
  ```powershell
  powershell -ExecutionPolicy Bypass -File docs\scripts\obtener-huellas.ps1
  ```

Muestra algo así. Copia los dos primeros valores:
```
SHA-1 (Firebase -> Add fingerprint):
  F9:10:AB:3C:58:6D:8D:...
HASH DE CLAVE (Meta -> Hashes de clave):
  +RCrPFhtjVdm5tO4MVOJIztA53c=
```
Si dice que no existe la llave, ejecuta la app una vez (paso 5) o usa el comando que el propio script
imprime para crearla.

### Forma manual, si el script no funciona

**SHA-1 desde Android Studio:** abre la pestaña **Gradle** (borde derecho) →
`firepractica → app → Tasks → android → signingReport` (doble clic). En la ventana de abajo busca la
variante **debug** y copia la línea `SHA1:`.

**SHA-1 desde la terminal** (PowerShell; ajusta la ruta de `keytool` si hace falta):
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```
Copia el valor de la línea `SHA1:`.

**Hash de clave** (es el SHA-1 pasado a base64). Con tu SHA-1 pegado entre las comillas:
```powershell
$sha1 = "PEGA_AQUI_TU_SHA1"
[Convert]::ToBase64String([byte[]]($sha1.Split(":") | ForEach-Object { [Convert]::ToByte($_,16) }))
```
o, en Git Bash directamente desde la llave:
```bash
keytool -exportcert -alias androiddebugkey -keystore "$USERPROFILE/.android/debug.keystore" -storepass android | openssl sha1 -binary | openssl base64
```

El hash tiene unos 28 caracteres y termina en `=`. Ejemplo de esta máquina:
`+RCrPFhtjVdm5tO4MVOJIztA53c=`.

---

## 7. Registrar el SHA-1 en Firebase

1. https://console.firebase.google.com → tu proyecto.
2. **⚙ → Configuración del proyecto → Tus apps** → tu app Android.
3. Baja a **Huellas digitales del certificado SHA** → **Add fingerprint** → pega el SHA-1 → **Guardar**.
   Puedes tener varias huellas (una por PC); no borres las anteriores.
4. **Vuelve a descargar `google-services.json`** (botón en esa misma pantalla) y reemplaza el de `app/`.
   Es necesario: el archivo nuevo incluye la llave de esta PC.
5. En Android Studio: **Run ▶** de nuevo.

## 8. Registrar el hash en Meta (Facebook)

1. https://developers.facebook.com/apps → tu app.
2. **Configuración de la app → Básica**, baja a la plataforma **Android**.
3. En **Hashes de clave** agrega el hash nuevo **sin borrar los anteriores** (acepta varios; usa el botón
   para añadir otra línea) → **Guardar cambios**.
4. Revisa en esa misma sección: **Nombre del paquete** y **Nombre de la clase de actividad
   predeterminada** deben ser los del proyecto (`com.pucp.firepractica` y
   `com.pucp.firepractica.auth.LoginActivity`).

---

## 9. Probar

| Prueba | Qué debe pasar |
|---|---|
| Registrarte con correo | Entra a "Mis Pronósticos"; aparece en Firebase → Authentication → Usuarios |
| Crear un pronóstico | Llega la notificación; aparece en Realtime Database → Datos |
| **Continuar con Google** | Elige tu cuenta y entra |
| **Continuar con GitHub** | Chrome → Authorize → vuelve a la app |
| **Continuar con Facebook** | Chrome → inicia sesión con **tu cuenta de Facebook** → vuelve a la app |

Chrome puede mostrar una pantalla de bienvenida la primera vez: elige **Stay signed out**.
La app de Facebook está en **modo desarrollo**: solo entra la cuenta que la creó o las agregadas en
**Roles → Probadores**.

---

## 10. Errores frecuentes

| Mensaje o síntoma | Causa probable | Solución |
|---|---|---|
| Google: *No credentials available* / `DEVELOPER_ERROR` | Falta el SHA-1 de **esta** PC, o no volviste a descargar el JSON | Pasos 7.3 y 7.4 |
| Facebook: *invalid key hash* | Falta el hash de **esta** PC en Meta | Paso 8 |
| Facebook: *Invalid Scopes: email* | El permiso `email` no está habilitado | La app ya pide solo `public_profile` |
| Facebook: no deja entrar | Modo desarrollo | Usa tu cuenta (la que creó la app) o agrégate como probador |
| `No matching client found for package name` | El `google-services.json` es de otro paquete | Descarga el JSON de la app correcta |
| `Unresolved reference` / falta `facebook_app_id` | No existe `facebook.xml` | Paso 4 |
| Compila pero Google muestra una cuenta y falla | El emulador no tiene cuenta de Google | Paso 2 |
| `Incompatible Gradle JVM` | JDK incorrecto | Paso 5.3 |
| El script dice que no existe la llave | Aún no ejecutaste la app en esta PC | Ejecuta la app (paso 5) o usa el comando que imprime el script |

---

## Atajo: copiar la llave de la otra PC (evita los pasos 6 a 8)

Si copias `C:\Users\TU_USUARIO\.android\debug.keystore` de la PC donde ya está todo registrado a la
**misma ruta** en la PC nueva (antes de abrir Android Studio la primera vez), esa PC usará la misma
llave: mismo SHA-1 y mismo hash, y no hace falta registrar nada. Es una llave de depuración, no de
producción.
