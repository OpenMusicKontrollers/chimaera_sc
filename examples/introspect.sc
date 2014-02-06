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
	var rx, tx, reply, win, tree, view, dict;

	thisProcess.openUDPPort(3333); // open port 3333 to listen for Tuio messages
	thisProcess.openUDPPort(4444); // open port 4444 for listening to chimaera configuration replies

	rx = NetAddr ("chimaera.local", 4444);
	tx = NetAddr ("chimaera.local", 4444);
	
	win = Window.new("Chimaera Configuration Wizard", Rect(0, 0, 800, 600), false).front;
	tree = TreeView.new(win, Rect(0, 0, 800, 600));
	tree.columns = ["path", "description", "type", "value", "unit"];

	dict = Dictionary.new();

	reply = OSCFunc({|msg, time, addr, port|
		AppClock.sched(0, {
			var json = msg[1].asString.parseYAML;
			var path = json["path"];
			var description = json["description"];
			var type = json["type"];
		
			if(path == "/") {
				view = tree.addItem([path, description, type]);
			} {
				view = dict[path];
				view = view.addChild([path, description, type]);
			};

			if(type == "node") {
				var items = json["items"];
				items.postln;
				items do: {|itm|
					var child = path++itm;
					dict[child] = view;
					tx.sendMsg(child++"!");
				}
			} {
				var arguments = json["arguments"];
				arguments do: {|argv|
					var description = argv["description"];
					var type = argv["type"];
					var optional = argv["optional"];
					var range = argv["range"];
					switch(type,
						"i", {
							range[0] = range[0].asInt;
							range[1] = range[1].asInt;
							if( (range[0] == 0) && (range[1] == 1) ) {
								view.setView(3, CheckBox.new(win, Rect(0, 0, 100, 20)));
								view.view(3).value = range[0];
							} {
								view.setView(3, NumberBox.new(win, Rect(0, 0, 100, 20)));
								view.view(3).value = range[0];
								view.view(3).decimals = 0;
								view.view(3).clipLo = range[0];
								view.view(3).clipHi = range[1];
							};
							view.setString(4, description);
						},
						"f", {
							range[0] = range[0].asFloat;
							range[1] = range[1].asFloat;
							view.setView(3, NumberBox.new(win, Rect(0, 0, 100, 20)));
							view.view(3).value = range[0];
							view.view(3).decimals = 6;
							view.view(3).clipLo = range[0];
							view.view(3).clipHi = range[1];
							view.setString(4, description);
						},
						"s", {
							view.setView(3, TextField.new(win, Rect(0, 0, 100, 20)));
						}
					);
					("\t"++[description, type, optional, range]).postln;
				}
			};
		});
	}, "/success", rx);

	tx.sendMsg("/!");
}.value;
