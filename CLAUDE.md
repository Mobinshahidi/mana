# Mana Project Context

## Project Overview
Mana is a minimalist, AI-powered expense tracker for Android that automatically extracts transaction data from SMS messages using on-device processing.

## Important Documents
Please reference these documents when working on this project:
- **Architecture**: `/docs/architecture.md` - MVVM + Clean Architecture patterns, layer responsibilities
- **Design System**: `/docs/design.md` - Material 3 theming, colors, typography, components
- **PRD**: `/prd.md` - Product requirements, features, timeline

## Key Technical Decisions
1. **UI Framework**: Jetpack Compose with Material 3
2. **Architecture**: MVVM with Clean Architecture (UI, Domain, Data layers)
3. **State Management**: Unidirectional Data Flow with StateFlow
4. **DI**: Hilt for dependency injection
5. **Database**: Room for local storage
6. **AI/ML**: MediaPipe LLM (Qwen 2.5) for on-device processing
7. **Background**: WorkManager for SMS scanning

## Design Principles
- **Material You**: Dynamic color from wallpaper (Android 12+)
- **Light/Dark Theme**: Full support with semantic color roles
- **Spacing**: 8dp grid system
- **Typography**: Material 3 type scale
- **Navigation**: NavigationBar for phones, NavigationRail for tablets
- **Edge-to-Edge**: All screens use ManaScaffold with default TopAppBar for consistent system bar handling
- **Consistent UI**: ManaScaffold provides default TopAppBar with options for title, navigation, actions, and transparency

## Code Style Guidelines
- Follow Kotlin coding conventions
- Use meaningful variable names
- Implement proper error handling with sealed classes
- Ensure UI components are reusable and testable
- Always test on both light and dark themes

## Current Phase
Working on Phase 1: Core Foundation (Project setup, Material 3 theming, Room database, Navigation)

## Commands to Run
- Build: `./gradlew build`
- Test: `./gradlew test`
- Lint: `./gradlew lint`

## Versioning Strategy
We follow Semantic Versioning (SemVer) - MAJOR.MINOR.PATCH:
- **MAJOR**: Breaking changes, major UI overhauls, architecture changes
- **MINOR**: New features, significant improvements
- **PATCH**: Bug fixes, minor improvements, performance optimizations

Current version: 2.1.3 (versionCode: 13)

Recent version history:
- 2.1.3: Federal Bank support, Discord community, GitHub issue templates
- 2.1.2: Spotlight tutorial, SBI/Indian Bank support, auto-scan on launch
- 2.0.1: Previous release

## Module Structure
The project now uses a multi-module architecture:
- **app**: Main Android application module
- **parser-core**: Standalone bank parser module (no Android dependencies)

## Bank Parser Architecture
Bank parsers are now in the `parser-core` module for reusability across platforms.

### When adding new bank parsers:
1. **Location**: Add to `parser-core/src/main/kotlin/com/mana/parser/core/bank/`
2. **Base Class**: All bank parsers extend `BankParser` abstract class
   - **Indian Banks**: MUST extend `BaseIndianBankParser` to inherit centralized mandate, subscription, and balance update logic.
   - **UAE Banks**: MUST extend `UAEBankParser` for currency and transaction type handling.
3. **Key Methods**:
   - `getBankName()`: Returns the bank's display name
   - `canHandle(sender: String)`: Checks if parser can handle SMS from sender
   - `parse(smsBody, sender, timestamp)`: Returns `ParsedTransaction` or null
4. **Override Patterns**: Banks typically override:
   - `extractAmount()`: Bank-specific amount patterns
   - `extractMerchant()`: Bank-specific merchant extraction
   - `extractTransactionType()`: If needed for special cases
5. **Registration**: Add new parser to `BankParserFactory.parsers` list in parser-core
6. **Return Type**: Use `ParsedTransaction` from parser-core
7. **Imports for parser-core**:
   - `com.mana.parser.core.TransactionType`
   - `com.mana.parser.core.ParsedTransaction`
   - `java.math.BigDecimal` for amounts

### Integration in main app:
- Use `com.mana.tracker.data.mapper.toEntity()` to convert ParsedTransaction to TransactionEntity
- The mapper handles type conversions between modules

## Supported Banks (46 parsers)
- Airtel Payments Bank
- **Alinma Bank (Saudi Arabia)** - Arabic SMS support
- American Express (AMEX)
- Axis Bank
- Bank of Baroda
- Bank of India
- Canara Bank
- Central Bank of India
- City Union Bank
- DBS Bank
- Federal Bank
- HDFC Bank
- HSBC Bank
- **Huntington Bank (USA)**
- ICICI Bank
- IDBI Bank
- IDFC First Bank
- Indian Bank
- Indian Overseas Bank
- India Post Payments Bank (IPPB)
- Jio Payments Bank
- JioPay
- Jammu & Kashmir Bank
- Jupiter Bank
- Juspay
- Karnataka Bank
- Kerala Gramin Bank
- Kotak Bank
- LazyPay
- **Liv Bank (UAE)** - Digital bank
- Mashreq Bank
- **M-PESA (Kenya)** - Mobile money service
- **Navy Federal Credit Union (USA)** - NFCU
- **NMB Bank / Nabil Bank (Nepal)**
- OneCard
- **Priorbank (Belarus)** - Russian/Belarusian SMS support
- Punjab National Bank (PNB)
- Saraswat Co-operative Bank
- **Siddhartha Bank Limited (Nepal)**
- State Bank of India (SBI)
- Slice
- South Indian Bank
- **Standard Chartered Bank**
- Union Bank
- Utkarsh Bank

