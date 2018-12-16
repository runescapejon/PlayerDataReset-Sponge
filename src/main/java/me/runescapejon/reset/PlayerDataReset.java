package me.runescapejon.reset;

import java.io.File;
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
			src.sendMessage(Text.of("The player need to be offline."));
		} else {
			this.deletedata(new File(Sponge.getServer().getDefaultWorld().get().getWorldName() + "/playerdata",
					target.getUniqueId() + ".dat"));
			src.sendMessage(Text.of("Reset ", target.getName(), " Data"));
		}
		return CommandResult.success();
	}

	private void deletedata(File file) {
		if (file.exists()) {
			file.delete();
		}
	}
}
