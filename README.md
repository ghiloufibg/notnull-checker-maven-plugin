# NotNull Annotation Checker Maven Plugin

A Maven plugin to enforce the use of `@NotNull` annotations in Java code.
It helps prevent `NullPointerException`s by ensuring critical elements in your code are explicitly marked as non-null.

---

## Features

- Checks class fields for missing `@NotNull` annotations.
- Checks method and constructor parameters.
- Checks lambda parameters.
- Generates warnings or fails the build if violations are found.

---

### ✅ Already implemented

- Fields
- Method parameters
- Constructor parameters
- Lambda parameters
- Generic type parameters
- Collection elements
- Method return types
- Constructor/factory method returns
- Method overrides / interface implementation null contract checks


## Usage

Add the plugin to your Maven `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.example</groupId>
            <artifactId>notnull-checker-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>check-notnull</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>