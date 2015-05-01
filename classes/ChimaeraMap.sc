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
