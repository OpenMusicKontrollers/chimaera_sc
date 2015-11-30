#!/usr/bin/env optsclang

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
s.options.numInputBusChannels = 8;
s.options.numOutputBusChannels = 8;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var gidOffset, n;
	
	gidOffset = 100;
	n = 160;

	0.to(7).do({|g|
		s.sendMsg('/g_new', g+gidOffset, \addToHead.asInt, 0);
	});
	s.sync;

	"./instruments2F.sc".load.value(n);
})
