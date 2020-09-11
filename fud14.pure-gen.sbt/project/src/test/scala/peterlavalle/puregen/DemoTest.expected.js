
// Sample(Age,List(),Set(ActionGet(=,List(Real32))))
	exports.fsfnAge = () => foo.bar.DemoTest.Age();
	exports.fsfiAge = (p) => () => p.apply();

// Event(Eve,List(),Set(ActionGet(=,List(Text))))
	exports.fsfnEve = () => foo.bar.DemoTest.Eve();
	exports.fsfiEve = (j) => (n) => (p) => () => {
			var value = p.apply();
			return (null == value) ? n : j(value);
		};

// Signal(Kick,List(),Set(ActionSet(=,List(Real32))))
	exports.fsfnKick = () => foo.bar.DemoTest.Kick();
	exports.fsfoKick = (p) => (o) => () => { p.apply(o); }

// Signal(Status,List(Text),Set(ActionSet(=,List(Text))))
	exports.fsfnStatus = a0 => () => foo.bar.DemoTest.Status(a0);
	exports.fsfoStatus = (p) => (o) => () => { p.apply(o); }
