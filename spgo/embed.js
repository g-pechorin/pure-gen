
var t = ['hey', 'you', 'key'];

print(t.length);

var save = (into, path, call) => {
	while (1 != path.length) {
		var next = path.shift();
		into = into[next] || (into[next] = {});
	}
	into[path[0]] = (a0) => call(a0);
}

var qqq = {};
save(qqq, t, _ => Duktape.version);

print('qqq is ' + qqq);
print('qqq.hey is ' + qqq.hey);
print('qqq.hey.you is ' + qqq.hey.you);
print('qqq.hey.you.key is ' + qqq.hey.you.key);
