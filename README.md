# FocusLock 2.0 — Android daily screen-time and focus companion

**Current maturity: SOURCE BUILD / UNVERIFIED BETA. NOT production-ready.**

Offline Android app written with the standard Android SDK in Java (no third-party runtime libraries). Android 8+ (`minSdk 26`). This repository contains source code and a GitHub Actions recipe to build a **debug APK**. The APK must be compiled and tested in an Android environment; that has NOT been done by the authoring environment.

## Features

- User-selected per-app daily time limits, daily usage progress and remaining allowance
- Best-effort return-to-Home blocking through Usage Access + narrowly scoped Accessibility Service
- Local-midnight reset; optional "strict mode" to defer all limit edits to tomorrow
- Home dashboard and motivation grounded in small achievable actions
- 5, 15, 25 or 45-minute focus sessions (timer is active while app is open; no background alarm)
- Daily routine checklist, bite-size screen-free alternatives, progress counts
- Evidence-informed focus/ADHD-friendly strategies and practical brain-fog reset ideas
- Optional intention, if-then planning, and daily journal, kept locally
- No `INTERNET`, camera, storage, contacts, SMS, overlay or notification permissions
- Built-in permission disclosure and source/health limitations

## Build an APK using GitHub Actions

1. Create a **private** GitHub repository and upload **all the contents** of this folder while preserving the paths (including `.github/workflows/build-apk.yml`).
2. Go to **Actions → Verify and Build FocusLock APK → Run workflow**, or push to `main`.
3. Confirm the behavior tests, `assembleDebug`, and `lintDebug` finish successfully.
4. Download the `FocusLock-Android-APK-debug` artifact. Unzip it; inside is `app-debug.apk` and `APK-SHA256.txt`.
5. On your computer, run `sha256sum app-debug.apk` (Windows: `certutil -hashfile app-debug.apk SHA256`) and compare it to the checksum file.
6. Review requested permissions with Android's package inspector / `apkanalyzer manifest permissions app-debug.apk`; scan with Play Protect and a trusted antivirus if desired.
7. Install on an Android test phone. Do **not** assume build success alone means that enforcement works on that device.
8. In-app enable **Usage Access** and, after reading the explicit disclosure, **FocusLock app blocking** in Android Accessibility settings.

A debug APK is suitable for private testing, **not a production-signed release**. For distribution, configure a private release signing key through a secure CI secret, make a release build, run extensive QA, and comply with platform policies (including Accessibility API declaration/disclosure). Never commit a private signing key into Git.

## Local checks in this bundle

```
bash tests/run-core-tests.sh
```

Tests check core date-key behavior, protected system-app exclusion, offline content catalog, and foreground interval accounting. They are **not** Android integration or security tests.

## Security and privacy characteristics

- No external runtime SDKs, trackers, ads, telemetry, internet access, remote cloud account, or external communication in the current source.
- `PACKAGE_USAGE_STATS` can expose usage metadata; an Accessibility Service receives foreground-window-change events. The service is configured `canRetrieveWindowContent=false` and does not request screenshots, gestures or text.
- Permissions are requested through Android settings only after explanatory in-app disclosure. Content and usage computations remain local.
- Backup disabled by manifest; SharedPreferences journal/intention are device-local and can be read by someone with physical access or a compromised device. Avoid storing highly sensitive secrets.
- We cannot guarantee malware freedom or absolute safety by source inspection alone. Check the APK signature, checksum, manifest, reproducibility, and scan the compiled file.
- No device-admin or device-owner privileges are used. Emergency/dialer, launchers, system settings and core OS packages are excluded from blocking.

## Important limitations / QA still required

- **Not unbreakable.** Users can revoke permissions, uninstall the app, clear data, change the clock, use other profiles/launchers, or exploit Android OS scheduling; system battery saving can stop the service.
- Accessibility event delivery and Android usage history vary by device. Some apps, split-screen use, screen overlays, cross-midnight sessions or Android work profiles may be miscounted or escape enforcement.
- Blocking navigates to Home; the selected app may briefly flash. It does not use system-level app suspension and may not be Play Store eligible for unrestricted distribution.
- `strict mode` is a voluntary friction mechanism, not security. It can be disabled starting the following local date.
- The focus timer does not notify if completed in the background. A reboot or manual time changes may invalidate a session.
- App content is not medical advice. ADHD has no established cure; persistent cognitive symptoms need medical evaluation. See [SOURCES.md](SOURCES.md).
- Before any claim of "production-ready", run device QA on Android 8, 11, 13, 14, 15/16, stress tests with relevant social apps, permission-revocation tests, long-running battery tests, accessibility audits, security audits, signed-release checks, and platform-policy reviews.

## Testing checklist for a test device

- [ ] Installs and launches on Android 8+, including Android 15/16 edge-to-edge UI
- [ ] Initially opens without permissions and explains why permissions are needed
- [ ] Grant/revoke Usage Access and Accessibility; warning UI updates
- [ ] Set app limit, exhaust it, observe return Home and re-entry blocked
- [ ] Set/remove limit and check local midnight boundary, DST and timezone changes
- [ ] Toggle strict mode and confirm edits apply next local day
- [ ] Validate counting across screen-off, app switching, split-screen, reboot, and OEM battery saving
- [ ] Test application picker with Instagram, YouTube, other installed applications
- [ ] Confirm dialer, Settings, emergency and launcher cannot be blocked
- [ ] Verify journaling/routine checkmarks and reboot persistence / day rollover
- [ ] Test Focus timer on opening, backgrounding, completion, cancellation and reboot
- [ ] Run accessibility and TalkBack review plus APK malware/permission scan
- [ ] Verify no data egress, correct signing certificate, checksum and Play-policy compliance
