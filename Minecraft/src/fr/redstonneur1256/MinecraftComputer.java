package fr.redstonneur1256;

import fr.redstonneur1256.common.Vars;
import fr.redstonneur1256.maps.display.Display;
import fr.redstonneur1256.maps.maps.Mode;
import fr.redstonneur1256.utils.SocketAcceptTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.net.ServerSocket;

public class MinecraftComputer extends JavaPlugin {

    private static MinecraftComputer instance;

    private Computer computer;
    private ServerSocket serverSocket;
    private BukkitTask acceptTask;

    public MinecraftComputer() {
        instance = this;
    }

    public static MinecraftComputer get() {
        return instance;
    }

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            FileConfiguration config = getConfig();

            int hostPort = config.getInt("host.port", Vars.defaultPort);
            serverSocket = new ServerSocket(hostPort);
            acceptTask = new SocketAcceptTask(serverSocket, socket -> {
                try {
                    computer.setSocket(socket);
                }catch(Exception exception) {
                    System.out.println("Failed to set socket computer :(");
                    exception.printStackTrace();
                    try {
                        socket.close();
                    }catch(Exception ignored) {
                    }
                }
            }).start();

            ConfigurationSection mapSection = config.getConfigurationSection("map");
            World world = Bukkit.getWorld(mapSection.getString("world", "world"));

            Display display = new Display(world, mapSection.getInt("width"), mapSection.getInt("height"),
                    (short) mapSection.getInt("id"), Mode.global);

            ConfigurationSection renderSection = config.getConfigurationSection("render");
            Rectangle renderArea = null;
            if(!renderSection.getBoolean("auto")) {
                renderArea = new Rectangle(renderSection.getInt("x"), renderSection.getInt("y"),
                        renderSection.getInt("width"), renderSection.getInt("height"));
            }

            computer = new Computer(display, config.getString("host.type", "JPG"), renderArea);

            display.update();

            getServer().getPluginManager().registerEvents(new Listeners(), this);

        }catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            serverSocket.close();

            acceptTask.cancel();

            if(computer.isConnected()) {
                computer.disconnect("Server shutting down");
            }
        }catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    public Computer getComputer() {
        return computer;
    }

}