#!/usr/bin/sclang

/*
 * Copyright (c) 2013 Hanspeter Portner (dev@open-music-kontrollers.ch)
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

	thisProcess.openUDPPort(3333); // open port 3333 to listen for dump messages
	rx = NetAddr("chimaera.local", 3333);

	chimconf = ChimaeraConf(s, tx, tx);

	chimconf.sendMsg("/chimaera/output/enabled", true); // enable output
	chimconf.sendMsg("/chimaera/output/address", "192.168.1.10:3333"); // send output stream to port 3333
	chimconf.sendMsg("/chimaera/output/offset", 0.001); // add 1ms offset to bundle timestamps
	chimconf.sendMsg("/chimaera/output/reset"); // reset all output engines

	chimconf.sendMsg("/chimaera/dump/enabled", true); // enable sensor array dump

	chimconf.sendMsg("/chimaera/calibration/reset"); // reset calibration data

	chimconf.sendMsg("/chimaera/sensors", {|n|
		var geo;
		var h = 100;
		var h1 = 1 / h;
		var w = 6;
		var w2 = w - 2;
		var m = 2200;
		var d = 2048 / m * h;
		var t = 2040 / m * h;
		var s = 1 / m * h;
		var p0, p1, p2, p3;

		n = n[0]; // first argument
		arr = Int8Array.newClear(n*2);
		geo = Rect(0, 0, n*w, h*2);
		p0 = Point.new(0, h-d);
		p1 = Point.new(n*w, h-d);
		p2 = Point.new(0, h+d);
		p3 = Point.new(n*w, h+d);

		Routine.run({
			win = Window.new("Chimaera dump", geo, false).front;
			view = UserView(win, geo);
			view.drawFunc = {
				Pen.strokeColor = Color.black;
				Pen.line(p0, p1);
				Pen.stroke;
				Pen.line(p2, p3);
				Pen.stroke;

				for(0,n-1, {|i|
					var msb, lsb, val;
					lsb = arr[i*2+1];
					if(lsb < 0) {lsb = 256+lsb};
					msb = arr[i*2] << 8;
					val = msb | lsb;
					val = val * s;
					if(val.abs >= t) {
						Pen.fillColor = Color.yellow;
					} {
						if(val < 0) {
							var red = 0 - val * h1;
							Pen.fillColor = Color.new(red, 0, 0);
						} {
							var green = val * h1;
							Pen.fillColor = Color.new(0, green, 0);
						};
					};
					Pen.addRect(Rect(i*w+1, h, w2, val));
					Pen.fill;
				});
			};
		}, clock:AppClock);

		fps = Routine.run({
			while(true) {
				view.refresh;
				(1/30).wait;
			}
		}, clock:AppClock);

		dump = OSCFunc({|msg, time, addr, port|
			arr = msg[2];
		}, "/dump", rx);
	});
}.value;
