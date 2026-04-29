# ADR-0003: Native Kotlin + Swift over cross-platform

**Date:** 2026-04-22
**Status:** Accepted

## Context

We need to choose between cross-platform (React Native, Flutter, KMP) and native (Kotlin + Swift). kehdo has heavy platform-integration needs: screenshots, OCR, clipboard, share sheets, keyboard extensions.

## Decision

Build **native apps**:
- Android: Kotlin 1.9+ + Jetpack Compose + Material 3
- iOS: Swift 5.9+ + SwiftUI (iOS 17+)

Shared code lives in:
- `/contracts/` — generates API clients
- `/design/tokens/` — generates Color.kt and Colors.swift

## Consequences

### Enables
- First-class platform APIs (MediaStore, Share Sheet, IMEs)
- Native performance for OCR and image work
- Modern UI paradigms (both declarative)
- Easier hiring (devs specialize in one platform)
- Phase 2 extensions work natively

### Costs
- Two codebases to maintain
- Feature parity requires discipline
- ~1.5x engineering investment vs cross-platform

### Mitigations
- OpenAPI generates identical API clients
- Design tokens generate identical visual language
- Shared architecture (MVI/MVVM) keeps mental model portable
