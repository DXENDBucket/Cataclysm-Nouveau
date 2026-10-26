# 0.1.0 CurseForge release

Date: 2026-10-27 (Asia/Shanghai).

Project ID: **1767646**. Release type: **Release**. Both client/server tags are selected. Manual release is disabled; files publish after moderation.

The API accepted the files below. File IDs are receipts, not proof that moderation has completed.

## Release files

| Platform | File ID | SHA-256 |
| --- | ---: | --- |
| neoforge | 9335919 | `3584d93f44d040dfd146627974d57f3edbae7630d1207a8343257f0346f2db74` |

## Source and validation

- Release commit: `c72fc3ac8c6f97b275a614d5ec26b63714e8e17e`; local annotated tag: `v0.1.0`.
- Fixed Ars Conflux build checkout: `b46be94b17799573b825a8e4a11870fa3dc857d6` (0.1.29); its tracked files remain unchanged.
- Production code was not changed for the release. Every production class is byte-for-byte identical to the previously validated development build.
- Java 21 for NeoForge and Java 17 for Forge; Forge release artifacts were reobfuscated.
- Binary/source package checks passed: version and loader metadata, recipe/localization/native-reference checks where provided, license notices, and exclusion of dependency/runtime-test content.
- No GameTest or client rerun for this version/documentation release. Prior server checks do not establish client visual correctness.
- No shared game instance deployment or Git remote push was performed.

### Class comparison

- neoforge: 311 production classes identical to `cataclysm-nouveau-0.1.0-dev.62-mc1.21.1-neoforge.jar`.

### Standard source JARs

- neoforge: `cataclysm-nouveau-0.1.0-sources.jar` — `b6c566c69e783e71abc22b3b3f73f848645dbb259ffc32f254729c443e7d5b5c`.

`verifyDistributions` passed for both binary and source distributions; no original Cataclysm code or media is bundled.

## Local release records

- Binary plan: `Ars-Conflux/build/curseforge/20261027-021315-f5565867/plan.json`.
- Shared upload ledger: `Ars-Conflux/build/curseforge/uploads.json`.
- Detailed receipts, class comparisons and source plan: `Ars-Conflux/build/curseforge/release-20261027/`.
- Isolated addon worktree: `parallel-workspaces/release-cataclysm-0.1.0-20261027/Cataclysm-Nouveau`.
