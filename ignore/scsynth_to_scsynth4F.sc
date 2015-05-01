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
	var chimconf, sidOffset, gidOffset, grp;

	gidOffset = 100;
	sidOffset = 200;
	grp = 0+gidOffset;

	s.sendMsg('/g_new', grp, \addToHead.asInt, 0);
	s.sync;

	chimconf = ChimaeraConf(s,
		addr:"chimaera.local", prot:\tcp, target:\serv);

	chimconf.sendMsg("/sensors/group/reset");
	chimconf.sendMsg("/sensors/group/attributes/0",
		0.0, 1.0, false, true, false);
	chimconf.sendMsg("/sensors/group/attributes/1",
		0.0, 1.0, true, false, false);

	chimconf.sendMsg("/engines/scsynth/enabled", true);
	chimconf.sendMsg("/engines/scsynth/derivatives", true);
	chimconf.sendMsg("/engines/scsynth/attributes/0",
		\base, sidOffset, grp, 0, 0, true, true, \addToHead.asInt, false);
	chimconf.sendMsg("/engines/scsynth/attributes/1",
		\lead, sidOffset, grp, 0, 5, false, false, \addToHead.asInt, true);

	chimconf.sendMsg("/sensors/number", {|msg|
		var n=msg[0];
		Routine.run({
			"./instruments4F.sc".load.value(n);
		}, clock:AppClock);
	});
});
