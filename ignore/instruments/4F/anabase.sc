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
	SynthDef(synthname, {|x1=0, y1=0, vx1=0, vy1=0, p1=0, x2=0, y2=0, vx2=0, vy2=0, p2=0, gate=1, out=0|
		var env, freq1, freq2, sig;

		env = Linen.kr(gate, 0.01, 1.0, 1.0, doneAction:2);

		freq1 = ChimaeraMapLinearCPS.kr(x1, n:n, oct:2);
		freq2 = ChimaeraMapLinearCPS.kr(x2, n:n, oct:2);

		sig = SyncSaw.ar(freq1, freq2, mul:y2*env);
		sig = RLPF.ar(sig, y1*1900+100, 0.1);
		sig = FreeVerb.ar(sig);

		OffsetOut.ar(out, sig);
	}).add;
}
