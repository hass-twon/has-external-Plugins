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
package net.runelite.client.plugins.hasOneClickBattlestaves;

import net.runelite.client.config.*;
import net.runelite.client.config.ConfigTitle;


@ConfigGroup("hasOneClickBattlestaves")
public interface hasOneClickBattlestavesConfiguration extends Config
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
		return "Start at the Grand Exchange, withdraw: x=14, enter items IDs";
	}

	@ConfigItem(
			keyName = "item1ID",
			name = "Item 1 ID",
			description = "",
			position = 21

	)
	default int item1ID()
	{
		return 573;
	}


	@ConfigItem(
			keyName = "item2ID",
			name = "Item 2 ID",
			description = "",
			position = 22

	)
	default int item2ID()
	{
		return 1391;
	}

	@ConfigItem(
			keyName = "animationID",
			name = "Animation ID",
			description = "",
			position = 23

	)
	default int animationID()
	{
		return 7531;
	}

	@ConfigItem(
			keyName = "idleTick",
			name = "Idle Ticks",
			description = "",
			position = 24

	)
	default int idleTick()
	{
		return 10;
	}


	@ConfigItem(
			keyName = "isSkillingMenu",
			name = "Skilling Menu",
			description = "if there is a skilling menu open when item used oj each other",
			position = 240

	)
	default boolean isSkillingMenu()
	{
		return true;
	}



}
