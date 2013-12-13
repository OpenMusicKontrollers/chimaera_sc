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

ChimaeraInDummy : ChimaeraIn {
	var on, off, set, idle, last;

	init {|s, conf, rx, iEngine|
		engine = iEngine;

		last = 0;

		conf.sendMsg("/chimaera/dummy/enabled", true); // enable dummy output engine

		on = OSCFunc({ |msg, time, addr, port|
			this.update(time);
			engine.on(time, msg[1], msg[2], msg[3], msg[4], msg[5]);
		}, "/on", rx);

		off = OSCFunc({ |msg, time, addr, port|
			this.update(time);
			engine.off(time, msg[1], msg[2], msg[3]);
		}, "/off", rx);

		set = OSCFunc({ |msg, time, addr, port|
			this.update(time);
			engine.set(time, msg[1], msg[2], msg[3], msg[4], msg[5]);
		}, "/set", rx);

		idle = OSCFunc({ |msg, time, addr, port|
			this.update(time);
			engine.idle(time);
		}, "/idle", rx);
	}

	update {|time|
		if(time != last) {
			engine.end(last);
			engine.start(time);
			last = time;
		}
	}
}
