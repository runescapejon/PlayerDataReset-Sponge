package me.runescapejon.reset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;

@Plugin(id = "playerdatareset-plugin", name = "PlayerDataReset-Plugin", description = "PlayerDataReset Plugin", version = "1.1")
public class PlayerDataReset {

	public static PlayerDataReset instance;
	private PlayerDataReset plugin;
	private Logger logger;
	private ConfigSettings configMsg;
	private GuiceObjectMapperFactory factory;
	private final File configDirectory;
	private static String playerdataPath;
	private static String playerdataBackupPath;

	@Inject
	public PlayerDataReset(Logger logger, @ConfigDir(sharedRoot = false) File configDir,
			GuiceObjectMapperFactory factory) {
		this.logger = logger;

		this.configDirectory = configDir;
		this.factory = factory;
		instance = this;
	}

	@Listener
	public void onPreInit(GamePreInitializationEvent event) {
		plugin = this;
		loadConfig();
	}

	@Listener
	public void onGameInitialization(GameInitializationEvent event) {
		CommandSpec execute = CommandSpec.builder().executor(this::execute).permission("playerreset.data")
				.arguments(GenericArguments.user(Text.of("Player"))).build();
		Sponge.getCommandManager().register(this, execute, "reset");
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		playerdataPath = Sponge.getServer().getDefaultWorld().get().getWorldName() + File.separatorChar + "playerdata";
		playerdataBackupPath = playerdataPath + File.separatorChar + "backups";
	}

	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		User target = args.<User>getOne("Player").get();

		//only proceed if the target player is offline
		if (target.isOnline()) {
			src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(ConfigSettings.OfflineMessage
                    .replace("%player%", target.getName())));
		}
		else {
		    //get the path for the target playerdata file
			File file = new File(playerdataPath,
					target.getUniqueId() + ".dat");

			if (ConfigSettings.BackUpPlayerdata) {
			    //perform the playerdata backup and save the path of the new backup file
                String backupFilePath = backupData(target, file);

                //if the backup function returned a path, indicating the backup was successful, proceed
				if(backupFilePath != null) {
				    src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(ConfigSettings.BackupMessage
                            .replace("%backupPath%", playerdataBackupPath  + File.separatorChar + backupFilePath)
                            .replace("%player%", target.getName())));
                }
                else {
                    //if the backup function returned null, the backup failed, and the delete must be aborted
                    src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(ConfigSettings.BackupFailedMessage
                            .replace("%player%", target.getName())));
                    return CommandResult.success();
                }
			}

			//try deleting the playerdata. if deletion is successful, send the success message
			if (deleteData(file)) {
				src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(ConfigSettings.ResetMessage
                        .replace("%player%", target.getName())));
			}
			else {
			    //if deletion fails, send the failure message
			    src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(ConfigSettings.ResetFailedMessage
                        .replace("%player%", target.getName())));
            }

		}
		return CommandResult.success();
	}

	//backs up a playerdata file
    //User is the player whose playerdata is being backed up
    //file is the playerdata file being backed up
    //returns the filename of the new backup file if successful, or null if unsuccessful
	private String backupData(User target, File file) {
		//make sure the file being backed up exists and is a file
		if (file.exists() && file.isFile()) {
		    //get the current date and time to add to the backup filename
			String dateAndTime = new SimpleDateFormat(" MM-dd-yyyy HH.mm.ss ").format(new Date());

			//create the backup file
			File backupFile = new File(playerdataBackupPath,
					target.getName() + dateAndTime + file.getName());
			//create the backup file's directory path if it doesn't already exist
			if(backupFile.getParentFile().exists()) {
                backupFile.getParentFile().mkdirs();
            }

			//perform the actual file copy
			try {
				Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
			    //if the file copy threw an exception, return null to indicate the backup failed
				return null;
			}

			//if the copy was a success, return the filename of the new backup file
			return backupFile.getName();
		} else
		    //if the playerdata file doesn't exist or is a directory, return null to indicate the backup failed
			return null;
	}

	private boolean deleteData(File file) {
		if (file.exists() && file.isFile()) {
			return file.delete();
		} else
			return false;
	}

	public Logger getLogger() {
		return logger;
	}

	public GuiceObjectMapperFactory getFactory() {
		return factory;
	}

	public ConfigSettings getConfigSettingsCfg() {
		return configMsg;
	}

	@Listener
	public void onReload(GameReloadEvent event) {
		loadConfig();
	}

	public boolean loadConfig() {
		if (!plugin.getConfigDirectory().exists()) {
			plugin.getConfigDirectory().mkdirs();
		}

		try {
			File configFile = new File(getConfigDirectory(), "config.conf");
			if (!configFile.exists()) {
				configFile.createNewFile();
				logger.info("Creating config for PlayerDataReset.");
			}

			ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
					.setFile(configFile).build();
			CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults()
					.setObjectMapperFactory(plugin.getFactory()).setShouldCopyDefaults(true));
			configMsg = config.getValue(TypeToken.of(ConfigSettings.class), new ConfigSettings());
			loader.save(config);
			return true;
		} catch (Exception error) {
			getLogger().error("Error saving or loading the config.", error);

			return false;
		}
	}

	public File getConfigDirectory() {
		return configDirectory;
	}
}