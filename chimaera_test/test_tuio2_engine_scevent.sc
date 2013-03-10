#!/usr/bin/sclang

/*
 * Copyright (c) 2012-2013 Hanspeter Portner (agenthp@users.sf.net)
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 * 
 *     2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 * 
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */

s.options.blockSize = 16;
s.options.memSize = 65536;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var rx, tx, chimconf, chimtuio2, instruments, baseID, leadID;

	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333

	chimconf.sendMsg("/chimaera/tuio/enabled", true); // enable Tuio output engine
	chimconf.sendMsg("/chimaera/tuio/long_header", false); // use short Tuio frame header (default)

	baseID = 1; // group 0 on chimaera device responds to everything and should not be overwritten
	leadID = 2;

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/add", baseID, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/add", leadID, ChimaeraConf.south, 0.0, 1.0); // add group

	// create groups in sclang
	instruments = Dictionary.new;
	instruments[baseID] = \base;
	instruments[leadID] = \lead;

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
	SynthDef(\base, {|freq=0, amp=0, gate=1, out=0| // define our synth
		var env, sig, cutoff;

		env = EnvGen.kr(Env.asr(0.01, 1.0, 0.02, 1.0, -3), gate);
		cutoff = LinExp.kr(amp, 0, 1, (2*12).midicps, (8*12).midicps);
		sig = Pulse.ar([freq/2, freq, freq*2, freq*4], 0.2, mul:[0.5, 1, 0.5, 0.25]);
		sig = Mix.ar(sig);
		sig = RLPF.ar(sig, cutoff, 0.2, mul:env);
		sig = FreeVerb.ar(sig);
		OffsetOut.ar(out, sig);
	}).add;

	// definition of a synced saw oscillator as lead instrument
	SynthDef(\lead, {|freq=0, amp=0, gate=1, out=0| // define our synth
		var env, sig, vol, cut;
		vol = LinExp.kr(amp, 0.0, 1.0, 0.5, 1.0);
		env = EnvGen.kr(Env.asr(0.01, 1.0, 10.0, 1.0, -3), gate);
		sig = Pluck.ar(WhiteNoise.ar(0.1), gate, 1, freq.reciprocal, 10, 0.20);
		sig = (sig*amp*1000).distort;
		sig = FreeVerb.ar(sig, mix:0.8, room:0.5, damp:0.1, mul:vol*env);
		cut = LinExp.kr(amp, 0.0, 1.0, 500, 1000);
		sig = RLPF.ar(sig, freq:cut, rq:0.3);
		OffsetOut.ar(out, sig);
	}).add;

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	rx = NetAddr ("chimaera.local", 3333);
	chimtuio2 = ChimaeraTuio2(s, rx);

	chimtuio2.on = { |sid, tid, gid, x, z|
		var id, midikey;

		id = sid%1000+1000; // recycle synth ids between 1000-1999
		midikey = x*48+48;

		( // send on event (sets gate=1)
			type: \on,
			instrument: instruments[gid], // choose instrument according to group id
			id: id,
			group: gid, // set group membership to group id
			out: gid-1, // output channels start counting at 0, group ids at 1
			midinote: midikey, // set frequency via midinote
			amp: z,
		).play;
	};

	chimtuio2.off = { |sid, tid, gid|
		var id;

		id = sid%1000+1000;

		( // send off event (sets gate=0)
			type: \off,
			id: id,
		).play;

		( // send delayed kill event
			type: \kill,
			id: id,
			lag: 2
		).play;
	};

	chimtuio2.set = { |sid, tid, gid, x, z|
		var id, midikey;

		id = sid%1000+1000;
		midikey = x*48+48;

		( // send update event
			type: \set,
			id: id,
			midinote: midikey,
			amp: z
		).play;
	};
})
