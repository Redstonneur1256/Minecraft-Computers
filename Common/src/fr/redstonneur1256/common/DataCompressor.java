package fr.redstonneur1256.common;

import fr.redstonneur1256.redutilities.io.compression.Compression;

public class DataCompressor {

    private static Compression compression;
    static {
        compression = new Compression();
        compression.setBufferSize(8192);
        compression.setMethod(Compression.Method.zLib);
        compression.setThreadSafe(false);
    }

    public static byte[] compress(byte[] data) throws Exception {
        return compression.compress(data);
    }

    public static byte[] decompress(byte[] data) throws Exception {
        return compression.decompress(data);
    }

}
