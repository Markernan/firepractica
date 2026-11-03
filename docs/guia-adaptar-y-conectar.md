# Guía: adaptar este proyecto a otro nombre/tema y conectarlo a Firebase

Esta app (pronósticos del Mundial) funciona como **plantilla**: pantalla de inicio de sesión con
correo, Google, GitHub y Facebook; lista con crear/editar/eliminar en tiempo real; estadísticas con
gráficos. Esta guía explica qué cambiar para llevarla a otro proyecto y cómo crear y conectar Firebase
desde cero.

> **¿Vas a abrir el proyecto en otra PC?** Empieza por [`00-empezar-en-cualquier-pc.md`](00-empezar-en-cualquier-pc.md): ahí está cómo sacar el SHA-1 y el hash de clave de esa PC (con script) y registrarlos.

Convenciones: `<PAQUETE>` es el nombre de paquete nuevo (ej. `com.tunombre.miapp`),
`<ID_PROYECTO>` es el ID del proyecto de Firebase (ej. `miapp-1a2b3`).

---

## 1. ¿Reutilizar lo que ya está o empezar de cero?

 adáptala (secciones 3 y 4).
:

| Pieza | ¿Se reutiliza? |
|---|---|
| Inicio de sesión, registro, recuperar contraseña (`auth/`) | Sí, solo cambia el paquete y los textos |
| Google / GitHub / Facebook en `LoginActivity` | Sí, no cambia código; cambia la configuración en consolas |
| Barra de navegación y `MainActivity` | Sí, cambia el nombre de las pestañas |
| Estructura repositorio + adaptador + formulario | Sí como patrón; los campos cambian según tu tema |
| Gráficos `BarChartView` / `PieChartView` | Sí, reciben etiquetas, valores y colores |
| Reglas de seguridad | Se adaptan al nuevo nombre de datos |

Lo que **cambia según el tema**: el modelo (`Pronostico`), sus campos, los textos y los layouts del
formulario y de la lista.

### Un solo proyecto de Firebase o uno por app

| Opción | Ventajas | Inconvenientes |
|---|---|---|
| **Mismo proyecto de Firebase** para varias apps (agregas otra app Android con su paquete) | No repites GitHub ni Facebook; los usuarios y la base se comparten | Las reglas y los datos de todas las apps conviven |
| **Un proyecto de Firebase por app** | Todo aislado | Repites la configuración y sumas URLs en GitHub y Meta |

Reglas prácticas:
- Cada app Android tiene su **propio paquete** y se registra aparte en Firebase (cada una con su `google-services.json`).
- **GitHub:** una misma OAuth App sirve para varios proyectos de Firebase; agrega cada URL de devolución en *Redirect URIs* (admite hasta 10).
- **Facebook:** una app de Meta tiene **un solo paquete Android**. Si el paquete cambia, crea otra app de Meta (o cambia el paquete registrado). La misma app de Meta sí admite varias URIs de OAuth.
- La **huella SHA-1** y el **hash de clave** pertenecen a tu PC (llave de depuración), no al proyecto; son los mismos para todos tus proyectos en este equipo.

---

## 2. Mapa de archivos

Raíz del proyecto:

| Archivo | Para qué sirve | ¿Se modifica al adaptar? |
|---|---|---|
| `settings.gradle` | Nombre del proyecto (`rootProject.name`) | Sí, el nombre |
| `build.gradle` (raíz) | Versiones de los plugins (AGP, google-services) | No |
| `gradle.properties` | Opciones de Gradle | No |
| `app/build.gradle` | `namespace`, `applicationId`, dependencias | Sí: `namespace` y `applicationId` |
| `app/google-services.json` | Conecta la app con Firebase | **Sí, uno nuevo por proyecto** |
| `database.rules.json` | Reglas de seguridad de Realtime Database | Sí, si cambia el nombre de los datos |
| `firebase.json`, `.firebaserc` | Solo para el emulador local de Firebase | Opcional (cambiar el ID de proyecto) |
| `.gitignore` | Lo que no se sube a Git | No |
| `docs/` | Esta guía | No |

`app/src/main/`:

