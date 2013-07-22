#!/usr/bin/sclang

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

s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var rx, tx, chimconf, chimtuio2, baseID, leadID, bndl;

	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333
	chimconf.sendMsg("/chimaera/output/offset", 0.002); // add 1ms offset to bundle timestamps
	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	chimconf.sendMsg("/chimaera/tuio/enabled", true); // enable Tuio output engine
	chimconf.sendMsg("/chimaera/tuio/long_header", false); // use short Tuio frame header (default)

	baseID = 0;
	leadID = 1;

	"../templates/single_group.sc".load.value(baseID);
	"../instruments/anabase_4d.sc".load.value(\base);

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/set", baseID, \base, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/set", leadID, \lead, ChimaeraConf.south, 0.0, 1.0); // add group

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	rx = NetAddr ("chimaera.local", 3333);
	chimtuio2 = ChimaeraTuio2(s, rx);

	bndl = List.new(8);

	chimtuio2.start = { |time|
		bndl.clear;
	};

	chimtuio2.end = { |time|
		var lag;

		lag = time - SystemClock.beats;	
		s.listSendBundle(lag, bndl);
	};

	chimtuio2.on = { |time, sid, pid, gid, x, z|
		var lag;

		sid = sid + 1000; // recycle synth ids between 1000-1999
		lag = time - SystemClock.beats;	
		//["on", time, sid, lag].postln;

		if(gid==baseID) {
			s.sendMsg('/s_new', \base, sid, \addToHead, gid, 'out', gid, 'gate', 0);
			bndl = bndl.add(['/n_set', sid, 0, x, 1, z, 2, pid, 'gate', 1]);
		} {
			bndl = bndl.add(['/n_set', baseID, 3, x, 4, z, 5, pid]);
		};
	};

	chimtuio2.off = { |time, sid, pid, gid|
		var lag;

		sid = sid + 1000;
		lag = time - SystemClock.beats;	
		//["off", time, sid].postln;

		if(gid==baseID) {
			bndl = bndl.add(['/n_set', sid, 'gate', 0]);
		};
	};

	chimtuio2.set = { |time, sid, pid, gid, x, z|
		var lag;

		sid = sid + 1000;
		lag = time - SystemClock.beats;	

		if(gid==baseID) {
			bndl = bndl.add(['/n_set', sid, 0, x, 1, z, 2, pid]);
		} {
			bndl = bndl.add(['/n_set', baseID, 3, x, 4, z, 5, pid]);
		};
	};

	chimtuio2.idle = { |time|
		var lag;
	
		lag = time - SystemClock.beats;

		s.sendBundle(lag, ['/n_set', baseID, 'gate', 0]);
	};
})
