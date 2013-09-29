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
	var srcDict, dstDict, srcPort, dstPort, win, srcDrop, dstDrop;

	srcDict = Dictionary.new;
	dstDict = Dictionary.new;

	srcPort = Dictionary.new;
	dstPort = Dictionary.new;

	MIDIClient.init;
	MIDIClient.sources.do({|endpoint| srcDict[endpoint.name]=endpoint});
	MIDIClient.destinations.do({|endpoint| dstDict[endpoint.name]=endpoint});

	win = Window.new("MIDI port", Rect(200,200,400,100)).front;

	srcDrop = PopUpMenu(win, Rect(10,10,180,20));
	srcDrop.items = srcDict.keys.asArray;
	srcDrop.action = {|n|
		srcPort[n.item] = MIDIIn(srcDict[n.item].uid);
	};

	dstDrop = PopUpMenu(win, Rect(200,10,180,20));
	dstDrop.items = dstDict.keys.asArray;
	dstDrop.action = {|n|
		dstPort[n.item] = MIDIOut(dstDict[n.item].uid);
	};
}
