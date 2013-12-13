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
	var win, button, num, text, desc, acts, is;

	*new {|s, conf|
		^super.new.init(s, conf);
	}

	*initClass {
		//TODO
	}

	init {|s, conf|
		win = Window.new("Chimaera Calibration Wizard", Rect(0, 0, 400, 250), false).front;

		desc = [
			"Start calibration procedure",
			"Determine the quiescent state of each sensor: Do not bring any magnetic source near the sensor array for some seconds and procede.",
			"Determine the threshold of each sensor: move the magnet at a fixed distance along the sensor array. This distance represents the sensors threshold, e.g. touch events will be triggered only above this threshold. Start left with one polarity, go right, return left, turn the polarity around, go right, return left and procede. This also gathers the first point for the 5-point sensor-distance curve-fit.",
			"Gather second point for 5-point sensor-distance curve-fit. Move both poles of the magnet at a fixed vicinity y1>0 (e.g. 0.25) over the given sensor only. Fill in your vicinity into the box and procede.",
			"Gather third point for 5-point sensor-distance curve-fit. Move both poles of the magnet at a fixed vicinity y2>y1 (e.g. 0.5) over the given sensor only. Fill in your vicinity into the box and procede.",
			"Gather fourth point for 5-point sensor-distance curve-fit. Move both poles of the magnet at a fixed vicinity y3>y2 (e.g. 0.75) over the given sensor only. Fill in your vicinity into the box and procede.",
			"Gather fithth point for 5-point sensor-distance curve-fit. Move both poles of the magnet at a fixed vicinity y4=1 over the given sensor only. This vicinity represents the maximal sensor value and hence the smallest distance (or even none) from the case.",
			"Determine the sensitivity of each sensor: move the magnet along the sensor array at a fixed vicinity (e.g. 0.75). Start left with one polarity, go right, return left, turn the polarity around, go right, return left and procede.",
			"You can now save your calibration data to EEPROM, so the device will load it at future boots.",
		];

		text = StaticText.new(win, Rect(10, 90, 380, 140));
		text.string = desc[0];

		acts = [
			{ conf.sendMsg("/chimaera/calibration/start"); },
			{ conf.sendMsg("/chimaera/calibration/zero"); },
			{ conf.sendMsg("/chimaera/calibration/min", {|msg|
				num.visible=true;
				is.string="Sensor #"++msg[0];
				is.visible=true;
			})},
			{ conf.sendMsg("/chimaera/calibration/mid", num.value); },
			{ conf.sendMsg("/chimaera/calibration/mid", num.value); },
			{ conf.sendMsg("/chimaera/calibration/mid", num.value, {|msg|
				num.value=1;
				num.visible=false;
			})},
			{ conf.sendMsg("/chimaera/calibration/max", {|msg|
				num.visible=true;
				is.visible=false;
			})},
			{ conf.sendMsg("/chimaera/calibration/end", num.value, {|msg|
				num.value=0;
				num.visible=false;
				is.visible=false;
			})},
			{ conf.sendMsg("/chimaera/calibration/save", 0); },
		];

		button = Button.new(win, Rect(10, 10, 380, 40));
		button.states = [
			["Start Calibration"],
			["Step 1: Quiescence"],
			["Step 2: Threshold @ y=0"],
			["Step 3: Curve fit @ y1>0"],
			["Step 4: Curve fit @ y2>y1"],
			["Step 5: Curve fit @ y3>y2"],
			["Step 6: Curve fit @ y=1"],
			["Step 6: Sensitivity @ y=?"],
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

		is= StaticText.new(win, Rect(10, 50, 190, 40));
		is.visible = false;
		is.string = "unknown";

    num = NumberBox.new(win, Rect(200, 50, 190, 40));
		num.clipLo = 0.0;
		num.clipHi = 1.0;
		num.decimals = 3;
		num.visible = false;
    num.value = 0.0;
	}
}
