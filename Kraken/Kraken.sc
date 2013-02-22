Kraken {
	classvar last, fid;
	var rx, tx;

	*new {|s|
		^super.new.init(s);
	}

	*initClass {
		last = Array.fill(8, nil);
		fid = Array.fill(8, 1);
	}

	init {|s, host="127.0.0.1", port=1212|

		tx = NetAddr(host, port);

		rx = OSCresponder(nil, '/kraken', {|time, obj, msg, addr|
			var channel, value;
			channel = msg[2];
			value = msg[3];

			if(value != last[channel])
			{
				tx.sendMsg('/kraken', fid[channel], channel, value);
				fid[channel] = fid[channel] + 1;
				last[channel] = value;
			}
		}).add;
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
