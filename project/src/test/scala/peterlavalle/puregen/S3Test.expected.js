
exports._AudioFeed =
  // args
  // the actual call
  () => _S3_.foo.bar.S3Test._new_AudioFeed(
    // args
  )

exports._AudioFeedCycle = (AudioFeed) => () =>
  () => AudioFeed.sample()

exports._LogOut =
  // args
    (a0) =>
  // the actual call
  () => _S3_.foo.bar.S3Test._new_LogOut(
    // args
      a0
  )

exports._LogOutCycle = (LogOut) =>
  (a0) =>
  () => LogOut.behave(a0)


exports._Sphinx4 =
  // the null one
    (eventNothing) =>
  // events
    (eventRecognised) =>
    (eventRotund) =>
  // structs
    (structGable) =>
    (structRetract) =>
    (structWordInfo) =>
  // args
  // the actual call
  () => _S3_.foo.bar.S3Test._new_Sphinx4(
    // event constructors
      (a0) => eventRecognised(a0),
      (a0, a1) => eventRotund(a0)(a1),
    // struct constructors
      (a0, a1) => structGable(a0)(a1),
      (a0, a1) => structRetract(a0)(a1),
      (a0, a1, a2) => structWordInfo(a0)(a1)(a2),
    // args
    // basic "null" constructor
      eventNothing
  )

exports._Sphinx4Event = (Sphinx4) => () =>
  () => {
    var out = Sphinx4.event()
    // print("S3Test/Sphinx4Event = " + JSON.stringify(out))
    return out
  }

exports._Sphinx4SignalLive = (Sphinx4) =>
  () => Sphinx4.signal().Live()

exports._Sphinx4SignalMute = (Sphinx4) =>
  (a0) => () => Sphinx4.signal().Mute(a0)

exports._Sphinx4SignalSkip = (Sphinx4) =>
  (a0) => (a1) => (a2) => (a3) => () => Sphinx4.signal().Skip(a0, a1, a2, a3)
