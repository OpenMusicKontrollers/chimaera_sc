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

{
	var rx, tx, chimconf, dump, arr, win, view, fps;

	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies
	tx = NetAddr("chimaera.local", 4444);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333
	chimconf.sendMsg("/chimaera/output/offset", 0.001); // add 1ms offset to bundle timestamps
	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	chimconf.sendMsg("/chimaera/dump/enabled", true); // enable sensor array dump

	thisProcess.openUDPPort(3333); // open port 3333 to listen for dump messages
	rx = NetAddr("chimaera.local", 3333);

	arr = Int8Array.new(288);
	
	win = Window.new("Chimaera dump", Rect(0,0,144*8,256)).front;
	view = UserView(win, Rect(0,0,144*8,256));
	view.drawFunc = {
		for(0,143, {|i|
			var msb, lsb, val;
			lsb = arr[i*2+1];
			if(lsb < 0) {lsb = 256+lsb};
			msb = arr[i*2] * 256;
			val = msb + lsb;
			val = val / 2048 * 128;
			if(val < 0) {
				var red = 1+(val/128);
				Pen.fillColor = Color.new(1, red, red);
			} {
				var green = 1-(val/128);
				Pen.fillColor = Color.new(green, 1, green);
			};
			Pen.addRect(Rect(i*8+1, 128, 6, val));
			Pen.fill;
		});
	};
	view.background = Color.black;

	dump = OSCFunc({|msg, time, addr, port|
		arr = msg[2];
	}, "/dump", rx);

	fps = Routine.run({
		while(true) {
			view.refresh;
			(1/30).wait;
		}
	}, clock:AppClock);
}.value;
