// <@ir/>
	exports.fsfn<@name/> = <@args/>() => <@full/>.<@name/>(<@pass/>);
	exports.fsfi<@name/> = (j) => (n) => (p) => () => {
			var value = p.apply();
			return (null == value) ? n : j(value);
		};


