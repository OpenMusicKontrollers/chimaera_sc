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

ChimaeraInTuio2 : ChimaeraIn {
	var firstFrame, frm, tok, alv, blobs, blobsOld, lastFid, lastTime, ignore;

	init {|s, conf, iEngine|
		engine = iEngine;

		firstFrame = 1;
		blobs = Order.new;
		blobsOld = Array.new;
		lastFid = 0;
		lastTime = 0;
		ignore = false;

		conf.sendMsg("/engines/tuio2/enabled", true); // enable Tuio2 output engine
		conf.sendMsg("/engines/tuio2/derivatives", true); // enable derivatives 

		frm = OSCFunc({ |msg, time, addr, port|
			var fid;

			fid = msg[1];

			if( (fid < lastFid) || (time < lastTime), {
				["TUIO2 packet missing or late"].postln;
				ignore = true;
			}, {
				ignore = false;
			});
	
			lastFid = fid;
			lastTime = time;
		}, "/tuio2/frm", conf.rx);

		tok = OSCFunc({ |msg, time, addr, port|
			var o, sid, pid, gid, x, z, vx, vz;

			if(ignore == false, {
				sid = msg[1];
				pid = msg[2] & 0xffff;
				gid = msg[3];
				x = msg[4];
				z = msg[5];
				//a = msg[6]; // not used
				vx = msg[7];
				vz = msg[8];

				blobs[sid] = [time, sid, gid, pid, x, z, vx, vz];
			});
		}, "/tuio2/tok", conf.rx);

		alv = OSCFunc({ |msg, time, addr, port|
			var n, tmp;

			if(ignore == false, {
				msg.removeAt(0); // remove /tuio2/alv

				n = msg.size;
				if(n==0, {
					engine.idle(time);
				});

				// search for disappeard blobs
				blobsOld do: {|v|
					if(msg.indexOf(v).isNil, {
						var b = blobs[v];
						engine.off(b[0], b[1]); // time, sid
						blobs.removeAt(v);
					});
				};

				// search for new blobs
				msg do: {|v|
					var b = blobs[v];
					if(blobsOld.indexOf(v).isNil, {
						engine.on(b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7]); // time, sid, gid, pid, x, z, vx, vz
					}, {
						engine.set(b[0], b[1], b[4], b[5], b[6], b[7]); // time, sid, x, z, vx, vz
					});
				};

				blobsOld = msg;
			});
		}, "/tuio2/alv", conf.rx);
	}
}
