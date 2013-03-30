#!/usr/bin/sclang

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

s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var rx, tx, chimconf, chimtuio2, instruments, baseID, leadID;

	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333
	chimconf.sendMsg("/chimaera/output/offset", 0.002); // add 1ms offset to bundle timestamps

	chimconf.sendMsg("/chimaera/tuio/enabled", true); // enable Tuio output engine
	chimconf.sendMsg("/chimaera/tuio/long_header", false); // use short Tuio frame header (default)

	baseID = 0;
	leadID = 1;

	"../templates/two_groups.sc".load.value(baseID, leadID);
	"../instruments/analog.sc".load.value(\base);
	"../instruments/syncsaw.sc".load.value(\lead);

	// create groups in sclang
	instruments = Dictionary.new;
	instruments[baseID] = \base;
	instruments[leadID] = \lead;

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/set", baseID, \base, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/set", leadID, \lead, ChimaeraConf.south, 0.0, 1.0); // add group

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	rx = NetAddr ("chimaera.local", 3333);
	chimtuio2 = ChimaeraTuio2(s, rx);

	chimtuio2.on = { |time, sid, pid, gid, x, z|
		var id, lag;

		id = sid+1000; // recycle synth ids between 1000-1999
		lag = time - SystemClock.beats;	

		//["on", sid].postln;

		( // send on event (sets gate=1)
			type: \on,
			addAction: \addToHead,
			instrument: instruments[gid], // choose instrument according to group id
			id: id,
			group: gid, // set group membership to group id
			out: gid, // output channels start counting at 0
			freq: x,
			amp: z,
			lag: lag
		).play;
	};

	chimtuio2.off = { |time, sid, pid, gid|
		var id, lag;

		id = sid+1000;
		lag = time - SystemClock.beats;	

		//["off", sid].postln;

		( // send off event (sets gate=0)
			type: \off,
			id: id,
			lag: lag
		).play;

		( // send delayed kill event
			type: \kill,
			id: id,
			lag: lag+1
		).play;
	};

	chimtuio2.set = { |time, sid, pid, gid, x, z|
		var id, lag;

		id = sid+1000;
		lag = time - SystemClock.beats;	

		( // send update event
			type: \set,
			id: id,
			freq: x,
			amp: z,
			lag: lag
		).play;
	};
})
