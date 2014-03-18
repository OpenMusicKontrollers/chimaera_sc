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

ChimaeraOutSCSynth2F : ChimaeraOut {
	var bndl, sidOffset, gidOffset, instruments, s;

	init {|iS, n, groups|
		instruments = groups;
		bndl = List.new(32);
		s = iS;

		gidOffset = 100;
		sidOffset = 200;

		instruments do: {|v, gid| 
			s.sendMsg('/g_new', gid+gidOffset, \addToHead.asInt, 0);
		}
	}

	start { |time|
		bndl.clear;
	}

	end { |time|
		var lag;
		lag = time - SystemClock.beats;	
		if(lag < 0) {
			("message late"+(lag*1000)+"ms").postln;
		};
		s.listSendBundle(lag, bndl);
	}

	on { |time, sid, gid, pid, x, z| // set callback function for blob on-events
		s.sendMsg('/s_new', instruments[gid], sid+sidOffset, \addToHead, gid+gidOffset, 'out', gid, 'gate', 0);
		bndl = bndl.add(['/n_set', sid+sidOffset, 0, x, 1, z, 2, pid, 'gate', 1]);
	}

	off { |time, sid, gid, pid| // set callback function for blob off-events
		bndl = bndl.add(['/n_set', sid+sidOffset, 'gate', 0]);
	}

	set { |time, sid, gid, pid, x, z| // set callback function for blob set-events
		bndl = bndl.add(['/n_set', sid+sidOffset, 0, x, 1, z, 2, pid]);
	}

	idle { |time|
		instruments do: {|v, gid| 
			bndl = bndl.add(['/n_set', gid+gidOffset, 'gate', 0]);
		}
	}
}