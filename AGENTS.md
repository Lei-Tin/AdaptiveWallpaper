# AdaptiveWallpaper Agent Instructions

These instructions keep Codex sessions consistent across devices.

## Start every task

1. Read `.agents/HANDOFF.md` for the current implementation and release state.
2. Read the newest relevant entries in `.agents/INTERACTION_HISTORY.md` before changing an established decision.
3. Run `git status -sb` and preserve unrelated or untracked user files.

## Keep the shared record current

- After every meaningful implementation, release, deployment, signing, or product decision, update `.agents/HANDOFF.md` in the same change.
- Append a concise dated entry to `.agents/INTERACTION_HISTORY.md` when a decision or outcome will matter to a future session.
- Record outcomes, rationale, verification, and important paths. Do not store a verbatim transcript.
- When the user has asked to commit or push project work, include the corresponding agent-record updates in that commit or an immediately following documentation commit.
- The owner prefers direct maintenance updates to `main`; do not open a PR unless the user asks for one. This preference does not authorize releases, force pushes, destructive operations, or unrelated external writes without a current request.

## Privacy and public-repository safety

This repository is public. Never record passwords, tokens, cookies, keystore contents, keystore passwords, private keys, Cloudflare credentials, precise private file locations, or private correspondence.

Signing secrets remain in ignored local files (`keystore.properties`, `*.jks`, and `*.keystore`). Public certificate identity and fingerprint may be documented because they are already part of APK verification.

## Website isolation

- Agent Markdown belongs only in `AGENTS.md` and `.agents/`.
- Do not copy agent records or additional Markdown into `site/` or `site/dist/`.
- `site/build.sh` should continue publishing only its explicit static assets, pages, checksum, and APK.

## Project conventions

- User-facing discussion is normally in Chinese.
- Preserve the package name `com.shouyihung.adaptivewallpaper` and the existing `CN=Ray Hung` signing identity unless the user explicitly requests a migration and accepts update incompatibility.
- Do not touch `Test Pic.jpg` or local `social/` assets unless the task explicitly includes them.
- Before pushing Android changes, run checks proportional to the change. The standard full check is:

  ```bash
  ./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
  ```
