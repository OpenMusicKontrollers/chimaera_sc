/*
 * Copyright (c) 2013 Hanspeter Portner (dev@open-music-kontrollers.ch)
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

{ | baseOut, leadOut, baseGrp, leadGrp|

	// create groups in scsynth via events
	s.sendMsg('/g_new', baseGrp, \addToHead.asInt, 0);
	s.sendMsg('/g_new', leadGrp, \addToHead.asInt, 0);

	SynthDef(\gain, {|amp=0, pan=0, out=0| // define our synth
		var sig;
		sig = In.ar(out);
		ReplaceOut.ar(out, sig*amp);
	}).add;

	SynthDef(\pan, { // define our synth
		var base, lead, sig;
		base = In.ar(baseOut);
		lead = In.ar(leadOut);
		base = Pan2.ar(base, -0.5, 0.3);
		lead = Pan2.ar(lead, 0.5, 0.3);
		sig = base + lead;
		ReplaceOut.ar(0, sig);
	}).add;

	s.sync;

	// group gains
	(
		type: \on,
		addAction: \addToTail,
		instrument: \gain,
		id: baseGrp+50,
		group: baseGrp,
		out: baseOut,
		amp: 0.5
	).play;

	(
		type: \on,
		addAction: \addToTail,
		instrument: \gain,
		id: leadGrp+50,
		group: leadGrp,
		out: leadOut,
		amp: 1.0
	).play;

	(
		type: \on,
		addAction: \addToTail,
		instrument: \pan,
		group: 0
	).play;
}
