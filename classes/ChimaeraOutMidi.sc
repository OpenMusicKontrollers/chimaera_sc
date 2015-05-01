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

ChimaeraOutMidi : ChimaeraOut {
	var midio, <>control, <>doublePrecision, bot, ran, lookup;

	init {|s, n, groups|
		MIDIClient.init;
		//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on Windows
		midio = MIDIOut(0); // use this on Linux/OSX
		midio.latency = 0; // send MIDI with no delay, instantaneously

		control = 0x07; // volume
		doublePrecision = true;

		bot = 2*12 - 0.5 - (n % 18 / 6);
		ran = n/3;

		lookup = Order.new; // lookup table of currently active keys
	}

	on {|time, sid, gid, pid, x, z, vx, vz| // set callback function for blob on-events
		var midikey, midinote, cc;

		midio.latency = ChimaeraOut.timeToLatency(time);

		midikey = x*ran+bot;
		midinote = midikey.round;

		lookup[sid] = [gid, midinote];
		midio.noteOn(gid, midinote, 0x7f); // we're using the group id (gid) as MIDI channel number
		midio.bend(gid, midikey-midinote/ran*0x2000+0x1fff); // we're using a pitchbend span of ran*100 cents
		if(doublePrecision) {
			cc = (z*0x3fff).asInteger;
			midio.control(gid, control | 0x20, cc & 0x7f); // control LSB
			midio.control(gid, control, cc >> 7); // control MSB
		} { // !doublePrecision
			cc = (z*0x7f).asInteger;
			midio.control(gid, control, cc); // control MSB
		}
	}

	off {|time, sid| // set callback function for blob off-events
		var midinote, gid;

		midio.latency = ChimaeraOut.timeToLatency(time);

		gid = lookup[sid][0];
		midinote =  lookup[sid][1];

		midio.noteOff(gid, midinote, 0x00);
		lookup[sid] = nil;
	}

	set {|time, sid, x, z, vx, vz| // set callback function for blob set-events
		var midikey, midinote, gid, cc;

		midio.latency = ChimaeraOut.timeToLatency(time);

		midikey = x*ran+bot;
		gid = lookup[sid][0];
		midinote = lookup[sid][1];

		midio.bend(gid, midikey-midinote/ran*0x2000+0x1fff); // we're using a pitchbend span of ran*100 cents
		if(doublePrecision) {
			cc = (z*0x3fff).asInteger;
			midio.control(gid, control | 0x20, cc & 0x7f); // control LSB
			midio.control(gid, control, cc >> 7); // control MSB
		} { // !doublePrecision
			cc = (z*0x7f).asInteger;
			midio.control(gid, control, cc); // control MSB
		}
	}

	idle {|time|
		midio.latency = ChimaeraOut.timeToLatency(time);
	}
}
