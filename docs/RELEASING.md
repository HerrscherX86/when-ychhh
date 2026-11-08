# When Yah — Release Guide

Step-by-step checklist for building, signing, and publishing a GitHub release.

This guide is written for the v1.1 release, and the same process applies to every later version.

---

## Before you start

Your existing v1.0 release APK is:

```text
app/release/whenyah-V1.0.apk
```

Reference values from that APK:

```text
versionCode = 1
versionName = 1.0
Certificate DN:
  C=ID, ST=South Kalimantan, L=Banjarmasin,
  O=F1zz.dev, OU=Individual, CN=Hafizh Pratama
Certificate SHA-256:
  c3e152abc484add379785c2dba04553179549e2e18ae89672fa4d07d596e4491
```

**v1.1 must be signed with the same keystore**, so the SHA-256 must match. If it does not, Android will refuse to update the existing app.

Back up the keystore and its passwords somewhere safe.

---

## One-time v1.1 checklist

- [ ] v1.0 keystore located and backed up
- [ ] `versionCode = 2`, `versionName = "1.1"`
- [ ] Signed v1.1 APK built
- [ ] v1.1 signature SHA-256 matches v1.0
- [ ] v1.1 installs over v1.0 on a test device/emulator
- [ ] Code pushed to `main`
- [ ] GitHub release published as a normal release (not prerelease)
- [ ] Release has `whenyah-V1.1.apk` and `update.json` assets
- [ ] `releases/latest/download/update.json` returns the correct JSON
- [ ] Manual “Check for updates” on v1.1 says “You're on the latest version.”
- [ ] v1.0 users told to install v1.1 manually once

---

## Step 1 — Confirm the version

Check `app/build.gradle.kts`:

```kotlin
versionCode = 2
versionName = "1.1"
```

Do not change these unless you deliberately choose a different version.

---

## Step 2 — Locate the v1.0 keystore

The keystore is not in this repository (and should never be committed).

If you do not remember the path, search by filename only:

```bash
find ~ -maxdepth 5 \( -iname '*.jks' -o -iname '*.keystore' \) 2>/dev/null
```

Identify the correct keystore with:

```bash
keytool -list -v -keystore /path/to/your-keystore.jks
```

Look for:

```text
SHA256: c3e152abc484add379785c2dba04553179549e2e18ae89672fa4d07d596e4491
```

If you cannot find that keystore, do not publish v1.1. Existing v1.0 users would have to uninstall and reinstall, losing data unless they exported a backup first.

---

## Step 3 — Build a signed v1.1 APK

### Option A — Android Studio

1. Open **Build → Generate Signed Bundle / APK**.
2. Choose **APK**, then **Next**.
3. Select **Choose existing** and pick the v1.0 keystore.
4. Enter the keystore password, key alias, and key password.
5. Select the **release** build variant.
6. Leave both signature versions enabled (V1 + V2).
7. Choose the output folder `app/release/`.
8. Name the file:

```text
whenyah-V1.1.apk
```

9. Finish.

Do **not** upload `app-release-unsigned.apk`.

### Option B — Command line

```bash
export SDK=/home/fizz/Android/Sdk
export BT="$SDK/build-tools/37.0.0"

./gradlew :app:assembleRelease

"$BT/zipalign" -p -f 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  app/release/whenyah-V1.1-aligned.apk

"$BT/apksigner" sign \
  --ks /path/to/your-keystore.jks \
  --ks-key-alias your-key-alias \
  --out app/release/whenyah-V1.1.apk \
  app/release/whenyah-V1.1-aligned.apk
```

`apksigner` will prompt for the keystore password.

---

## Step 4 — Verify the APK before uploading

### Version

```bash
export SDK=/home/fizz/Android/Sdk
export BT="$SDK/build-tools/37.0.0"

"$BT/aapt2" dump badging app/release/whenyah-V1.1.apk | head -1
```

Expected:

```text
versionCode='2' versionName='1.1'
```

### Signature

```bash
"$BT/apksigner" verify --print-certs app/release/whenyah-V1.1.apk
```

Expected SHA-256:

```text
c3e152abc484add379785c2dba04553179549e2e18ae89672fa4d07d596e4491
```

If the fingerprint differs, stop. You used the wrong keystore.

---

## Step 5 — Test an actual update over v1.0

The debug build installed on a development phone is signed with the debug key, so a release-signed APK will not install over it.

To test the real update path:

