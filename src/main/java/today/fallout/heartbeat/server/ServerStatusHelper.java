package today.fallout.heartbeat.server;

import com.google.gson.Gson;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import today.fallout.heartbeat.HeartbeatPlugin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerStatusHelper
{
	private static final String USER_AGENT = HeartbeatPlugin.config.getNode("user-agent").getString();
	private static final String STATUS_URL = HeartbeatPlugin.config.getNode("url").getString();
	private static final List<String> MAIN_MODS = Arrays.asList("Minecraft", "mcp", "FML");
	private static HttpURLConnection connection;

	public static boolean sendStatus()
	{
		ServerInfo serverInfo = new ServerInfo();
		String postData = new Gson().toJson(serverInfo);

		try
		{
			connection = (HttpURLConnection) new URL(STATUS_URL).openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Content-type", "application/json");
			connection.setDoOutput(true);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setFixedLengthStreamingMode(postData.length());
			connection.connect();

			System.out.println(postData);

			OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
			output.write(postData);
			output.flush();
			output.close();

			System.out.println(connection.getResponseCode());

			return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
		}
		catch (ConnectException e) { return false; }
		catch (IOException e) { return false; }
		finally { connection.disconnect(); }
	}

	private static class ServerInfo
	{
		ServerStatus server;
		List<String> plugins = new ArrayList<>();
		List<String> players = new ArrayList<>();

		ServerInfo()
		{
			server = new ServerStatus();
			for (PluginContainer plugin: Sponge.getPluginManager().getPlugins()) { if (!MAIN_MODS.contains(plugin.getId())) plugins.add(plugin.getId()); }
			for (Player player: Sponge.getServer().getOnlinePlayers()) { players.add(player.getName()); }
		}

		class ServerStatus
		{
			String ip;
			int port;
			String version;
			String motd;
			int max_players;
			int current_players;
			String gamemode;
			boolean whitelist;
			boolean online_mode;

			ServerStatus()
			{
				this.ip = Sponge.getServer().getBoundAddress().get().getHostName();
				this.port = Sponge.getServer().getBoundAddress().get().getPort();
				this.version = Sponge.getPlatform().getMinecraftVersion().getName();
				this.motd = Sponge.getServer().getMotd().toPlain();
				this.max_players = Sponge.getServer().getMaxPlayers();
				this.current_players = Sponge.getServer().getOnlinePlayers().size();
				this.gamemode = Sponge.getServer().getDefaultWorld().get().getGameMode().getName();
				this.whitelist = Sponge.getServer().hasWhitelist();
				this.online_mode = Sponge.getServer().getOnlineMode();
			}
		}
	}
}
