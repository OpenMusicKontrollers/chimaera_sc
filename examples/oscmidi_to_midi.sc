#!/usr/bin/sclang

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

{
	var rx, tx, chimconf, midio, func, effect;

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies

	rx = NetAddr ("chimaera.local", 3333);
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group", 0, ChimaeraConf.north, 0.0, 1.0, false); // add group
	chimconf.sendMsg("/chimaera/group", 1, ChimaeraConf.south, 0.0, 1.0, false); // add group

	effect = 0x07; // volume
	chimconf.sendMsg("/chimaera/sensors", {|msg|
		var n=msg[0];
		Routine.run({
			var bot = 3*12 - 0.5 - (n/3 % 12 / 2);
			var ran = n/3 + 1;
			chimconf.sendMsg("/chimaera/oscmidi/enabled", true); // enable OSCMidi output engine
			chimconf.sendMsg("/chimaera/oscmidi/offset", bot); // lowest MIDI Note
			chimconf.sendMsg("/chimaera/oscmidi/range", ran); // MIDI Note range
			chimconf.sendMsg("/chimaera/oscmidi/effect", effect); // effect corresponding to z-direction
		}, clock:AppClock);
	});

	MIDIClient.init;
	//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on MacOS, Windows to connect to the MIDI stream of choice
	midio = MIDIOut(0); // use this on Linux, as patching is usually done via ALSA/JACK
	midio.latency = 0; // send MIDI with no delay, instantaneously

	func = OSCFunc({|msg, time, addr, port|
		midio.latency = time - SystemClock.beats;
		if(midio.latency < 0) {("message late"+(midio.latency*1000)+"ms").postln};
		msg.removeAt(0); // remove path
		msg.do({|m|
			switch(0x100 + m[1], // int8 -> uint8
				0x90, {midio.noteOn(m[0], m[2], m[3])},
				0x80, {midio.noteOff(m[0], m[2], m[3])},
				0xe0, {midio.bend(m[0], (m[3]<<7) + m[2])},
				0xb0, {midio.control(m[0], m[2], m[3])}, 
			);
		});
	}, "/midi", rx);
}.value;
