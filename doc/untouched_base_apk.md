
`art::MaybeOverrideVerbosity()`
`runtime/runtime_options.def`
// Parse-able keys from the command line.
- RUNTIME_OPTIONS_KEY (verifier::VerifyMode, Verify, verifier::VerifyMode::kEnable)
- RUNTIME_OPTIONS_KEY (Unit, NoDexFileFallback)

/home/weisgerber/mount/colossus04/weisgerber/aosp/aosp_7.1.1_r6_arm-eng/art/cmdline/cmdline.h => main()
- => art::Runtime => (Singleton, art::Runtime::Runtime is private)
  - art::Runtime::Create(RuntimeArgumentMap&& runtime_options) => Factory Method
art::StartRuntime()
art::Runtime::Init

art::OpenDexFilesFromImage()

## Interesting:
- JavaVMExt::JavaVMExt
- 




  // Look for a native bridge.
  //
  // The intended flow here is, in the case of a running system:
  //
  // Runtime::Init() (zygote):
  //   LoadNativeBridge -> dlopen from cmd line parameter.
  //  |
  //  V
  // Runtime::Start() (zygote):
  //   No-op wrt native bridge.
  //  |
  //  | start app
  //  V
  // DidForkFromZygote(action)
  //   action = kUnload -> dlclose native bridge.
  //   action = kInitialize -> initialize library
  //
  //
  // The intended flow here is, in the case of a simple dalvikvm call:
  //
  // Runtime::Init():
  //   LoadNativeBridge -> dlopen from cmd line parameter.
  //  |
  //  V
  // Runtime::Start():
  //   DidForkFromZygote(kInitialize) -> try to initialize any native bridge given.
  //   No-op wrt native bridge.

# runtime.h
// Contains the build fingerprint, if given as a parameter.
std::string fingerprint_;

// Oat file manager, keeps track of what oat files are open.
OatFileManager* oat_file_manager_;

