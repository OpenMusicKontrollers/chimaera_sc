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

ChimaeraTuio2 {
	classvar first_frame;
	var rx, frm, tok, alv, <>on, <>off, <>set, old_blobs, new_blobs, last_fid, last_time, missing, ignore;

	*new {|s, iRx|
		^super.new.init(s, iRx);
	}

	*classInit {
		first_frame = 1;
	}

	initConn {|iRx|
		rx = iRx;
		old_blobs = Dictionary.new;
		new_blobs = Dictionary.new;
		last_fid = 0;
		last_time = 0;
		missing = 0;
		ignore = false;

		// handling tuio messages
		frm = OSCFunc({|msg, time, addr, port|
			var fid, timestamp;

			fid = msg[1];
			timestamp = msg[2];

			/*
			if(fid == first_frame) { // the chimaera has been reset, reset our structs, too
				old_blobs.clear;
				new_blobs.clear;
				last_fid = fid;
				missing = 0;
				ignore = false;
				"chimaera has been reset, plugin is being reset, too".postln;
			} {
				if( (fid < last_fid) && (missing > 0) ) {
					ignore = true;
					missing = missing - 1;
				} {
					if( (last_fid != first_frame) && (fid > last_fid + 1) ) {
						missing = missing + (fid - last_fid - 1);
						((fid-last_fid-1)++" bundles ("++(last_fid)++"-"++(fid-1)++" were just found to be missing, total missing bundles: "++(missing)).postln;
					};
				};
			};
			*/
	
			last_fid = fid;
			last_time = timestamp;

		}, "/tuio2/frm", rx);

		tok = OSCFunc({|msg, time, addr, port|
			var sid, pid, gid, x, z, a;

			sid = msg[1];
			pid = msg[2] & 0xffff;
			gid = msg[3];
			x = msg[4];
			z = msg[5];
			//a = msg[6]; // not used

			new_blobs[sid] = [time, sid, pid, gid, x, z];
		}, "/tuio2/tok", rx);

		alv = OSCFunc({|msg, time, addr, port|
			var n, tmp;

			/*
			if(ignore == true) {
				^1;
			};
			*/

			n = msg.size - 1;
			if (n != new_blobs.size) {
				n = new_blobs.size};

			// search for disappeard blobs
			old_blobs do: {|v|
				if (new_blobs[v[1]].isNil)
				{
					if(off.notNil) {off.value (v[0], v[1], v[2], v[3])};
				};
			};

			// search for new blobs
			new_blobs do: {|v|
				if (old_blobs[v[1]].isNil) {
					if(on.notNil) {on.value (v[0], v[1], v[2], v[3], v[4], v[5])};
				}
				{	
					if(set.notNil) {set.value (v[0], v[1], v[2], v[3], v[4], v[5])};
				};
			};

			tmp = old_blobs;
			old_blobs = new_blobs;
			new_blobs = tmp.clear;
		}, "/tuio2/alv", rx);

		on = nil;
		off = nil;
		set = nil;
	}

	init {|s, iRx|
		this.initConn(iRx);
	}
}