When implementing any feature, please ensure it aligns with the architecture patterns and design system defined in the documentation.


# Important
Never use pii in comments, code anywhere

# Test implementation standards for parsers
- Parser tests must use the shared JUnit helpers under `ParserTestUtils`. For
  full guidance (examples, migration checklist), read `docs/parser-test-standards.md`.

<!-- rtk-instructions v2 -->
# RTK (Rust Token Killer) - Token-Optimized Commands

## Golden Rule

**Always prefix commands with `rtk`**. If RTK has a dedicated filter, it uses it. If not, it passes through unchanged. This means RTK is always safe to use.

**Important**: Even in command chains with `&&`, use `rtk`:
```bash
# ❌ Wrong
git add . && git commit -m "msg" && git push

# ✅ Correct
rtk git add . && rtk git commit -m "msg" && rtk git push
```

## RTK Commands by Workflow

### Build & Compile (80-90% savings)
```bash
rtk cargo build         # Cargo build output
rtk cargo check         # Cargo check output
rtk cargo clippy        # Clippy warnings grouped by file (80%)
rtk tsc                 # TypeScript errors grouped by file/code (83%)
rtk lint                # ESLint/Biome violations grouped (84%)
rtk prettier --check    # Files needing format only (70%)
rtk next build          # Next.js build with route metrics (87%)
```

### Test (60-99% savings)
```bash
rtk cargo test          # Cargo test failures only (90%)
rtk go test             # Go test failures only (90%)
rtk jest                # Jest failures only (99.5%)
rtk vitest              # Vitest failures only (99.5%)
rtk playwright test     # Playwright failures only (94%)
rtk pytest              # Python test failures only (90%)
rtk rake test           # Ruby test failures only (90%)
rtk rspec               # RSpec test failures only (60%)
rtk test <cmd>          # Generic test wrapper - failures only
```

### Git (59-80% savings)
```bash
rtk git status          # Compact status
rtk git log             # Compact log (works with all git flags)
rtk git diff            # Compact diff (80%)
rtk git show            # Compact show (80%)
rtk git add             # Ultra-compact confirmations (59%)
rtk git commit          # Ultra-compact confirmations (59%)
rtk git push            # Ultra-compact confirmations
rtk git pull            # Ultra-compact confirmations
rtk git branch          # Compact branch list
rtk git fetch           # Compact fetch
rtk git stash           # Compact stash
rtk git worktree        # Compact worktree
```

Note: Git passthrough works for ALL subcommands, even those not explicitly listed.

### GitHub (26-87% savings)
```bash
rtk gh pr view <num>    # Compact PR view (87%)
rtk gh pr checks        # Compact PR checks (79%)
rtk gh run list         # Compact workflow runs (82%)
rtk gh issue list       # Compact issue list (80%)
rtk gh api              # Compact API responses (26%)
```

### JavaScript/TypeScript Tooling (70-90% savings)
```bash
rtk pnpm list           # Compact dependency tree (70%)
rtk pnpm outdated       # Compact outdated packages (80%)
rtk pnpm install        # Compact install output (90%)
rtk npm run <script>    # Compact npm script output
rtk npx <cmd>           # Compact npx command output
rtk prisma              # Prisma without ASCII art (88%)
```

### Files & Search (60-75% savings)
```bash
rtk ls <path>           # Tree format, compact (65%)
rtk read <file>         # Code reading with filtering (60%)
rtk grep <pattern>      # Search grouped by file (75%). Format flags (-c, -l, -L, -o, -Z) run raw.
rtk find <pattern>      # Find grouped by directory (70%)
```

### Analysis & Debug (70-90% savings)
```bash
rtk err <cmd>           # Filter errors only from any command
rtk log <file>          # Deduplicated logs with counts
rtk json <file>         # JSON structure without values
rtk deps                # Dependency overview
rtk env                 # Environment variables compact
rtk summary <cmd>       # Smart summary of command output
rtk diff                # Ultra-compact diffs
```

### Infrastructure (85% savings)
```bash
rtk docker ps           # Compact container list
rtk docker images       # Compact image list
rtk docker logs <c>     # Deduplicated logs
rtk kubectl get         # Compact resource list
rtk kubectl logs        # Deduplicated pod logs
```

### Network (65-70% savings)
```bash
rtk curl <url>          # Compact HTTP responses (70%)
rtk wget <url>          # Compact download output (65%)
```

### Meta Commands
```bash
rtk gain                # View token savings statistics
rtk gain --history      # View command history with savings
rtk discover            # Analyze Claude Code sessions for missed RTK usage
rtk proxy <cmd>         # Run command without filtering (for debugging)
rtk init                # Add RTK instructions to CLAUDE.md
rtk init --global       # Add RTK to ~/.claude/CLAUDE.md
```

## Token Savings Overview

| Category | Commands | Typical Savings |
|----------|----------|-----------------|
| Tests | vitest, playwright, cargo test | 90-99% |
| Build | next, tsc, lint, prettier | 70-87% |
| Git | status, log, diff, add, commit | 59-80% |
| GitHub | gh pr, gh run, gh issue | 26-87% |
| Package Managers | pnpm, npm, npx | 70-90% |
| Files | ls, read, grep, find | 60-75% |
| Infrastructure | docker, kubectl | 85% |
| Network | curl, wget | 65-70% |

Overall average: **60-90% token reduction** on common development operations.
<!-- /rtk-instructions -->