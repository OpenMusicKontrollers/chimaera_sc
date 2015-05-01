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

	on {|time, sid, gid, pid, x, z, vx, vz| // set callback function for blob on-events
		var lag = ChimaeraOut.timeToLatency(time);

		lookup[sid] = gid;

		if(gid==0) {
			s.sendMsg('/s_new', instruments[gid], sid+sidOffset, \addToHead, grp, 4, pid, 'out', gid, 'gate', 1);
			s.sendBundle(lag, ['/n_setn', sid+sidOffset, 0, 4, x, z, vx, vz]);
		} {
			s.sendBundle(lag, ['/n_set', grp, 9, pid]);
			s.sendBundle(lag, ['/n_setn', grp, 5, 4, x, z, vx, vz]);
		};
	}

	off {|time, sid| // set callback function for blob off-events
		var lag = ChimaeraOut.timeToLatency(time);
		var gid;
		
		gid = lookup[sid];

		if(gid==0) {
			s.sendBundle(lag, ['/n_set', sid+sidOffset, 'gate', 0]);
		};

		lookup[sid] = nil;
	}

	set {|time, sid, x, z, vx, vz| // set callback function for blob set-events
		var lag = ChimaeraOut.timeToLatency(time);
		var gid;
		
		gid = lookup[sid];

		if(gid==0) {
			s.sendBundle(lag, ['/n_setn', sid+sidOffset, 0, 4, x, z, vx, vz]);
		} {
			s.sendBundle(lag, ['/n_setn', grp, 5, 4, x, z, vx, vz]);
		};
	}

	idle {|time|
		var lag = ChimaeraOut.timeToLatency(time);

		s.sendBundle(lag, ['/n_set', 0+gidOffset, 'gate', 0]);
	}
}
