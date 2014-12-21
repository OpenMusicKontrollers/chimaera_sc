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

{
	var hostname, rx, tx, rate, chimconf, chimin, chimout;

	hostname = "hostname".unixCmdGetStdOutLines[0]++".local";

	tx = NetAddr ("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	rate = 3000;
	chimconf.sendMsg("/engines/reset");
	chimconf.sendMsg("/engines/address", hostname++":"++3333); // send output stream to port 3333
	chimconf.sendMsg("/engines/offset", 0.002);
	
	chimconf.sendMsg("/engines/custom/reset");
	chimconf.sendMsg("/engines/custom/append", "frame", "/frm", "i($f)");
	chimconf.sendMsg("/engines/custom/append", "idle",  "/idl", "i($f)");
	//chimconf.sendMsg("/engines/custom/append", "end",   "/end", "");

	chimconf.sendMsg("/engines/custom/append", "on",  "/gate", "i($g 8* $b 8%+) i(1)");
	chimconf.sendMsg("/engines/custom/append", "off", "/gate", "i($g 8* $b 8%+) i(0)");
	chimconf.sendMsg("/engines/custom/append", "set", "/cv1",  "i($g 8* $b 8%+) f($x 2* 1-)");
	chimconf.sendMsg("/engines/custom/append", "set", "/cv2",  "i($g 8* $b 8%+) f($z 0.5* 0.5+)");

	//chimconf.sendMsg("/engines/custom/append", "on",  "/midi", "m($g 8* $b 8%+ 0x90 $b 0x7f& 0x7f)");
	//chimconf.sendMsg("/engines/custom/append", "off", "/midi", "m($g 8* $b 8%+ 0x80 $b 0x7f& 0x7f)");
	//chimconf.sendMsg("/engines/custom/append", "set", "/midi", "m($g 8* $b 8%+ 0xc0 0x27 $z 0x3fff* 0x7f&)");
	//chimconf.sendMsg("/engines/custom/append", "set", "/midi", "m($g 8* $b 8%+ 0xc0 0x07 $z 0x3fff* 7<<)");

	chimconf.sendMsg("/engines/custom/enabled", true);

	chimconf.sendMsg("/sensors/rate", rate);
	chimconf.sendMsg("/sensors/group/reset"); // reset groups
	chimconf.sendMsg("/sensors/group/attributes/0", 0.0, 1.0, false, true, false); // add group
	chimconf.sendMsg("/sensors/group/attributes/1", 0.0, 1.0, true, false, false); // add group
}.value;
