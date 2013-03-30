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
