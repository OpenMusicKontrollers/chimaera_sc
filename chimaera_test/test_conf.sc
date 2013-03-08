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

s.options.blockSize = 16;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var tx, rx, chimconf;

	thisProcess.openUDPPort(4444); // chimaera sends its replies to configuration request to port 4444 by default
	tx = NetAddr ("chimaera.local", 4444); // chimaera listens for configuration request on port 4444 by default
	rx = NetAddr ("chimaera.local", 4444);
	tx.postln;
	rx.postln;

	chimconf = ChimaeraConf(s, tx, rx);

	chimconf.sendMsg("/chimaera/version", {|ver| ("firmware version:" + ver).postln;}); // get firmware version of device
	chimconf.sendMsg("/chimaera/comm/mac", {|mac| ("MAC address:" + mac).postln;}); // get MAC address of device

	chimconf.sendMsg("/chimaera/group/clear"); // clear groups
	chimconf.sendMsg("/chimaera/group/add", 1, ChimaeraConf.north, 0.0, 1.0); // add a group
	chimconf.sendMsg("/chimaera/group/add", 2, ChimaeraConf.south, 0.0, 1.0); // add another group

	chimconf.sendMsg("/chimaera/config/save"); // save configuration to EEPROM
})
