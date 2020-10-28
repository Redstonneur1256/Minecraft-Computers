package fr.redstonneur1256;

import fr.redstonneur1256.redutilities.Utils;

import javax.swing.*;

public class MinecraftComputerClient {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception exception) {
            System.out.println("Failed to set System look & feel: " + exception);
        }

        try {
            boolean headless = args.length == 1 && args[0].equalsIgnoreCase("headless");

            AbstractClientController controller = headless ? new AbstractClientController() : new GuiController();

            controller.getClient().resetSended();
        }catch(Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to run application:\n" + Utils.errorMessage(exception),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
