#!/usr/bin/env sclang

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
	var hostname, rx, tx, chimconf, rate, chimin, chimout;

	hostname = "hostname".unixCmdGetStdOutLines[0]++".local";

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies

	rx = NetAddr ("chimaera.local", 3333);
	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	rate = 3000;
	chimconf.sendMsg("/engines/reset");
	chimconf.sendMsg("/engines/address", hostname++":"++3333); // send output stream to port 3333
	chimconf.sendMsg("/engines/offset", 0.002);

	chimconf.sendMsg("/sensors/rate", rate);
	chimconf.sendMsg("/sensors/group/reset"); // reset groups
	chimconf.sendMsg("/sensors/group/attributes", 0, ChimaeraConf.north, 0.0, 1.0, false); // add group
	chimconf.sendMsg("/sensors/group/attributes", 1, ChimaeraConf.south, 0.0, 1.0, false); // add group

	chimconf.sendMsg("/sensors/number", {|msg|
		var n = msg[0];
		chimout = ChimaeraOutSCSynth4F(s, n, [\base]);
		chimin = ChimaeraInTuio2(s, chimconf, rx, chimout);
		Routine.run({"./instruments4F.sc".load.value(n);}, clock:AppClock);
	});
})
