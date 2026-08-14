# NEXA release shrinking rules.
# The baseline ships with proguard-android-optimize.txt (see build.gradle.kts).
# Library-specific keep rules are added here alongside the dependency that needs
# them, never speculatively.

# Timber: no reflection use; safe under R8 defaults.
# Hilt/Room/Compose: ship consumer rules in their AARs; no manual rules required.
