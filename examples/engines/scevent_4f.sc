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

{ |baseGrp, baseOut|
	var bndl = List.new(16);

	(
		start: { |time|
			bndl.clear;
		},

		end: { |time|
			var lag;

			lag = time - SystemClock.beats;	
			s.listSendBundle(lag, bndl);
		},

		on: { |time, sid, pid, gid, x, z|
			var lag;

			sid = sid + 200;
			lag = time - SystemClock.beats;	
			//["on", time, sid, lag].postln;

			if(gid==baseOut) {
				s.sendMsg('/s_new', \base, sid, \addToHead, baseGrp, 'out', baseOut, 'gate', 0);
				bndl = bndl.add(['/n_set', sid, 0, x, 1, z, 2, pid, 'gate', 1]);
			} {
				bndl = bndl.add(['/n_set', baseGrp, 3, x, 4, z, 5, pid]);
			};
		},

		off: { |time, sid, pid, gid|
			var lag;

			sid = sid + 200;
			lag = time - SystemClock.beats;	
			//["off", time, sid].postln;

			if(gid==baseOut) {
				bndl = bndl.add(['/n_set', sid, 'gate', 0]);
			};
		},

		set: { |time, sid, pid, gid, x, z|
			var lag;

			sid = sid + 200;
			lag = time - SystemClock.beats;	

			if(gid==baseOut) {
				bndl = bndl.add(['/n_set', sid, 0, x, 1, z, 2, pid]);
			} {
				bndl = bndl.add(['/n_set', baseGrp, 3, x, 4, z, 5, pid]);
			};
		},

		idle: { |time|
			var lag;
		
			lag = time - SystemClock.beats;

			s.sendBundle(lag, ['/n_set', baseGrp, 'gate', 0]);
		}
	);
}
