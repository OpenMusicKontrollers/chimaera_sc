#!/usr/bin/sclang

s.options.blockSize = 16;
s.options.memSize = 65536;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var sd, rx, tx, chim, midio, lookup;

	sd = SynthDef(\syncsaw, {|freq=0, amp=0, gate=1, out=0|
		var env, sig;

		env = EnvGen.kr(Env.asr(0.01, 1.0, 0.02, 1.0, -3), gate);
		sig = SyncSaw.ar(freq, LinExp.kr(amp, 0, 1, 100, 400), mul:env*amp);
		sig = FreeVerb.ar(sig);
		OffsetOut.ar(out, sig);
	}).add;

	thisProcess.openUDPPort(3333);
	thisProcess.openUDPPort(4444);

	//rx = NetAddr ("localhost", nil);
	//tx = NetAddr ("localhost", 4444);
	rx = NetAddr ("chimaera", 3333);
	tx = NetAddr ("chimaera", 4444);
	chim = Chimaera(s, rx, tx);
	//chim.configure('/chimaera/version', {|version| ["version", version].postln;});
	//chim.configure('/chimaera/comm/mac', {|mac| ["MAC", mac].postln;});
	//chim.configure('/chimaera/output/enabled', true);
	//chim.configure('/chimaera/tuio/enabled', true);
	//chim.configure('/chimaera/dump/enabled', true);

	MIDIClient.init;
	//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // MacOS, Windows
	midio = MIDIOut(0); // Linux
	midio.latency = 0;

	lookup = Dictionary.new;

	chim.on = { |sid, tid, gid, x, z|
		var midikey = x*48+40;

		(
			type: \on,
			instrument: \syncsaw,
			id: sid+1000,
			out: gid,
			midinote: midikey,
			amp: z
		).play;

		midio.noteOn(gid, midikey.round, 0x7f);
		lookup[sid] = midikey.round;
		midio.bend(gid, (midikey-lookup[sid])/48*0x2000+0x2000);
		midio.control(gid, 0x4a, z*0x7f); // sound controller 5
	};

	chim.off = { |sid, tid, gid|
		(
			type: \off,
			id: sid+1000,
		).play;

		(
			type: \kill,
			id: sid+1000,
			delay: 1
		).play;

		midio.noteOff(gid, lookup[sid], 0x00);
		lookup[sid] = nil;
		//midio.allNotesOff(gid);
	};

	chim.set = { |sid, tid, gid, x, z|
		var midikey = x*48+40;

		(
			type: \set,
			id: sid+1000,
			midinote: midikey,
			amp: z
		).play;

		midio.bend(gid, (midikey-lookup[sid])/48*0x2000+0x2000);
		midio.control(gid, 0x4a, z*0x7f); // sound controller 5
	};
})
