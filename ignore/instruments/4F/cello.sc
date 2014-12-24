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
	SynthDef(synthname, {|x1=0, y1=0, vx1=0, vy1=0, p1=0, x2=0, y2=0, vx2=0, vy2=0, p2=0, gate=1, out=0|
		var env, freq1, sig;

		env = Linen.kr(gate, 0.01, 1.0, 1.0, doneAction:2);

		freq1 = ChimaeraMapLinearCPS.kr(x1, n:n, oct:2);
		y2 = y2 - OnePole.kr(y2, 0.998); // differentiate
		y2 = RunningSum.kr(y2.abs.tan, 20)*0.05;

		sig = Mix.ar(Saw.ar([freq1, freq1/3, freq1*5], mul:y2*env));
		sig = RLPF.ar(sig, y1*900+100, 0.1);

		OffsetOut.ar(out, sig);
	}).add;
}
