/*
 * Copyright (c) 2014 Hanspeter Portner (dev@open-music-kontrollers.ch)
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
