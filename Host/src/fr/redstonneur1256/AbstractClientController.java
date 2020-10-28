package fr.redstonneur1256;

import fr.redstonneur1256.redutilities.Utils;

import java.awt.*;

public class AbstractClientController {

    protected Client client;
    private Thread updateThread;
    private double updateTime;
    public AbstractClientController() throws AWTException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        this.client = new Client(this, new Rectangle(0, 0, screenSize.width, screenSize.height));
        this.updateThread = new Thread(this::update);
        this.updateTime = 1_000_000_000.0 / 20;

        this.updateThread.setDaemon(true);
        this.updateThread.start();
    }

    public void showDisconnected(String message) {
        System.out.println(message);
    }

    public void setFrameRate(int value) {
        updateTime = 1_000_000_000.0 / value;
    }

    private void update() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        int updates = 0;

        while(!updateThread.isInterrupted()) {
            if(lastTime + updateTime < System.nanoTime()) {
                lastTime += updateTime;

                try {
                    client.sendData();

                    updates ++;
                }catch(Exception exception) {
                    exception.printStackTrace();
                }

                if(timer + 1000 < System.currentTimeMillis()) {
                    onInfo(updates, client.getDataSended());

                    client.resetSended();
                    updates = 0;

                    timer += 1000;
                    lastTime = System.nanoTime();
                }

            }else {
                Utils.sleep(1);
            }
        }
    }

    public Client getClient() {
        return client;
    }

    protected void onInfo(int frameRate, long bytesSent) {
        System.out.println(frameRate + " FPS sent (" + Utils.sizeFormat(bytesSent, "B/s") + ")");
    }

}
