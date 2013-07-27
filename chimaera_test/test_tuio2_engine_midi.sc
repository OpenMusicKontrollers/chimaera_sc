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
	var rx, tx, chimconf, chimtuio2, midio, lookup, baseID, leadID, effect;

	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333
	chimconf.sendMsg("/chimaera/output/offset", 0.0012); // add 1.2ms offset to bundle timestamps
	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	chimconf.sendMsg("/chimaera/tuio/enabled", true); // enable Tuio output engine
	chimconf.sendMsg("/chimaera/tuio/long_header", false); // use short Tuio frame header (default)

	baseID = 0;
	leadID = 1;

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/set", baseID, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/set", leadID, ChimaeraConf.south, 0.0, 1.0); // add group

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	rx = NetAddr ("chimaera.local", 3333);
	chimtuio2 = ChimaeraTuio2(s, rx);

	MIDIClient.init;
	//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on MacOS, Windows to connect to the MIDI stream of choice
	midio = MIDIOut(0); // use this on Linux, as patching is usually done via ALSA/JACK
	midio.latency = 0; // send MIDI with no delay, instantaneously

	lookup = Order.new; // lookup table of currently active keys
	effect = 0x07; // volume

	chimtuio2.start = { |time|
		midio.latency = time - SystemClock.beats;
	};

	chimtuio2.end = { |time|
		midio.latency = 0;
	};

	chimtuio2.on = { |time, sid, pid, gid, x, z| // set callback function for blob on-events
		var midikey, cc;
		midikey = x*48+24;
		cc = (z*0x3fff).asInteger;
	
		//(time-SystemClock.beats).postln; //uncomment this to check whether there are late messages (if so, adjust the offset on the device)

		lookup[sid] = midikey.round;
		midio.noteOn(gid, lookup[sid], 0x7f); // we're using the group id (gid) as MIDI channel number
		midio.bend(gid, midikey-lookup[sid]/48*0x2000+0x2000); // we're using a pitchbend span of 4800 cents
		midio.control(gid, effect | 0x20, cc & 0x7f); // effect LSB
		midio.control(gid, effect | 0x00, cc >> 7); // effect MSB
	};

	chimtuio2.off = { |time, sid, pid, gid| // set callback function for blob off-events
		midio.noteOff(gid, lookup[sid], 0x00);
		lookup[sid] = nil;
	};

	chimtuio2.set = { |time, sid, pid, gid, x, z| // set callback function for blob set-events
		var midikey, cc;
		midikey = x*48+24;
		cc = (z*0x3fff).asInteger;

		midio.bend(gid, midikey-lookup[sid]/48*0x2000+0x2000); // we're using a pitchbend span of 4800 cents
		midio.control(gid, effect | 0x20, cc & 0x7f); // effect LSB
		midio.control(gid, effect | 0x00, cc >> 7); // effect MSB
	};

	chimtuio2.idle = { |time|
		midio.allNotesOff(baseID);
		midio.allNotesOff(leadID);
	};
}.value;
