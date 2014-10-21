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

{|synthname, n|
	SynthDef(synthname, {|x=0, y=0, p=0, gate=0, out=0|
		var env, freq, amp, sig;

		env = Linen.kr(gate, 0.01, 1.0, 1.0, doneAction:2);

		freq = ChimaeraMapLinearCPS.kr(x, n:n, oct:3);
		amp = y.cubed;

		sig = SinOsc.ar(freq, mul:amp*env);
		sig = sig * VarSaw.ar(freq*1.001, 0.2, mul:5);
		sig = sig.distort;
		sig = BPF.ar(sig, 500, 0.1, mul:5);
		sig = FreeVerb.ar(sig, mix:0.3, room:0.8, damp:0.1);

		OffsetOut.ar(out, sig);
	}).add;
}
