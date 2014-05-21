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

ChimaeraConf {
	var tx, rx, count, success, fail, cb;

	*new {|s, iTx, iRx|
		^super.new.init(s, iTx, iRx);
	}

	*initClass {
	}

	initConn {|iTx, iRx|
		tx = iTx;
		rx = iRx;
		cb = Order.new;
		count = 1000.rand;

		success = OSCFunc({|msg, time, addr, port|
			var id;
			var dest;
		
			msg.removeAt(0); // path
			id = msg.removeAt(0);
			dest = msg.removeAt(0);
			this.success(id, dest, msg);
		}, "/success", rx);

		fail = OSCFunc({|msg, time, addr, port|
			var id;
			var dest;

			msg.removeAt(0); // path
			id = msg.removeAt(0);
			dest = msg.removeAt(0);
			this.fail(id, dest, msg);
		}, "/fail", rx);
	}

	init {|s, iTx, iRx|
		this.initConn(iTx, iRx);
	}

	success {|id, dest, msg|
		if (cb[id].isFunction) {
			cb.[id].value(msg);
		};
		cb.removeAt(id);
		("Chimaera request #"++id+"succeeded:"+dest).postln;
	}

	fail {|id, dest, msg|
		if (cb[id].notNil) {
			cb.removeAt(id);
			("Chimaera request #"++id+"failed:"+dest+"("++msg[0]++")").postln;
		};
	}

	sendMsg {|... args|
		var path, callback, res;

		path = args.removeAt(0);
		if(args[args.size-1].isFunction) {
			callback = args.pop;
		} {
			callback = true;
		};
		cb[count] = callback;
		if( (args[args.size-1].isArray) && (args[args.size-1].isString.not) ) {
			var arr = args.pop;
			args = args ++ arr;
			tx.performList(\sendMsg, path, count, args);
		} {
			tx.performList(\sendMsg, path, count, args);
		};

		// send timeout to ourselfs
		AppClock.sched(1, {
			this.fail(count, ["timed out"])
		});

		count = count + 1;
	}
}
