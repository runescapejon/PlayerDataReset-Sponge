package me.runescapejon.reset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "playerdatareset-plugin", name = "PlayerDataReset-Plugin", description = "PlayerDataReset Plugin", version = "1.0")
public class PlayerDataReset {
	@Inject
	Game game;

	@Listener
	public void onGameInitlization(GameInitializationEvent event) {
		CommandSpec execute = CommandSpec.builder().executor(this::execute).permission("playerreset.data")
				.arguments(GenericArguments.user(Text.of("Player"))).build();
		game.getCommandManager().register(this, execute, "reset");
	}

	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		User target = args.<User>getOne("Player").get();
		if (target.isOnline()) {
			src.sendMessage(Text.of("The player needs to be offline."));
		} else {
			File file = new File(Sponge.getServer().getDefaultWorld().get().getWorldName() + File.separatorChar + "playerdata",
					target.getUniqueId() + ".dat");
			String backupFilename = backupData(target, file);
			if(backupFilename != null) {
				if(deleteData(file)) {
					src.sendMessage(Text.of("Reset ", target.getName(), "'s data."));
					src.sendMessage(Text.of("A backup has been saved to ",
							Sponge.getServer().getDefaultWorld().get().getWorldName(), File.separatorChar + "playerdata" + File.separatorChar + "backups"
									+ File.separatorChar, backupFilename));
				}
				else {
					src.sendMessage(Text.of("An error occurred resetting the player's data."));
				}
			}
			else {
				src.sendMessage(Text.of("An error occurred backing up the player's data before reset. The reset has been aborted."));
			}
		}
		return CommandResult.success();
	}

	private String backupData(User target, File file) {
		if (file.exists() && file.isFile()) {
			String dateAndTime = new SimpleDateFormat(" MM-dd-yyyy HH.mm.ss ").format(new Date());
			File backupFile = new File(file.getParent() + File.separatorChar +"backups", target.getName() + dateAndTime + file.getName());

			try {
				Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
				return null;
			}

			return backupFile.getName();
		}
		else return null;
	}

	private boolean deleteData(File file) {
		if (file.exists() && file.isFile()) {
			return file.delete();
		}
		else return false;
	}
}
