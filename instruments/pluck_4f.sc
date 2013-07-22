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
 * plucked instrument with 4 degrees of freedom
 *
 * - pitch
 * - volume
 * - decaytime
 * - coef
 */

{|modname, pitchname, busX, busY|
	busX.set(1.0);
	busY.set(0.2);

	SynthDef(modname, {|freq=0, amp=0, p=0, gate=0, out=0|
		var suicide, up=0.1, down=1.0;

		suicide = DetectSilence.kr(Line.kr(0.1, 0.0, 1.0)+gate, 0.0001, down, doneAction:2);

		Out.kr([busX, busY], [freq*9+1, amp*0.5]);
	}).add;

	SynthDef(pitchname, {|freq=0, amp=0, p=0, gate=0, out=0|
		var suicide, up=0.1, down=1.0, env, sig;

		suicide = DetectSilence.kr(Line.kr(0.1, 0.0, 1.0)+gate, 0.0001, down, doneAction:2);
		env = Linen.kr(gate, up, 1.0, down);

		freq = LinExp.kr(freq, 0, 1, (3*12-0.5).midicps, (7*12+0.5).midicps);
		sig = Pluck.ar(WhiteNoise.ar(0.1), gate, 1, freq.reciprocal, 10.0, 0.2, mul:env*amp);

		OffsetOut.ar(out, sig);
	}).add;
}
