# Minecraft Computers
Control your computer from Minecraft

# How to install:
+ [Video tutorial](https://youtu.be/blabla)
+ Text tutorial:
  - Clone this repository to your computer
  - Run gradle tasks
    * `gradlew Host:shadowJar`
    * `gradlew Minecraft:shadowJar`
  - Copy the file from `Minecraft/build/libs` to your minecraft server plugins folder
  - Copy the file from `Host/build/libs` to the computer you want to control from Minecraft
  - Restart your Minecraft server for the plugin to be loaded
  - Open the file `Host.jar`, enter your server address, click on connect and have fun