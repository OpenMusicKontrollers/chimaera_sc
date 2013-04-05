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

/*
 * simple sine-wave
 *
 * x := freq
 * z := volume
 */

{|synthname|

	SynthDef(synthname, {|freq=0, amp=0, p=0, gate=1, out=0|
		var env, sig, trig, trigsplit, delaytimes;

		freq = LinExp.kr(freq, 0, 1, (3*12-0.5).midicps, (7*12+0.5).midicps);

		env = Linen.kr(gate, 0.01, 1.0, 1.0);

		trig = Dust.ar(LinExp.kr(amp, 0, 1, 2, 20));
		trigsplit = PulseDivider.ar(trig, 2, [0, 1]);
		delaytimes = Lag.ar(TRand.ar(0.005, 0.05, trigsplit), 0.07); 

		sig = Saw.ar(freq * [1, 1.003], mul:env).sum * 0.1;
		sig = LPF.ar(sig, 9000); 
		sig = sig + DelayL.ar(sig, 0.05, delaytimes); 

		OffsetOut.ar(out, sig);
	}).add;
}
