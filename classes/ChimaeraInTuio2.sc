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

ChimaeraInTuio2 : ChimaeraIn {
	var firstFrame, frm, tok, alv, blobs, blobsOld, lastFid, lastTime, ignore;

	init {|s, conf, rx, iEngine|
		engine = iEngine;

		firstFrame = 1;
		blobs = Order.new;
		blobsOld = Array.new;
		lastFid = 0;
		lastTime = 0;
		ignore = false;

		conf.sendMsg("/engines/tuio2/enabled", true); // enable Tuio2 output engine

		frm = OSCFunc({ |msg, time, addr, port|
			var fid, timestamp;

			fid = msg[1];
			timestamp = time; //msg[2]; // TODO sclang does not support the OSC timestamp as argument

			if( (fid < lastFid) || (timestamp < lastTime), {
				["TUIO2 packet missing or late"].postln;
				ignore = true;
			}, {
				ignore = false;
			});
	
			lastFid = fid;
			lastTime = timestamp;
		}, "/tuio2/frm", rx);

		tok = OSCFunc({ |msg, time, addr, port|
			var o, sid, pid, gid, x, z;

			if(ignore == false, {
				sid = msg[1];
				pid = msg[2] & 0xffff;
				gid = msg[3];
				x = msg[4];
				z = msg[5];
				//a = msg[6]; // not used

				blobs[sid] = [time, sid, gid, pid, x, z];
			});
		}, "/tuio2/tok", rx);

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
						engine.on(b[0], b[1], b[2], b[3], b[4], b[5]); // time, sid, gid, pid, x, z
					}, {
						engine.set(b[0], b[1], b[4], b[5]); // time, sid, x, z
					});
				};

				blobsOld = msg;
			});
		}, "/tuio2/alv", rx);
	}
}
