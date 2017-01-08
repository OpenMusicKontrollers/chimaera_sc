#!/usr/bin/env sclang

/*
 * Copyright (c) 2017 Hanspeter Portner (dev@open-music-kontrollers.ch)
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

/*
 * vanilla configuration
*/
{
	var tx, chimconf;

	NetAddr.broadcastFlag = true;

	tx = NetAddr("255.255.255.255", 4444);
	tx.sendMsg("/info/name", 1, "chimaera");
	tx.sendMsg("/comm/ip", 2, "192.168.1.177/24");

	chimconf = ChimaeraConf(s,
		addr:"chimaera.local", rate:3000, offset:0.0025);

	chimconf.sendMsg("/ipv4ll/enabled", false);
	chimconf.sendMsg("/dhcp/enabled", false);

	chimconf.sendMsg("/ptp/enabled", true);
	chimconf.sendMsg("/ptp/offset_stiffness", 64);
	chimconf.sendMsg("/ptp/delay_stiffness", 64);

	chimconf.sendMsg("/sntp/enabled", false);
	chimconf.sendMsg("/sntp/address", 'this:123');

	chimconf.sendMsg("/sensors/group/reset");
	chimconf.sendMsg("/sensors/group/attributes/0/min", 0.0);
	chimconf.sendMsg("/sensors/group/attributes/0/max", 1.0);
	chimconf.sendMsg("/sensors/group/attributes/0/north", false);
	chimconf.sendMsg("/sensors/group/attributes/0/south", true);
	chimconf.sendMsg("/sensors/group/attributes/0/scale", false);
	chimconf.sendMsg("/sensors/group/attributes/1/min", 0.0);
	chimconf.sendMsg("/sensors/group/attributes/1/max", 1.0);
	chimconf.sendMsg("/sensors/group/attributes/1/north", true);
	chimconf.sendMsg("/sensors/group/attributes/1/south", false);
	chimconf.sendMsg("/sensors/group/attributes/1/scale", false);

	chimconf.sendMsg("/engines/scsynth/reset");
	chimconf.sendMsg("/engines/custom/reset");
	chimconf.sendMsg("/engines/oscmidi/reset");

	chimconf.sendMsg("/engines/enabled", false);
	chimconf.sendMsg("/engines/tuio2/enabled", true);
	chimconf.sendMsg("/engines/tuio2/derivatives", true);

	chimconf.sendMsg("/engines/address", "this:"++3333, {
		chimconf.sendMsg("/engines/server", false);
		chimconf.sendMsg("/engines/mode", "osc.udp");
		chimconf.sendMsg("/engines/enabled", true);

		chimconf.sendMsg("/config/save");
		chimconf.sendMsg("/reset/soft");
	});
}.value;
