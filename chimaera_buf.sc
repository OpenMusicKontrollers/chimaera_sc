#!/usr/bin/sclang

s.options.blockSize = 16;
s.options.memSize = 65536;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var chim, synth, tx, rx;

	thisProcess.openUDPPort(3333);
	thisProcess.openUDPPort(4444);

	rx = NetAddr ("localhost", nil);
	tx = NetAddr ("localhost", 4444);
	chim = ChimaeraBuf(s, 8, rx, tx);

	chim.groupAdd(0, Chimaera.both, 0, 1);

	synth = {
		var gid, gate, env, freq1, freq2, sig;

		gid = 0;
		gate = chim.kr(gid, ChimaeraBuf.gate);
		env = EnvGen.kr(Env.asr(0.01, 1.0, 0.02, 1.0, -3), gate);
		freq1 = chim.kr(gid, ChimaeraBuf.xdim);
		freq2 = chim.kr(gid, ChimaeraBuf.zdim);

		freq1 = LinExp.kr(freq1, 0, 1, 40.midicps, 88.midicps);
		freq2 = LinExp.kr(freq2, 0, 1, 40.midicps, 88.midicps);

		sig = SyncSaw.ar(freq1, mul:env);
		OffsetOut.ar([0,1], Mix.ar(sig));
	}.play;
})
