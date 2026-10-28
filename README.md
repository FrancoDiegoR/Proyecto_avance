# SmartGas — App móvil (Android · Jetpack Compose)

Frontend móvil de **SmartGas**, monitoreo de gas y temperatura en cocinas (hogares y restaurantes).
Consume el mismo backend REST que la versión web (`SmartGas.Api`, ASP.NET Core + PostgreSQL).

## Arquitectura

El proyecto está organizado por **bounded contexts** (DDD) y, dentro de cada uno, por capas
siguiendo el patrón de los proyectos del curso (MealsCompose / SuperHeroCompose):

```
com.smartgas_frontend/
├── common/                      Resource, UIState, Constants
├── iam/                         Cuentas: login, registro, perfil, ajustes
├── kitchenmonitoring/           Zonas, sensores y lecturas
├── incidentdetection/           Incidentes (pestaña History)
├── incidentpreventionnotification/  Alertas y notificaciones (pestaña Alerts)
├── paymentmanagement/           Planes y suscripción
├── postincidentprocedures/      Reportes
├── shared/                      Home, navegación, componentes comunes, sesión, i18n
└── ui/theme/
```

Cada bounded context tiene:

| Capa | Contenido |
|---|---|
| `data/remote` | Interfaces Retrofit (`XService`) y `dto/` con sus mappers `toX()` |
| `data/repository` | Repositorios que devuelven `Resource<T>` usando `withContext(Dispatchers.IO)` |
| `data/di` | `XDataModule`: crea services y repositorios |
| `domain/model` | Modelos de dominio |
| `presentation/<pantalla>` | `XScreen` (Compose) + `XViewModel` (expone `State<UIState<T>>`) |
| `presentation/di` | `XPresentationModule`: crea los ViewModels |

`SmartGasApplication` (como `MealsApplication`) da el contexto a los `DataModule`.
Retrofit, la sesión y las preferencias son instancias únicas (`by lazy`) en `SharedDataModule`.

## Navegación

Barra inferior según el diseño: **Home · Zones · Alerts · History · Settings**.

- Zones → tocar una zona abre **Zone detail**; el botón **+** abre **Add sensor**.
- Zone detail → tocar un sensor abre **Sensor detail** (con ✏️ para editar).
- Home → ícono de usuario abre **Profile**. Settings → **Cerrar sesión**.

## Cómo ejecutarlo

La app apunta a `http://localhost:5048/api/v1/` (`common/Constants.kt`).

### Opción A — Backend simulado (rápido, sin base de datos)
```bash
node docs/mock/server.js                       # escucha en el puerto 5048
adb reverse tcp:5048 tcp:5048                  # el emulador ve el localhost de la PC
```
Usuario de prueba: `usuario@smartgas.com` / `smartgas`.

### Opción B — Backend real (SmartGas.Api)
1. PostgreSQL con base `smartgas_db`, usuario `smartgas_user` / `smartgas_password` (puerto 5432).
2. En la carpeta del backend: `dotnet run --launch-profile http` (crea tablas y planes al arrancar).
3. `adb reverse tcp:5048 tcp:5048` y registrarse desde la app.

> Si `adb` no está en el PATH: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.
> Con varios dispositivos conectados usar `adb -s emulator-5554 reverse tcp:5048 tcp:5048`.

## Estado del avance (según diseños 01–17)

**Hecho:** Sign in (01–02), Create account (03), Zones (05), Zone detail (06), Sensor detail + edición (07),
Add sensor (08), Plans (16), Settings con cierre de sesión, "Keep me signed in".

**Por adaptar al diseño:** Home (04), Alerts (10), History (14), Settings (15).

**Pendiente:** QR (09), push (11), Alert detail (12), Incident (13), modo offline (17),
actualización automática de datos, intentos restantes y recuperación de contraseña (requieren backend).
