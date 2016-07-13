#!/usr/bin/env sclang

/*
 * Copyright (c) 2015 Hanspeter Portner (dev@open-music-kontrollers.ch)
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the Artistic License 2.0 as published by
 * The Perl Foundation.
 * 
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Artistic License 2.0 for more details.
 * 
 * You should have received a copy of the Artistic License 2.0
 * along the source as a COPYING file. If not, obtain it from
 * http://www.perlfoundation.org/artistic_license_2_0.
 */

{
	var chimconf, midio, func;

	chimconf = ChimaeraConf(s,
		addr:"chimaera.local", prot:\tcp);

	chimconf.sendMsg("/sensors/group/reset");

	chimconf.sendMsg("/sensors/group/attributes/0/min", 0.0);
	chimconf.sendMsg("/sensors/group/attributes/0/max", 1.0);
	chimconf.sendMsg("/sensors/group/attributes/0/north", false);
	chimconf.sendMsg("/sensors/group/attributes/0/south", true);
	chimconf.sendMsg("/sensors/group/attributes/0/scale", false);

	chimconf.sendMsg("/sensors/group/attributes/1/min", 0.0);
	chimconf.sendMsg("/sensors/group/attributes/1/max", 1.0);
	chimconf.sendMsg("/sensors/group/attributes/1/north", true);
	chimconf.sendMsg("/sensors/group/attributes/1/south", false);
	chimconf.sendMsg("/sensors/group/attributes/1/scale", false);

	chimconf.sendMsg("/engines/oscmidi/enabled", true);
	chimconf.sendMsg("/engines/oscmidi/multi", true);
	chimconf.sendMsg("/engines/oscmidi/format", "midi");
	chimconf.sendMsg("/engines/oscmidi/path", "/midi");

	chimconf.sendMsg("/sensors/number", {|msg|
		var n=msg[0];
		Routine.run({
			var bot = 3*12 - 0.5 - (n % 18 / 6);
			var ran = n/3;
			chimconf.sendMsg("/engines/oscmidi/reset");

			chimconf.sendMsg("/engines/oscmidi/attributes/0/mapping", "control_change");
			chimconf.sendMsg("/engines/oscmidi/attributes/0/offset", bot);
			chimconf.sendMsg("/engines/oscmidi/attributes/0/range", ran);
			chimconf.sendMsg("/engines/oscmidi/attributes/0/controller", 0x07);

			chimconf.sendMsg("/engines/oscmidi/attributes/1/mapping", "control_change");
			chimconf.sendMsg("/engines/oscmidi/attributes/1/offset", bot);
			chimconf.sendMsg("/engines/oscmidi/attributes/1/range", ran);
			chimconf.sendMsg("/engines/oscmidi/attributes/1/controller", 0x07);
		}, clock:AppClock);
	});

	MIDIClient.init;
	//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on Windows
	midio = MIDIOut(0); // use this on Linux/OSX

	// OSC MIDI responder
	func = OSCFunc({|msg, time, addr, port|
		midio.latency = ChimaeraOut.timeToLatency(time);

		msg.removeAt(0); // remove path
		msg.do({|m| // iterate over all MIDI messages
			var hiStatus = m[1] & 0xf0; // upper status byte
			var loStatus = m[1] & 0x0f; // lower status byte (channel)
			var dat0 = m[2]; // data byte 0
			var dat1 = m[3]; // data byte 1
			[hiStatus, loStatus, dat0, dat1].postln;
			midio.write(3, hiStatus, loStatus, dat0, dat1);
		});
	}, "/midi", chimconf.rx);
}.value;
