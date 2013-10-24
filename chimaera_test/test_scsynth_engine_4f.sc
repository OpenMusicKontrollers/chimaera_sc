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

Server.supernova;
s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var inst, txrx, chimconf, instruments, baseOut, leadOut, baseGrp, leadGrp;

	thisProcess.openUDPPort(4444);
	txrx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, txrx, txrx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output socket on device
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:57110"); // send to scsynth port
	chimconf.sendMsg("/chimaera/output/offset", 0.001); // add 1ms offset to bundle timestamps
	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	chimconf.sendMsg("/chimaera/interpolation/order", 2); // cubic interpolation

	baseOut = 0;
	leadOut = 1;
	baseGrp = 100 + baseOut;
	leadGrp = 100 + leadOut;

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/set", baseOut, ChimaeraConf.north, 0.0, 1.0); // add group
	chimconf.sendMsg("/chimaera/group/set", leadOut, ChimaeraConf.south, 0.0, 1.0); // add group

	chimconf.sendMsg("/chimaera/scsynth/enabled", true); // enable scsynth output engine
	chimconf.sendMsg("/chimaera/scsynth/group", baseOut, \base, 200, baseGrp, baseOut, 0, true, true, \addToHead.asInt, false);
	chimconf.sendMsg("/chimaera/scsynth/group", leadOut, \lead, 200, baseGrp, leadOut, 3, false, false, \addToHead.asInt, true);

	chimconf.sendMsg("/chimaera/sensors", {|msg|
		var n=msg[0];
		Routine.run({
			"../templates/single_group.sc".load.value(baseGrp);
			"../instruments/pluck_4f.sc".load.value(\base, n);
		}, clock:AppClock);
	});
});
