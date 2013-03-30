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

Server.supernova;
s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var inst, txrx, chimconf, instruments, baseID, leadID, stompotto, baseInst, leadInst;

	/*
	 * Chimaera
	 */
	thisProcess.openUDPPort(4444);
	txrx = NetAddr("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, txrx, txrx);

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
		"sine",
		"analog",
		"syncsaw",
		"pluck"
	];

	leadInst = [
		"grain",
		"analog",
		"syncsaw",
		"pluck"
	];

	"../templates/two_groups.sc".load.value(baseID, leadID);
	"../instruments/analog.sc".load.value(\base);
	"../instruments/syncsaw.sc".load.value(\lead);

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/set", baseID, \base, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/set", leadID, \lead, ChimaeraConf.south, 0.0, 1.0); // add group

	/*
	 * StompOtto
	 */
	thisProcess.openUDPPort(9999);
	stompotto = StompOtto(s, NetAddr("localhost", nil));

	stompotto.on = {|id| // set callback function for on-events
		["on", id].postln;

		if (id < 4) {
			("../instruments/"++baseInst[id]++".sc").load.value(\base);
		} {
			("../instruments/"++leadInst[id-4]++".sc").load.value(\lead);
		}
	};

	stompotto.off = {|id| // set callback function for off-events
		["off", id].postln;
	};
})
