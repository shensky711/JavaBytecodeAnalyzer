package site.hanschen.bytecode.java.utils;

import site.hanschen.bytecode.java.ClassAccess;

import java.io.InputStream;

/**
 * @author chenhang
 */
public class Utils {

    private Utils() {
    }

    public static String dumpHex(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (i % 16 == 0 && i != 0) {
                builder.append("\n");
            }

            builder.append(String.format("0x%02X", data[i])).append(", ");
        }
        return builder.toString();
    }

    public static InputStream getStreamFromResource(String name) {
        InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(name);
        if (inputStream == null) {
            throw new RuntimeException("can't find " + name + " in Resource");
        }
        return inputStream;
    }

    public static String getClassAccessFlags(short accessFlags) {
        StringBuilder builder = new StringBuilder();
        for (ClassAccess access : ClassAccess.values()) {
            if ((accessFlags & access.getFlag()) != 0) {
                builder.append(access.name()).append(" ");
            }
        }
        return builder.toString();
    }
}
