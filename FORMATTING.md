# Formatting Rules

## Goal

This project uses a simple, standardized formatting policy to keep the codebase consistent, predictable, and easy to maintain across the team.

## Source of Truth

- `Spotless` is the formatter enforcement tool.
- `.editorconfig` defines editor defaults.
- `Google Java Style` is the base style for Java formatting.

## Rules

### Java

- Formatter: `google-java-format`
- Base indentation: `2` spaces
- Unused imports are removed automatically
- Files end with a newline
- Trailing whitespace is removed

### Gradle

- Indentation: `2` spaces
- Trailing whitespace is removed
- Files end with a newline

### Properties

- Indentation: `2` spaces when indentation is used
- Trailing whitespace is removed
- Files end with a newline

### YAML / YML

- Indentation: `2` spaces
- Trailing whitespace is removed
- Files end with a newline

### TOML

- Indentation: `2` spaces
- Trailing whitespace is removed
- Files end with a newline

## Commands

### Apply formatting

```bash
./gradlew spotlessApply
```

### Verify formatting

```bash
./gradlew spotlessCheck
```

### Verify formatting and tests

```bash
./gradlew spotlessCheck test
```

## Team Agreement

- Do not use custom indentation styles in this repository.
- Prefer the formatter over manual alignment.
- If an IDE formatter conflicts with `Spotless`, `Spotless` wins.

## Recommendation

Run `spotlessApply` before committing changes and let CI enforce `spotlessCheck`.
