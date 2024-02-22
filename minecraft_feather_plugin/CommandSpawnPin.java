package com.foamyguy.myfirstplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpawnPin implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            Player player  = (Player) sender;
            Location playerLoc = player.getLocation();
            Location loc = new Location(playerLoc.getWorld(),playerLoc.getBlockX(),playerLoc.getBlockY() + 3,playerLoc.getBlockZ());

            System.out.println(loc.getBlock().getBlockData());
            loc.getBlock().setType(Material.BLUE_WOOL);

            Location torch_loc = new Location(loc.getWorld(),loc.getBlockX(),loc.getBlockY(),loc.getBlockZ() + 1);
            torch_loc.getBlock().setType(Material.REDSTONE_WALL_TORCH);
            BlockData blockData = torch_loc.getBlock().getBlockData();
            Directional torchData = (Directional) blockData;
            torchData.setFacing(BlockFace.SOUTH);
            torch_loc.getBlock().setBlockData(torchData);
        }

        return false;

    }
}
