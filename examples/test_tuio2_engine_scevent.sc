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
	var rx, tx, chimconf, chimtuio2, instruments, baseOut, leadOut, baseGrp, leadGrp, engine;

	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333
	chimconf.sendMsg("/chimaera/output/offset", 0.002); // add 1ms offset to bundle timestamps
	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	baseOut = 0;
	leadOut = 1;
	baseGrp = 100 + baseOut;
	leadGrp = 100 + leadOut;

	// create groups in sclang
	instruments = Order.new;
	instruments[baseOut] = \base;
	instruments[leadOut] = \lead;

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group", baseOut, ChimaeraConf.north, 0.0, 1.0, false); // add group
	chimconf.sendMsg("/chimaera/group", leadOut, ChimaeraConf.south, 0.0, 1.0, false); // add group

	chimconf.sendMsg("/chimaera/tuio2/enabled", true); // enable Tuio output engine
	chimconf.sendMsg("/chimaera/tuio2/long_header", false); // use short Tuio frame header (default)

	chimconf.sendMsg("/chimaera/sensors", {|msg|
		var n=msg[0];
		"templates/two_groups_separate.sc".load.value(baseOut, leadOut, baseGrp, leadGrp);
		"scsynth_instrument_chooser_2f.sc".load.value(n);
	});

	engine = "engines/scevent_2f.sc".load.value(instruments);

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	rx = NetAddr ("chimaera.local", 3333);
	chimtuio2 = ChimaeraTuio2(s, rx, engine);
})
