#!/usr/bin/sclang

/*
 * Copyright (c) 2013 Hanspeter Portner (agenthp@users.sf.net)
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

Server.supernova;
s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var inst, txrx, chimconf, baseID, leadID, stompotto, baseInst, leadInst, looper, loadInst;

	/*
	 * load instrument
	 */
	loadInst = {|group, groupID, inst|
		( type: \off,
			id: groupID,
		).play;

		{s.sync}.fork;

		("../instruments/"++inst++".sc").load.value(group);

		{s.sync}.fork;
	};

	/*
	 * Chimaera
	 */
	thisProcess.openUDPPort(4444);
	txrx = NetAddr("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, txrx, txrx);

	chimconf.sendMsg("/chimaera/scsynth/enabled", true); // enable scsynth output engine

	chimconf.sendMsg("/chimaera/movingaverage/enabled", true); // enable moving average of ADC sampling
	chimconf.sendMsg("/chimaera/movingaverage/samples", 8); // set moving average window to 8 samples

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output socket on device
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:57110"); // send to scsynth port
	chimconf.sendMsg("/chimaera/output/offset", 0.001); // add 1ms offset to bundle timestamps

	chimconf.sendMsg("/chimaera/scsynth/enabled", true); // enable scsynth output engine
	chimconf.sendMsg("/chimaera/scsynth/instrument", \lead); // set scsynth instrument name
	chimconf.sendMsg("/chimaera/scsynth/prealloc", false); // use dynamic mode of scsynth output engine
	chimconf.sendMsg("/chimaera/scsynth/offset", 1000); // offset of new synthdef ids
	chimconf.sendMsg("/chimaera/scsynth/modulo", 8000); // modulo of new synthdef ids
	// id numbers on device will cycle linearly from offset to (offset+modulo) circularly

	baseID = 0;
	leadID = 1;

	baseInst = [
		"analog",
		"syncsaw",
		"sine",
		"grain",
		"pluck",
		"blip"
	];

	leadInst = [
		"analog",
		"syncsaw",
		"sine",
		"grain",
		"pluck",
		"blip"
	];

	"../templates/two_groups_separate.sc".load.value(baseID, leadID);
	//"../templates/two_groups.sc".load.value(baseID, leadID);
	loadInst.value(\base, baseID, baseInst[0]);
	loadInst.value(\lead, leadID, leadInst[0]);

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/set", baseID, \base, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/set", leadID, \lead, ChimaeraConf.south, 0.0, 1.0); // add group

	/*
	 * SooperLooper
	 */
	looper = SooperLooper(s, NetAddr("localhost", 9951));

	/*
	 * StompOtto
	 */
	thisProcess.openUDPPort(9999);
	stompotto = StompOtto(s, NetAddr("localhost", nil));

	stompotto.on = {|id| // set callback function for on-events
		["on", id].postln;

		switch(id,
			0, { baseInst=baseInst.rotate(-1); loadInst.value(\base, baseID, baseInst[0]) },
			1, { baseInst=baseInst.rotate( 1); loadInst.value(\base, baseID, baseInst[0]) },
			2, { leadInst=leadInst.rotate(-1); loadInst.value(\lead, leadID, leadInst[0]) },
			3, { leadInst=leadInst.rotate( 1); loadInst.value(\lead, leadID, leadInst[0]) },

			4, { looper.record(baseID) },
			5, { looper.substitute(baseID) },
			6, { looper.record(leadID) },
			7, { looper.substitute(leadID) });
	};

	stompotto.off = {|id| // set callback function for off-events
		["off", id].postln;

		switch(id,
			0, { baseInst=baseInst.rotate(-1); loadInst.value(\base, baseID, baseInst[0]) },
			1, { baseInst=baseInst.rotate( 1); loadInst.value(\base, baseID, baseInst[0]) },
			2, { leadInst=leadInst.rotate(-1); loadInst.value(\lead, leadID, leadInst[0]) },
			3, { leadInst=leadInst.rotate( 1); loadInst.value(\lead, leadID, leadInst[0]) },

			4, { looper.record(baseID) },
			5, { looper.substitute(baseID) },
			6, { looper.record(leadID) },
			7, { looper.substitute(leadID) });
	};
})
