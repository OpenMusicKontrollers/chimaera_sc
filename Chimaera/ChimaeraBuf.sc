ChimaeraBuf : Chimaera {
	classvar <gate, <xdim, <zdim;
	var sidict, gidict, num;

	*new {|s, n, iRx, iTx|
		^super.new.initConn(iRx, iTx).init(s, n);
	}

	*initClass {
		gate = 1;
		xdim = 2;
		zdim = 3;
	}

	init {|s, n|
		num = n;
		sidict = Dictionary.new;
		gidict = Dictionary.new;

		groupAddCb = {|gid|
			gidict[gid] = [
				0, // ptr
				Bus.control(s,num), // gate
				Bus.control(s,num), // x
				Bus.control(s,num)  // z
			];
		};

		on = {|sid, tid, gid, x, z|
			var arr = gidict[gid];

			sidict[sid] = arr[0]; // ptr
			arr[0] = arr[0] + 1;
			if(arr[0] >= num) {arr[0] = 0};

			arr[1].set(sidict[sid], 1); // gate
			arr[2].set(sidict[sid], x); // x
			arr[3].set(sidict[sid], z); // z
		};

		off = {|sid, tid, gid|
			var arr = gidict[gid];

			arr[1].set(sidict[sid], 0); // gate

			sidict[sid] = nil;
		};

		set = {|sid, tid, gid, x, z, vx, vz|
			var arr = gidict[gid];

			arr[2].set(sidict[sid], x);
			arr[3].set(sidict[sid], z);
		}
	}

	kr {|gid=0, dim=1|
		^In.kr(gidict[gid][dim], num);
	}
}
