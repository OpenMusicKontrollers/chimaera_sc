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
 * Blip
 *
 * x := frequency
 * z := synced frequency 
 */

{|synthname|

	SynthDef(synthname, {|freq=0, amp=0, p=0, gate=1, out=0|
		var env, freq2, sig;

		freq = LinExp.kr(freq, 0, 1, (2*12-0.5).midicps, (6*12+0.5).midicps);

		env = Linen.kr(gate, 0.01, 0.5, 0.02);
		sig = Blip.ar(freq, amp*20, mul:env) * XLine.kr(1, 0.01, 5.0);
		OffsetOut.ar(out, sig);
	}).add;
}
