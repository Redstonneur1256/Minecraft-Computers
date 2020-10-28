package fr.redstonneur1256.utils;

import fr.redstonneur1256.MinecraftComputer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketAcceptTask extends BukkitRunnable {

    private ServerSocket serverSocket;
    private Consumer<Socket> listener;

    public SocketAcceptTask(ServerSocket serverSocket, Consumer<Socket> listener) {
        this.serverSocket = serverSocket;
        this.listener = listener;
    }

    @Override
    public void run() {
        while(!isCancelled()) {
            try {
                Socket socket = serverSocket.accept();
                listener.accept(socket);
            }catch(IOException exception) {
                if(serverSocket.isClosed()) {
                    return;
                }
                exception.printStackTrace();
            }
        }
    }

    public BukkitTask start() {
        return runTaskAsynchronously(MinecraftComputer.get());
    }

}
