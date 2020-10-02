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

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * @author <a href="https://stackoverflow.com/a/3758880/804976">aioobe</a>, 
 * <a href="https://www.curseforge.com/minecraft/bukkit-plugins/heapdump">_ForgeUser10219879</a>
 */
public final class HeapDumper {
    private static volatile Object hotspotMBean;
    
    private HeapDumper() {
        throw new UnsupportedOperationException();
    }

    public static String humanReadableByteCountBin(long bytes) {
        // https://stackoverflow.com/a/3758880/804976
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static void dumpHeap(String fileName, boolean live) {
        initHotspotMBean();

        try {
            Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            Method m = clazz.getMethod("dumpHeap", String.class, Boolean.TYPE);
            m.invoke(hotspotMBean, fileName, live);
        } catch (RuntimeException var4) {
            throw var4;
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
    }

    private static void initHotspotMBean() {
        if (hotspotMBean == null) {
            synchronized(HeapDumper.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }

    }

    private static Object getHotspotMBean() {
        try {
            Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            return ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
