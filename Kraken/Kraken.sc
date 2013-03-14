Kraken {
	classvar last, fid;
	var listen, tx;

	*new {|s, iTx|
		^super.new.init(s, iTx);
	}

	*initClass {
		last = Array.fill(8, nil);
		fid = Array.fill(8, 1);
	}

	init {|s, iTx|
		tx = iTx;

		listen = OSCFunc({|msg, time, addr, port|
			var channel, value;
			channel = msg[2];
			value = msg[3];

			if(value != last[channel])
			{
				tx.sendMsg('/kraken', fid[channel], channel, value);
				fid[channel] = fid[channel] + 1;
				last[channel] = value;
			}
		}, "/kraken", nil);
	}

	ar {|channel=0, in, rate=2000|
		var trig, out;
		trig = Impulse.ar(rate);
		out = SendReply.ar(trig, '/kraken', in, channel);
		^out;
	}

	kr {|channel=0, in, rate=2000|
		var trig, out;
		trig = Impulse.kr(rate);
		out = SendReply.kr(trig, '/kraken', in, channel);
		^out;
	}
}
