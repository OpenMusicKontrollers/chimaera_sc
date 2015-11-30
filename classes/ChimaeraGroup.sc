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

ChimaeraGroup {
	var config, win, layout, range, north, south, scale;

	*new {|s, conf, rx|
		^super.new.init(s, conf, rx);
	}

	*initClass {
		//TODO
	}

	get {|gid|
		config.sendMsg("/sensors/group/attributes/"++gid++"/min", {|msg|
			Routine.run({
				range[gid].lo = msg[0];
			}, clock:AppClock);
		});
		config.sendMsg("/sensors/group/attributes/"++gid++"/max", {|msg|
			Routine.run({
				range[gid].hi = msg[0];
			}, clock:AppClock);
		});
		config.sendMsg("/sensors/group/attributes/"++gid++"/north", {|msg|
			Routine.run({
				north[gid].value = msg[0].asBoolean;
			}, clock:AppClock);
		});
		config.sendMsg("/sensors/group/attributes/"++gid++"/south", {|msg|
			Routine.run({
				south[gid].value = msg[0].asBoolean;
			}, clock:AppClock);
		});
		config.sendMsg("/sensors/group/attributes/"++gid++"/scale", {|msg|
			Routine.run({
				scale[gid].value = msg[0].asBoolean;
			}, clock:AppClock);
		});
	}

	set {|gid|
		config.sendMsg("/sensors/group/attributes/"++gid++"/min", range[gid].lo);
		config.sendMsg("/sensors/group/attributes/"++gid++"/max", range[gid].hi);
		config.sendMsg("/sensors/group/attributes/"++gid++"/north", north[gid].value);
		config.sendMsg("/sensors/group/attributes/"++gid++"/south", south[gid].value);
		config.sendMsg("/sensors/group/attributes/"++gid++"/s", scale[gid].value);
	}

	init {|s, conf, rx|
		config = conf;
		config.sendMsg("/sensors/group/number", {|n|
			n = n[0];

			range = Order.new();
			north = Order.new();
			south = Order.new();
			scale = Order.new();

			Routine.run({
				win = Window.new("Chimaera Group Configurator", Rect(0, 0, 800, 400), false).front;
				layout = VLayout();
				win.layout = layout;

				n.do {|i|
					var v2 = View.new();
					var lay = HLayout();
					var updt = {this.set(i)};

					range[i] = EZRanger(parent:v2, label:"Group"+i++" ", initVal:[0,1], action:updt).setColors(knobColor:Color.red(0.733));
					north[i] = CheckBox().action_(updt);
					south[i] = CheckBox().action_(updt);
					scale[i] = CheckBox().action_(updt);

					layout.add(lay);
					lay.add(v2, stretch:8);
					lay.add(StaticText().string_("north?"), stretch:1);
					lay.add(north[i], stretch:1);
					lay.add(StaticText().string_("south?"), stretch:1);
					lay.add(south[i], stretch:1);
					lay.add(StaticText().string_("scale?"), stretch:1);
					lay.add(scale[i], stretch:1);

					this.get(i);
				};

				layout.add(HLayout().add(
					Button().states_([["load"]]).action_({
						config.sendMsg("/config/load");
						n.do {|i| this.get(i)};
					})).add(
					Button().states_([["save"]]).action_({config.sendMsg("/config/save")})));
			}, clock:AppClock);
		});
	}
}
