/*
 * Copyright (c) 2012-2013 Hanspeter Portner (agenthp@users.sf.net)
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

ChimaeraConf {
	classvar <south, <north, <both;
	var tx, rx, count, success, fail, cb;

	*new {|s, iTx, iRx|
		^super.new.init(s, iTx, iRx);
	}

	*initClass {
		south = 0x80;
		north = 0x100;
		both = 0x180;
	}

	initConn {|iTx, iRx|
		tx = iTx;
		rx = iRx;
		cb = Dictionary.new;
		count = 0;

		success = OSCFunc({|msg, time, addr, port|
			var id;
			
			msg.removeAt(0); // path
			id = msg.removeAt(0);
			if (cb[id].isFunction) {
				cb[id].value(msg);
			};
			cb[id] = nil;
		}, "/success", rx);

		fail = OSCFunc({|msg, time, addr, port|
			var id;

			msg.removeAt(0); // path
			id = msg.removeAt(0);
			["ChimaeraConf: configure request failed", msg].postln;
			cb[id] = nil;
		}, "/fail", rx);
	}

	init {|s, iTx, iRx|
		this.initConn(iTx, iRx);
	}

	sendMsg {|... args|
		var path, callback, res;

		path = args.removeAt(0);
		if (args[args.size-1].isFunction) {
			callback = args.pop;
		} {
			callback = true;
		};
		cb[count] = callback;
		tx.performList(\sendMsg, path, count, args);
		count = count + 1;
	}
}
