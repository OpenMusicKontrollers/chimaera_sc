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
 * discrete pitch instrument
 *
 * x := frequency
 * z := volume, distortion, cutoff frequency of low-pass filter
 */

{|synthname, n|
	var scale, buf;
	var bot = 3*12 - 0.5 - (n % 18 / 6);
	var top = n/3 + bot;
		
	scale = FloatArray[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]; // dorian scale
	buf = Buffer.alloc(s, scale.size, 1, {|b| b.setnMsg(0, scale) });

	SynthDef(synthname, {|freq=0, amp=0, p=0, gate=0, out=0|
		var suicide, up=0.1, down=1.0, env, sig, vol;

		suicide = DetectSilence.kr(Line.kr(0.1, 0.0, 1.0)+gate, 0.0001, down, doneAction:2);
		env = Linen.kr(gate, up, 1.0, down);

		freq = DegreeToKey.kr(buf.bufnum, freq*(top-bot), 12, 1, bot).midicps;

		vol = LinExp.kr(amp, 0.0, 1.0, 0.5, 1.0);
		sig = SinOsc.ar(freq);
		sig = FreeVerb.ar(sig, mix:0.8, room:0.1, damp:0.1, mul:vol*env);
		OffsetOut.ar(out, sig);
	}).add;
}
