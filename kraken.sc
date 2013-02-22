#!/usr/bin/sclang

s.boot;

s.doWhenBooted({
	//var krak = Kraken(s, "kraken.local", 1212);
	var krak = Kraken(s, "127.0.0.1", 1212);
	
	{krak.ar(0, SinOsc.ar(10), 2000)}.play;
	{krak.kr(1, SinOsc.kr(220), 20000)}.play;
});
