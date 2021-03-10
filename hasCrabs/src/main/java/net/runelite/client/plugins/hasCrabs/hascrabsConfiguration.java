/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.hasCrabs;

import net.runelite.client.config.*;

@ConfigGroup("hascrabs")
public interface hascrabsConfiguration extends Config
{



	@ConfigItem(
			keyName = "crabType",
			name = "crab type",
			description = "which typa crabs ye wanna do",
			position = 1,
			section = "SandCrab Positions"
	)
	default CrabType crabType() { return CrabType.CUSTOM; }




	@ConfigTitle(
			keyName = "coordsTitle",
			name = "Custom Coordinate Settings",
			description = "",
			position = 2
	)
	String coordsTitle = "coordsTitle";

	@ConfigItem(
			keyName = "customCrabLocation",
			name = "Custom Crab Location",
			description = "Enter the location you want to walk to, to Aggro crab (x,y,z)",
			position = 4,
			title = "Custom Locations"

	)
	default String customCrabLocation()
	{
		return "0,0,0";
	}

	@ConfigItem(
			keyName = "customResetLocation",
			name = "Custom Reset Location",
			description = "Enter the location you want to walk to, to Reset Aggro (x,y,z)",
			position = 5,
			title = "Custom Locations"

	)
	default String customResetLocation()
	{
		return "0,0,0";
	}


	@ConfigItem(
			keyName = "resetTime",
			name = "Reset TIme",
			description = "How long to wait before resetting",
			position = 5
	)
	default int customTime()
	{
		return 620;
	}



	@ConfigItem(
			keyName = "startButton",
			name = "Start",
			description = "",
			position = 6,
			section = "Controls"
	)
	default Button startButton() { return new Button(); }

	@ConfigItem(
			keyName = "stopButton",
			name = "Stop",
			description = "",
			position = 7,
			section = "Controls"
	)
	default Button stopButton() { return new Button(); }
}
