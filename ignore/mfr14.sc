#!/usr/bin/env optsclang

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

s.options.blockSize = 0x10;
s.options.memSize = 0x10000;
s.options.numInputBusChannels = 0;
s.options.numOutputBusChannels = 10;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var gidOffset, mix;
	
	gidOffset = 100;

	s.sendMsg('/g_new', 0+gidOffset, \addToHead.asInt, 0);
	s.sendMsg('/g_new', 1+gidOffset, \addToHead.asInt, 0);
	s.sendMsg('/g_new', 2+gidOffset, \addToHead.asInt, 0);
	s.sendMsg('/g_new', 3+gidOffset, \addToHead.asInt, 0);
	s.sendMsg('/g_new', 4+gidOffset, \addToHead.asInt, 0);
	s.sendMsg('/g_new', 5+gidOffset, \addToHead.asInt, 0);
	s.sendMsg('/g_new', 6+gidOffset, \addToHead.asInt, 0);
	s.sendMsg('/g_new', 7+gidOffset, \addToHead.asInt, 0);

	s.sendMsg('/g_new', 8+gidOffset, \addToHead.asInt, 0); // mix
	s.sync;

	SynthDef(\mixer, {|g0=0, g1=0, g2=0, g3=0, g4=0, g5=0, g6=0, g7=0|
		var a0, a1, a2, a3, a4, a5, a6, a7;
		var p0, p1, p2, p3, p4, p5, p6, p7;

		// audio from SuperCollider
		a0 = InFeedback.ar(2) * g0; // base1
		a1 = InFeedback.ar(3) * g1; // lead1
		a2 = InFeedback.ar(4) * g2; // base2
		a3 = InFeedback.ar(5) * g3; // lead2
		a4 = InFeedback.ar(6) * g4; // base3
		a5 = InFeedback.ar(7) * g5; // lead3
		a6 = InFeedback.ar(8) * g6; // base4
		a7 = InFeedback.ar(9) * g7; // lead4

		// mix four mono channels to stereo
		p0 = Pan2.ar(a0, -1);
		p1 = Pan2.ar(a1,  1);
		p2 = Pan2.ar(a2, -1);
		p3 = Pan2.ar(a3,  1);
		p4 = Pan2.ar(a4, -1);
		p5 = Pan2.ar(a5,  1);
		p6 = Pan2.ar(a6, -1);
		p7 = Pan2.ar(a7,  1);

		OffsetOut.ar([0,1], Mix.ar([p0, p1, p2, p3, p4, p5, p6, p7]));
	}).add;
	s.sync;

	mix = Synth(\mixer, target:8+gidOffset, addAction:\addToHead);
	s.sync;

	// /n_set iif 108 0 0.5

	"./instruments2F.sc".load.value(96, \base1, \lead1); // mini
	//"./instruments2F.sc".load.value(128, \base2, \lead2); // midi
	"./instruments2F.sc".load.value(128, \base3, \lead3); // medi
	"./instruments2F.sc".load.value(160, \base4, \lead4); // maxi
})
