#!/usr/bin/env sclang

/*
 * Copyright (c) 2015 Hanspeter Portner (dev@open-music-kontrollers.ch)
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the Artistic License 2.0 as published by
 * The Perl Foundation.
 * 
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Artistic License 2.0 for more details.
 * 
 * You should have received a copy of the Artistic License 2.0
 * along the source as a COPYING file. If not, obtain it from
 * http://www.perlfoundation.org/artistic_license_2_0.
 */

{
	var chimconf;

	chimconf = ChimaeraConf(s,
		addr:"chimaera.local");
	
	chimconf.sendMsg("/engines/custom/reset");
	chimconf.sendMsg("/engines/custom/append", "frame", "/frm", "i($f)");
	chimconf.sendMsg("/engines/custom/append", "idle",  "/idl", "i($f)");
	chimconf.sendMsg("/engines/custom/append", "end",   "/end", "");

	chimconf.sendMsg("/engines/custom/append",
		"on",  "/gate", "i($g 8* $b 8%+) i(1)");
	chimconf.sendMsg("/engines/custom/append",
		"off", "/gate", "i($g 8* $b 8%+) i(0)");
	chimconf.sendMsg("/engines/custom/append",
		"set", "/cv1",  "i($g 8* $b 8%+) f($x 2* 1-)");
	chimconf.sendMsg("/engines/custom/append",
		"set", "/cv2",  "i($g 8* $b 8%+) f($z 0.5* 0.5+)");

	/*
	chimconf.sendMsg("/engines/custom/append",
		"on",  "/midi", "m($g 8* $b 8%+ 0x90 $b 0x7f& 0x7f)");
	chimconf.sendMsg("/engines/custom/append",
		"off", "/midi", "m($g 8* $b 8%+ 0x80 $b 0x7f& 0x7f)");
	chimconf.sendMsg("/engines/custom/append",
		"set", "/midi", "m($g 8* $b 8%+ 0xc0 0x27 $z 0x3fff* 0x7f&)");
	chimconf.sendMsg("/engines/custom/append",
		"set", "/midi", "m($g 8* $b 8%+ 0xc0 0x07 $z 0x3fff* 7<<)");
	*/

	chimconf.sendMsg("/engines/custom/enabled", true);

	chimconf.sendMsg("/sensors/group/reset");
	chimconf.sendMsg("/sensors/group/attributes/0",
		0.0, 1.0, false, true, false);
	chimconf.sendMsg("/sensors/group/attributes/1",
		0.0, 1.0, true, false, false);
}.value;
