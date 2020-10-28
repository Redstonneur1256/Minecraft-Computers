package fr.redstonneur1256;

import fr.redstonneur1256.redutilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Listeners implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // TODO: make a distance from where the computer is usable

        String message = event.getMessage();

        MinecraftComputer plugin = MinecraftComputer.get();
        Computer computer = plugin.getComputer();

        boolean control = message.startsWith("CTRL ");
        if(control) {
            message = message.substring("CTRL ".length());
        }

        char[] chars = message.toCharArray();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            for(char c : chars) {
                Utils.sleep(1);

                computer.type(c, false, control);
            }

        });
    }

}
