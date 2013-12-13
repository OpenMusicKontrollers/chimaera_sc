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

{
	var rx, tx, chimconf, chimdump;

	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies
	tx = NetAddr("chimaera.local", 4444);

	thisProcess.openUDPPort(3333); // open port 3333 to listen for dump messages
	rx = NetAddr("chimaera.local", 3333);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333
	chimconf.sendMsg("/chimaera/output/offset", 0.001); // add 1ms offset to bundle timestamps
	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	//chimconf.sendMsg("/chimaera/calibration/reset"); // uncomment to reset quiescent output

	chimdump = ChimaeraDump(s, chimconf, rx);
}.value;
