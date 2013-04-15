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

StompOtto {
	classvar ctrue, cfalse;
	var rx, rat, stomp, channel, mytrig, cols, <>on, <>off;

	*new {|s, iRx, rate=50|
		^super.new.init(s, iRx, rate);
	}

	*initClass {
		ctrue = 'T'.ascii[0];
		cfalse = 'F'.ascii[0];
	}

	initConn {|iRx|
		rx = iRx;

		// handling tuio messages
		stomp = OSCFunc({|msg, time, addr, port|
			var state, id;
	
			state = msg[1];
			id = msg[2];

			if(state.ascii==ctrue) {
				if(on.notNil) {
					on.value(id);
				};
			} {
				if(off.notNil) {
					off.value(id);
				}
			};
		}, "/stompotto/key", rx);

		channel = OSCFunc({|msg, time, addr, port|
			var id, channel, red, green, blue, col;
			id = msg[1];
			channel = msg[2];
			red = msg[3].asInteger;
			green = msg[4].asInteger;
			blue = msg[5].asInteger;
			col = (red << 16) | (green << 8) | blue;
			cols[channel] = col;
			//tx.sendMsg('/stompotto/led', channel, value);
			//rx.sendMsg('/stompotto/led', channel, red.asInteger, green.asInteger, blue.asInteger);
		}, "/stomp/channel", nil);

		mytrig = OSCFunc({|msg, time, addr, port|
			//rx.sendMsg('/stompotto/led', cols); // sending as blob
			rx.sendMsg('/stompotto/led', cols[0], cols[1], cols[2], cols[3], cols[4], cols[5], cols[6], cols[7]); // sending as blob
		}, "/stomp/trig", nil);

		cols = Array.newClear(8);

		on = nil;
		off = nil;
	}

	init {|s, iRx, rate|
		rat = rate;
		this.initConn(iRx);
	}

	ar {|channel=0, red, green, blue, rate=2000|
		var trig, out;
		trig = Impulse.ar(rat);
		out = SendReply.ar(trig, '/stomp/channel', [red, green, blue], replyID: channel);
		^out;
	}

	kr {|channel=0, red, green, blue, rate=2000|
		var trig, out;
		trig = Impulse.kr(rat);
		out = SendReply.kr(trig, '/stomp/channel', [red, green, blue], replyID: channel);
		^out;
	}

	play {
		{
			var trig;
			trig = Impulse.kr(rat);
			SendReply.kr(trig, '/stomp/trig');
		}.play;
	}
}
