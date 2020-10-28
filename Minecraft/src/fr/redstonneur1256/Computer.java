package fr.redstonneur1256;

import fr.redstonneur1256.common.DataCompressor;
import fr.redstonneur1256.common.Vars;
import fr.redstonneur1256.maps.display.Display;
import fr.redstonneur1256.maps.display.Renderer;
import fr.redstonneur1256.maps.display.TouchListener;
import fr.redstonneur1256.redutilities.graphics.ImageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Computer implements Renderer, TouchListener {

    private static final Font errorFont;
    private static final BufferedImage powerButtonImage;

    static {
        try {

            errorFont = new Font("Monospace", Font.PLAIN, 40);
            powerButtonImage = ImageIO.read(MinecraftComputer.get().getResource("power.png"));

           /* JFrame f = new JFrame();
            f.add(new JLabel(new ImageIcon(powerButtonImage)));
            f.setVisible(true);*/

        }catch(IOException exception) {
            throw new IllegalStateException("Failed to create computer", exception);
        }
    }

    private Display display;
    private String clientFormat;
    private Rectangle powerButtonBounds;
    private boolean powered;
    private BufferedImage lastFrame;
    private Rectangle renderArea;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private BukkitTask reader;

    public Computer(Display display, String clientFormat, Rectangle renderArea) {
        int powerSize = Math.min(display.getHeightResolution() / 4, 64);
        int x = display.getWidthResolution() / 2;
        int y = display.getHeightResolution() / 2;

        this.display = display;
        this.clientFormat = clientFormat;
        this.powerButtonBounds = new Rectangle(x - powerSize / 2, y - powerSize / 2, powerSize, powerSize);
        this.lastFrame = new BufferedImage(display.getWidthResolution(), display.getHeightResolution(), BufferedImage.TYPE_INT_RGB);
        this.renderArea = renderArea;

        this.display.addRenderer(this);
        this.display.addListener(this);
    }

    public void setSocket(Socket socket) throws Exception {
        if(isConnected()) {
            try {
                disconnect("Another socket has been connected");
            }catch(Exception exception) {
                exception.printStackTrace();
            }
        }

        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());

        this.output.write(Vars.header);
        this.output.writeByte(Vars.setResolution);
        this.output.writeShort(display.getWidthResolution());
        this.output.writeShort(display.getHeightResolution());

        this.output.writeByte(Vars.setFormat);
        this.output.writeUTF(clientFormat);

        this.reader = Bukkit.getScheduler().runTaskAsynchronously(MinecraftComputer.get(), this::readData);

        this.display.update();
    }

    private void readData() {
        while(true) {
            try {
                int count = input.readInt();
                byte[] data = new byte[count];
                input.readFully(data);

                ByteArrayInputStream input = new ByteArrayInputStream(DataCompressor.decompress(data));

                BufferedImage image = ImageIO.read(input);

                Graphics2D graphics = (Graphics2D) lastFrame.getGraphics();

                if(renderArea == null) {
                    // TODO: Keep image ratio while centering it
                    graphics.drawImage(image, 0, 0, lastFrame.getWidth(), lastFrame.getHeight(), null);
                }else {
                    graphics.drawImage(image, renderArea.x, renderArea.y, renderArea.width, renderArea.height, null);
                }

                graphics.dispose();

                image.flush();

                display.update();
            }catch(Exception exception) {
                if(exception instanceof EOFException || exception instanceof SocketException) {
                    System.out.println("Socket has been remotely closed !");
                    socket = null;
                    input = null;
                    output = null;
                    setPowered(false);

                    Bukkit.getScheduler().runTaskLater(MinecraftComputer.get(), display::update, 1);
                    return;
                }

                exception.printStackTrace();
            }
        }
    }

    public void disconnect(String message) throws Exception {
        reader.cancel();

        output.writeByte(Vars.disconnect);
        output.writeUTF(message);

        reader = null;
        socket = null;
        input = null;
        output = null;
    }

    public boolean isConnected() {
        return socket != null && input != null && output != null;
    }

    public void click(double x, double y, boolean leftClick) {
        try {
            output.write(Vars.clicking);
            output.writeDouble(x);
            output.writeDouble(y);
            output.writeInt(leftClick ? MouseEvent.BUTTON1_DOWN_MASK : MouseEvent.BUTTON3_DOWN_MASK);
        }catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    public void type(char key, boolean shift, boolean control) {
        try {
            output.write(Vars.keyboardType);
            output.writeBoolean(shift);
            output.writeBoolean(control);
            output.writeChar(key);
        }catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void render(BufferedImage image, Player player) {
        Graphics2D graphics = (Graphics2D) image.getGraphics();

        if(!isConnected()) {

            drawErrorMessage(graphics, image, "Computer not connected");

        }else if(!powered) {

            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.drawImage(powerButtonImage, powerButtonBounds.x, powerButtonBounds.y, powerButtonBounds.width,
                    powerButtonBounds.height, null);

            graphics.dispose();

        }else if(lastFrame != null) {

            graphics.drawImage(lastFrame, 0, 0, image.getWidth(), image.getHeight(), null);

        }

        graphics.dispose();
    }

    @Override
    public void onTouch(Display display, Player player, int x, int y, boolean leftClick) {
        if(!isConnected()) {
            return;
        }

        if(!powered) {
            if(powerButtonBounds.contains(x, y)) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                setPowered(true);
                display.update();
            }
            return;
        }

        y -= 15; // TODO: Fix coordinates click on minecraft maps.

        click((double) x / display.getWidthResolution(), (double) y / display.getHeightResolution(), leftClick);
    }

    private void setPowered(boolean powered) {
        this.powered = powered;
        if(!isConnected()) {
            return;
        }
        try {
            output.write(powered ? Vars.startSending : Vars.stopSending);
        }catch(IOException exception) {
            exception.printStackTrace();
        }

        display.update();
    }

    private void drawErrorMessage(Graphics2D graphics, BufferedImage image, String message) {
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(Color.WHITE);
        graphics.setFont(errorFont);
        ImageHelper.drawCenterText(graphics, message, image.getWidth() / 2, image.getHeight() / 2);
    }

}
