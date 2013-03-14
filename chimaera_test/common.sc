{
	arg baseID, leadID;

	// create groups in scsynth via events
	(
		type: \group,
		id: baseID,
		group: 0 // as child of root group
	).play;

	(
		type: \group,
		id: leadID,
		group: 0 // as child of root group
	).play;

	// definition of a low-pass filtered pulse width oscillator as base instrument
	SynthDef(\base, {|x=0, z=0, p=0, gate=1, out=0| // define our synth
		var env, freq, sig, cutoff;

		freq = LinExp.kr(x, 0, 1, (3*12).midicps, (7*12).midicps);

		env = EnvGen.kr(Env.asr(0.01, 1.0, 0.02, 1.0, -3), gate);
		cutoff = LinExp.kr(z, 0, 1, (1*12).midicps, (7*12).midicps);
		sig = Pulse.ar([freq/2, freq, freq*2, freq*4], 0.2, mul:[0.5, 1, 0.5, 0.25]);
		sig = Mix.ar(sig);
		sig = RLPF.ar(sig, cutoff, 0.2, mul:z*env);
		sig = FreeVerb.ar(sig);
		OffsetOut.ar(out, sig);
	}).add;

	// definition of a synced saw oscillator as lead instrument
	SynthDef(\lead, {|x=0, z=0, p=0, gate=1, out=0| // define our synth
		var env, freq, sig, vol, cut;

		freq = LinExp.kr(x, 0, 1, (3*12).midicps, (7*12).midicps);

		vol = LinExp.kr(z, 0.0, 1.0, 0.5, 1.0);
		env = EnvGen.kr(Env.asr(0.01, 1.0, 10.0, 1.0, -3), gate);
		sig = Pluck.ar(WhiteNoise.ar(0.1), gate, 1, freq.reciprocal, 10, 0.20);
		sig = (sig*z*1000).distort;
		sig = FreeVerb.ar(sig, mix:0.8, room:0.5, damp:0.1, mul:vol*env);
		cut = LinExp.kr(z, 0.0, 1.0, 500, 1000);
		sig = RLPF.ar(sig, freq:cut, rq:0.3);
		OffsetOut.ar(out, sig);
	}).add;

	SynthDef(\gain, {|amp=0, pan=0, out=0| // define our synth
		var sig;
		sig = In.ar(out);
		ReplaceOut.ar(out, sig*amp);
	}).add;

	SynthDef(\pan, { // define our synth
		var base, lead, sig;
		base = In.ar(baseID);
		lead = In.ar(leadID);
		base = Pan2.ar(base, -0.5, 0.3);
		lead = Pan2.ar(lead, 0.5, 0.3);
		sig = base + lead;
		ReplaceOut.ar(0, sig);
	}).add;

	1.wait;

	// group gains
	(
		type: \on,
		addAction: \addToTail,
		instrument: \gain,
		id: baseID+50,
		group: baseID,
		out: baseID,
		amp: 0.5
	).play;

	(
		type: \on,
		addAction: \addToTail,
		instrument: \gain,
		id: leadID+50,
		group: leadID,
		out: leadID,
		amp: 1.0
	).play;

	(
		type: \on,
		addAction: \addToTail,
		instrument: \pan,
		group: 0
	).play;

	"synths defined".postln;
}