1. Use a second phone or an emulator.
2. Install `app/release/whenyah-V1.0.apk`.
3. Install `app/release/whenyah-V1.1.apk` over it.
4. It should update without asking you to uninstall.

If you see `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, the signatures differ. Do not publish.

On a development phone, you can only install v1.1 after uninstalling the debug build. Export a backup first if you care about the test data.

---

## Step 6 — Commit and push the code

Check what changed:

```bash
git status
```

Do not use `git add .` blindly. `app/release/` is now gitignored, but check the status anyway.

If `app/release/` metadata was committed before the ignore rule existed, untrack it without deleting the files:

```bash
git rm -r --cached app/release
```

Commit the app code, tests, docs, manifest, and build file. Do not commit the signed APK or the keystore.

```bash
git add app/src app/build.gradle.kts app/schemas \
        AGENTS.md README.md docs/RELEASING.md docs/update.json
git commit -m "Add update checker and reminder scheduling fixes"
git push origin main
```

---

## Step 7 — Create the GitHub release

1. Open the repository on GitHub.
2. Go to **Releases → Draft a new release**.
3. Create a new tag:

```text
v1.1
```

4. Target branch: `main`.
5. Release title:

```text
When Yah v1.1
```

6. **Do not** mark it as a prerelease. `releases/latest` ignores prereleases.

7. Release description:

```markdown
## When Yah v1.1

- Fixed weekly/monthly reminders firing daily.
- Added automatic update checks from GitHub Releases (daily check, 3-day “Later” snooze, Settings toggle + manual check).
- Database migrated to v2; existing reminders are preserved.
- **v1.0 users:** install this APK manually once. v1.1 and newer can detect future updates automatically.

Download the APK from the Assets section below.
```

8. Attach two assets:

   - `app/release/whenyah-V1.1.apk`
   - `docs/update.json` — the uploaded asset must be named exactly:

```text
update.json
```

9. Publish the release.

---

## Step 8 — Verify the update URL

After publishing:

```bash
curl -L https://github.com/HerrscherX86/when-ychhh/releases/latest/download/update.json
```

Expected:

```json
{
  "versionCode": 2,
  "versionName": "1.1",
  "releaseUrl": "https://github.com/HerrscherX86/when-ychhh/releases/latest",
  "notes": "Perbaikan jadwal reminder mingguan/bulanan dan pengecekan update dari GitHub."
}
```

If it returns 404:

- the release is still a draft,
- it is marked as a prerelease,
- or the asset is not named exactly `update.json`.

The app uses exactly this URL. You can also open it in a browser.

---

## Step 9 — Test the real checker on v1.1

Install the signed v1.1 on a device where it can be installed.

Open **Settings → About → Check for updates**.

Because the manifest says `versionCode: 2` and v1.1 is version code 2, it should say:

```text
You're on the latest version.
```

That confirms the URL, network request, JSON parsing, and version comparison.

You will not see the update dialog for v1.1 itself. The first real in-app update prompt will happen when you release v1.2.

---

## Step 10 — Tell v1.0 users

v1.0 does not have the update checker. For this release only:

- Post the v1.1 release link wherever v1.0 was announced.
- Ask users to install the v1.1 APK manually once.
- From v1.1 onward, future updates can be detected automatically.

---

## Every future release

1. Bump the version in `app/build.gradle.kts`:

```kotlin
versionCode = 3
versionName = "1.2"
```

2. Build and sign with the same keystore.
3. Update `docs/update.json`:

```json
{
  "versionCode": 3,
  "versionName": "1.2",
  "releaseUrl": "https://github.com/HerrscherX86/when-ychhh/releases/latest",
  "notes": "What changed in 1.2."
}
```

4. Create a normal (non-prerelease) GitHub release.
5. Attach the signed APK and `update.json`.

Users on v1.1+ will see the prompt after the next daily check, or immediately if they tap **Check for updates**.

---

## Troubleshooting

| Problem | Likely cause |
|---|---|
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Different signing key |
| `INSTALL_FAILED_VERSION_DOWNGRADE` | `versionCode` is not higher |
| “Couldn't check for updates” | `update.json` URL is 404, the release is prerelease, or the asset name is wrong |
| “You're on the latest version” when you expected a prompt | `update.json` `versionCode` is not higher than the installed app |
| Update dialog never appears automatically | 24-hour check throttle, 3-day “Later” snooze, or automatic checks disabled in Settings |
| App does not open GitHub after tapping Update | No browser/handler on the device; the app shows an error toast |

The most important checkpoint is **Step 4**: the v1.1 signature must match v1.0’s SHA-256.
