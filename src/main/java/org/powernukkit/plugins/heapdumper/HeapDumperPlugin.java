/*
 *     HeapDumperPlugin - Allows to take a heap dump of the JVM environment
 *     Copyright (C) 2020  José Roberto de Araújo Júnior
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.powernukkit.plugins.heapdumper;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author joserobjr
 */
public class HeapDumperPlugin extends PluginBase {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "heapdump":
                takeHeapDump(sender, args);
                return true;
            case "cleardump":
                clearSingleDump(sender, args);
                return true;
            case "clearalldumps":
                clearAllDumps(sender, args);
                return true;
            default:
                return false;
        }
    }
    
    private void clearAllDumps(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("The command expects zero arguments. "+args.length+" was given.");
        }
        AtomicInteger count = new AtomicInteger();
        AtomicLong size = new AtomicLong();
        try {
            Files.list(Paths.get(getServer().getDataPath()))
                    .filter(Files::isRegularFile)
                    .filter(path-> path.toString().toLowerCase().endsWith(".hprof"))
                    .forEachOrdered(path -> {
                        try {
                            long currentSize = Files.size(path);
                            Files.delete(path);
                            count.incrementAndGet();
                            size.addAndGet(currentSize);
                        } catch (IOException e) {
                            sender.sendMessage("Failed to delete "+path+": "+e);
                            getLogger().warning("Failed to delete "+path, e);
                        }
                    });
            
        } catch (IOException e) {
            sender.sendMessage("Failed to clear the dumps: "+e);
            getLogger().error("Failed to clear all dumps", e);
        } finally {
            sendAndLog(sender, count.get() + " hprof files deleted. " + HeapDumper.humanReadableByteCountBin(size.get())
                    + " of disk space was freed");
        }

    }
    
    private void clearSingleDump(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("The command expects only one argument. "+args.length+" was given.");
        }
        String fileName = safeName(args[0]);
        if (!fileName.toLowerCase().endsWith(".hprof")) {
            fileName = fileName + ".hprof";
        }

        Path path = Paths.get(getServer().getDataPath(), fileName);

        if (Files.isReadable(path)) {
            sendAndLog(sender, "The file does not exists: "+ path);
        }
        
        try {
            long size = Files.size(path);
            Files.delete(path);
            sendAndLog(sender, "The file "+fileName+" was deleted and "
                    + HeapDumper.humanReadableByteCountBin(size) + " was freed");
        } catch (IOException e) {
            sender.sendMessage("Could not delete the file "+path+": "+e);
            getLogger().error("Could not delete the file "+path, e);
        }
    }
    
    private void sendAndLog(CommandSender sender, String message) {
        sender.sendMessage(message);
        getLogger().info(message);
    }
    
    private String safeName(String name) {
        return name.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
    }
    
    private void takeHeapDump(CommandSender sender, String[] args) {
        if (args.length > 1) {
            sender.sendMessage("The command expects only zero or one argument. "+args.length+" was given.");
        }
        
        String fileName = args.length > 0? safeName(args[0]) : "";
        if (fileName.isEmpty()) {
            fileName = "heapdump_"+new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss.SSSZ").format(new Date());
        }
        
        fileName = fileName+".hprof";

        File file = new File(getServer().getDataPath(), fileName);
        try {
            HeapDumper.dumpHeap(file.getAbsolutePath(), true);
            if (file.isFile()) {
                sender.sendMessage(
                        "Heap dump taken. File Size: "
                                + HeapDumper.humanReadableByteCountBin(file.length()) 
                                + ", Name: " + fileName);
            } else {
                sender.sendMessage("Heap dump failed. The file was not created: "+file.getAbsolutePath());
            }
        } catch (Exception e) {
            sender.sendMessage("Heap dump failed. An exception has been caught: "+e);
            getLogger().error("Failed to take heap dump with file name "+file, e);
        }
    }
}
