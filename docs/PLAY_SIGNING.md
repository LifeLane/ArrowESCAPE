# Google Play App Signing & Keystore Configuration

This document describes the secure release architecture for **Arrow Escape** (`com.mitsara.arrowescape`).

## Keystore Architecture

Arrow Escape utilizes **Google Play App Signing**:
1. **Google Play App Signing Key**: Managed securely in Google Cloud / Play Console.
2. **Upload Keystore**: Used locally or in GitHub Actions to sign the AAB before uploading to Play Console.

## Generating the Upload Keystore

Generate an upload key using keytool:

```bash
keytool -genkey -v -keystore upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

## Configuring GitHub Secrets

For CI/CD automated release builds, set the following secrets in GitHub Repository Settings:

- `KEYSTORE_BASE64`: Base64 encoded content of `upload-key.jks`
- `STORE_PASSWORD`: Keystore store password
- `KEY_ALIAS`: Alias name (`upload`)
- `KEY_PASSWORD`: Key password