| Archivo | Qué hace | ¿Se modifica? |
|---|---|---|
| `AndroidManifest.xml` | Pantallas, permisos, datos de Facebook | Revisar `android:name` si cambias clases |
| `java/.../MundialApp.java` | Inicializa Firebase y guarda la instancia única de la base de datos | Solo el nombre de la clase (opcional) |
| `java/.../auth/LoginActivity.java` | Login: correo, Google, GitHub, Facebook, recuperar contraseña | No (solo el paquete) |
| `java/.../auth/RegistroActivity.java` | Crear cuenta con correo | No |
| `java/.../auth/AuthErrores.java` | Traduce errores de Firebase a español | No |
| `java/.../model/Pronostico.java` | Modelo de datos | **Sí, según tu tema** |
| `java/.../data/PronosticoRepository.java` | Crear, leer, actualizar y eliminar en Firebase | **Sí: ruta y reglas de negocio** |
| `java/.../ui/MainActivity.java` | Barra de navegación y cierre de sesión | Textos |
| `java/.../ui/PronosticosFragment.java` | Lista en tiempo real, eliminar con confirmación | Sí, nombres |
| `java/.../ui/PronosticoAdapter.java` | Dibuja cada elemento de la lista | Sí, campos |
| `java/.../ui/PronosticoFormActivity.java` | Formulario de crear/editar | **Sí, campos y validaciones** |
| `java/.../ui/EstadisticasFragment.java` | Cálculo y filtro de estadísticas | Sí, qué se cuenta |
| `java/.../ui/BarChartView.java`, `PieChartView.java` | Gráficos dibujados a mano | No |
| `java/.../util/Notificaciones.java` | Notificaciones del sistema | Textos |
| `java/.../util/Fechas.java` | Formato de fechas | No |
| `res/layout/activity_login.xml`, `activity_registro.xml`, `dialog_recuperar.xml` | Pantallas de acceso | Solo el diseño si lo quieres |
| `res/layout/activity_main.xml`, `fragment_*.xml`, `item_*.xml`, `activity_pronostico_form.xml` | Pantallas de la app | Sí, según tus campos |
| `res/menu/menu_principal.xml` | Las 3 opciones de la barra inferior | Textos/íconos |
| `res/values/strings.xml` | **Todos los textos** de la app | **Sí** |
| `res/values/colors.xml`, `themes.xml` | Colores y tema | Sí, para otra identidad visual |
| `res/values/facebook.xml` | ID y token de cliente de Facebook | **Sí, uno nuevo por app de Meta** |
| `res/drawable/ic_logo_mundial.xml` | Logo (también ícono de la app) | Sí, para tu logo |

Otros: `app/src/debug/AndroidManifest.xml` (permite http para el emulador local de Firebase, solo en
depuración) y `proguard-rules.pro` (no se usa).

---

## 3. Renombrar el proyecto (paquete, título, colores)

### 3.1 Nombre de paquete

1. En Android Studio, en el panel **Project**, pulsa el engranaje y desmarca **Compact Middle Packages**.
2. Clic derecho sobre la carpeta del paquete (la última, ej. `firepractica`) → **Refactor → Rename**
   → **Rename package**. Acepta y confirma en **Do Refactor**.
3. Abre `app/build.gradle` y cambia **a mano**:
   ```gradle
   namespace '<PAQUETE>'
   ...
   applicationId "<PAQUETE>"
   ```
4. Si usas el emulador local, cambia el ID de proyecto en `.firebaserc`.
5. **Importante:** el paquete nuevo se registra en Firebase (sección 5, paso 4) y se descarga un
   `google-services.json` nuevo. Si el paquete no coincide con el del JSON, la compilación falla con
   `No matching client found for package name`.
6. Si usas Facebook: actualiza el **Nombre del paquete** y la **clase** en Meta (sección 7).

### 3.2 Título y textos

- `res/values/strings.xml`: `app_name` (nombre bajo el ícono), `login_titulo`, `est_titulo`,
  `nav_pronosticos`, `nav_estadisticas`, `pronosticos_titulo` y los demás textos del tema.
- `AndroidManifest.xml`: `android:label="@string/app_name"` ya usa `app_name`.
- Nombre del proyecto en el menú de Android Studio: `settings.gradle` → `rootProject.name`.

### 3.3 Colores e identidad visual

