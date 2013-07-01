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

{
	var rx, tx, chimconf, chimtuio2, midio, baseID, leadID, note_on, note_off, pitch_bend, control_change;

	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333
	chimconf.sendMsg("/chimaera/output/offset", 0.0012); // add 1.2ms offset to bundle timestamps
	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	chimconf.sendMsg("/chimaera/oscmidi/enabled", true); // enable Tuio output engine
	chimconf.sendMsg("/chimaera/oscmidi/offset", 24); // lowest MIDI Note
	chimconf.sendMsg("/chimaera/oscmidi/effect", 0x07); // effect corresponding to z-direction

	baseID = 0;
	leadID = 1;

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/set", baseID, \base, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/set", leadID, \lead, ChimaeraConf.south, 0.0, 1.0); // add group

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	rx = NetAddr ("chimaera.local", 3333);

	MIDIClient.init;
	//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on MacOS, Windows to connect to the MIDI stream of choice
	midio = MIDIOut(0); // use this on Linux, as patching is usually done via ALSA/JACK
	midio.latency = 0; // send MIDI with no delay, instantaneously

	note_on = OSCFunc({|msg, time, addr, port|
		SystemClock.schedAbs(time, {
			midio.noteOn(msg[1], msg[2], msg[3]);
		});
	}, "/midi/note_on", rx);

	note_off = OSCFunc({|msg, time, addr, port|
		SystemClock.schedAbs(time, {
			midio.noteOff(msg[1], msg[2], msg[3]);
		});
	}, "/midi/note_off", rx);

	pitch_bend = OSCFunc({|msg, time, addr, port|
		SystemClock.schedAbs(time, {
			midio.bend(msg[1], msg[2]);
		});
	}, "/midi/pitch_bend", rx);

	control_change = OSCFunc({|msg, time, addr, port|
		SystemClock.schedAbs(time, {
			if(msg.size == 4) {
				midio.control(msg[1], msg[2], msg[3]);
			} {
				midio.control(msg[1], msg[2]);
			};
		});
	}, "/midi/control_change", rx);
}.value;
