// Pipe(TTS,List(),Set(ActionSet(Silent,List(Real32)), ActionGet(Silence,List(Real32)), ActionSet(Speak,List(Real32, Text)), ActionGet(Spoken,List(Real32, Text)), ActionGet(Speaking,List(Real32, Text))))
exports.fsfnTTS = () => foo.bar.PipesTest.TTS();

exports.fsfiTTS = (n) => (Silence) => (Speaking) => (Spoken) => (p) => () => p.link([n, Silence, Speaking, Spoken]);

exports.fsfoTTS_Silent = (p) => (a0) => () => { p.post().Silent(a0); }
exports.fsfoTTS_Speak = (p) => (a0) => (a1) => () => { p.post().Speak(a0, a1); }
