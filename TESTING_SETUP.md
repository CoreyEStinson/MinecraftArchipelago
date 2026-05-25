# Testing Setup for MinecraftArchipelago

## What Has Been Created

### Test Infrastructure
- **Test Directory Structure**: Created comprehensive test directory structure matching the main source code:
  - `src/test/java/com/minecraftarchipelago/`
  - `src/test/java/com/minecraftarchipelago/apitems/`
  - `src/test/java/com/minecraftarchipelago/aplocations/`
  - `src/test/java/com/minecraftarchipelago/apstages/`
  - `src/test/java/com/minecraftarchipelago/hud/`
  - `src/test/java/com/minecraftarchipelago/mixin/`

- **Test Dependencies**: Added JUnit 5 and Mockito to `build.gradle`:
  - `org.junit.jupiter:junit-jupiter:5.10.0`
  - `org.mockito:mockito-core:5.5.0`
  - `org.mockito:mockito-junit-jupiter:5.5.0`

- **Test Configuration**: Added test task configuration to `build.gradle`

### Test Classes Created

1. **APClientTest.java** - Tests for APClient class
2. **APConnectionStateTest.java** - Tests for APConnectionState class
3. **APSessionTest.java** - Tests for APSession class
4. **DeathLinkHandlerTest.java** - Tests for DeathLinkHandler class
5. **MinecraftArchipelagoClientTest.java** - Tests for MinecraftArchipelagoClient class
6. **APItemRegistryTest.java** - Tests for APItemRegistry class
7. **APLocationsReloadListenerTest.java** - Tests for APLocationsReloadListener class
8. **BossKillListenerTest.java** - Tests for BossKillListener class
9. **StageRegistryTest.java** - Tests for StageRegistry class
10. **APHudStateTest.java** - Tests for APHudState class
11. **PlayerDeathMixinTest.java** - Tests for PlayerDeathMixin class
12. **SimpleTest.java** - Simple test to verify testing framework

### Test Resources
- **log4j2.xml** - Logging configuration for tests

## Test Coverage

The tests cover:
- **Static method testing** for utility classes (APSession, DeathLinkHandler, etc.)
- **Constructor testing** for instantiable classes
- **Method behavior testing** for core functionality
- **Edge cases** and error conditions
- **Integration points** between components

## How to Run Tests

### Prerequisite: Java 21
The project requires Java 21. If you don't have it installed:

1. **Install Java 21**: Download from [Adoptium](https://adoptium.net/temurin/releases/?version=21)
2. **Set JAVA_HOME**: 
   ```bash
   export JAVA_HOME=/path/to/jdk-21
   export PATH=$JAVA_HOME/bin:$PATH
   ```

### Running Tests

#### Option 1: Using Gradle (Recommended)
```bash
# Compile test classes
./gradlew compileTestJava

# Run all tests (if test execution works)
./gradlew unitTest

# Run specific test class
./gradlew unitTest --tests SimpleTest
```

#### Option 2: Manual Test Execution
If Gradle test execution doesn't work due to Fabric Loom conflicts:

1. **Compile test classes**:
   ```bash
   ./gradlew compileTestJava
   ```

2. **Run tests manually using JUnit**:
   ```bash
   java -cp "build/classes/java/test:build/classes/java/main:$(find ~/.gradle/caches -name 'junit-jupiter-*.jar'):$(find ~/.gradle/caches -name 'mockito-core-*.jar'):$(find ~/.gradle/caches -name 'mockito-junit-jupiter-*.jar')" org.junit.platform.console.ConsoleLauncher --select-class com.minecraftarchipelago.SimpleTest
   ```

### Test Results

Test results will be available in:
- `build/reports/tests/` - HTML test reports
- Console output during test execution

## Test Structure

Each test class follows this pattern:

```java
package com.minecraftarchipelago;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClassNameTest {

    @Test
    void testMethodName() {
        // Arrange
        // Act
        // Assert
    }
}
```

## Adding New Tests

To add tests for new functionality:

1. **Create test class** in corresponding package
2. **Add test methods** with `@Test` annotation
3. **Use assertions** from `org.junit.jupiter.api.Assertions`
4. **Use Mockito** for mocking dependencies when needed
5. **Run tests** to verify they pass

## Known Limitations

- **Fabric Loom Integration**: The Fabric Loom build system may interfere with standard Gradle test execution
- **Minecraft Dependencies**: Some tests may require mocking of Minecraft-specific classes
- **Integration Tests**: Full integration tests would require a running Minecraft instance

## Future Improvements

- Add more comprehensive test coverage for edge cases
- Create integration test framework
- Add test coverage reporting
- Implement continuous integration testing
- Add property-based testing for complex logic
