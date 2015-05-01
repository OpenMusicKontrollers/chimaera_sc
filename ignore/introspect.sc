#!/usr/bin/env sclang

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

{
	var chimconf;
	var success, fail;
	var handleQueryResponse;
	var win, tree, view, dict, bound, line, gray, grays, dark, darks;

	chimconf = ChimaeraConf(s,
		addr:"chimaera.local");
	
	win = Window.new("Chimaera Configuration Wizard", Rect(0, 0, 1024, 640), true).front;
	win.onClose = {0.exit};
	tree = TreeView.new(win, Rect(0, 0, 1024, 640));
	tree.resize = 5;
	tree.columns = ["Description", "Value", "Attribute", "OSC Path", "Get", "Set", "Call"];
	gray = Color.gray(0.9);
	grays = [gray, gray, gray, gray, gray];
	dark = Color.gray(0.7);
	darks = [dark, dark, dark, dark, dark];

	bound = Rect(0, 0, 400, 20);
	line = 0;
	dict = Dictionary.new();

	handleQueryResponse = {|msg|
		var json = msg[0].asString.parseYAML;
		
		var path = json["path"];
		var description = json["description"];
		var type = json["type"];

		//msg[0].asString.postln;
	
		AppClock.sched(0, {
			if(path == "/") {
				view = tree.addItem([description, nil, nil, path]);
			} {
				view = dict[path];
				view = view.addChild([description, nil, nil, path]);
			};
			
			if(type == "node") {
				var items = json["items"];
				view.colors = darks;
				
				items do: {|itm|
					var child = path++itm;
					dict[child] = view;
					AppClock.sched(0.1, {
						chimconf.sendMsg(child++"!", handleQueryResponse);
					});
				}
			} {
				var arguments = json["arguments"];
				var fields = VLayout();
				var labels = VLayout();
				var fieldsStack = view.setView(1, View.new()).view(1).layout_(fields);
				var labelsStack = view.setView(2, View.new()).view(2).layout_(labels);
				var get;
				var set;
				var arr = Order.new();
				var hasR = 0;
				var hasW = 0;
				
				if(line%2 == 1) { view.colors = grays; };
				line = line+1;
				
				arguments do: {|argv, i|
					var type = argv["type"];
					var read = false;
					var write = false;
					var description = argv["description"];
					var itm;

					if(argv["read"] == "true") {read=true};
					if(argv["write"] == "true") {write=true};

					if(read) {hasR = hasR + 1};
					if(write) {hasW = hasW + 1};

					switch(type,
						"i", {
							var range = argv["range"];
							var values = argv["values"];
							if(range.notNil, {
								range[0] = range[0].asInt;
								range[1] = range[1].asInt;
								range[2] = range[2].asInt;
								if( (range[0] == 0) && (range[1] == 1) ) {
									itm = CheckBox.new();
									itm.enabled = write;
									itm.value = range[0];
								} {
									itm = NumberBox.new();
									itm.enabled = write;
									itm.value = range[0];
									itm.decimals = 0;
									itm.clipLo = range[0];
									itm.clipHi = range[1];
								};
							}, if(values.notNil, {
								itm = NumberBox.new();
								itm.enabled = write;
							}))
						},
						"f", {
							var range = argv["range"];
							var values = argv["values"];
							if(range.notNil, {
								range[0] = range[0].asFloat;
								range[1] = range[1].asFloat;
								range[2] = range[2].asFloat;
								itm = NumberBox.new();
								itm.enabled = write;
								itm.value = range[0];
								itm.decimals = 6;
								itm.clipLo = range[0];
								itm.clipHi = range[1];
							}, if(values.notNil, {
								itm = NumberBox.new();
								itm.enabled = write;
							}))
						},
						"s", {
							var range = argv["range"];
							var values = argv["values"];
							if(range.notNil, {
								range[0] = range[0].asInt;
								range[1] = range[1].asInt;
								range[2] = range[2].asInt;
								itm = TextField.new();
								itm.enabled = write;
							}, if(values.notNil, {
								itm = TextField.new();
								itm.enabled = write;
							}))
						}
					);
					fields.add(itm);
					arr[i] = itm;
					labels.add(StaticText.new().string_(description));
					//view.setString(2, description);
				};

				if(hasR > 0) {
					get = view.setView(4, Button.new()).view(4);
					get.states = [["get"]];
					get.action = {
						chimconf.sendMsg(path, {|msg|
							AppClock.sched(0, {
								arr do: {|itm, i|
									switch(arguments[i]["type"], 
										"i", {itm.value = msg[0+i].asInt},
										"f", {itm.value = msg[0+i].asFloat},
										"s", {itm.value = msg[0+i].asString}
									);
								};
							});
						});
					};
					get.action.value;
				};

				if(hasW > 0) {
					set = view.setView(5, Button.new()).view(5);
					set.states = [["set"]];
					set.action = {
						var vals = Array.new(hasW);

						arr do: {|itm, i|
							if(arguments[i]["write"] == "true") {
								switch(arguments[i]["type"], 
									"i", {vals.add(itm.value.asInt)},
									"f", {vals.add(itm.value.asFloat)},
									"s", {vals.add(itm.value.asString)}
								);
							};
						};

						chimconf.sendMsg(path, vals);
					};
				};

				if( (hasR == 0) && (hasW == 0) ) {
					set = view.setView(6, Button.new()).view(6);
					set.states = [["call"]];
					set.action = { chimconf.sendMsg(path); };
				};
			};
		});
	};

	chimconf.sendMsg("/!", handleQueryResponse);
}.value;
