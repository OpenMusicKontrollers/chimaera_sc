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
