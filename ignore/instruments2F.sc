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

{|n, baseSym, leadSym|
	var baseInst, leadInst, loadInst, win, adrop, bdrop, path;

	/*
	 * populate instrument name arrays
	 */
	baseInst = Array.new(64);
	leadInst = Array.new(64);
	path = "./instruments/2F/";
	p = PathName(path);
	p.filesDo({|n|
		var name = n.fileNameWithoutExtension;
		baseInst.add(name);
		leadInst.add(name);
	});

	/*
	 * load instrument
	 */
	loadInst = {|group, inst|
		(path++inst++".sc").load.value(group, n);
	};

	loadInst.value(\base, baseInst[0]);
	loadInst.value(\lead, leadInst[0]);

	win = Window.new("2F Instruments ("++n++")", Rect(200,200,400,100)).front;

	adrop = PopUpMenu(win, Rect(10,10,180,20));
	adrop.items = baseInst;
	adrop.action = {|menu|
		loadInst.value(baseSym, menu.item);
	};

	bdrop = PopUpMenu(win, Rect(200,10,180,20));
	bdrop.items = leadInst;
	bdrop.action = {|menu|
		loadInst.value(leadSym, menu.item);
	};
}
