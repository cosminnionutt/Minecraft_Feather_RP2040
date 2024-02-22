package com.foamyguy.myfirstplugin;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/*
public class CommandTestPin implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){

            SerialPort comPort = SerialPort.getCommPorts()[0];
            System.out.println("Opening port: " + comPort.getSystemPortName());

            OutputStream os = comPort.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.println("pin D5 1");
            pw.flush();
            comPort.closePort();
        }

        return false;

    }
}
*/
