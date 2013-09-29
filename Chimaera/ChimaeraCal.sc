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

ChimaeraCal {
	var win, button, text, desc, acts;

	*new {|s, conf|
		^super.new.init(s, conf);
	}

	*initClass {
		//TODO
	}

	init {|s, conf|
		win = Window.new("Chimaera Calibration", Rect(0, 0, 400, 200), false).front;

		desc = [
			"Starts calibration procedure",
			"Determines the quiescent state of each sensor: Do not bring any magnetic source near the sensor array for some seconds and procede.",
			"Determine the threshold of each sensor: move the magnet at a fixed distance along the sensor array. This distance represents the sensors threshold, e.g. touch events will be triggered only above this threshold. Start left with one polarity, go right, return left, turn the polarity around, go right, return left and procede.",
			"Determine the sensitivity of each sensor: move the magnet along the sensor array right above the casing. This distance represents the maximal sensor values. Start left with one polarity, go right, return left, turn the polarity around, go right, return left and procede.",
			"You can now save your calibration data to EEPROM, wo the device will load it at future boots.",
		];

		text = StaticText.new(win, Rect(10, 50, 380, 140));
		text.string = desc[0];
	
		//conf.sendMsg("/chimaera/calibration/reset"); // reset all output engines

		acts = [
			{ conf.sendMsg("/chimaera/calibration/start"); },
			{ conf.sendMsg("/chimaera/calibration/zero"); },
			{ conf.sendMsg("/chimaera/calibration/min"); },
			{ conf.sendMsg("/chimaera/calibration/mid", 0.7); },
			{ conf.sendMsg("/chimaera/calibration/save", 0); },
		];

		button = Button.new(win, Rect(10, 10, 380, 40));
		button.states = [
			["Start Calibration"],
			["Step 1: Quiescence"],
			["Step 2: Threshold"],
			["Step 3: Sensitivity"],
			["Save Calibration"],
		];
		button.action = { |b|
			var i, j;
			i = b.value;
			if(i == 0) {j=desc.size-1} {j=i-1};
			[i, j].postln;
			acts[j].value();
			text.string = desc[i];
		};

		/*
		b= Button.new(win,Rect(10,0,80,30)).states_([["Hide"],["Show"]]);
		s = Slider.new(w,Rect(95,0,150,30));
		c = CompositeView.new(w,Rect(20,35,100,60));
		StaticText.new(c,Rect(0,0,80,30)).string_("Hello");
		StaticText.new(c,Rect(20,30,80,30)).string_("World!");
		b.action = { c.visible = b.value.asBoolean.not };
		*/
	}
}
