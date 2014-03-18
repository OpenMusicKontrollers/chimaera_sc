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

ChimaeraGroup {
	var config, win, layout, range, north, south, scale;

	*new {|s, conf, rx|
		^super.new.init(s, conf, rx);
	}

	*initClass {
		//TODO
	}

	get {|i|
		config.sendMsg("/sensor/group/attributes", i, {|msg|
			var gid = msg[0];
			var pid = msg[1];
			var x0 = msg[2];
			var x1 = msg[3];
			var no = pid & 0x100 == 0x100;
			var so = pid & 0x80 == 0x80;
			var s = msg[4].asBoolean;

			Routine.run({
				range[i].lo = x0;
				range[i].hi = x1;
				north[i].value = no;
				south[i].value = so;
				scale[i].value = s;
			}, clock:AppClock);
		});
	}

	set {|i|
		var gid = i;
		var pid = 0;
		var x0 = range[i].lo;
		var x1 = range[i].hi;
		var s = scale[i].value;
		if(north[i].value) {pid = pid | 0x100};
		if(south[i].value) {pid = pid | 0x080};
		config.sendMsg("/sensor/group/attributes", gid, pid, x0, x1, s);
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
						config.sendMsg("/sensor/group/load");
						n.do {|i| this.get(i)};
					})).add(
					Button().states_([["save"]]).action_({config.sendMsg("/sensor/group/save")})));
			}, clock:AppClock);
		});
	}
}
