package me.runescapejon.reset;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ConfigSettings {
	@Setting(value = "OfflineMessage")
	public static String OfflineMessage = "&c%player% must be offline to reset their playerdata.";

	@Setting(value = "BackUpPlayerdata", comment = "If true, backup playerdata before resetting it.")
	public static boolean BackUpPlayerdata = true;

	@Setting(value = "ResetMessage")
	public static String ResetMessage = "&aReset %player%'s data.";

	@Setting(value = "ResetFailedMessage")
    public static String ResetFailedMessage = "&cError resetting %player%'s data.";

	@Setting(value = "BackupMessage")
	public static String BackupMessage = "&aA backup has been saved to %backupPath%.";

	@Setting(value = "BackupFailedMessage")
    public static String BackupFailedMessage = "&cError backing up %player%'s data before reset. The reset has been aborted.";
}
