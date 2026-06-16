# BigLifts 🏋️

App Android de seguimiento de entrenamiento de fuerza con backend en Supabase.

## Features

- **Workout Tracking** - Registra sets, peso, reps, RIR/RPE con debounce de 500ms
- **Progressive Overload** - Muestra valores previos por ejercicio para superarte
- **Rest Timer** - Timer de descanso con notificaciones y sonido
- **Exercise Library** - 90+ ejercicios categorizados por grupo muscular
- **Body Measurements** - Registro de peso, grasa corporal y circunferencias
- **1RM Calculator** - 6 fórmulas (Brzycki, Epley, Lander, Lombardi, O'Conner, Mayhew)
- **Workout Templates** - Crea y reutiliza rutinas
- **Volume Analytics** - Gráficos de volumen por grupo muscular
- **Exercise History** - Gráficos de progresión 1RM y volumen con MPAndroidChart
- **PR Tracking** - Registra y visualiza records personales
- **Intensity Techniques** - Soporte para dropsets, rest-pause, myoreps
- **Material Design** - Transiciones fade-through, container transform, slide animations

## Tech Stack

- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34
- **Backend:** Supabase (PostgreSQL + Auth + Edge Functions)
- **DI:** Hilt
- **Navigation:** Jetpack Navigation Component
- **Charts:** MPAndroidChart
- **Images:** Coil
- **Local Cache:** Room (prepared)

## Architecture

```
app/src/main/java/com/biglifts/workouttracker/
├── data/
│   ├── api/          # ApiClient (HTTP to Supabase Edge Functions)
│   ├── models/       # Data classes
│   ├── repositories/ # Repository pattern (alternative)
│   └── supabase/     # Supabase client config
├── ui/
│   ├── auth/         # Login, Register, Onboarding
│   ├── home/         # Dashboard con stats
│   ├── workout/      # Active workout, Set adapter, Timer
│   ├── workouts/     # History, Detail
│   ├── exercises/    # Browser, Picker, History
│   ├── templates/    # Workout templates
│   ├── measurements/ # Body measurements
│   ├── calculator/   # 1RM calculator
│   ├── analytics/    # Volume by muscle
│   ├── profile/      # User profile
│   ├── splash/       # Splash screen
│   └── main/         # Main activity + fragment
└── WorkoutTrackerApplication.kt
```

## Setup

1. Clone the repo
2. Abrir en Android Studio
3. Configurar `local.properties` con tu SDK path
4. Reemplazar la API key de Supabase en `ApiClient.kt` y `SupabaseClient.kt`
5. Ejecutar el schema SQL en tu proyecto Supabase
6. Build y Run

## Backend Setup

El proyecto usa Supabase Edge Functions. Necesitas:

1. Crear proyecto en [Supabase](https://supabase.com)
2. Ejecutar el schema SQL (ver `app/src/main/res/raw/schema.sql`)
3. Deploy de las Edge Functions: `auth`, `api`, `sets`, `profile`, `body`
4. Configurar las API keys en el cliente

## Screenshots

*(Agregar screenshots aquí)*

## License

MIT License