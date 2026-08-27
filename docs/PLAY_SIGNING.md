# Arrow Escape — Google Play Signing & Release Security Protocol

**Package Name:** `com.mitsara.arrowescape`  
**Publisher:** Mitsara Games  

---

## 1. Overview
This document details the signing pipeline and Google Play App Signing architecture for **Arrow Escape**.

---

## 2. Upload Keystore & Release Signing

The application release builds use a 2048-bit RSA upload key generated via `keytool`:

- **Key Alias:** `upload` (or configured via `RELEASE_KEY_ALIAS`)
- **Keystore File:** `release.keystore`
- **Validity:** 10,000 days (25+ years)

---

## 3. GitHub Secrets Pipeline

Release builds are signed automatically in GitHub Actions using encrypted repository secrets:

- `RELEASE_KEYSTORE_BASE64` — Base64-encoded string of `release.keystore`.
- `RELEASE_KEYSTORE_PASSWORD` — Keystore store password.
- `RELEASE_KEY_ALIAS` — Key alias.
- `RELEASE_KEY_PASSWORD` — Key password.

In `app/build.gradle.kts`, the `signingConfigs.release` block decodes the environment variables securely on the ephemeral GitHub runner.

---

## 4. Google Play App Signing Setup

1. Log into Google Play Console → **Arrow Escape** → **Setup → App Integrity**.
2. Opt into **Google Play App Signing**.
3. Upload your initial signed AAB (`app-release.aab`).
4. Google Play will register your `upload` key fingerprint and issue the final production app signing key stored safely in Google's cloud key management service.

---

## 5. Security & Key Backup Protocol

- **Never Commit Secrets:** `.keystore`, `.jks`, `.p12`, `.pem`, and `.base64` files are strictly excluded via `.gitignore`.
- **Encrypted Offline Backup:** Keep a backup of `release.keystore` and its passwords in an encrypted password vault and off-site drive.
