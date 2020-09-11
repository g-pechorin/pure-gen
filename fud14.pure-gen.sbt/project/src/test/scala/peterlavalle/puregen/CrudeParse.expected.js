
// Event(Flap,List(Text),Set(ActionGet(=,List(Text))))
	exports.fsfnFlap = a0 => () => foo.bar.CrudeParse.Flap(a0);
	exports.fsfiFlap = (j) => (n) => (p) => () => {
			var value = p.apply();
			return (null == value) ? n : j(value);
		};

// Sample(Ship,List(SInt32),Set(ActionGet(=,List(Real32))))
	exports.fsfnShip = a0 => () => foo.bar.CrudeParse.Ship(a0);
	exports.fsfiShip = (p) => () => p.apply();

// Signal(Vampire,List(),Set(ActionSet(=,List(Real64))))
	exports.fsfnVampire = () => foo.bar.CrudeParse.Vampire();
	exports.fsfoVampire = (p) => (o) => () => { p.apply(o); }
