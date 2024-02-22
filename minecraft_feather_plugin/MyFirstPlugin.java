package com.foamyguy.myfirstplugin;

import com.fazecast.jSerialComm.SerialPort;

import com.google.gson.Gson;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MyFirstPlugin extends JavaPlugin implements Listener {
    SerialPort comPort;


    HashMap<String, Boolean> writePinStates;

    public final static int PIN_X_DISTANCE = 6; // blocks
    public final static int PIN_Z_DISTANCE = 50; // blocks
    String[] LONG_ROW_PINS = {"D4", "TX", "RX", "MISO", "MOSI", "SCK", "D25", "D24", "A3", "A2", "A1", "A0"};
    String[] SHORT_ROW_PINS = {"SDA", "SCL", "D5", "D6", "D9", "D10", "D11", "D12", "D13"};

    ArrayList<String> readModePins;

    HashMap<String, Location> pinMap;

    @Override
    public void onEnable() {
        writePinStates = new HashMap<>();
        pinMap = new HashMap<>();
        readModePins = new ArrayList<>();

        //writePinStates.put("D5", false);
        // Plugin startup logic
        System.out.println("Hello World Blinkas Pink Feather");

        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("check").setExecutor(new CommandCheck());
        this.getCommand("spawnpin").setExecutor(new CommandSpawnPin());
        this.getCommand("testpin").setExecutor(new CommandTestPin());
        this.getCommand("start_feather").setExecutor(new CommandStartFeather());
        this.getCommand("set_pin_read").setExecutor(new CommandSetPinRead());
        this.getCommand("set_pin_write").setExecutor(new CommandSetPinWrite());


        int index_of_acm0 = -1;
        for (int i = 0; i < SerialPort.getCommPorts().length; i++) {

            System.out.println(i + " : " + SerialPort.getCommPorts()[i].getSystemPortName() + " : " + SerialPort.getCommPorts()[i].getDescriptivePortName());
            if (SerialPort.getCommPorts()[i].getSystemPortName().equals("ttyACM0")) {
                index_of_acm0 = i;
            }
        }
        comPort = SerialPort.getCommPorts()[index_of_acm0];
        comPort.openPort();

       /* SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        try {
            while (true) {
                while (comPort.bytesAvailable() == 0)
                    Thread.sleep(20);

                byte[] readBuffer = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                System.out.println("Read " + numRead + " bytes.");
                System.out.println(new String(readBuffer, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {


                //System.out.println("tick tock bytes avail: " + comPort.bytesAvailable());
                if (comPort.bytesAvailable() > 0) {
                    byte[] readBuffer = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                    String incoming = new String(readBuffer, StandardCharsets.UTF_8);
                    System.out.println("Read " + numRead + " bytes from com port");
                    System.out.println(incoming);

                    String[] incomingCommands = incoming.split("\n");
                    for(int i = 0; i < incomingCommands.length; i++ ){

                        String curCommand = incomingCommands[i];
                        if (incoming.startsWith("VALUES|")) {
                            String[] parts = curCommand.split("\\|");

                            Gson gson = new Gson();
                            Map<String, Boolean> incomingCommandsMap = new HashMap<String, Boolean>();
                            System.out.println("JSON: ");
                            System.out.println(parts[1]);
                            incomingCommandsMap = (Map<String, Boolean>) gson.fromJson(parts[1], incomingCommandsMap.getClass());

                            for (String pinName : incomingCommandsMap.keySet()){
                                if (pinMap.containsKey(pinName)) {
                                    boolean curState = incomingCommandsMap.get(pinName);
                                    Location loc = pinMap.get(pinName);
                                    if (curState) {
                                        //x=51.0,y=76.0,z=-514.0
                                        loc.getBlock().setType(Material.REDSTONE_TORCH);
                                    } else {
                                        loc.getBlock().setType(Material.AIR);
                                    }
                                }
                            }

                        }
                    }


                }

                for(String pinName : writePinStates.keySet()){
                    //x=27.0,y=76.0,z=-514.0
                    Location loc = pinMap.get(pinName);
                    Block block = loc.getBlock();
                    BlockData data = block.getBlockData();

                    if (((Lightable)data).isLit()){
                        if (writePinStates.get(pinName) == false){
                            OutputStream os = comPort.getOutputStream();
                            PrintWriter pw = new PrintWriter(os);
                            pw.print("pin "+ pinName + " 1\r");
                            pw.flush();
                            pw.close();
                        }
                        writePinStates.put(pinName, true);
                    }else{
                        if (writePinStates.get(pinName) == true){
                            OutputStream os = comPort.getOutputStream();
                            PrintWriter pw = new PrintWriter(os);
                            pw.print("pin " + pinName + " 0\r");
                            pw.flush();
                            pw.close();
                        }
                        writePinStates.put(pinName, false);
                    }
                }


            }
        }, 0L, 10L);

    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic

        comPort.closePort();
        System.out.println("My First Plugin disabled");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        System.out.println("The player has joined Hooray!!");
        event.getPlayer().sendExperienceChange(1.0f);
        System.out.println(event.getPlayer().getLocation().toString());
        System.out.println("after print loc!!");
        event.setJoinMessage("Welcome to Feather Server: " + event.getPlayer().getDisplayName());
        Location playerLoc = event.getPlayer().getLocation();
        Location loc = new Location(playerLoc.getWorld(), playerLoc.getBlockX(), playerLoc.getBlockY() + 3, playerLoc.getBlockZ());
        loc.getBlock().setType(Material.BLUE_WOOL);

        Location torch_loc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() + 1);
        torch_loc.getBlock().setType(Material.REDSTONE_WALL_TORCH);
        System.out.println("cur direction: ");
        System.out.println(torch_loc.getBlock().getLocation().getDirection());

        BlockData blockData = torch_loc.getBlock().getBlockData();

        //System.out.println(blockData.toString());
        //((Lightable) blockData).setLit(false);
        //System.out.println(torch_loc.getBlock().getBlockData());
        //torch_loc.getBlock().setBlockData(blockData);
        //System.out.println(torch_loc.getBlock().getBlockData());
        //System.out.println("updating: " + torch_loc.getBlock().getState().update());

        //System.out.println(torch_loc.getBlock().getFace());
        System.out.println(torch_loc.getBlock().getBlockData());
        ((Directional) blockData).setFacing(BlockFace.SOUTH);
        torch_loc.getBlock().setBlockData(blockData);
        System.out.println(torch_loc.getBlock().getBlockData());

        blockData = torch_loc.getBlock().getBlockData();
        ((Lightable) blockData).setLit(false);
        torch_loc.getBlock().setBlockData(blockData);
        System.out.println(torch_loc.getBlock().getBlockData());

        //Vector new_direction = new Vector(0,0,-1);
        //torch_loc.getBlock().getLocation().setDirection(new_direction);
        //System.out.println("after direction: ");
        //System.out.println(torch_loc.getBlock().getLocation().getDirection());


    }

    public class CommandTestPin implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {

                OutputStream os = comPort.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                pw.print("pin D5 1\r");
                pw.flush();

                pw.print("pin D11 R\r");
                pw.flush();
                pw.close();
            }
            return false;

        }
    }

    public class CommandStartFeather implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {

                Player player  = (Player) sender;
                Location lookingLoc = player.getTargetBlockExact(5).getLocation();
                System.out.println("looking at: " + lookingLoc);

                for (int i = 0; i < LONG_ROW_PINS.length; i++){
                    String pinName = LONG_ROW_PINS[i];

                    Location pinLocation = new Location(
                            player.getWorld(),
                            lookingLoc.getBlockX() - (i * PIN_X_DISTANCE),
                            lookingLoc.getBlockY() + 1,
                            lookingLoc.getBlockZ());
                    System.out.println(pinName + " - " + pinLocation);
                    pinMap.put(pinName, pinLocation);

                }

                for (int i = 0; i < SHORT_ROW_PINS.length; i++){
                    String pinName = SHORT_ROW_PINS[i];

                    Location pinLocation = new Location(
                            player.getWorld(),
                            lookingLoc.getBlockX() - (i * PIN_X_DISTANCE),
                            lookingLoc.getBlockY() + 1,
                            lookingLoc.getBlockZ() - PIN_Z_DISTANCE);
                    System.out.println(pinName + " - " + pinLocation);

                    pinMap.put(pinName, pinLocation);
                }
                player.sendMessage("Feather Initialized Successfully");


            }
            return false;

        }
    }

    public class CommandSetPinRead implements CommandExecutor {

        //example:     /set_pin_read D11
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player  = (Player) sender;
                String pinToSet = args[0];

                if (pinMap.containsKey(pinToSet)){
                    player.sendMessage("setting " + pinToSet + " to R");
                    OutputStream os = comPort.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    pw.print("pin " + pinToSet + " R\r");
                    pw.flush();
                    pw.close();
                    if(!readModePins.contains(pinToSet)){
                        readModePins.add(pinToSet);
                    }

                }else{
                    player.sendMessage("Invalid Pin");
                }


            }
            return false;

        }
    }

    public class CommandSetPinWrite implements CommandExecutor {

        //example:     /set_pin_write D5
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player  = (Player) sender;
                String pinToSet = args[0];

                if (pinMap.containsKey(pinToSet)){
                    player.sendMessage("setting " + pinToSet + " to write mode");
                    writePinStates.put(pinToSet, false);
                    Location writePinLocation = pinMap.get(pinToSet);
                    writePinLocation.getBlock().setType(Material.REDSTONE_WALL_TORCH);
                    BlockData blockData = writePinLocation.getBlock().getBlockData();
                    if (Arrays.asList(LONG_ROW_PINS).contains(pinToSet)){
                        ((Directional) blockData).setFacing(BlockFace.SOUTH);
                    }else if(Arrays.asList(SHORT_ROW_PINS).contains(pinToSet)){
                        ((Directional) blockData).setFacing(BlockFace.NORTH);

                    }
                    writePinLocation.getBlock().setBlockData(blockData);


                    readModePins.remove(pinToSet);

                }else{
                    player.sendMessage("Invalid Pin");
                }
            }
            return false;

        }
    }
}
