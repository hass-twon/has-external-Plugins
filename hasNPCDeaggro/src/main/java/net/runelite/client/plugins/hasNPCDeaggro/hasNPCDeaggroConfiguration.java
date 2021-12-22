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
package net.runelite.client.plugins.hasNPCDeaggro;

import net.runelite.client.config.*;
import net.runelite.client.config.ConfigTitle;


@ConfigGroup("hasNPCDeaggro")
public interface hasNPCDeaggroConfiguration extends Config
{

	@ConfigTitle(
		keyName = "delayConfig",
		name = "Sleep Delay Configuration",
		description = "Configure how the bot handles sleep delays",
		position = 2
	)
	boolean delayConfig = false;

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepMin",
		name = "Sleep Min",
		description = "",
		position = 3,
		section = "delayConfig"
	)
	default int sleepMin()
	{
		return 60;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepMaximus",
		name = "Sleep Maximus",
		description = "",
		position = 4,
		section = "delayConfig"
	)
	default int sleepMax()
	{
		return 350;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepTarget",
		name = "Sleep Target",
		description = "",
		position = 5,
		section = "delayConfig"
	)
	default int sleepTarget()
	{
		return 100;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepDeviation",
		name = "Sleep Deviation",
		description = "",
		position = 6,
		section = "delayConfig"
	)
	default int sleepDeviation()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "sleepWeightedDistribution",
		name = "Sleep Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 7,
		section = "delayConfig"
	)
	default boolean sleepWeightedDistribution()
	{
		return false;
	}

	@ConfigTitle(
		keyName = "delayTickConfig",
		name = "Game Tick Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
		position = 8
	)
	String delayTickConfig = "delayTickConfig";

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayMin",
		name = "Game Tick Min",
		description = "",
		position = 9,
		section = "delayTickConfig"
	)
	default int tickDelayMin()
	{
		return 1;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayMax",
		name = "Game Tick Max",
		description = "",
		position = 10,
		section = "delayTickConfig"
	)
	default int tickDelayMax()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayTarget",
		name = "Game Tick Target",
		description = "",
		position = 11,
		section = "delayTickConfig"
	)
	default int tickDelayTarget()
	{
		return 2;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayDeviation",
		name = "Game Tick Deviation",
		description = "",
		position = 12,
		section = "delayTickConfig"
	)
	default int tickDelayDeviation()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "tickDelayWeightedDistribution",
		name = "Game Tick Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 13,
		section = "delayTickConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}

	@ConfigTitle(
		keyName = "instructionsTitle",
		name = "Setup and Instructions",
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
		return "Deaggros NPC, enter the start and stop coords";
	}


	@ConfigItem(
			keyName = "customNPCLocation",
			name = "Custom Crab Location",
			description = "Enter the location you want to walk to, to Aggro NPC (x,y,z)",
			position = 40,
			title = "Custom Locations"

	)
	default String customNPCLocation()
	{
		return "0,0,0";
	}

	@ConfigItem(
			keyName = "customResetLocation",
			name = "Custom Reset Location",
			description = "Enter the location you want to walk to, to Reset Aggro (x,y,z)",
			position = 50,
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
			position = 55
	)
	default int resetTime()
	{
		return 620;
	}

	@ConfigItem(
			keyName = "resetTimeRandomization",
			name = "Reset Time Randomization",
			description = "How long to wait before resetting",
			position = 56
	)
	default int resetTimeRandomization()
	{
		return 5;
	}

	@ConfigItem(
			keyName = "npcName",
			name = "NPC Name",
			description = "Enter the exact name of the NPC",
			position = 57

	)
	default String npcName()
	{
		return "Sand Crab";
	}

	@ConfigItem(
			keyName = "enableSpec",
			name = "Enable Using Special Attack",
			description = "Enable to turn on using weapon special attack",
			position = 95
	)
	default boolean enableSpec()
	{
		return false;
	}

	@ConfigItem(
			keyName = "specCost",
			name = "Spec Cost",
			description = "Enter the amount of Spec energy it uses",
			position = 96

	)
	default int specCost()
	{
		return 50;
	}

	@ConfigItem(
			keyName = "forceDeaggro",
			name = "Force Reset",
			description = "Will force a reset run",
			position = 99
	)
	default Button forceDeaggro()
	{
		return new Button();
	}


	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "on/off plugin",
			position = 100
	)
	default Button startButton()
	{
		return new Button();
	}


}
