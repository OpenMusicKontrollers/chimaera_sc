/*
 * Copyright (c) 2015 Hanspeter Portner (dev@open-music-kontrollers.ch)
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the Artistic License 2.0 as published by
 * The Perl Foundation.
 * 
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Artistic License 2.0 for more details.
 * 
 * You should have received a copy of the Artistic License 2.0
 * along the source as a COPYING file. If not, obtain it from
 * http://www.perlfoundation.org/artistic_license_2_0.
 */

ChimaeraDump {
	var win, view, arr, cb, fps;

	*new {|s, conf, n|
		^super.new.init(s, conf, n);
	}

	*initClass {
		//TODO
	}

	init {|s, conf, n|
		var geo;
		var h = 100;
		var h1 = 1 / h;
		var w = 6;
		var w2 = w - 2;
		var m = 2200;
		var sc = 1 / m * h;
		var d = 2048 * sc;
		var t = 2032 * sc; //TODO
		var p0, p1, p2, p3;
		
		conf.sendMsg("/engines/dump/enabled", true); // enable sensor array dump

		arr = Int8Array.newClear(n*2);
		geo = Rect(0, 0, n*w, h*2);
		p0 = Point.new(0, h-d);
		p1 = Point.new(n*w, h-d);
		p2 = Point.new(0, h+d);
		p3 = Point.new(n*w, h+d);

		Routine.run({
			win = Window.new("Chimaera Sensor Dump", geo, false).front;
			view = UserView(win, geo);
			view.drawFunc = {
				// draw grid lines
				Pen.strokeColor = Color.black;
				Pen.line(p0, p1);
				Pen.stroke;
				Pen.line(p2, p3);
				Pen.stroke;
				for(1,n-1, {|i|
					Pen.line(Point.new(i*16*w, 0), Point.new(i*16*w, 2*h));
					Pen.stroke;
				});

				// draw bars
				for(0,n-1, {|i|
					var msb, lsb, val;
					lsb = arr[i*2+1];
					if(lsb < 0) {lsb = 256+lsb};
					msb = arr[i*2] << 8;
					val = msb | lsb;
					val = val * sc;
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

		cb = OSCFunc({|msg, time, addr, port|
			arr = msg[2];
		}, "/dump", conf.rx);
	}
}
