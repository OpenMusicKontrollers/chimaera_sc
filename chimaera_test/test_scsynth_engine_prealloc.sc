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
	var inst, txrx, chimconf, baseID, leadID;

	thisProcess.openUDPPort(4444);
	txrx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, txrx, txrx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:57110"); // send to scsynth port
	chimconf.sendMsg("/chimaera/output/offset", 0.001); // add 1ms offset to bundle timestamps

	chimconf.sendMsg("/chimaera/scsynth/enabled", true); // enable scsynth output engine
	chimconf.sendMsg("/chimaera/scsynth/prealloc", true); // use prealloc mode of scsynth output engine
	chimconf.sendMsg("/chimaera/scsynth/offset", 2000); // offset of preallocated scsynth ids
	chimconf.sendMsg("/chimaera/scsynth/modulo", 4); // number of preallocated scsynth ids to cycle through

	baseID = 0;
	leadID = 1;

	"../templates/two_groups.sc".load.value(baseID, leadID);
	"../instruments/sine.sc".load.value(\base);
	"../instruments/sine.sc".load.value(\lead);

	s.sync;

	// preallocate 4 synths per group
	(
		type: \on,
		addAction: \addToHead,
		instrument: \base,
		id: [2000, 2001, 2002, 2003],
		group: baseID,
		out: baseID,
		freq: 0.1, // we need to initialize this
		amp: 0.1
	).play;

	(
		type: \on,
		addAction: \addToHead,
		instrument: \lead,
		id: [2004, 2005, 2006, 2007],
		group: leadID,
		out: leadID,
		freq: 0.1, // we need to initialize this
		amp: 0.1
	).play;

	(
		type: \off,
		id: [2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007]
	).play;

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/set", baseID, \base, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/set", leadID, \lead, ChimaeraConf.south, 0.0, 1.0); // add group
})
