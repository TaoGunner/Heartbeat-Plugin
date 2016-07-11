package today.fallout.heartbeat;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import today.fallout.heartbeat.server.ServerStatusHelper;

import java.io.IOException;
import java.net.URL;

import static today.fallout.heartbeat.HeartbeatPlugin.*;

@Plugin(id = MODID, name = NAME, version = VERSION, description = DESCRIPTION, url = URL, authors = AUTHORS)
public class HeartbeatPlugin
{
	static final String MODID = "heartbeat";
	static final String NAME = "Heartbeat Plugin";
	static final String VERSION = "1.0";
	static final String DESCRIPTION = "Server monitoring system";
	static final String URL = "http://fallout.today";
	static final String AUTHORS = "TaoGunner";
	public static ConfigurationNode config;

	@Listener
	public void onServerStarting(GameStartingServerEvent event)
	{
		try
		{
			URL jarConfigFile = this.getClass().getResource("config.conf");
			ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setURL(jarConfigFile).build();
			config = loader.load();
		} catch (IOException e) { e.printStackTrace(); }
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
