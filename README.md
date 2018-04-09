# ArtistGui - The ARTist deployment app

[![Build Status](https://travis-ci.org/Project-ARTist/ArtistGui.svg?branch=master)](https://travis-ci.org/Project-ARTist/ArtistGui) [![Gitter](https://badges.gitter.im/Project-ARTist/meta.svg)](https://gitter.im/project-artist/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=body_badge)


ArtistGui is the Android application that allows to deploy and utilize ARTist on rooted stock devices. It provides the user interface and management logic while ARTist itself only takes care of instrumenting concrete apps. For more information about the ARTist ecosystem, see the dedicated section below. 

## Workflow

The current version of ArtistGui ships with a version of ARTist in the form of a specialized ```dex2oat``` compiler binary and some library dependencies (e.g., ```libart-compiler.so```), while later versions will be able to download and manage multiple versions of ARTist and several modules.

The user interface exposes a list of all currently installed applications and allows the user to recompile them with the current ARTist modules by tapping on the app name or icon. It is also possible to keep an app instrumented across updates so that whenever an app receives an update, the recompilation is repeated on the new code. 

Currently, a [CodeLib](https://github.com/Project-ARTist/CodeLib) that provides the methods that are utilized by ARTist needs to be provided by hand, but this will be fixed as a part of our beta release (see beta section below). 

## Quick Demo

The ARTist ecosystem is complicated to start with, but if you have a rooted device at hand, why not downloading a ready-made ArtistGui version that already ships with the correct ARTist version and demo modules? You can download ArtistGui versions that use our ```trace``` module, which writes the names of invoked Java methods to the log (method tracing) [here](https://artist.cispa.saarland/binaries/).

## Build

After checkout, make sure you initialize the dexterous git submodule:

```
git submodule update --init
```

ArtistGui is a regular Android app, so you can build it using gradle:

```
./gradlew build
```

In the current version, you need to explicitly plant the compiled ARTist version and CodeLib in the correct asset folders before deploying the app to the device. However, we have tool support to assist you here. In the ```scripts``` directory, there are scripts that automate building ARTist in the context of AOSP and copying the corresponding files into the correct folders (```dex2oat```, ```libartist-compiler.so```, ...). A lot will change here as soon as we reach beta state, i.e. building without AOSP and no more fiddling with binaries and codelibs (see beta section).

## Dexterous

ArtistGui relies on the [dexterous](https://github.com/Project-ARTist/dexterous) tool to prepare app apk files before providing them to ARTist. Currently, dexterous is embedded as a git submodule, but in the future, the current release will be pulled from GitHub. 

## Library Dependencies

All third-tarty code has a valid license (Apache-2.0) and is either included or defined as a
gradle dependency.

### gradle dependencies

- Android Support Libs:
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

### included sourcode

- Kellinwood-ZipSigner (heavily modified and stripped)
    - https://github.com/AllanWang/Kellinwood-ZipSigner
    - https://code.google.com/archive/p/zip-signer/source/default/source
- Android's dex-lib: https://android.googlesource.com/platform/libcore/dex
- Android's dx-tool: https://android.googlesource.com/platform/dalvik/dx


# ARTist - The Android Runtime Instrumentation and Security Toolkit

ARTist is a flexible open source instrumentation framework for Android's apps and Java middleware. It is based on the Android Runtime’s (ART) compiler and modifies code during on-device compilation. In contrast to existing instrumentation frameworks, it preserves the application's original signature and operates on the instruction level. 

ARTist can be deployed in two different ways: First, as a regular application using our [ArtistGui](https://github.com/Project-ARTist/ArtistGui) project (this repository) that allows for non-invasive app instrumentation on rooted devices, or second, as a system compiler for custom ROMs where it can additionally instrument the system server (Package Manager Service, Activity Manager Service, ...) and the Android framework classes (```boot.oat```). It supports Android versions after (and including) Marshmallow 6.0. 

For detailed tutorials and more in-depth information on the ARTist ecosystem, have a look at our [official documentation](https://artist.cispa.saarland) and join our [Gitter chat](https://gitter.im/project-artist/Lobby).

## Upcoming Beta Release

We are about to enter the beta phase soon, which will bring a lot of changes to the whole ARTist ecosystem, including a dedicated ARTist SDK for simplified Module development, a semantic versioning-inspired release and versioning scheme, an improved and updated version of our online documentation, great new Modules, and a lot more improvements. However, in particular during the transition phase, some information like the one in the repositories' README.md files and the documentation at [https://artist.cispa.saarland](https://artist.cispa.saarland) might be slightly out of sync. We apologize for the inconvenience and happily take feedback at [Gitter](https://gitter.im/project-artist/Lobby). To keep up with the current progress, keep an eye on the beta milestones of the Project: ARTist repositories and check for new blog posts at [https://artist.cispa.saarland](https://artist.cispa.saarland) . 

## Contribution

We hope to create an active community of developers, researchers and users around Project ARTist and hence are happy about contributions and feedback of any kind. There are plenty of ways to get involved and help the project, such as testing and writing Modules, providing feedback on which functionality is key or missing, reporting bugs and other issues, or in general talk about your experiences. The team is actively monitoring [Gitter](https://gitter.im/project-artist/) and of course the repositories, and we are happy to get in touch and discuss. We do not have a full-fledged contribution guide, yet, but it will follow soon (see beta announcement above). 

## Academia

ARTist is based on a paper called **ARTist - The Android Runtime Instrumentation and Security Toolkit**, published at the 2nd IEEE European Symposium on Security and Privacy (EuroS&P'17). The full paper is available [here](https://artist.cispa.saarland/res/papers/ARTist.pdf). If you are citing ARTist in your research, please use the following bibliography entry:

```
@inproceedings{artist,
  title={ARTist: The Android runtime instrumentation and security toolkit},
  author={Backes, Michael and Bugiel, Sven and Schranz, Oliver and von Styp-Rekowsky, Philipp and Weisgerber, Sebastian},
  booktitle={2017 IEEE European Symposium on Security and Privacy (EuroS\&P)},
  pages={481--495},
  year={2017},
  organization={IEEE}
}
```

There is a follow-up paper where we utilized ARTist to cut out advertisement libraries from third-party applications, move the library to a dedicated app (own security principal) and reconnect both using a custom Binder IPC protocol, all while preserving visual fidelity by displaying the remote advertisements as floating views on top of the now ad-cleaned application. The full paper **The ART of App Compartmentalization: Compiler-based Library Privilege Separation on Stock Android**, as it was published at the 2017 ACM SIGSAC Conference on Computer and Communications Security (CCS'17), is available [here](https://artist.cispa.saarland/res/papers/CompARTist.pdf).
