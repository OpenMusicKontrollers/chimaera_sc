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

s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var rx, tx, rate, chimconf, chimin, chimout;

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages

	rx = NetAddr ("chimaera.local", 3333);
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	rate = 3000;
	chimconf.sendMsg("/engines/reset");
	chimconf.sendMsg("/engines/offset", 0.0025);
	
	chimconf.sendMsg("/engines/enabled", false);
	chimconf.sendMsg("/engines/server", true);
	chimconf.sendMsg("/engines/mode", "osc.tcp");
	chimconf.sendMsg("/engines/enabled", true, {|msg| rx.connect;}); // connect via TCP

	chimconf.sendMsg("/sensors/rate", rate);
	chimconf.sendMsg("/sensors/group/reset"); // reset groups
	chimconf.sendMsg("/sensors/group/attributes/0", 0.0, 1.0, false, true, false); // add group
	chimconf.sendMsg("/sensors/group/attributes/1", 0.0, 1.0, true, false, false); // add group

	chimconf.sendMsg("/sensors/number", {|msg|
		var n = msg[0];
		chimout = ChimaeraOutSCSynth2F(s, n, [\base, \lead]);
		chimin = ChimaeraInDummy(s, chimconf, rx, chimout);
		Routine.run({"./instruments2F.sc".load.value(n, \base, \lead);}, clock:AppClock);
	});
})
