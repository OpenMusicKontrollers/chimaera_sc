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
	var rx, tx, rate, chimconf, midio, func, immediate, protocol;

	immediate = 2085978496;
	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies

	rx = NetAddr ("chimaera.local", 3333);
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	rate = 3000;
	protocol = \tcp; // choose between \udp and \tcp (TCP is recommended because of the MIDI note-off problem)
	chimconf.sendMsg("/engines/reset"); // reset all output engines
	chimconf.sendMsg("/engines/offset", 0.002);
	
	chimconf.sendMsg("/engines/enabled", false);
	if(protocol == \tcp, {
		chimconf.sendMsg("/engines/server", true);
		chimconf.sendMsg("/engines/mode", "osc.tcp");
		chimconf.sendMsg("/engines/enabled", true, {|msg| rx.connect.postln;}); // connect via TCP
	}, { // protocol == \udp
		chimconf.sendMsg("/engines/server", false);
		chimconf.sendMsg("/engines/mode", "osc.udp");
		chimconf.sendMsg("/engines/enabled", true);
	});

	chimconf.sendMsg("/sensors/rate", rate);
	chimconf.sendMsg("/sensors/group/reset"); // reset groups
	chimconf.sendMsg("/sensors/group/attributes/0", 0.0, 1.0, false, true, false); // add group
	chimconf.sendMsg("/sensors/group/attributes/1", 0.0, 1.0, true, false, false); // add group

	chimconf.sendMsg("/engines/oscmidi/enabled", true); // enable OSCMidi output engine
	chimconf.sendMsg("/engines/oscmidi/multi", true);
	chimconf.sendMsg("/engines/oscmidi/format", "int32");
	chimconf.sendMsg("/engines/oscmidi/path", "/midi");

	chimconf.sendMsg("/sensors/number", {|msg|
		var n=msg[0];
		Routine.run({
			var bot = 3*12 - 0.5 - (n % 18 / 6);
			var ran = n/3;
			chimconf.sendMsg("/engines/oscmidi/reset");
			chimconf.sendMsg("/engines/oscmidi/attributes/0", "control_change", bot, ran, 0x07);
			chimconf.sendMsg("/engines/oscmidi/attributes/1", "note_pressure", bot, ran, 0x07);
		}, clock:AppClock);
	});

	MIDIClient.init;
	//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on MacOS, Windows to connect to the MIDI stream of choice
	midio = MIDIOut(0); // use this on Linux, as patching is usually done via ALSA/JACK

	// OSC MIDI responder
	func = OSCFunc({|msg, time, addr, port|
		// handle time stamp
		if(time <= immediate, { // no time synchronization
			midio.latency = 0; // schedule MIDI for immediate execution
		}, {
			midio.latency = time - SystemClock.seconds;
			if(midio.latency < 0, {
				midio.latency = 0; // schedule MIDI for immediate execution
				("message late"+(midio.latency*1000)+"ms").postln
			});
		});

		msg.removeAt(0); // remove path
		msg.do({|i| // iterate over all MIDI messages
			var hiStatus = i & 0xf0; // upper status byte
			var loStatus = i & 0x0f; // lower status byte (channel)
			var dat0 = (i & 0x7f00) >> 8; // data byte 0
			var dat1 = (i & 0x7f0000) >> 16; // data byte 1
			midio.write(3, hiStatus, loStatus, dat0, dat1);
		});
	}, "/midi", rx);
}.value;
