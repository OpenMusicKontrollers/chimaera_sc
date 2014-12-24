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
	SynthDef(synthname, {|x=0, y=0, vx=0, vy=0, p=0, gate=0, out=0|
		var env, freq, vol, cut, sig;

		env = Linen.kr(gate, 0.01, 1.0, 1.0, doneAction:2);

		freq = ChimaeraMap3rdOrderStepCPS.kr(x, n:n, oct:3);
		vol = LinExp.kr(y, 0.0, 1.0, 0.5, 1.0);
		cut = LinExp.kr(y, 0.0, 1.0, 500, 1000);

		sig = Pluck.ar(WhiteNoise.ar(0.1), gate, 1, freq.reciprocal, 10, 0.20);
		sig = (sig*y*1000).distort;
		sig = FreeVerb.ar(sig, mix:0.8, room:0.5, damp:0.1, mul:vol*env);
		sig = RLPF.ar(sig, freq:cut, rq:0.3);

		OffsetOut.ar(out, sig);
	}).add;
}
