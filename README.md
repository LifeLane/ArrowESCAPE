# Arrow Escape — Android Puzzle Game

**Arrow Escape** (`com.mitsara.arrowescape`) is a production-ready Android puzzle game built with Kotlin, Jetpack Compose, Room Database, and Material 3.

## Core Gameplay

- **TAP AN UNOBSTRUCTED ARROW**
- **ARROW ESCAPES THE BOARD**
- **BOARD SPACE BECOMES CLEAR**
- **OTHER ARROWS BECOME UNBLOCKED**
- **REPEAT UNTIL THE BOARD IS EMPTY**

## Architecture

- **UI**: Jetpack Compose, Material Design 3, Smooth Canvas & Scale Animations
- **Game Engine**: Deterministic `PuzzleSolver` with BFS backtracking and solvability validation
- **Storage**: Room Local Persistence for level progress, stars, and user settings
- **Monetization Abstraction**: AdMob & RevenueCat premium entitlement interfaces
- **Testing**: JUnit Unit Tests & Robolectric JVM testing

## Build & Run

```bash
# Run unit tests
gradle testDebugUnitTest

# Assemble debug APK
gradle assembleDebug
```
