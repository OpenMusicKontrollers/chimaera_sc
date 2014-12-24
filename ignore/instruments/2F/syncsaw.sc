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
		var env, freq, freq2, sig;

		env = Linen.kr(gate, 0.01, 1.0, 1.0, doneAction:2);

		freq = ChimaeraMapLinearCPS.kr(x, n:n, oct:3);
		freq2 = LinLin.kr(y, 0, 1, (0*12).midicps, (5*12).midicps);

		sig = SyncSaw.ar(freq, freq2, mul:env);
		sig = RLPF.ar(sig, freq2*8, 0.1);
		sig = FreeVerb.ar(sig);

		OffsetOut.ar(out, sig);
	}).add;
}
