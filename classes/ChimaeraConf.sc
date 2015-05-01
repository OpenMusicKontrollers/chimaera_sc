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

ChimaeraConf {
	var tx, <rx, count, success, fail, cb;

	*new {|s, addr="chimaera.local", prot=\udp, target=\lang, rate=3000, offset=0.0025|
		^super.new.init(s, addr, prot, target, rate, offset);
	}

	*initClass {
	}

	init {|s, addr, prot, target, rate, offset|
		var port;
		tx = NetAddr(addr, 4444);
		rx = NetAddr(addr, 3333);
		cb = Order.new;
		count = 1000.rand;

		// create success callback function
		success = OSCFunc({|msg, time, addr, port|
			var id;
			var dest;
		
			msg.removeAt(0); // path
			id = msg.removeAt(0);
			dest = msg.removeAt(0);
			this.success(id, dest, msg);
		}, "/success", tx);

		// create fail callback function
		fail = OSCFunc({|msg, time, addr, port|
			var id;
			var dest;

			msg.removeAt(0); // path
			id = msg.removeAt(0);
			dest = msg.removeAt(0);
			this.fail(id, dest, msg);
		}, "/fail", tx);
	
		// set port depending on target
		if(target == \lang, {
			port = NetAddr.langPort
		}, { // target == \serv
			port = s.addr.port
		});

		// initialization
		this.sendMsg("/engines/enabled", false);
		this.sendMsg("/engines/reset");
		this.sendMsg("/engines/offset", offset);
		if(prot == \udp, {
			this.sendMsg("/engines/address", "this:"++port, {
				this.sendMsg("/engines/server", false);
				this.sendMsg("/engines/mode", "osc.udp");
				this.sendMsg("/engines/enabled", true);
			});
		}, { // prot == \tcp
			if(target == \lang, {
				this.sendMsg("/engines/server", true);
				this.sendMsg("/engines/mode", "osc.tcp");
				this.sendMsg("/engines/enabled", true, {|msg| rx.connect;});
			}, { // target == \serv
				this.sendMsg("/engines/address", "this:"++port, {
					this.sendMsg("/engines/server", false);
					this.sendMsg("/engines/mode", "osc.tcp");
					this.sendMsg("/engines/enabled", true);
				});
			});
		});
		if(rate > 2000, {
			this.sendMsg("/engines/parallel", true);
		}, {
			this.sendMsg("/engines/parallel", false);
		});
		this.sendMsg("/sensors/rate", rate);
	}

	success {|id, dest, msg|
		if(cb[id].isFunction, {
			cb.[id].value(msg);
		});
		cb.removeAt(id);
		("Chimaera request #"++id+"succeeded:"+dest).postln;
	}

	fail {|id, dest, msg|
		if(cb[id].notNil, {
			cb.removeAt(id);
			("Chimaera request #"++id+"failed:"+dest+"("++msg[0]++")").postln;
		});
	}

	sendMsg {|... args|
		var path, callback, res;

		path = args.removeAt(0);
		if(args[args.size-1].isFunction, {
			callback = args.pop;
		}, {
			callback = true;
		});
		cb[count] = callback;
		if( (args[args.size-1].isArray) && (args[args.size-1].isString.not), {
			var arr = args.pop;
			args = args ++ arr;
			tx.performList(\sendMsg, path, count, args);
		}, {
			tx.performList(\sendMsg, path, count, args);
		});

		// send timeout to ourselfs
		AppClock.sched(1, {
			this.fail(count, ["timed out"])
		});

		count = count + 1;
	}
}
