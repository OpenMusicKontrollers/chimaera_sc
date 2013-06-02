#!/usr/bin/sclang

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

s.boot;

s.doWhenBooted({
	var tx, krak;

	tx = NetAddr ("192.168.1.188", 9999);
	//tx = NetAddr ("localhost", 1212);
	krak = Kraken(s, tx);
	
	//{krak.ar(0, SinOsc.ar(0.5, mul:1000, add:1000).round, 10)}.play;
	{krak.ar(0, PinkNoise.ar(5000), 10)}.play;

	//{krak.kr(1, SinOsc.kr(220), 20000)}.play;
});
