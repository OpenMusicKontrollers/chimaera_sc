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
	var rx, tx, rate, chimconf, chimin, chimout, midio, on , off, bend, control;

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies

	rx = NetAddr ("chimaera.local", 3333);
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	rate = 3000;
	chimconf.sendMsg("/engines/reset");
	chimconf.sendMsg("/engines/offset", 0.0025);

	chimconf.sendMsg("/engines/enabled", false);
	chimconf.sendMsg("/engines/server", true);
	chimconf.sendMsg("/engines/mode", "osc.tcp");
	chimconf.sendMsg("/engines/enabled", true, {|msg| rx.connect;}); // connect via TCP
	
	chimconf.sendMsg("/engines/custom/reset");
	chimconf.sendMsg("/engines/custom/append", "on",  "/noteOn", "i($g) i(3 12* 0.5- $n 18% 6/- $n 6/+) i(0x7f)");
	chimconf.sendMsg("/engines/custom/append", "off",  "/noteOff", "i($g) i(3 12* 0.5- $n 18% 6/- $n 6/+) i(0x7f)");
	chimconf.sendMsg("/engines/custom/append", "set", "/bend",  "i($g) i($x 0x3fff*)");
	chimconf.sendMsg("/engines/custom/append", "set", "/control",  "i($g) i(0x27) i($z 0x3fff* 0x7f&)");
	chimconf.sendMsg("/engines/custom/append", "set", "/control",  "i($g) i(0x07) i($z 0x3fff* 7>>)");

	chimconf.sendMsg("/engines/custom/enabled", true);

	chimconf.sendMsg("/sensors/rate", rate);
	chimconf.sendMsg("/sensors/group/reset"); // reset groups
	chimconf.sendMsg("/sensors/group/attributes/0", 0.0, 1.0, false, true, false); // add group
	chimconf.sendMsg("/sensors/group/attributes/1", 0.0, 1.0, true, false, false); // add group

	MIDIClient.init;
	//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on MacOS, Windows to connect to the MIDI stream of choice
	midio = MIDIOut(0); // use this on Linux, as patching is usually done via ALSA/JACK
	midio.latency = 0; // send MIDI with no delay, instantaneously

	on = OSCFunc({|msg, time, addr, port|
		midio.latency = time - SystemClock.beats;
		if(midio.latency < 0) {("message late"+(midio.latency*1000)+"ms").postln};

		midio.noteOn(msg[1], msg[2], msg[3]);
	}, "/noteOn", rx);

	off = OSCFunc({|msg, time, addr, port|
		midio.latency = time - SystemClock.beats;
		if(midio.latency < 0) {("message late"+(midio.latency*1000)+"ms").postln};

		midio.noteOff(msg[1], msg[2], msg[3]);
	}, "/noteOff", rx);

	bend = OSCFunc({|msg, time, addr, port|
		midio.latency = time - SystemClock.beats;
		if(midio.latency < 0) {("message late"+(midio.latency*1000)+"ms").postln};

		midio.bend(msg[1], msg[2]);
	}, "/bend", rx);

	control = OSCFunc({|msg, time, addr, port|
		midio.latency = time - SystemClock.beats;
		if(midio.latency < 0) {("message late"+(midio.latency*1000)+"ms").postln};

		midio.control(msg[1], msg[2], msg[3]);
	}, "/control", rx);
}.value;
