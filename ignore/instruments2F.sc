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

{|n=160, symbols=nil|
	var instruments, loadInst, win, drops, path;

	/*
	 * populate instrument name array
	 */
	instruments = Array.new(64);
	path = "./instruments/2F/";
	p = PathName(path);
	p.filesDo({|n|
		var name = n.fileNameWithoutExtension;
		instruments.add(name);
	});

	/*
	 * load instrument
	 */
	loadInst = {|group, inst|
		(path++inst++".sc").load.value(group, n);
	};

	if(symbols.isNil, {
		symbols = [\synth_0, \synth_1, \synth_2, \synth_3, \synth_4, \synth_5, \synth_6, \synth_7];
	});

	win = Window.new("2F Instruments ("++n++")", Rect(0, 0, 200, 20*symbols.size+20)).front;

	symbols.do({|sym, i|
		var drop = PopUpMenu(win, Rect(10, 20*i+10, 180, 20));
		drop.items = instruments;
		drop.action = {|menu|
			loadInst.value(sym, menu.item);
		};
		drops.add(drop);

		loadInst.value(sym, instruments[0]);
	});
}
