# Security Policy

## Reporting Security Vulnerabilities

If you discover a security vulnerability within **Arrow Escape**, please report it directly to the development team:

- **Email:** `security@mitsara.com`
- **Response Time:** We aim to acknowledge reports within 48 hours and release fixes promptly.

Please do **NOT** report security vulnerabilities through public GitHub issues.

## Signing & Credentials Policy
- Never commit signing keys (`.keystore`, `.jks`), passwords, or API credentials to public repositories.
- All release builds are signed via encrypted GitHub Secrets (`RELEASE_KEYSTORE_BASE64`).
