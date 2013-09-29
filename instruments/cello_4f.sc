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

{|synthname|

	SynthDef(synthname, {|freq=0, amp=0, p=0, freq1=0, amp1=0, p1=0, gate=0, out=0|
		var up=0.1, down=0.5, env, suicide, sig, x, y;

		suicide = DetectSilence.kr(Line.kr(0.1, 0.0, 1.0)+gate, 0.0001, down, doneAction:2);
		env = Linen.kr(gate, up, 1.0, down);

		freq = LinExp.kr(freq, 0, 1, (3*12-0.5).midicps, (7*12+0.5).midicps);

		freq1 = freq1 - OnePole.kr(freq1, 0.998); // differentiate
		freq1 = RunningSum.kr(freq1.abs.tan, 20)*0.05;

		sig = Mix.ar(Saw.ar([freq, freq/3, freq*5], mul:freq1*env));
		sig = RLPF.ar(sig, amp*900+100, 0.1);

		OffsetOut.ar(out, sig);
	}).add;
}
