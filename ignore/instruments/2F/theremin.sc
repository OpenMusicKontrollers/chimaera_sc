/*
 * Copyright (c) 2015 Hanspeter Portner (dev@open-music-kontrollers.ch)
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the Artistic License 2.0 as published by
 * The Perl Foundation.
 * 
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Artistic License 2.0 for more details.
 * 
 * You should have received a copy of the Artistic License 2.0
 * along the source as a COPYING file. If not, obtain it from
 * http://www.perlfoundation.org/artistic_license_2_0.
 */

{|synthname, n|
	SynthDef(synthname, {|x=0, y=0, vx=0, vy=0, p=0, gate=0, out=0|
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
