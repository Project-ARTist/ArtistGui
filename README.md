# ArtistGui

> This is the current deployment App / GUI for the modified dex2oat compiler.

For further information, please visit the [ARTist project website](https://artist.cispa.saarland).

## Third-party Code and Dependencies

All Third-party code has a valid license (Apache-2.0) and is either included or defined as
gradle dependency.

### via gradle dependency

- Android Stuff
    - com.android.support:support-v13:25.1.0
    - com.android.support:appcompat-v7:25.1.0
    - com.android.support:design:25.1.0
    - com.android.support.constraint:constraint-layout:1.0.2
- Spongycastle:
    - com.madgag.spongycastle:core:1.54.0.0
    - com.madgag.spongycastle:pkix:1.54.0.0
    - com.madgag.spongycastle:prov:1.54.0.0
- Utilities:
    - co.trikita:log:1.1.5
    - org.apache.directory.studio:org.apache.commons.io:2.4
- Testing
    - junit:junit:4.12

### via included sourcode

- Kellinwood-ZipSigner (heavily modified and stripped)
    - https://github.com/AllanWang/Kellinwood-ZipSigner
    - https://code.google.com/archive/p/zip-signer/source/default/source
- Android's dex-lib: https://android.googlesource.com/platform/libcore/dex
- Android's dx-tool: https://android.googlesource.com/platform/dalvik/dx
