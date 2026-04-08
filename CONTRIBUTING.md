# Contributing

This guide provides instructions for contributing to this Capacitor plugin.

## Developing

### Local Setup

1. Fork and clone the repo.
2. Install dependencies.

```shell
bun install
```

3. Install SwiftLint if you're on macOS.

```shell
brew install swiftlint
```

### Scripts

#### `bun run build`

Builds plugin web assets and generates API documentation with [`@capacitor/docgen`](https://github.com/ionic-team/capacitor-docgen).

#### `bun run verify`

Builds and validates iOS, Android, and Web.

#### `bun run lint` / `bun run fmt`

Checks or auto-fixes formatting and linting.

## Publishing

The `prepublishOnly` hook prepares the plugin before publishing.

```shell
bun publish
```

> The `files` array in `package.json` controls what is published. Update it if you move files.
