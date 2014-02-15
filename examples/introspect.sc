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
	var rx, tx;
	var success, fail;
	var handleQueryResponse, handleStandardResponse, callbacks;
	var win, tree, view, dict, bound, line, gray, grays, dark, darks;
	var modeR, modeW, modeX;

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies

	rx = NetAddr ("chimaera.local", 4444);
	tx = NetAddr ("chimaera.local", 4444);
	
	win = Window.new("Chimaera Configuration Wizard", Rect(0, 0, 1024, 640), false).front;
	tree = TreeView.new(win, Rect(0, 0, 1024, 640));
	tree.columns = ["description", "type", "value", "unit", "path", "read", "write", "call"];
	gray = Color.gray(0.9);
	grays = [gray, gray, gray, gray, gray];
	dark = Color.gray(0.7);
	darks = [dark, dark, dark, dark, dark];

	bound = Rect(0, 0, 400, 20);
	line = 0;
	dict = Dictionary.new();
	callbacks = Dictionary.new();

	modeR = 1;
	modeW = 2;
	modeX = 4;

	handleQueryResponse = {|msg, time, addr, port|
		var uuid = msg[1];
		var dest = msg[2].asString;
		var json = msg[3].asString.parseYAML;
		
		var path = json["path"];
		var description = json["description"];
		var type = json["type"];
		
		if(path == "/") {
			view = tree.addItem([description, type, nil, nil, path, nil]);
		} {
			view = dict[path];
			view = view.addChild([description, type, nil, nil, path, nil]);
		};
		
		if(type == "node") {
			var items = json["items"];
			view.colors = darks;
			
			items do: {|itm|
				var child = path++itm;
				dict[child] = view;
				tx.sendMsg(child++"!", 1000.rand);
			}
		} {
			var arguments = json["arguments"];
			var layout = VLayout();
			var stack = view.setView(2, View.new()).view(2).layout_(layout);
			var get;
			var set;
			var arr = Order.new();
			var hasR = 0;
			var hasW = 0;
			var hasX = 0;
			
			if(line%2 == 1) { view.colors = grays; };
			line = line+1;
			
			arguments do: {|argv, i|
				var type = argv["type"];
				var mode = argv["mode"].asInt;
				var description = argv["description"];
				var itm;

				if(mode & modeR != 0) {hasR = hasR + 1};
				if(mode & modeW != 0) {hasW = hasW + 1};
				if(mode & modeX != 0) {hasX = hasX + 1};

				switch(type,
					"i", {
						var range = argv["range"];
						range[0] = range[0].asInt;
						range[1] = range[1].asInt;
						if( (range[0] == 0) && (range[1] == 1) ) {
							itm = CheckBox.new();
							itm.enabled = mode != modeR;
							itm.value = range[0];
						} {
							itm = NumberBox.new();
							itm.enabled = mode != modeR;
							itm.value = range[0];
							itm.decimals = 0;
							itm.clipLo = range[0];
							itm.clipHi = range[1];
						};
					},
					"f", {
						var range = argv["range"];
						range[0] = range[0].asFloat;
						range[1] = range[1].asFloat;
						itm = NumberBox.new();
						itm.enabled = mode != modeR;
						itm.value = range[0];
						itm.decimals = 6;
						itm.clipLo = range[0];
						itm.clipHi = range[1];
					},
					"s", {
						itm = TextField.new();
						itm.enabled = mode != modeR;
					}
				);
				layout.add(itm);
				arr[i] = itm;
				view.setString(3, description);
			};

			if(hasR > 0) {
				get = view.setView(5, Button.new()).view(5);
				
				get.states = [["get"]];

				get.action = {
					var vals = Array.new(hasX);

					arr do: {|itm, i|
						if(arguments[i]["mode"].asInt & modeX != 0) {
							switch(arguments[i]["type"], 
								"i", {vals.add(itm.value.asInt)},
								"f", {vals.add(itm.value.asFloat)},
								"s", {vals.add(itm.value.asString)}
							);
						};
					};

					tx.sendMsg(path, 1000.rand, *vals);
					
					callbacks[path] = {|msg, time, addr, port|
						var uuid = msg[1].asInt;
						var dest = msg[2].asString;
						
						["get callback", msg, time, addr, port].postln;
						arr do: {|itm, i|
							switch(arguments[i]["type"], 
								"i", {itm.value = msg[3+i].asInt},
								"f", {itm.value = msg[3+i].asFloat},
								"s", {itm.value = msg[3+i].asString}
							);
						};
					};
				};
			};

			if(hasW > 0) {
				set = view.setView(6, Button.new()).view(6);
				
				set.states = [["set"]];

				set.action = {
					var vals = Array.new(hasW);

					arr do: {|itm, i|
						if(arguments[i]["mode"].asInt & modeW != 0) {
							switch(arguments[i]["type"], 
								"i", {vals.add(itm.value.asInt)},
								"f", {vals.add(itm.value.asFloat)},
								"s", {vals.add(itm.value.asString)}
							);
						};
					};

					tx.sendMsg(path, 1000.rand, *vals);

					callbacks[path] = {|msg, time, addr, port|
						var uuid = msg[1].asInt;
						var dest = msg[2].asString;
						
						["set callback", msg, time, addr, port].postln;
						//TODO
					};
				};
			};

			if( (hasR == 0) && (hasW == 0) ) {
				set = view.setView(7, Button.new()).view(7);
				
				set.states = [["call"]];

				set.action = {
					tx.sendMsg(path, 1000.rand);

					callbacks[path] = {|msg, time, addr, port|
						var uuid = msg[1].asInt;
						var dest = msg[2].asString;
						
						["call callback", msg, time, addr, port].postln;
						//TODO
					};
				};

			};
		};
	};

	handleStandardResponse = {|msg, time, addr, port|
		var uuid = msg[1].asInt;
		var dest = msg[2].asString;

		callbacks[dest].value(msg, time, addr, port);
	};

	success = OSCFunc({|msg, time, addr, port|
		var uuid = msg[1].asInt;
		var dest = msg[2].asString;

		AppClock.sched(0, {
			if(msg[2].asString.endsWith("!")) {
				handleQueryResponse.value(msg, time, addr, port);
			} {
				handleStandardResponse.value(msg, time, addr, port);
			};
		});
	}, "/success", rx);

	fail = OSCFunc({|msg, time, addr, port|
		var uuid = msg[1].asInt;
		var dest = msg[2].asString;
		var err = msg[3].asString;
		["fail", uuid, dest, err].postln;
	}, "/fail", rx);

	tx.sendMsg("/!", 1000.rand);
}.value;
