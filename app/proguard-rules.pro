# AndroidX libraries used by StarIntel Wear publish their own consumer rules.
# Keep this file intentionally small so R8 can remove the unused Compose/icon graph.

# Preserve generic signatures and annotations used by AndroidX runtime components.
-keepattributes Signature,*Annotation*
