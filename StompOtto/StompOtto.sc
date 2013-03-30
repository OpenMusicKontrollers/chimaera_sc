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
	var rx, stomp, <>on, <>off;

	*new {|s, iRx|
		^super.new.init(s, iRx);
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
		}, "/stompotto", rx);

		on = nil;
		off = nil;
	}

	init {|s, iRx|
		this.initConn(iRx);
	}
}
