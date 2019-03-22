package me.runescapejon.reset;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ConfigSettings {
	@Setting(value = "OfflineMessage")
	public static String OfflineMessage = "&cThe player must be offline to reset their playerdata.";

	@Setting(value = "BackUpPlayerdata", comment = "If true, backup playerdata before resetting it.")
	public static boolean BackUpPlayerdata = false;

	@Setting(value = "ResetMessage")
	public static String ResetMessage = "&cReset %player%'s data.";

	@Setting(value = "BackupMessage")
	public static String BackupMessage = "&cA backup has been saved to %backupPath%.";
}
