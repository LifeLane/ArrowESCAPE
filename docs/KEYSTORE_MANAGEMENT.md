# Secure Keystore Generation, Backup, & Secret Management Procedure

**Brand:** Arrow Escape  
**Application ID:** `com.mitsara.arrowescape`  
**Package:** `com.mitsara.arrowescape`  

This document outlines the strict protocol for generating, backing up, and configuring the permanent release/upload signing keystore for **Arrow Escape**.

---

## ⚠️ SECURITY MANDATES (CRITICAL)

1. **NEVER** commit `.keystore`, `.jks`, `.p12`, passwords, or private key files to git or GitHub repositories.
2. **NEVER** log passwords, private key base64 strings, or service account JSON credentials during build or CI steps.
3. **PERMANENT RETENTION:** If you lose the original signing key or keystore password without Google Play App Signing enabled, you will be permanently unable to update **Arrow Escape** on Google Play!

---

## 1. Local Keystore Generation Procedure

Run the following command on a secure local terminal to generate a permanent 2048-bit RSA upload keystore with a 25-year validity period (10,000 days):

```bash
keytool -genkeypair -v \
  -keystore release.keystore \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Arrow Escape, OU=Mobile Games, O=Mitsara Games, L=San Francisco, ST=California, C=US"
```

### Password Requirements:
- Use a cryptographically strong, randomly generated password (at least 20+ characters with mixed case, numbers, and symbols).
- Set identical or equally strong passwords for both the Keystore and Key Alias.

---

## 2. Base64 Encoding for GitHub Encrypted Secrets

Convert the binary `release.keystore` into a single-line base64 string to store as a GitHub Secret:

### Linux / Windows (Git Bash):
```bash
base64 -w 0 release.keystore > release.keystore.base64
```

### macOS:
```bash
base64 -i release.keystore -o release.keystore.base64
```

---

## 3. GitHub Encrypted Secrets Configuration

In your GitHub repository, navigate to **Settings → Secrets and variables → Actions**, and create the following repository secrets:

| Secret Name | Description / Format |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | The single-line base64 string output from `release.keystore.base64` |
| `RELEASE_KEYSTORE_PASSWORD` | Password created during `keytool` execution |
| `RELEASE_KEY_ALIAS` | Alias name used in keytool (`upload`) |
| `RELEASE_KEY_PASSWORD` | Key alias password |
| `PLAY_SERVICE_ACCOUNT_JSON` | Google Play Developer API Service Account JSON key for automated publishing |

---

## 4. Permanent Offline Backup Procedure

To prevent loss of signing capability:
1. **Encrypted Vault Storage:** Store `release.keystore` and its passwords in a secure password manager vault (1Password, Bitwarden, or KeePass) and a physical encrypted USB backup drive kept off-site.
2. **Fingerprint Recording:** Store the SHA-256 and SHA-1 fingerprints for verification:
   ```bash
   keytool -list -v -keystore release.keystore -alias upload
   ```
3. **Google Play App Signing:** Register the upload key with Google Play Console under **Setup → App Integrity**. Google Play App Signing securely manages the app signing key in Google's infrastructure while accepting releases signed with your upload key.

---

## 5. Gradle Integration Architecture

In `app/build.gradle.kts`, the release signing block dynamically reads environment variables injected by GitHub Actions workflows without risking hardcoded paths or plain-text exposure:

```kotlin
signingConfigs {
    create("release") {
        val keystorePath = System.getenv("KEYSTORE_PATH")
            ?: System.getenv("RELEASE_KEYSTORE_PATH")
            ?: "${rootDir}/release.keystore"
        
        val keystoreFile = file(keystorePath)
        if (keystoreFile.exists()) {
            storeFile = keystoreFile
            storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD") 
                ?: System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS") 
                ?: System.getenv("KEY_ALIAS") 
                ?: "upload"
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD") 
                ?: System.getenv("KEY_PASSWORD")
        }
    }
}
```