- `res/values/colors.xml`: `verde_cancha` (color principal), `verde_oscuro` (barra superior),
  `dorado` (acento) y los de estados (`estado_pendiente`, `estado_acertado`, `estado_fallado`).
- `res/values/themes.xml`: el tema `Theme.Mundial` toma esos colores. Si lo renombras, cámbialo también
  en `AndroidManifest.xml` (`android:theme`).
- Logo: reemplaza `res/drawable/ic_logo_mundial.xml` (o crea el tuyo y cambia las referencias en
  `activity_login.xml`, `activity_registro.xml`, `Notificaciones.java` y `AndroidManifest.xml`).

---

## 4. Cambiar el tema de los datos (de "pronósticos" a otra cosa)

Ejemplo: de `Pronostico` (selección A/B, fecha, goles, estado) a `Tarea` (título, fecha, prioridad, estado).

1. **Modelo** — `model/Pronostico.java`: renombra la clase y cambia los campos y sus getters/setters.
   Mantén el constructor vacío (Firebase lo exige) y los métodos `@Exclude` para `id`.
2. **Repositorio** — `data/PronosticoRepository.java`:
   - cambia la ruta `getReference("pronosticos")` por la tuya (`"tareas"`);
   - la regla "solo se edita/elimina si está Pendiente" está en `actualizar` y `eliminar`; ajústala o quítala.
3. **Reglas de seguridad** — `database.rules.json`: renombra `pronosticos` y actualiza los campos
   en `.validate`. Vuelve a publicarlas en Firebase (sección 5, paso 6).
4. **Formulario** — `ui/PronosticoFormActivity.java` y `res/layout/activity_pronostico_form.xml`:
   agrega, quita o cambia campos y sus validaciones.
5. **Lista** — `ui/PronosticoAdapter.java` y `res/layout/item_pronostico.xml`: muestra tus campos.
6. **Estadísticas** — `ui/EstadisticasFragment.java`: cambia qué se cuenta y cómo se filtra.
7. **Textos** — `strings.xml`: textos del formulario, avisos y notificaciones.

Para renombrar las clases usa **Refactor → Rename** (Shift + F6) y no el explorador de archivos: así
Android Studio actualiza todas las referencias y los archivos `layout` que generan los `Binding`.

---

## 5. Crear y conectar Firebase desde cero

### Paso 0 — Requisitos (una sola vez)
- Cuenta de Google con la **verificación en 2 pasos** activada (myaccount.google.com → Seguridad).
  Firebase la exige.

### Paso 1 — Crear el proyecto
1. Entra a https://console.firebase.google.com
2. **Crear un proyecto** → ponle nombre → marca **Acepto las condiciones de Firebase** → **Continuar**.
3. Apaga Google Analytics → **Crear proyecto** → **Continuar**.
4. Anota el **ID del proyecto** (en Configuración del proyecto, engranaje ⚙).

### Paso 2 — Obtener el SHA-1 de tu PC
Opción A, en Android Studio: pestaña **Gradle** → `app → Tasks → android → signingReport` y copia el
`SHA1` de la variante `debug`.

