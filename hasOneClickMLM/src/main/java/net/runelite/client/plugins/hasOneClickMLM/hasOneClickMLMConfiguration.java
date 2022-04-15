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
package net.runelite.client.plugins.hasOneClickMLM;

import net.runelite.client.config.*;
import net.runelite.client.config.ConfigTitle;


@ConfigGroup("hasOneClickMLM")
public interface hasOneClickMLMConfiguration extends Config
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
		return "One Click MLM - Select Level, and other relevant options, start By MLM Bank Chest. Lower level needs medium fal diary";
	}


	@ConfigItem(
			keyName = "floorLevel",
			name = "Gem",
			description = "Select Floor to Mine On",
			position = 22
	)
	default FloorLevel floorLevel()	{ return FloorLevel.LOWER_LEVEL; }

	@ConfigItem(
			keyName = "delayAfterClickingOre",
			name = "Delay Ore",
			description = "Ticks To wait after ore clicked",
			position = 23

	)
	default int delayAfterClickingOre() {
		return 8;
	}

	@ConfigItem(
			keyName = "loadsBeforeCollection",
			name = "Load Count",
			description = "How many Loads beore you collect from sack, recc= 2",
			position = 24

	)
	default int loadsBeforeCollection() {
		return 2;
	}

	@ConfigItem(
			keyName = "useSpec",
			name = "Use Special Attack",
			description = "Only turn on for dpick",
			position = 25

	)
	default boolean useSpec() {
		return false;
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
