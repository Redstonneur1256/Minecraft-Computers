package fr.redstonneur1256;

import fr.redstonneur1256.common.DataCompressor;
import fr.redstonneur1256.common.KeyMap;
import fr.redstonneur1256.common.Vars;
import fr.redstonneur1256.redutilities.Utils;
import fr.redstonneur1256.redutilities.graphics.ImageHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    private static BufferedImage cursorImage;
    static {
        try {
            cursorImage = ImageIO.read(Client.class.getResourceAsStream("/cursor.png"));
        }catch(IOException exception) {
            throw new IllegalStateException("Failed to load cursor", exception);
        }
    }

    private final Object sendLock;
    private AbstractClientController controller;
    private Rectangle screen;
    private String sendingFormat;
    private Robot robot;
    private ByteArrayOutputStream tempOutput;
    private Socket socket;
    private DataInputStream socketInput;
    private DataOutputStream socketOutput;
    private Thread receiveThread;
    private int sendWidth, sendHeight;
    private boolean sending;

    private boolean allowMouseControl;
    private boolean allowKeyboardControl;

    private long dataSended;

    public Client(AbstractClientController controller, Rectangle screen) throws AWTException {
        this.sendLock = new Object();
        this.controller = controller;
        this.screen = screen;
        this.sendingFormat = "JPG";
        this.robot = new Robot();
        this.tempOutput = new ByteArrayOutputStream();
        this.socket = null;
        this.socketInput = null;
        this.socketOutput = null;
        this.receiveThread = null;
        this.sendWidth = 0;
        this.sendHeight = 0;
        this.sending = false;
        this.allowMouseControl = false;
        this.allowKeyboardControl = false;
    }

    public boolean isConnected() {
        return socket != null && socketInput != null && socketOutput != null && socket.isConnected();
    }

    public void connect(String address, int port) throws Exception {
        socket = new Socket();
        socket.connect(new InetSocketAddress(address, port));
        socketInput = new DataInputStream(socket.getInputStream());
        socketOutput = new DataOutputStream(socket.getOutputStream());

        for(byte b : Vars.header) {
            if(socketInput.read() != b) {
                disconnect();
                throw new IllegalStateException("Invalid header received from the server !");
            }
        }

        receiveThread = new Thread(this::read);
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void disconnect() {
        try {
            socket.close();
        }catch(IOException exception) {
            exception.printStackTrace();
        }
        receiveThread.interrupt();

        sending = false;

        socket = null;
        socketInput = null;
        socketOutput = null;
        receiveThread = null;
    }

    private void read() {
        while(receiveThread != null && !receiveThread.isInterrupted()) {
            try {
                int opCode = socketInput.readByte();
                switch(opCode) {
                    case Vars.setResolution:
                        sendWidth = socketInput.readShort();
                        sendHeight = socketInput.readShort();
                        break;

                    case Vars.setFormat:
                        sendingFormat = socketInput.readUTF();
                        break;

                    case Vars.disconnect:
                        String message = socketInput.readUTF();
                        disconnect();
                        controller.showDisconnected("Remotely disconnect from server: " + message);
                        break;

                    case Vars.startSending:
                        sending = true;
                        break;

                    case Vars.stopSending:
                        sending = false;
                        break;

                    case Vars.clicking:

                        double x = socketInput.readDouble();
                        double y = socketInput.readDouble();
                        int button = socketInput.readInt();

                        if(!allowMouseControl) {
                            break;
                        }

                        int clickX = (int) (x * screen.width);
                        int clickY = (int) (y * screen.height);

                        robot.mouseMove(clickX, clickY);
                        Utils.sleep(25);
                        robot.mousePress(button);
                        Utils.sleep(25);
                        robot.mouseRelease(button);
                        break;

                    case Vars.keyboardType:

                        boolean shift = socketInput.readBoolean();
                        boolean control = socketInput.readBoolean();
                        char c = socketInput.readChar();

                        if(!allowKeyboardControl) {
                            break;
                        }

                        KeyMap.KeyData key = KeyMap.getCode(c);
                        if(key == null) {
                            break;
                        }
                        shift |= key.shift;

                        if(shift) {
                            robot.keyPress(KeyEvent.VK_SHIFT);
                        }
                        if(control) {
                            robot.keyPress(KeyEvent.VK_CONTROL);
                        }

                        try {
                            robot.keyPress(key.code);
                            Utils.sleep(25);
                            robot.keyRelease(key.code);
                        }catch(Exception ignored) {

                        }

                        if(control) {
                            robot.keyRelease(KeyEvent.VK_CONTROL);
                        }
                        if(shift) {
                            robot.keyRelease(KeyEvent.VK_SHIFT);
                        }

                        break;

                    default:
                        System.out.println("Unknown opcode: " + opCode);
                        break;
                }

            }catch(Throwable exception) {
                if(socket == null || socket.isClosed()) {
                    return;
                }
                controller.showDisconnected("Error occurred in connection " + exception);
                exception.printStackTrace();
            }
        }
    }

    public void sendData() throws Exception {
        if(sendWidth == 0 || sendHeight == 0 || !sending) {
            return;
        }

        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();

        BufferedImage capture = robot.createScreenCapture(screen);
        BufferedImage resized = ImageHelper.resize(capture, sendWidth, sendHeight);

        int pointerDrawX = (int) (mouseLoc.getX() / capture.getWidth() * resized.getWidth());
        int pointerDrawY = (int) (mouseLoc.getY() / capture.getHeight() * resized.getHeight());

        Graphics graphics = resized.getGraphics();
        graphics.drawImage(cursorImage, pointerDrawX, pointerDrawY, 8, 8, null);
        graphics.dispose();

        tempOutput.reset();
        ImageIO.write(resized, sendingFormat, tempOutput);

        byte[] imageData = DataCompressor.compress(tempOutput.toByteArray());

        synchronized(sendLock) {
            socketOutput.writeInt(imageData.length);
            socketOutput.write(imageData);
            socketOutput.flush();

            dataSended += 4 + imageData.length;
        }

        capture.flush();
        resized.flush();
    }

    public long getDataSended() {
        return dataSended;
    }

    public void resetSended() {
        dataSended = 0;
    }

    public boolean isAllowMouseControl() {
        return allowMouseControl;
    }

    public void setAllowMouseControl(boolean allowMouseControl) {
        this.allowMouseControl = allowMouseControl;
    }

    public boolean isAllowKeyboardControl() {
        return allowKeyboardControl;
    }

    public void setAllowKeyboardControl(boolean allowKeyboardControl) {
        this.allowKeyboardControl = allowKeyboardControl;
    }

}
