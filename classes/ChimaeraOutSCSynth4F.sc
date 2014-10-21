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

ChimaeraOutSCSynth4F : ChimaeraOut {
	var sidOffset, gidOffset, instruments, s, grp, lookup;

	init {|iS, n, groups|
		instruments = groups;
		s = iS;

		gidOffset = 100;
		sidOffset = 200;

		grp = 0+gidOffset;

		s.sendMsg('/g_new', grp, \addToHead.asInt, 0);
		
		lookup = Order.new; // lookup table of currently active blobs
	}

	on { |time, sid, gid, pid, x, z| // set callback function for blob on-events
		var lag = time - SystemClock.seconds;	
		if(lag < 0) { ("message late"+(lag*1000)+"ms").postln; };

		lookup[sid] = gid;

		if(gid==0) {
			s.sendMsg('/s_new', instruments[gid], sid+sidOffset, \addToHead, grp, 'out', gid, 'gate', 0);
			s.sendBundle(lag, ['/n_set', sid+sidOffset, 0, x, 1, z, 2, pid, 'gate', 1]);
		} {
			s.sendBundle(lag, ['/n_set', grp, 3, x, 4, z, 5, pid]);
		};
	}

	off { |time, sid| // set callback function for blob off-events
		var gid;
		var lag = time - SystemClock.seconds;	
		if(lag < 0) { ("message late"+(lag*1000)+"ms").postln; };
		
		gid = lookup[sid];

		if(gid==0) {
			s.sendBundle(lag, ['/n_set', sid+sidOffset, 'gate', 0]);
		};

		lookup[sid] = nil;
	}

	set { |time, sid, x, z| // set callback function for blob set-events
		var gid;
		var lag = time - SystemClock.seconds;	
		if(lag < 0) { ("message late"+(lag*1000)+"ms").postln; };
		
		gid = lookup[sid];

		if(gid==0) {
			s.sendBundle(lag, ['/n_set', sid+sidOffset, 0, x, 1, z]);
		} {
			s.sendBundle(lag, ['/n_set', grp, 3, x, 4, z]);
		};
	}

	idle { |time|
		var lag = time - SystemClock.seconds;	
		if(lag < 0) { ("message late"+(lag*1000)+"ms").postln; };

		s.sendBundle(lag, ['/n_set', 0+gidOffset, 'gate', 0]);
	}
}