Opción B, por terminal (Git Bash, ajusta la ruta del JDK):
```bash
"C:/Program Files/Eclipse Adoptium/jdk-21.0.2.13-hotspot/bin/keytool.exe" -list -v -keystore "$USERPROFILE/.android/debug.keystore" -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

### Paso 3 — Registrar la app Android
1. En el proyecto, pulsa el ícono de **Android**.
2. **Nombre del paquete:** el `applicationId` de `app/build.gradle`.
3. **SHA-1:** el del paso 2.
4. **Registrar app**. No hace falta seguir los pasos del SDK: ya están en el proyecto.

Si ya la registraste sin SHA-1: **⚙ → Configuración del proyecto → Tus apps → Add fingerprint**.

### Paso 4 — Activar los métodos de inicio de sesión
Menú izquierdo: **Seguridad → Authentication → Comenzar → Método de acceso**.

- **Correo electrónico/contraseña:** **Habilitar** el primer interruptor → **Guardar**.
- **Google:** **Agregar proveedor nuevo → Google → Habilitar**, elige tu correo de asistencia → **Guardar**.
- **GitHub** y **Facebook:** secciones 6 y 7. Cada uno muestra una **URL de devolución** que copiarás a su consola.

La URL de devolución de todos los proveedores es:
```
https://<ID_PROYECTO>.firebaseapp.com/__/auth/handler
```

### Paso 5 — Crear la base de datos
1. **Bases de datos y almacenamiento → Realtime Database → Crear base de datos**.
2. Ubicación por defecto (Estados Unidos) → **Siguiente**.
3. **Iniciar en modo bloqueado** → **Habilitar**.

### Paso 6 — Publicar las reglas (el código que se pega en Realtime Database)

Al crear la base, Firebase muestra una pestaña **Reglas** con un código que bloquea todo. Hay que
reemplazarlo por el de este proyecto, que está en el archivo `database.rules.json` (en la raíz del
proyecto, junto a `settings.gradle`).

1. En Firebase: **Bases de datos y almacenamiento → Realtime Database → pestaña Reglas**.
2. Haz clic dentro del editor y borra todo (`Ctrl + A`, `Supr`).
3. Abre `database.rules.json` en el Bloc de notas o en Android Studio, copia **todo** su contenido
   (`Ctrl + A`, `Ctrl + C`) y pégalo en el editor de Firebase (`Ctrl + V`).
4. Pulsa **Publicar**. Debe aparecer un aviso de que se publicaron las reglas.

Qué hacen: cada usuario solo lee y escribe sus propios datos, valida los campos y, una vez que un
elemento deja de estar `Pendiente`, no se puede modificar ni borrar.

**Cuándo repetirlo:** solo cuando creas un proyecto de Firebase nuevo, o si cambias el nombre de los datos
(por ejemplo `pronosticos` por `tareas`). Las reglas viven en Firebase, no en la PC: **abrir el proyecto
en otra computadora no exige volver a pegarlas**. Si ves `Permission denied` al guardar, casi siempre es
porque las reglas no están publicadas o tienen otro nombre de datos.

### Paso 7 — Descargar el google-services.json
Hazlo **después** de activar Google (así incluye el cliente web) y de crear la base de datos (así
incluye `firebase_url`).

1. **⚙ → Configuración del proyecto → Tus apps → google-services.json**.
2. Cópialo a `app/` y reemplaza el que existe.
3. Compruébalo con el Bloc de notas: debe tener tu `project_id`, tu `package_name` y una línea
   `firebase_url` con `firebaseio.com`. Si dice `demo-...`, es el marcador viejo.

### Paso 8 — Ejecutar
1. Abre la carpeta raíz en Android Studio.
2. **File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**: elige 17 o 21.
3. Elige un emulador **con Google Play** (por ejemplo Pixel 8 Pro) → **Run ▶**.

---

## 6. Inicio de sesión con GitHub

1. En Firebase: **Agregar proveedor nuevo → GitHub → Habilitar**. Deja el panel abierto: muestra
   *ID de cliente*, *Secreto del cliente* y la URL de devolución.
2. En GitHub: https://github.com/settings/developers → **OAuth Apps → New OAuth app**.
3. Completa:
   - **Application name:** el nombre que quieras.
   - **Homepage URL:** cualquier dirección válida, por ejemplo `https://<ID_PROYECTO>.firebaseapp.com` (GitHub no la verifica).
   - **Redirect URI** (antes llamado "Authorization callback URL"): `https://<ID_PROYECTO>.firebaseapp.com/__/auth/handler`. Debe ser **idéntica** a la de Firebase.
   - Deja sin marcar *Allow wildcard matching* y *Enable Device Flow*.
4. **Register application** → copia el **Client ID** → **Generate a new client secret** y cópialo (se muestra una sola vez).
5. Pega Client ID y secreto en Firebase → **Guardar**.

Para otro proyecto de Firebase con la misma OAuth App: en GitHub agrega otra URL con **Add redirect URI**.

---

## 7. Inicio de sesión con Facebook

> Firebase **no permite** el flujo web genérico para Facebook (provoca un cierre de la app con el mensaje
> *"Sign in with Facebook is not supported via this method"*). Por eso el proyecto usa el **SDK oficial
> de Facebook** (`com.facebook.android:facebook-login`).

