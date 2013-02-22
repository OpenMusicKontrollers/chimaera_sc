Chimaera {
	classvar <south, <north, <both;
	var <>rx, <>tx, frm, tok, alv, reply, <>on, <>off, <>set, <>groupAddCb, old_blobs, new_blobs, cb;

	*new {|s, iRx, iTx|
		^super.new.init(s, iRx, iTx);
	}

	*initClass {
		south = 0x80;
		north = 0x100;
		both = 0x180;
	}

	initConn {|iRx, iTx|
		rx = iRx;
		tx = iTx;
		old_blobs = Dictionary.new;
		new_blobs = Dictionary.new;
		cb = Dictionary.new;

		// handling tuio messages
		frm = OSCFunc({|msg, time, addr, port|
			var fid, timestamp;

			//[msg, time, addr, port].postln;

			fid = msg[1];
			timestamp = msg[2];
		}, "/tuio2/frm", rx);

		tok = OSCFunc({|msg, time, addr, port|
			var sid, tuid, tid, gid, x, z;

			//[msg, time, addr, port].postln;

			sid = msg[1];
			tuid = msg[2];
			tid = tuid >> 16;
			gid = tuid & 0xffff;
			x = msg[3];
			z = msg[4];

			new_blobs[sid] = [sid, tid, gid, x, z];
		}, "/tuio2/_STxz", rx);

		alv = OSCFunc({|msg, time, addr, port|
			var n;
			
			//[msg, time, addr, port].postln;

			n = msg.size - 1;
			if (msg[1] == 'N') {n = 0};

			if (n != new_blobs.size) {
				n = new_blobs.size};

			// search for disappeard blobs
			old_blobs do: {|v|
				if (new_blobs[v[0]].isNil)
				{
					if(off.notNil) {off.value (v[0], v[1], v[2])};
				};
			};

			// search for new blobs
			new_blobs do: {|v|
				if (old_blobs[v[0]].isNil) {
					if(on.notNil) {on.value (v[0], v[1], v[2], v[3], v[4])};
				}
				{	
					if(set.notNil) {set.value (v[0], v[1], v[2], v[3], v[4])};
				};
			};

			old_blobs = new_blobs;
			new_blobs = Dictionary.new;
		}, "/tuio2/alv", rx);

		reply = OSCFunc({|msg, time, addr, port|
			var id, status;
			id = msg[1];
			status = msg[2];
			if(status) {
				cb[id].value(msg[3]);
			}
			{ //else
				"configure request failed".postln;
			};
			cb[id] = nil;
		}, "/reply", tx);

		on = nil;
		off = nil;
		set = nil;
		groupAddCb = nil;
	}

	init {|s, iRx, iTx|
		this.initConn(iRx, iTx);
	}

	groupAdd {|gid=0, pole=0x180, x0=0, x1=1|
		tx.sendMsg('/chimaera/group/add', gid, pole, x0, x1);
		if (groupAddCb.notNil)
		{
			groupAddCb.value(gid);
		};
	}

	configure {|... args|
		var path, callback;
		path = args[0];
		callback = args[args.size-1];
		if(callback.isFunction) {
			cb[13] = args[args.size-1];
		}
		{ // notFunction
			cb[13] = true;
		};
		tx.sendMsg(args[0], 13, args[1]);
	}
}
