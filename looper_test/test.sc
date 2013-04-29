#!/usr/bin/sclang

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

s.boot;

s.doWhenBooted({
	var looper, channel, trigbus, pacemaker;

	trigbus = 20;
	looper = Looper(s, 4, 10);

	pacemaker = PaceMaker(s, trigbus, 1/4);

	channel = 0;

	OSCFunc({|msg, time, addr, port|
		var channel, state;
		channel = msg[1];
		state = msg[2];
		if(state != 0) {
			("starting recording on channel" ++ channel).postln;
			looper.record(channel, 8, trigbus);
		} {
			("stopping recording on channel" ++ channel).postln;
			looper.abort(channel);
		}
	}, "/looper/record", nil, 1212);

	OSCFunc({|msg, time, addr, port|
		var channel, state;
		channel = msg[1];
		state = msg[2];
		if(state != 0) {
			("starting playback on channel" ++ channel).postln;
			looper.play(channel, 0, trigbus);
		} {
			("stopping playback on channel" ++ channel).postln;
			looper.stop(channel);
		}
	}, "/looper/play", nil, 1212);
})
