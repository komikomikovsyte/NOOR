# NOOR 0.6
Android 13 / Redmi Note 11 Pro oriented build.

## Daily lock-screen content
- Daily ayah: fetched from Al Quran Cloud in Arabic + Russian (Elmir Kuliev edition identifier `ru.kuliev`).
- Daily dua: selected from a curated list of Quranic supplication verses, fetched from the same Quran API.
- Cached locally: if internet is unavailable, the last successfully fetched content remains available.
- Android WorkManager requests a refresh roughly every 24 hours. MIUI/Android may delay background work to protect battery; opening NOOR also triggers a refresh request.
- Lock screen is implemented as public silent persistent notifications because Android 13 / MIUI does not provide a general third-party lock-screen widget surface comparable to home-screen AppWidget placement.
- No audio adhan.

After installation allow NOOR notifications and lock-screen notification content in MIUI settings. For the most reliable refresh, allow background activity/autostart if MIUI offers those controls.
