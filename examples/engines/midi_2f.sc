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

{ |midio, bot, ran, effect|
	var lookup = Order.new; // lookup table of currently active keys

	(
		start: { |time|
			midio.latency = time - SystemClock.beats;
		},

		end: { |time|
			midio.latency = 0;
		},

		on: { |time, sid, pid, gid, x, z| // set callback function for blob on-events
			var midikey, cc;
			midikey = x*ran + bot;
			cc = (z*0x3fff).asInteger;
		
			//(time-SystemClock.beats).postln; //uncomment this to check whether there are late messages (if so, adjust the offset on the device)

			lookup[sid] = midikey.round;
			midio.noteOn(gid, lookup[sid], 0x7f); // we're using the group id (gid) as MIDI channel number
			midio.bend(gid, midikey-lookup[sid]/ran*0x2000+0x2000); // we're using a pitchbend span of ran*100 cents
			midio.control(gid, effect | 0x20, cc & 0x7f); // effect LSB
			midio.control(gid, effect | 0x00, cc >> 7); // effect MSB
		},

		off: { |time, sid, pid, gid| // set callback function for blob off-events
			midio.noteOff(gid, lookup[sid], 0x00);
			lookup[sid] = nil;
		},

		set:  { |time, sid, pid, gid, x, z| // set callback function for blob set-events
			var midikey, cc;
			midikey = x*ran + bot;
			cc = (z*0x3fff).asInteger;

			midio.bend(gid, midikey-lookup[sid]/ran*0x2000+0x2000); // we're using a pitchbend span of ran*100 cents
			midio.control(gid, effect | 0x20, cc & 0x7f); // effect LSB
			midio.control(gid, effect | 0x00, cc >> 7); // effect MSB
		},

		idle: { |time|
			midio.allNotesOff(baseID);
			midio.allNotesOff(leadID);
		}
	);
