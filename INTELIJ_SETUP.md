# IntelliJ IDEA Setup Guide for MinecraftArchipelago

## Prerequisites

### Java 21 Installation
The project requires **Java 21**. If you don't have it installed:

1. **Download Java 21**: Get it from [Adoptium](https://adoptium.net/temurin/releases/?version=21)
2. **Install**: Run the installer
3. **Set JAVA_HOME**: 
   - Windows: `set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`
   - Add to PATH: `%JAVA_HOME%\bin`

### Verify Java Installation
```bash
java -version
# Should show: openjdk version "21.0.11" 2026-04-21 LTS
```

## IntelliJ IDEA Setup

### 1. Import the Project

1. **Open IntelliJ IDEA**
2. **File -> Open** and select the `MinecraftArchipelago` directory
3. **Wait** for Gradle to sync (this may take a few minutes)

### 2. Configure Project SDK

1. **File -> Project Structure (Ctrl+Alt+Shift+S)**
2. **Project Settings -> Project**
3. **Project SDK**: Select Java 21
4. **Project language level**: 21
5. **Apply -> OK**

### 3. Configure Module SDK

1. **File -> Project Structure (Ctrl+Alt+Shift+S)**
2. **Modules** section
3. **Dependencies tab**
4. **Module SDK**: Select Java 21
5. **Apply -> OK**

### 4. Configure Gradle

1. **File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle**
2. **Gradle JDK**: Select Java 21
3. **Gradle user home**: Leave as default or set to custom location
4. **Service directory mode**: `Auto`
5. **Apply -> OK**

### 5. Enable Annotation Processing

1. **File -> Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors**
2. **Enable annotation processing**: Checked
3. **Obtain processors from all project modules**: Checked
4. **Apply -> OK**

## Running Tests in IntelliJ

### Option 1: Run Individual Test Class

1. **Navigate** to the test file (e.g., `src/test/java/com/minecraftarchipelago/SimpleTest.java`)
2. **Right-click** on the test class
3. **Run 'SimpleTest'** (or the specific test class name)
4. **View results** in the Run tool window

### Option 2: Run All Tests

1. **View -> Tool Windows -> Project** (Alt+1)
2. **Expand** the `src/test/java` directory
3. **Right-click** on the `com.minecraftarchipelago` package
4. **Run 'All Tests'**
5. **View results** in the Run tool window

### Option 3: Create Test Configuration

1. **Run -> Edit Configurations**
2. **Click **+** -> **JUnit**
3. **Test class**: Browse to your test class
4. **VM options**: Add `-Djunit.jupiter.extensions.autodetection.enabled=true`
5. **Working directory**: `$ProjectFileDir$`
6. **Name**: Give it a descriptive name (e.g., "AP Tests")
7. **Apply -> OK**
8. **Run** the configuration

## Troubleshooting

### "Could not resolve all artifacts for configuration 'classpath'"

**Cause**: Gradle is using the wrong Java version.

**Solution**:
1. **Close IntelliJ**
2. **Run**: `set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`
3. **Run**: `set PATH=%JAVA_HOME%\bin;%PATH%`
4. **Open IntelliJ** again
5. **Wait for Gradle sync**

### "Dependency requires at least JVM runtime version 21"

**Cause**: IntelliJ is using Java 8 or 17 instead of Java 21.

**Solution**:
1. **File -> Project Structure**
2. **Change Project SDK to Java 21**
3. **Change Module SDK to Java 21**
4. **Restart IntelliJ**

### "Test process encountered an unexpected problem"

**Cause**: Fabric Loom conflicts with JUnit execution.

**Solution**:
1. **Compile tests first**: Run `gradlew compileTestJava` from command line
2. **Create manual test configuration** in IntelliJ
3. **Use the compiled classes** instead of Gradle execution

## Test Structure

All test classes follow this pattern:

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

## Common Test Operations

### Assertions
```java
assertTrue(condition);           // Verify condition is true
assertFalse(condition);          // Verify condition is false
assertEquals(expected, actual);  // Verify equality
assertNotNull(object);           // Verify object is not null
assertNull(object);              // Verify object is null
assertThrows(Exception.class, () -> { /* code */ });  // Verify exception thrown
```

### Mocking with Mockito
```java
@Mock
private SomeInterface mockDependency;

@Test
void testWithMock() {
    // Arrange
    SomeInterface mock = mock(SomeInterface.class);
    when(mock.someMethod()).thenReturn("expected");
    
    // Act
    // Test code using mock
    
    // Assert
    verify(mock).someMethod();
}
```

## Adding New Tests

1. **Create new test class** in the appropriate package
2. **Add `@ExtendWith(MockitoExtension.class)`** annotation
3. **Add test methods** with `@Test` annotation
4. **Use assertions** from `org.junit.jupiter.api.Assertions`
5. **Run test** to verify it passes

## Useful IntelliJ Shortcuts

- **Run test**: Ctrl+Shift+F10
- **Debug test**: Ctrl+Shift+F9
- **Reformat code**: Ctrl+Alt+L
- **Optimize imports**: Ctrl+Alt+O
- **Generate test**: Right-click -> Generate -> Test
- **Navigate to test**: Ctrl+Shift+T

## Project Structure

```
src/test/java/com/minecraftarchipelago/
├── APClientTest.java
├── APConnectionStateTest.java
├── APSessionTest.java
├── DeathLinkHandlerTest.java
├── MinecraftArchipelagoClientTest.java
├── apitems/
│   └── APItemRegistryTest.java
├── aplocations/
│   ├── APLocationsReloadListenerTest.java
│   └── BossKillListenerTest.java
├── apstages/
│   └── StageRegistryTest.java
├── hud/
│   └── APHudStateTest.java
└── mixin/
    └── PlayerDeathMixinTest.java
```

## Test Coverage

The tests cover:
- **Core functionality** of main classes
- **Static methods** for utility classes
- **Constructor behavior** for instantiable classes
- **Edge cases** and error conditions
- **Integration points** between components

## Need Help?

If you encounter issues:
1. **Check the logs** in the Run tool window
2. **Look at the Gradle output** for compilation errors
3. **Consult the TESTING_SETUP.md** file for more details
4. **Try recompiling** with `gradlew clean compileTestJava`
5. **Restart IntelliJ** if Gradle sync seems stuck