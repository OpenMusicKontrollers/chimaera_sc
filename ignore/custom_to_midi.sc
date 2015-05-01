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
	var chimconf, midio, on , off, bend, control;

	chimconf = ChimaeraConf(s,
		addr:"chimaera.local", prot:\tcp, target:\lang);

	chimconf.sendMsg("/engines/custom/reset");
	chimconf.sendMsg("/engines/custom/append",
		"on",  "/noteOn",		"i($g) i(35.5 $n 18% 6/- $n 3/ $x @@ $g[*+) i(0x7f)");
	chimconf.sendMsg("/engines/custom/append",
		"off", "/noteOff",	  "i($g) i(35.5 $n 18% 6/- $n 3/ $g]*+) i(0x7f)");
	chimconf.sendMsg("/engines/custom/append",
		"set", "/bend",			"i($g) i($x $g]- 0x2000* 0x1fff+)");
	chimconf.sendMsg("/engines/custom/append",
		"set", "/control",		"i($g) i(0x27) i($z 0x3fff* 0x7f&)");
	chimconf.sendMsg("/engines/custom/append",
		"set", "/control",		"i($g) i(0x07) i($z 0x3fff* 7>>)");
	chimconf.sendMsg("/engines/custom/enabled", true);

	chimconf.sendMsg("/sensors/group/reset");
	chimconf.sendMsg("/sensors/group/attributes/0",
		0.0, 1.0, false, true, false);
	chimconf.sendMsg("/sensors/group/attributes/1",
		0.0, 1.0, true, false, false);

	MIDIClient.init;
	//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on Windows
	midio = MIDIOut(0); // use this on Linux/OSX

	on = OSCFunc({|msg, time, addr, port|
		midio.latency = ChimaeraOut.timeToLatency(time);
		midio.noteOn(msg[1], msg[2], msg[3]);
	}, "/noteOn", chimconf.rx);

	off = OSCFunc({|msg, time, addr, port|
		midio.latency = ChimaeraOut.timeToLatency(time);
		midio.noteOff(msg[1], msg[2], msg[3]);
	}, "/noteOff", chimconf.rx);

	bend = OSCFunc({|msg, time, addr, port|
		midio.latency = ChimaeraOut.timeToLatency(time);
		midio.bend(msg[1], msg[2]);
	}, "/bend", chimconf.rx);

	control = OSCFunc({|msg, time, addr, port|
		midio.latency = ChimaeraOut.timeToLatency(time);
		midio.control(msg[1], msg[2], msg[3]);
	}, "/control", chimconf.rx);
}.value;
