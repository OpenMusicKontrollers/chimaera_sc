#!/usr/bin/sclang

s.options.blockSize = 16;
//s.options.memSize = 65536;
s.latency = nil;
s.boot;

s.doWhenBooted({
	var inst, ev, txrx, chimconf, saddr;

	inst = SynthDef(\myinstrument, {|x, z, gate|
		var base, freq, sig;

		base = 24+12;
		freq = LinExp.kr(x, 0, 1, base.midicps, (base+48).midicps);	
		sig = SinOsc.ar(freq, mul:gate*z);

		OffsetOut.ar([0,1], sig);
	}).add;

	ev = (
		type: \on,
		instrument: \myinstrument,
		id: [1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007],
		gate: 0
	).play;

	thisProcess.openUDPPort(4444);
	txrx = NetAddr ("chimaera.local", 4444);
	txrx.postln;

	chimconf = ChimaeraConf(s, txrx, txrx);

	chimconf.sendMsg("/chimaera/version", {|ver| ["firmware version", ver].postln;});
	chimconf.sendMsg("/chimaera/comm/mac", {|mac| ["MAC", mac].postln;});

	chimconf.sendMsg("/chimaera/output/enabled", true);
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:57110"); // send to scsynth

	chimconf.sendMsg("/chimaera/scsynth/enabled", true);
	chimconf.sendMsg("/chimaera/scsynth/instrument", \myinstrument);

	/*
	chimconf.sendMsg("/chimaera/scsynth/prealloc", false);
	chimconf.sendMsg("/chimaera/scsynth/offset", 1000);
	chimconf.sendMsg("/chimaera/scsynth/modulo", 1000);
	*/

	chimconf.sendMsg("/chimaera/scsynth/prealloc", true);
	chimconf.sendMsg("/chimaera/scsynth/offset", 1000);
	chimconf.sendMsg("/chimaera/scsynth/modulo", 8);

	chimconf.sendMsg("/chimaera/group/clear");
	chimconf.sendMsg("/chimaera/group/add", 1, ChimaeraConf.south, 0.0, 1.0);
	chimconf.sendMsg("/chimaera/group/add", 2, ChimaeraConf.north, 0.0, 1.0);

	chimconf.sendMsg("/chimaera/config/save");
})
