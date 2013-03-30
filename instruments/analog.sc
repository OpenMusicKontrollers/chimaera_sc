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
 * low-pass filtered pulse width oscillator
 *
 * x := freq
 * y := cutoff frequency of low-pass filter
 */

{|synthname|

	SynthDef(synthname, {|freq=0, amp=0, p=0, gate=1, out=0|
		var env, sig, cutoff;

		freq = LinExp.kr(freq, 0, 1, (3*12-0.5).midicps, (7*12).midicps);

		env = EnvGen.kr(Env.asr(0.01, 1.0, 0.02, 1.0, -3), gate);
		cutoff = LinExp.kr(amp, 0, 1, (1*12).midicps, (7*12).midicps);
		sig = Pulse.ar([freq/2, freq, freq*2, freq*4], 0.2, mul:[0.5, 1, 0.5, 0.25]);
		sig = Mix.ar(sig);
		sig = RLPF.ar(sig, cutoff, 0.2, mul:amp*env);
		sig = FreeVerb.ar(sig);
		OffsetOut.ar(out, sig);
	}).add;
}
