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
 * plucked instrument with 4 degrees of freedom
 *
 * - pitch
 * - volume
 * - decaytime
 * - coef
 */

{|synthname, n|
	var bot = 3*12 - 0.5 - (n % 18 / 6);
	var top = n/3 + bot;

	SynthDef(synthname, {|freq=0, amp=0, p=0, freq1=0, amp1=0, p1=0, gate=0, out=0|
		var suicide, up=0.1, down=5.0, env, sig;

		suicide = DetectSilence.kr(Line.kr(0.1, 0.0, 1.0)+gate, 0.0001, down, doneAction:2);
		env = Linen.kr(gate, up, 1.0, down);

		freq = LinExp.kr(freq, 0, 1, bot.midicps, top.midicps);
		freq1 = 1 - freq1 * 0.5;
		amp1 = LinExp.kr(amp1, 0, 1, (1*12-0.5).midicps, (11*12+0.5).midicps);

		sig = Pluck.ar(WhiteNoise.ar(0.1), gate, 1, freq.reciprocal, 10, freq1, mul:env*amp);
		sig = RLPF.ar(sig, amp1, 0.1);

		OffsetOut.ar(out, sig);
	}).add;
}
