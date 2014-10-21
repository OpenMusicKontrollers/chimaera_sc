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

ChimaeraOutMidi : ChimaeraOut {
	var midio, <>control, <>doublePrecision, bot, ran, lookup;

	init {|s, n, groups|
		MIDIClient.init;
		//midio = MIDIOut(0, MIDIClient.destinations[0].uid); // use this on MacOS, Windows to connect to the MIDI stream of choice
		midio = MIDIOut(0); // use this on Linux, as patching is usually done via ALSA/JACK
		midio.latency = 0; // send MIDI with no delay, instantaneously

		control = 0x07; // volume
		doublePrecision = true;

		bot = 2*12 - 0.5 - (n % 18 / 6);
		ran = n/3;

		lookup = Order.new; // lookup table of currently active keys
	}

	on { |time, sid, gid, pid, x, z| // set callback function for blob on-events
		var midikey, midinote, cc;

		midio.latency = time - SystemClock.seconds;
		if(midio.latency < 0) { ("message late"+(midio.latency*1000)+"ms").postln; };

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

	off { |time, sid| // set callback function for blob off-events
		var midinote, gid;

		midio.latency = time - SystemClock.seconds;
		if(midio.latency < 0) { ("message late"+(midio.latency*1000)+"ms").postln; };

		gid = lookup[sid][0];
		midinote =  lookup[sid][1];

		midio.noteOff(gid, midinote, 0x00);
		lookup[sid] = nil;
	}

	set { |time, sid, x, z| // set callback function for blob set-events
		var midikey, midinote, gid, cc;

		midio.latency = time - SystemClock.seconds;
		if(midio.latency < 0) { ("message late"+(midio.latency*1000)+"ms").postln; };

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

	idle { |time|
		midio.latency = time - SystemClock.seconds;
		if(midio.latency < 0) { ("message late"+(midio.latency*1000)+"ms").postln; };
	}
}
