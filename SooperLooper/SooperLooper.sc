/*
 * Copyright (c) 2012-2013 Hanspeter Portner (agenthp@users.sf.net)
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

SooperLooper {
	classvar ctrue, cfalse;
	var rx, rat, stomp, channel, mytrig, cols, <>on, <>off;

	*new {|s, iRx|
		^super.new.init(s, iRx);
	}

	initConn {|iRx|
		rx = iRx;
	}

	init {|s, iRx|
		this.initConn(iRx);
	}

	hit {|channel, cmd| rx.sendMsg("/sl/"++channel++"/hit", cmd)}
	get {|channel, key| rx.sendMsg("/sl/"++channel++"/get", key)}
	set {|channel, key, val| rx.sendMsg("/sl/"++channel++"/set", key, val)}

	record {|channel=0| this.hit(channel, \record)}
	overdub {|channel=0| this.hit(channel, \overdub)}
	multiply {|channel=0| this.hit(channel, \multiply)}
	insert {|channel=0| this.hit(channel, \insert)}
	replace {|channel=0| this.hit(channel, \replace)}
	reverse {|channel=0| this.hit(channel, \reverse)}
	substitute {|channel=0| this.hit(channel, \substitute)}

	mute {|channel=0| this.hit(channel, \mute)}
	mute_on {|channel=0| this.hit(channel, \mute_on)}
	mute_off {|channel=0| this.hit(channel, \mute_off)}

	undo {|channel=0| this.hit(channel, \undo)}
	redo {|channel=0| this.hit(channel, \redo)}
	undo_all {|channel=0| this.hit(channel, \undo_all)}
	redo_all {|channel=0| this.hit(channel, \redo_all)}

	oneshot {|channel=0| this.hit(channel, \oneshot)}
	trigger {|channel=0| this.hit(channel, \trigger)}
	pause {|channel=0| this.hit(channel, \pause)}

	solo {|channel=0| this.hit(channel, \solo)}
	solo_next {|channel=0| this.hit(channel, \solo_next)}
	solo_prev {|channel=0| this.hit(channel, \solo_prev)}
	record_solo {|channel=0| this.hit(channel, \record_solo)}
	record_solo_next {|channel=0| this.hit(channel, \record_solo_next)}
	record_solo_prev {|channel=0| this.hit(channel, \record_solo_prev)}

	set_sync_pos {|channel=0| this.hit(channel, \set_sync_pos)}
	reset_sync_pos {|channel=0| this.hit(channel, \reset_sync_pos)}
}
