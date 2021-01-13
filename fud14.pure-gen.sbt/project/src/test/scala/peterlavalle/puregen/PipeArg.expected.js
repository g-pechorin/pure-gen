exports._Foo =
  // the null one
    (eventNothing) =>
  // events
    (eventStatus) =>
  // structs
  // args
    (a0) =>
  // the actual call
  () => _S3_.foo.bar.PipeArg._new_Foo(
    // event constructors
      (a0) => eventStatus(a0),
    // struct constructors
    // args
      a0,
    // basic "null" constructor
      eventNothing
  )

exports._FooEvent = (Foo) => () =>
  () => {
    var out = Foo.event()
    // print("PipeArg/FooEvent = " + JSON.stringify(out))
    return out
  }
exports._FooSignalAction = (Foo) =>
  (a0) => () => Foo.signal().Action(a0)
