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

ChimaeraMapLinearCPS {
	*kr { |freq, n=128, oct=2|
		var bot = oct*12 - 0.5 - (n % 18 / 6);
		var top = n/3 + bot;
	
		// linear mapping from [0,1] to MIDI notes
		freq = LinLin.kr(freq, 0, 1, bot, top);

		// MIDI notes to frequency mapping
		freq = freq.midicps;

		^freq;
	}
}

ChimaeraMapStepCPS {
	*kr { |freq, n=128, oct=2|
		var bot = oct*12 - 0.5 - (n % 18 / 6);
		var top = n/3 + bot;
	
		// linear mapping from [0,1] to MIDI notes
		freq = LinLin.kr(freq, 0, 1, bot, top);

		// binary stepwise mapping
		freq = freq.round;

		// MIDI notes to frequency mapping
		freq = freq.midicps;

		^freq;
	}
}

ChimaeraMapPolyStepCPS {
	*kr { |freq, n=128, oct=2, order=3|
		var bot = oct*12 - 0.5 - (n % 18 / 6);
		var top = n/3 + bot;

		var ex = 2 ** ((order-1) / order);
		var ro, rel, odd, sig;
	
		// linear mapping from [0,1] to MIDI notes
		freq = LinLin.kr(freq, 0, 1, bot, top);

		// polynomial stepwise mapping
		ro = freq.round; // round to whole note
		rel = freq - ro; // relative difference to next whole note
		odd = order.mod(2); // is order odd?
		sig = Select.kr(odd, [
			InRange.kr(rel, 0, 1) * 2 - 1, // even: is rel negative?
			1 // odd:
		]);
		freq = rel * ex ** order * sig + ro;

		// MIDI notes to frequency mapping
		freq = freq.midicps;

		^freq;
	}
}

ChimaeraMap2ndOrderStepCPS {
	*kr { |freq, n=128, oct=2|
		^ChimaeraMapPolyStepCPS.kr(freq, n:n, oct:oct, order:2);
	}
}

ChimaeraMap3rdOrderStepCPS {
	*kr { |freq, n=128, oct=2|
		^ChimaeraMapPolyStepCPS.kr(freq, n:n, oct:oct, order:3);
	}
}

ChimaeraMap4thOrderStepCPS {
	*kr { |freq, n=128, oct=2|
		^ChimaeraMapPolyStepCPS.kr(freq, n:n, oct:oct, order:4);
	}
}

ChimaeraMap5thOrderStepCPS {
	*kr { |freq, n=128, oct=2|
		^ChimaeraMapPolyStepCPS.kr(freq, n:n, oct:oct, order:5);
	}
}
