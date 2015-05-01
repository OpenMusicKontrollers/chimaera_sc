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

ChimaeraInDummy : ChimaeraIn {
	var on, off, set, idle;

	init {|s, conf, iEngine|
		engine = iEngine;

		conf.sendMsg("/engines/dummy/enabled", true); // enable dummy output engine
		conf.sendMsg("/engines/dummy/redundancy", false); // disable redundant output
		conf.sendMsg("/engines/dummy/derivatives", true); // enable derivatives 

		on = OSCFunc({ |msg, time, addr, port|
			engine.on(time, msg[1], msg[2], msg[3], msg[4], msg[5], msg[6], msg[7]) // sid, gid, pid, x, z, vx, vz
		}, "/on", conf.rx);

		off = OSCFunc({ |msg, time, addr, port|
			engine.off(time, msg[1]) // sid
		}, "/off", conf.rx);

		set = OSCFunc({ |msg, time, addr, port|
			engine.set(time, msg[1], msg[2], msg[3], msg[4], msg[5]) // sid, x, z, vx, vz
		}, "/set", conf.rx);

		idle = OSCFunc({ |msg, time, addr, port|
			engine.idle(time);
		}, "/idle", conf.rx);
	}
}
