# Notra project context (formerly Catat-Catat)

- User preference: put every new download, installation, build cache, temporary build file, emulator, and packaged artifact on drive D. Use `D:\CatatCatat-tools` for this project's tools and outputs. Reuse the existing Java/Android SDK/Gradle already installed on D.
- Editable source is this repository. `build.ps1` copies it to `D:\CatatCatat-tools\source` before building so Windows KSP does not cross drive roots.
- Product: native Android expense tracking in Indonesian; voice recognition, parsing, storage, and reports work offline. No network speech fallback. Review drafts before saving; support multiple expenses and manual corrections.
- Main target phone: Infinix GT 10 Pro, physical RAM 8 GB. Extended RAM is not the capacity baseline.
- Voice design follows the user's grayscale orb references. Draw and animate it natively; maintain simple Indonesian controls.
- Visible app name is Notra. Use the supplied stacked-notes logo. Keep the existing application ID, database, backup format, and signing key compatible with Catat-Catat updates. Settings offers Light, Dark, and System themes; default is System. Splash animates three dots into a rotating circle.
- See `RENCANA.md` for agreed behavior and `README.md` for build/use instructions. The user will test their own voice on the phone later; distinguish emulator/synthetic speech results from physical-phone performance.