### 7.1 Crear la app en Meta
1. https://developers.facebook.com → **Empezar** → inicia sesión y completa el registro de desarrollador.
2. **Mis apps → Crear app** → nombre y correo.
3. Caso de uso: **Autenticar y solicitar datos de usuarios con inicio de sesión con Facebook**.
   Si pide una empresa, elige no vincularla. **Crear app**.
4. **Configuración de la app → Básica:** copia el **ID de la app** y la **Clave secreta**.

### 7.2 Conectar con Firebase
1. En Firebase: **Agregar proveedor nuevo → Facebook → Habilitar**. Pega el **ID de la app** y la **Clave secreta**.
2. Copia la **URI de redirección de OAuth** que Firebase muestra y **Guardar**.
3. En Meta: **Casos de uso → Personalizar → Configuración de Facebook Login → URI de redireccionamiento de OAuth válidos**: pega esa URI → **Guardar cambios**.

### 7.3 Registrar la app Android en Meta (obligatorio)
**Configuración de la app → Básica → Añadir plataforma → Android → tienda: Google Play** y completa:

| Campo | Valor |
|---|---|
| Nombre del paquete | `<PAQUETE>` |
| Nombre de la clase de actividad predeterminada | `<PAQUETE>.auth.LoginActivity` |
| Hashes de clave | el *key hash* de tu PC (sección 8) |

Sin esto el login falla con *"invalid key hash"*.

### 7.4 Token de cliente
1. En Meta: **Configuración de la app → Avanzada → Seguridad → Token de cliente** → cópialo.
2. En el proyecto: `res/values/facebook.xml`:
   ```xml
   <string name="facebook_app_id">ID_DE_LA_APP</string>
   <string name="fb_login_protocol_scheme">fbID_DE_LA_APP</string>
   <string name="facebook_client_token">TOKEN_DE_CLIENTE</string>
   ```
   (`fb_login_protocol_scheme` es la letra `fb` seguida del ID de la app, sin espacios.)

### 7.5 Cosas a saber
- La app de Meta nace en **modo desarrollo**: solo entran la cuenta que la creó y las que agregues en
  **Roles → Probadores**. Alcanza para pruebas y entrega; no hace falta publicarla.
- El código pide solo `public_profile`. Pedir `email` exige habilitar ese permiso en
  **Casos de uso → Personalizar → Permisos y funciones**; si no, Meta responde *"Invalid Scopes: email"*.

---

## 8. Hash de clave de Facebook (a partir del SHA-1)

El *key hash* es tu SHA-1 pasado a base64. Con Git Bash:
```bash
"C:/Program Files/Eclipse Adoptium/jdk-21.0.2.13-hotspot/bin/keytool.exe" -exportcert -alias androiddebugkey -keystore "$USERPROFILE/.android/debug.keystore" -storepass android | openssl sha1 -binary | openssl base64
```
Para esta máquina el resultado es `+RCrPFhtjVdm5tO4MVOJIztA53c=` (el SHA-1 correspondiente es
`F9:10:AB:3C:58:6D:8D:57:66:E6:D3:B8:31:53:89:23:3B:40:E7:77`).

---

## 9. Lista de comprobación para un proyecto nuevo

- [ ] Copiar la carpeta y renombrarla; ajustar `rootProject.name` en `settings.gradle`
- [ ] Cambiar el nombre de paquete (3.1) y `namespace`/`applicationId`
- [ ] Cambiar `app_name` y textos en `strings.xml`; colores y logo
- [ ] Adaptar modelo, repositorio, formulario, lista, estadísticas y reglas (sección 4)
- [ ] Crear proyecto de Firebase y registrar la app con el paquete nuevo + SHA-1
- [ ] Activar Correo/contraseña y Google; crear Realtime Database; publicar reglas
- [ ] Descargar el `google-services.json` nuevo y reemplazarlo en `app/`
- [ ] GitHub: nueva URL de devolución en la OAuth App; pegar ID y secreto en Firebase
- [ ] Facebook: URI en Meta, plataforma Android (paquete, clase, hash), token de cliente, probadores
- [ ] Ejecutar y probar: registro por correo, crear/editar/eliminar, estadísticas, Google, GitHub, Facebook
- [ ] Verificar en la consola: **Authentication → Usuarios** y **Realtime Database → Datos**

---

