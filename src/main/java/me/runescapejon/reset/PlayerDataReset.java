package me.runescapejon.reset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
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
	private ConfigSettings configmsg;
	GuiceObjectMapperFactory factory;
	private final File configDirectory;

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
	public void onGameInitlization(GameInitializationEvent event) {
		CommandSpec execute = CommandSpec.builder().executor(this::execute).permission("playerreset.data")
				.arguments(GenericArguments.user(Text.of("Player"))).build();
		Sponge.getCommandManager().register(this, execute, "reset");
	}

	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		User target = args.<User>getOne("Player").get();
		if (target.isOnline()) {
			src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(ConfigSettings.OfflineMessage));
		} else {
			File file = new File(
					Sponge.getServer().getDefaultWorld().get().getWorldName() + File.separatorChar + "playerdata",
					target.getUniqueId() + ".dat");
			if (ConfigSettings.BackUpPlayerdata) {
				String b = PlayerDataReset.getBackup().getPath();
				src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(ConfigSettings.BackupMessage.replace("%backupPath%", b)));
				backupData(target, file);

			}
			if (deleteData(file)) {
				src.sendMessage(TextSerializers.FORMATTING_CODE
						.deserialize(ConfigSettings.ResetMessage.replace("%player%", target.getName())));
			}

		}
		return CommandResult.success();
	}

	public static File getBackup() {
		File file = new File(
				Sponge.getServer().getDefaultWorld().get().getWorldName() + File.separatorChar + "playerdata",
				"backups");
		if (!file.exists())
			file.mkdir();
		return file;
	}

	private boolean backupData(User target, File file) {
		if (!PlayerDataReset.getBackup().exists()) {
			PlayerDataReset.getBackup().mkdirs();
		}
		if (file.exists() && file.isFile()) {
			String dateAndTime = new SimpleDateFormat(" MM-dd-yyyy HH.mm.ss ").format(new Date());
			File backupFile = new File(file.getParent() + File.separatorChar + "backups",
					target.getName() + dateAndTime + file.getName());
			try {
				Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
				return null != null;
			}

			return backupFile.getName() != null;
		} else
			return null != null;
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
		return configmsg;
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
			configmsg = config.getValue(TypeToken.of(ConfigSettings.class), new ConfigSettings());
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