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

s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var freq, amp, id;

	"../instruments/blip.sc".load.value(\inst);

	1.wait;

	(
		type: \group,
		id: 20,
		group: 0
	).play;

	Pbind(
		\instrument, \inst,
		\freq, Pgeom(0.5, 1.0, inf),
		\amp, Pgeom(0.5, 1.0, inf),
		\dur, Prand([0.1, 0.2, 0.4], inf),
		\out, [0,1],
		\group, 20
	).play;

	OSCFunc({|msg, time, addr, port|
		var f, a;

		msg.postln;
		f = msg[1];
		a = msg[2];

		(
			group: 20,
			freq: f,
			amp: a
		).play;
	}, "/inst", nil);

	{
		SendTrig.kr(Impulse.kr(2000), "/inst", SinOsc.kr(20), SinOsc.kr(20));
	}.play;
});
