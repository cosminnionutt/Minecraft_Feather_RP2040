# Video Walkthrough of the Installtion
https://youtu.be/a_ZD0LNgVDs

# Spigot Server Setup
The minecraft components for this project rely on the Minecraft server software: [Spigot](https://www.spigotmc.org/)

The primary way that the server software is distributed is `BuildTools.jar` file. You run this file and it builds a copy of the server application which you can then run and join the server using the standard Minecraft client.

This page covers the install process for using `BuildTools.jar`: https://www.spigotmc.org/wiki/buildtools/


# Running the server
Create a start script. On Linux this can be a `.sh` file. On windows you can make a `.bat` file.

This process is covered on this page: https://www.spigotmc.org/wiki/spigot-installation/

Replace the `#` in the'se scripts with a number, I use `2`

### Linux Start Script
```
#!/bin/sh

java -Xms#G -Xmx#G -XX:+UseG1GC -jar spigot.jar nogui
```
 
### Windows Start Script
```
@echo off
java -Xms#G -Xmx#G -XX:+UseG1GC -jar spigot.jar nogui
pause
```

### EULA
When you run the server for the first time it will fail until you open the `eula.txt` file and edit the boolean to indicate that you agree to it.

The server directory contains many configuration files that can be used to customize various parts of your server. The Feather does not require further configuration. But if you intend to have other people play on the server there are some things you'll probably want to do.

### Verify it's working
Accept the eula by editing the file. Run the server and watch the output to see if it starts up successfully. It may take a few minutes to start up the first time.

Once the server is running, launch your Minecraft client and join the server with it. This will ensure that the Spigot server is up and running successfully. It's better to resolve any issues now before trying to move on to plugins and the Feather.

When your player is logged in use the server terminal console and run `/op [your_player_name]` to make your player a server operator.