# مانا | Mana Tracker

[![Android CI](https://github.com/your-org/mana-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/your-org/mana-tracker/actions/workflows/ci.yml)
[![Release](https://github.com/your-org/mana-tracker/actions/workflows/release.yml/badge.svg)](https://github.com/your-org/mana-tracker/actions/workflows/release.yml)
[![License: AGPL v3](https://img.shields.io/badge/license-AGPL%20v3-blue.svg)](LICENSE)

Mana is a privacy-first expense tracker for Iranian users.

## Highlights

- Persian-first UX with RTL layout, English as secondary locale
- IRR-only accounting and locale-aware number formatting
- Iranian bank SMS parser set (Mellat, Melli, Sepah, Tejarat, Saderat, Pasargad, Saman, Karafarin, Parsian, Shahr, Qavamin, Mehr Iran, Day, Tose'e Ta'avon)
- AI Chat UI kept as placeholder screen (no AI backend)
- MVVM + Room + Hilt + WorkManager + Jetpack Compose + Material 3
- Home, Analytics, Transactions, Rules, Subscriptions, Settings, Budgets, Accounts
- CSV/PDF export placeholders and `.manabackup` backup placeholder
- Biometric capability check scaffold and app widgets scaffold

## Tech

- Kotlin, Jetpack Compose, Material Design 3
- Room Database, Hilt DI, Coroutines, WorkManager
- Min SDK 26, Target SDK 35

## Build (GitHub Actions first)

Primary flow is GitHub Actions:

1. Push branch or open PR for CI build.
2. Tag `vX.Y.Z` for release workflow.

Local (optional):

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## Signing

Copy `keystore.properties.template` to `keystore.properties` and fill values.

For GitHub Actions release signing, configure secrets:

- `MANA_KEYSTORE_BASE64`
- `MANA_KEYSTORE_PASSWORD`
- `MANA_KEY_ALIAS`
- `MANA_KEY_PASSWORD`

## Package

- App ID: `com.mana.tracker`
- Package: `com.mana.tracker`

## License

AGPL v3. See `LICENSE`.
