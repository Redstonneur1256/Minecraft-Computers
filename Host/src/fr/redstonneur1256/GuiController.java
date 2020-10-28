package fr.redstonneur1256;

import fr.redstonneur1256.common.Vars;
import fr.redstonneur1256.redutilities.Utils;

import javax.swing.*;
import java.awt.*;

public class GuiController extends AbstractClientController {

    private JFrame frame;
    private JTextField addressField;
    private JButton connectButton;
    private JCheckBox mouseControl;
    private JCheckBox keyboardControl;
    private JSpinner updateRate;
    private JLabel statusLabel;

    public GuiController() throws AWTException {
        super();

        frame = new JFrame();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("MinecraftComputer - Host");
        frame.setResizable(false);
        Container panel = frame.getContentPane();
        panel.setPreferredSize(new Dimension(400, 60));
        frame.pack();
        frame.setLocationRelativeTo(null);

        panel.setLayout(null);

        JLabel addressLabel = new JLabel("Server address:");
        addressLabel.setBounds(5, 0, 95, 20);
        panel.add(addressLabel);

        addressField = new JTextField("127.0.0.1");
        addressField.setBounds(100, 0, 200, 20);
        panel.add(addressField);

        connectButton = new JButton("Connect");
        connectButton.setBounds(300, 0, 100, 20);
        connectButton.addActionListener(event -> connect());
        panel.add(connectButton);

        mouseControl = new JCheckBox("Mouse control");
        mouseControl.setBounds(0, 20, 200, 20);
        mouseControl.addChangeListener(event -> client.setAllowMouseControl(mouseControl.isSelected()));
        panel.add(mouseControl);

        keyboardControl = new JCheckBox("Keyboard control");
        keyboardControl.setBounds(0, 40, 200, 20);
        keyboardControl.addChangeListener(event -> client.setAllowKeyboardControl(keyboardControl.isSelected()));
        panel.add(keyboardControl);

        JLabel updateRateLabel = new JLabel("Updates/s:");
        updateRateLabel.setBounds(200, 20, 100, 20);
        panel.add(updateRateLabel);

        updateRate = new JSpinner(new SpinnerNumberModel(20, 1, 20, 1));
        updateRate.setBounds(300, 20, 100, 20);
        updateRate.addChangeListener(event -> setFrameRate((int) updateRate.getValue()));
        panel.add(updateRate);

        statusLabel = new JLabel();
        statusLabel.setBounds(200, 40, 200, 20);
        panel.add(statusLabel);

        setFieldsEnabled(false);

        frame.setVisible(true);
    }

    private void connect() {
        if(client.isConnected()) {
            client.disconnect();
            setFieldsEnabled(false);
        }else {
            try {
                String[] parts = addressField.getText().split(":");

                String address = parts[0];
                int port = parts.length == 2 ? Integer.parseInt(parts[1]) : Vars.defaultPort;

                client.connect(address, port);
                setFieldsEnabled(true);
            }catch(Exception exception) {
                String message = exception.getMessage();

                exception.printStackTrace();

                JOptionPane.showMessageDialog(frame, "Error while connecting: " + message,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setFieldsEnabled(boolean connected) {
        addressField.setEnabled(!connected);

        connectButton.setText(connected ? "Disconnect" : "Connect");

        mouseControl.setEnabled(connected);
        keyboardControl.setEnabled(connected);

        updateRate.setEnabled(connected);
    }

    @Override
    public void showDisconnected(String message) {
        JOptionPane.showMessageDialog(frame, message, "Disconnected", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void onInfo(int frameRate, long bytesSent) {
        statusLabel.setText("FPS: " + frameRate + " | Data sent: " + Utils.sizeFormat(bytesSent, "B/s"));
    }

}
