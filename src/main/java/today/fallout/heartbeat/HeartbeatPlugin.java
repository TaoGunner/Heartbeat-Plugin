package today.fallout.heartbeat;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import today.fallout.heartbeat.server.ServerStatusHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static today.fallout.heartbeat.HeartbeatPlugin.*;

@Plugin(id = MODID, name = NAME, version = VERSION, description = DESCRIPTION, url = URL, authors = AUTHORS)
public class HeartbeatPlugin
{
	public static final String MODID = "heartbeat";
	public static final String NAME = "Heartbeat Plugin";
	public static final String VERSION = "1.0.1";
	public static final String DESCRIPTION = "Pulse for your Sponge server";
	public static final String URL = "http://fallout.today";
	public static final String AUTHORS = "TaoGunner";
	public static ConfigurationNode config;

	@Inject
	private PluginContainer plugin;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private Path defaultConfig;

	@Listener
	public void onInit(GameInitializationEvent event) throws IOException
	{
		if (Files.notExists(defaultConfig)) plugin.getAsset("default.conf").get().copyToFile(defaultConfig);
	}

	@Listener
	public void onServerStarting(GameStartingServerEvent event) throws IOException
	{
			ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
			config = loader.load();
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent event)
	{
		Scheduler scheduler = Sponge.getScheduler();
		Task.Builder taskBuilder = scheduler.createTaskBuilder();

		Task task = taskBuilder.execute(ServerStatusHelper::sendStatus)
				.delayTicks(100)
				.intervalTicks(config.getNode("interval").getInt() * 20)
				.name(NAME + " - Server Status Message")
				.async()
				.submit(this);
	}
}
