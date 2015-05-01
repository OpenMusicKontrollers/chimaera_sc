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

s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var chimconf, chimin, chimout;

	chimconf = ChimaeraConf(s,
		addr:"chimaera.local");

	chimconf.sendMsg("/sensors/group/reset");
	chimconf.sendMsg("/sensors/group/attributes/0",
		0.0, 1.0, false, true, false);
	chimconf.sendMsg("/sensors/group/attributes/1",
		0.0, 1.0, true, false, false);

	chimconf.sendMsg("/sensors/number", {|msg|
		var n = msg[0];
		chimout = ChimaeraOutSCSynth4F(s, n, [\base, \lead]);
		chimin = ChimaeraInTuio2(s, chimconf, chimout);
		Routine.run({
			"./instruments4F.sc".load.value(n, \base, \lead);
		}, clock:AppClock);
	});
})
