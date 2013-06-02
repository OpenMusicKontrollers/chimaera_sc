/*
 * Copyright (c) 2013 Hanspeter Portner (agenthp@users.sf.net)
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

Looper {
	var buffers, recording, playing;

	*new {|s, num, sec|
		^super.new.init(s, num, sec);
	}

	init {|s, num=4, sec=10|
		SynthDef(\looper_record, {|in, bufnum, trigbus, recLevel=1, preLevel=0, loop=0, gate=1|
			var sig, env, trig;
			trig = In.kr(trigbus);
			gate = Latch.kr(gate, trig);
			env = Linen.kr(gate, 0.1, 1, 0.2, doneAction:2);
			sig = In.ar(in) * env;
			RecordBuf.ar(sig, bufnum, offset:0, recLevel:recLevel, preLevel:preLevel, run:gate, loop:loop, trigger:trig);
		}).add;

		SynthDef(\looper_play, {|out, bufnum, trigbus, loop=0, gate=1|
			var env, sig, trig;
			trig = In.kr(trigbus);
			gate = Latch.kr(gate, trig);
			env = Linen.kr(gate, 0.1, 1, 0.2, doneAction:2);
			sig = PlayBuf.ar(1, bufnum, rate:1.0, trigger:trig, startPos:0, loop:loop) * env;
			OffsetOut.ar(out, sig);
		}).add;

		buffers = Array.fill(num, {Buffer.alloc(s, s.sampleRate*sec, 1)});
		recording = Array.fill(num, nil);
		playing = Array.fill(num, nil);
	}

	record {|c, in, trigbus, recLevel=1, preLevel=0, loop=0|
		this.abort(c);
		recording[c] = ( type: \on,
			instrument: \looper_record,
			in: in,
			bufnum: buffers[c].bufnum,
			trigbus: trigbus,
			recLevel: recLevel,
			preLevel: preLevel,
			loop: loop
		).play
	}

	play {|c, out, trigbus, loop=0|
		this.stop(c);
		playing[c] = ( type: \on,
			instrument: \looper_play,
			out: out,
			bufnum: buffers[c].bufnum,
			trigbus: trigbus,
			loop: loop
		).play;
	}

	abort {|c|
		if(recording[c].notNil) {
			( type: \off,
				id: recording[c].id
			).play;
		recording[c] = nil;
		}
	}

	stop {|c|
		if(playing[c].notNil) {
			( type: \off,
				id: playing[c].id
			).play;
		playing[c] = nil;
		}
	}

	free {
		//TODO free synths first
		buffers.free;
		recording.free;
		playing.free;
	}
}

PaceMaker {
	var pace, rat;

	*new {|s, out, rate|
		^super.new.init(s, out, rate);
	}

	init {|s, out, rate|
		SynthDef(\pace_maker, {|out=20, rate=0.25|
			Out.kr(out, Impulse.kr(rate));
		}).add;

		rat = rate;

		pace = ( type: \on,
			instrument: \pace_maker,
			out: out,
			rate: rat
		).play;
	}

	free {
		if(pace.notNil) {
			( type: \free,
				id: pace.id
			).play;
		}
	}

	rate {
		^rat;
	}

	rate_ {|rate|
		rat = rate;

		( type: \set,
			id: pace.id,
			rate: rat
		).play;
	}
}