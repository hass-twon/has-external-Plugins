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
package net.runelite.client.plugins.hasBirdHouseRun;

import net.runelite.client.config.*;
import net.runelite.client.config.ConfigTitle;


@ConfigGroup("hasBirdHouseRun")
public interface hasBirdHouseRunConfiguration extends Config
{


	@ConfigTitle(
		keyName = "instructionsTitle",
		name = "Instructions:",
		description = "",
		position = 16
	)
	String instruction = "Instructions";

	@ConfigItem(
		keyName = "instructions",
		name = "",
		description = "Instructions. Don't enter anything into this field",
		position = 20,
		title = "instructionsTitle"
	)
	default String instructions()
	{
		return "Enter Seed ID And Log ID," +
				" Will only redo the traps, so for first ever birdrun you will have to setup on your own." +
				" Items Needed: Digsite Pendant(Any), Hammer, Chisel, Hop Seeds, ";
	}


	@ConfigItem(
			keyName = "seedID",
			name = "Seed ID",
			description = "GEt ID of hop seed to put in birdhouse",
			position = 21

	)
	default int seedID()
	{
		return 5308;
	}

	@ConfigItem(
			keyName = "logID",
			name = "Log ID",
			description = "Get ID of Log to make birdhouse",
			position = 22

	)
	default int logID()
	{
		return 1515;
	}



	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "Test button that changes variable value",
			position = 100
	)
	default Button startButton()
	{
		return new Button();
	}



}
