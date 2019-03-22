package me.runescapejon.reset;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ConfigSettings {
	@Setting(value = "OfflineMessage")
	public static String OfflineMessage = "&cThe player needs to be offline.";

	@Setting(value = "BackUpPlayerdata", comment = "If true, once you reset a playerdata then a backup will perform.")
	public static boolean BackUpPlayerdata = false;

	@Setting(value = "ResetMessage")
	public static String ResetMessage = "&cReset %player% data.";

	@Setting(value = "BackupMessage")
	public static String BackupMessage = "&cA backup has been saved to ";
}
