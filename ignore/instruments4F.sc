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

{|n|
	var baseInst, loadInst, win, adrop, path;

	/*
	 * populate instrument name arrays
	 */
	baseInst = Array.new(64);
	path = "./instruments/4F/";
	p = PathName(path);
	p.filesDo({|n|
		var name = n.fileNameWithoutExtension;
		baseInst.add(name);
	});

	/*
	 * load instrument
	 */
	loadInst = {|group, inst|
		(path++inst++".sc").load.value(group, n);
	};

	loadInst.value(\inst_0, baseInst[0]);

	win = Window.new("4F Instruments", Rect(200,200,200,100)).front;

	adrop = PopUpMenu(win, Rect(10,10,180,20));
	adrop.items = baseInst;
	adrop.action = {|menu|
		loadInst.value(\inst_0, menu.item);
	};
}