## 10. Errores frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| `No matching client found for package name` al compilar | El `applicationId` no coincide con el `google-services.json` | Registrar el paquete en Firebase y descargar otro JSON |
| La app se cierra al pulsar Facebook con *"not supported via this method"* | Se usó el flujo genérico de Firebase para Facebook | Usar el SDK de Facebook (ya implementado aquí) |
| Facebook: *Invalid Scopes: email* | El permiso `email` no está habilitado en Meta | Pedir solo `public_profile` o habilitar `email` en Meta |
| Facebook: *invalid key hash* | Falta o está mal la plataforma Android en Meta | Revisar paquete, clase y hash (7.3) |
| Facebook: "app no activa" o no deja entrar | Modo desarrollo | Entrar con la cuenta creadora o agregar probadores |
| Google: *No credentials available* / `DEVELOPER_ERROR` | Falta el SHA-1 o no se re-descargó el JSON tras activar Google | Agregar SHA-1 y descargar el JSON de nuevo |
| GitHub: `redirect_uri_mismatch` | La Redirect URI no es idéntica a la de Firebase | Copiarla desde el panel de GitHub en Firebase |
| `Permission denied` al guardar | Reglas sin publicar o con otro nombre de datos | Publicar `database.rules.json` ajustado |
| Lista vacía sin errores | El JSON no tiene `firebase_url` | Crear la base y descargar el JSON de nuevo |
| Gradle: *Incompatible Gradle JVM* | JDK demasiado nuevo | Gradle JDK 17 o 21 |
| Error de compilación `Unexpected character` en un `.gradle` | Archivo guardado con BOM | Guardarlo como UTF-8 sin BOM |

---

## 11. Probar sin crear un proyecto (emulador local de Firebase)

Sirve para revisar correo/contraseña y la base de datos sin consola (Google/GitHub/Facebook no funcionan):

```bash
npm install -g firebase-tools
firebase emulators:start --only auth,database --project demo-firepractica
./gradlew assembleDebug -PuseEmulator=true
```
El emulador de Android alcanza tu PC en `10.0.2.2`. Cambia `demo-firepractica` por el ID de `.firebaserc`.

---

## 12. Archivos de credenciales (no se suben a Git)

Estos dos archivos están en `.gitignore`: existen en tu PC pero **no viajan a GitHub**. Quien clone el
proyecto (o tú en otro equipo) debe crearlos antes de compilar; si faltan, la compilación falla.

| Archivo | Ruta | Qué contiene | De dónde sale | Plantilla |
|---|---|---|---|---|
| `google-services.json` | `app/google-services.json` | Conexión con tu proyecto de Firebase y clave de API | Firebase → ⚙ Configuración del proyecto → Tus apps → **google-services.json** | `docs/plantillas/google-services.json.example` |
| `facebook.xml` | `app/src/main/res/values/facebook.xml` | ID de la app y **token de cliente** de Facebook | Meta for Developers (ID: Configuración de la app → Básica; token: Avanzada → Seguridad → Token de cliente) | `docs/plantillas/facebook.xml.example` |

**Cómo crearlos en un clon nuevo**
1. `google-services.json`: descárgalo desde Firebase (sección 5, paso 7) y cópialo a `app/`.
2. `facebook.xml`: copia `docs/plantillas/facebook.xml.example` a
   `app/src/main/res/values/facebook.xml` (renombrándolo, sin `.example`) y completa los tres valores.
3. Si solo quieres compilar sin conectar Facebook, deja el ejemplo con valores de relleno: la app compila, pero ese botón no iniciará sesión.

**Si ya los habías subido a Git** (antes de agregarlos a `.gitignore`), hay que sacarlos del seguimiento:
```bash
git rm --cached app/google-services.json app/src/main/res/values/facebook.xml
git commit -m "Quitar credenciales del repositorio"
```
Los archivos siguen en tu PC; solo dejan de versionarse. Aun así quedan en el historial anterior: si el
repositorio es público, regenera el token de cliente en Meta y restringe la clave de API en Google Cloud.

**Otras reglas de seguridad**
- Los **secretos** (Client Secret de GitHub, Clave secreta de Facebook) viven solo en las consolas,
  nunca en el código.
- No subas `local.properties` (ya está en `.gitignore`).
