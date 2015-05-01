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

ChimaeraOut {
	classvar <immediate;

	*new {|s, n, groups|
		^super.new.init(s, n, groups);
	}

	*initClass {
		immediate = 2085978496;
	}

	*timeToLatency {|time|
		var latency;

		if(time <= immediate, {
			latency = 0;
		}, {
			latency = time - SystemClock.seconds;

			if(latency < 0, {
				("message late"+(latency*1000)+"ms").postln;
			});
		});

		^latency;
	}

	init {|s, n, groups|
		"init".postln;
	}

	on {|time, sid, gid, pid, x, z, vx, vz|
		"on".postln;
	}

	off {|time, sid|
		"off".postln;
	}

	set {|time, sid, x, z, vx, vz|
		"set".postln;
	}

	idle {|time|
		"idle".postln;
	}
}
