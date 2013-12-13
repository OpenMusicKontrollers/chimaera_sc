/*
 * Copyright (c) 2013 Hanspeter Portner (dev@open-music-kontrollers.ch)
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

/*
 * plucked instrument
 *
 * x := frequency
 * z := volume, distortion, cutoff frequency of low-pass filter
 */

{|synthname, n|
	var bot = 3*12 - 0.5 - (n/3 % 12 / 2);
	var top = n/3 + bot + 1;

	SynthDef(synthname, {|freq=0, amp=0, p=0, gate=0, out=0|
		var suicide, up=0.1, down=1.0, env, sig, vol, cut;

		suicide = DetectSilence.kr(Line.kr(0.1, 0.0, 1.0)+gate, 0.0001, down, doneAction:2);
		env = Linen.kr(gate, up, 1.0, down);

		freq = LinExp.kr(freq, 0, 1, bot.midicps, top.midicps);

		vol = LinExp.kr(amp, 0.0, 1.0, 0.5, 1.0);
		sig = Pluck.ar(WhiteNoise.ar(0.1), gate, 1, freq.reciprocal, 10, 0.20);
		sig = (sig*amp*1000).distort;
		sig = FreeVerb.ar(sig, mix:0.8, room:0.5, damp:0.1, mul:vol*env);
		cut = LinExp.kr(amp, 0.0, 1.0, 500, 1000);
		sig = RLPF.ar(sig, freq:cut, rq:0.3);
		OffsetOut.ar(out, sig);
	}).add;
}
