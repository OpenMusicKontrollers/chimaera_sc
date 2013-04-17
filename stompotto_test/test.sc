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
	var rx, stompotto, rainbow;

	thisProcess.openUDPPort(9999); // open port 9999 for listening to stompotto events
	rx = NetAddr ("192.168.1.188", 9999);

	stompotto = StompOtto(s, rx, rate:50);

	stompotto.on = {|id| // set callback function for on-events
		["on", id].postln;
	};

	stompotto.off = {|id| // set callback function for off-events
		["off", id].postln;
	};

	stompotto.play;

	SynthDef(\led, {|channel, r1, g1, b1, r2, g2, b2, time|
		var red, green, blue, lum;
		red = Line.kr(r1, r2, time, doneAction:2);
		green = Line.kr(g1, g2, time);
		blue = Line.kr(b1, b2, time);
		//lum = Line.kr(1, 0, time);
		lum = 1;
		stompotto.kr(channel, red*lum, green*lum, blue*lum);
	}).add;

	s.sync;

	rainbow = [
		// red, yellow, green, cyan, blue, magenta
		255*[1.0, 0.5, 0.0, 0.0, 0.0, 0.5], // red channel
		255*[0.0, 0.5, 1.0, 0.5, 0.0, 0.0], // green channel
		255*[0.0, 0.0, 0.0, 0.5, 1.0, 0.5]  // blue	channel
	];

	Pbind(
		\instrument, \led,

		\channel, 0,

		\r1, Pseq(rainbow[0], inf),
		\g1, Pseq(rainbow[1], inf),
		\b1, Pseq(rainbow[2], inf),

		\r2, Pseq(rainbow[0].rotate(-1), inf),
		\g2, Pseq(rainbow[1].rotate(-1), inf),
		\b2, Pseq(rainbow[2].rotate(-1), inf),

		\time, 1,
		\dur, 1
	).play;

	0.5.wait;

	Pbind(
		\instrument, \led,

		\channel, 1,

		\r1, Pseq(rainbow[0], inf),
		\g1, Pseq(rainbow[1], inf),
		\b1, Pseq(rainbow[2], inf),

		\r2, Pseq(rainbow[0].rotate(-1), inf),
		\g2, Pseq(rainbow[1].rotate(-1), inf),
		\b2, Pseq(rainbow[2].rotate(-1), inf),

		\time, 1,
		\dur, 1
	).play;
})
