exports._AudioFeed =
  // args
  // the actual call
  () => _S3_.foo.bar.S3Import._new_AudioFeed(
    // args
  )

exports._AudioFeedCycle = (AudioFeed) => () =>
  () => AudioFeed.sample()

exports._LogOut =
  // args
    (a0) =>
  // the actual call
  () => _S3_.foo.bar.S3Import._new_LogOut(
    // args
      a0
  )

exports._LogOutCycle = (LogOut) =>
  (a0) =>
  () => LogOut.behave(a0)
