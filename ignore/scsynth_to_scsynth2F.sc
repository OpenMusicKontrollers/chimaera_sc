#!/usr/bin/env sclang

/*
 * Copyright (c) 2014 Hanspeter Portner (dev@open-music-kontrollers.ch)
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

s.options.protocol = \tcp;
s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

Routine.run({
	2.wait;
	s.addr.connect; // connect via TCP
}, clock:AppClock);

s.doWhenBooted({
	var hostname, tx, chimconf, rate, sidOffset, gidOffset;

	hostname = "hostname".unixCmdGetStdOutLines[0]++".local";

	gidOffset = 100;
	sidOffset = 200;

	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	rate = 3000;
	chimconf.sendMsg("/engines/enabled", false);
	chimconf.sendMsg("/engines/reset");
	chimconf.sendMsg("/engines/offset", 0.0025);
	chimconf.sendMsg("/engines/address", hostname++":"++s.addr.port, {
		chimconf.sendMsg("/engines/server", false);
		chimconf.sendMsg("/engines/mode", "osc.tcp");
		chimconf.sendMsg("/engines/enabled", true);
	});

	chimconf.sendMsg("/sensors/rate", rate);
	chimconf.sendMsg("/sensors/group/reset"); // reset groups
	chimconf.sendMsg("/sensors/group/attributes/0", 0.0, 1.0, false, true, false); // add group
	chimconf.sendMsg("/sensors/group/attributes/1", 0.0, 1.0, true, false, false); // add group

	chimconf.sendMsg("/engines/scsynth/enabled", true); // enable scsynth output engine
	chimconf.sendMsg("/engines/scsynth/derivatives", false);
	chimconf.sendMsg("/engines/scsynth/attributes/0", \base, sidOffset, 0+gidOffset, 0, 0, true, true, \addToHead.asInt, false);
	chimconf.sendMsg("/engines/scsynth/attributes/1", \lead, sidOffset, 1+gidOffset, 1, 0, true, true, \addToHead.asInt, false);

	s.sendMsg('/g_new', 0+gidOffset, \addToHead.asInt, 0);
	s.sendMsg('/g_new', 1+gidOffset, \addToHead.asInt, 0);
	s.sync;

	chimconf.sendMsg("/sensors/number", {|msg|
		var n=msg[0];
		Routine.run({
			"./instruments2F.sc".load.value(n, \base, \lead);
		}, clock:AppClock);
	});
})
