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
	var chimconf, chimin, chimout;

	chimconf = ChimaeraConf(s,
		addr:"chimaera.local");

	chimconf.sendMsg("/sensors/group/reset");

	chimconf.sendMsg("/sensors/group/attributes/0/min", 0.0);
	chimconf.sendMsg("/sensors/group/attributes/0/max", 1.0);
	chimconf.sendMsg("/sensors/group/attributes/0/north", false);
	chimconf.sendMsg("/sensors/group/attributes/0/south", true);
	chimconf.sendMsg("/sensors/group/attributes/0/scale", false);

	chimconf.sendMsg("/sensors/group/attributes/1/min", 0.0);
	chimconf.sendMsg("/sensors/group/attributes/1/max", 1.0);
	chimconf.sendMsg("/sensors/group/attributes/1/north", true);
	chimconf.sendMsg("/sensors/group/attributes/1/south", false);
	chimconf.sendMsg("/sensors/group/attributes/1/scale", false);

	chimconf.sendMsg("/sensors/number", {|msg|
		var n = msg[0];
		chimout = ChimaeraOutMidi(s, n, [\base, \lead]);
		chimout.control = 0x07;
		chimout.doublePrecision = true;
		chimin = ChimaeraInTuio2(s, chimconf, chimout);
	});
}.value;
