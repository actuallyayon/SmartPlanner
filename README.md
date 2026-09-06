# Smart Planner (Android Client)

Smart Planner is an Android application designed to help users build routines that actually adapt to real-life triggers instead of demanding absolute perfection. Users can create a plan with goals and daily constraints, stack new habits on top of existing triggers, log daily progress, and view consistency statistics.

Currently, this client is built to operate entirely with local in-memory data via a Mock Repository seam. It is fully ready for backend integration in the next development phase.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3 (custom light/dark themes)
- **Architecture**: MVC (Model-View-Controller)
  - `model/` - Plain data classes representing core objects plus the `Repository` interface and its injected mock implementation.
  - `view/` - Pure stateless Compose layout files, separating formatting from business logic.
  - `controller/` - ViewModels managing UI stateflows, user interaction events, and repository requests.
- **Dependency Injection**: Dagger Hilt
- **Navigation**: Jetpack Navigation Compose
- **Build Tool**: Gradle (Kotlin DSL)

---

## Getting Started

### Prerequisites
- Android Studio Koala (or newer)
- Android SDK 34 (Upside Down Cake)
- JDK 17 or higher (configured in Android Studio Gradle settings)

### Installation
1. Clone this repository to your local machine:
   ```bash
   git clone https://github.com/your-team/smart-planner-android.git
   ```
2. Open Android Studio.
3. Select **File -> Open** and choose the `Smart Planner` project root directory.
4. Let Android Studio download dependencies and index files (Gradle Sync).

### Building and Running
- To compile and build the app, run the following in your terminal or use the Android Studio build button:
  ```bash
  ./gradlew assembleDebug
  ```
- To run the application on an emulator or a connected physical device:
  1. Click the **Run** button in Android Studio's toolbar.
  2. Or run:
     ```bash
     ./gradlew installDebug
     ```

---

## Testing & Verification

### Unit and UI Instrumentation Tests
The project contains modular instrumentation and unit tests validating logo layout rendering, button click selection states, text inputs, onboarding steps, progress chart variables, and details modifications.

To run all checks:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## Architecture Design

To ensure the client can easily transition to a real server API, we have decoupled the views/controllers from data operations using a clean **Repository Interface seam**:

```
[ Compose Views ] <--> [ ViewModels / Controllers ] <--> [ Repository Interface ]
                                                                 ^
                                                                 |
                                                     [ MockRepository (In-Memory) ]
                                                        (To be swapped in Phase 2)
```

By changing a single binding in Hilt's `AppModule`, the entire application can be linked to a REST API or a Room database implementation without modifying a single line of code in the View or Controller packages.

---

## Team
Developed by the Smart Planner Frontend Engineering Team.
