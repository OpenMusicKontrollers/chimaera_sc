#!/usr/bin/env sclang

/*
 * Copyright (c) 2014 Hanspeter Portner (dev@open-music-kontrollers.ch)
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
	var rx, tx, rate, chimconf, midio, func, effect;

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies

	rx = NetAddr ("chimaera.local", 3333);
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	rate = 3000;
	chimconf.sendMsg("/engines/reset"); // reset all output engines
	chimconf.sendMsg("/engines/offset", 0.002);
	
	chimconf.sendMsg("/engines/enabled", false);
	chimconf.sendMsg("/engines/server", true);
	chimconf.sendMsg("/engines/mode", "osc.tcp");
	chimconf.sendMsg("/engines/enabled", true, {|msg| rx.connect;}); // connect via TCP

	chimconf.sendMsg("/sensors/rate", rate);
	chimconf.sendMsg("/sensors/group/reset"); // reset groups
	chimconf.sendMsg("/sensors/group/attributes/0", 0.0, 1.0, false, true, false); // add group
	chimconf.sendMsg("/sensors/group/attributes/1", 0.0, 1.0, true, false, false); // add group

	effect = 0x07; // volume
	chimconf.sendMsg("/engines/oscmidi/enabled", true); // enable OSCMidi output engine
	chimconf.sendMsg("/engines/oscmidi/effect", effect); // effect corresponding to z-direction

	chimconf.sendMsg("/sensors/number", {|msg|
		var n=msg[0];
		Routine.run({
			var bot = 3*12 - 0.5 - (n % 18 / 6);
			var ran = n/3;
			chimconf.sendMsg("/engines/oscmidi/offset", bot); // lowest MIDI Note
			chimconf.sendMsg("/engines/oscmidi/range", ran); // MIDI Note range
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
