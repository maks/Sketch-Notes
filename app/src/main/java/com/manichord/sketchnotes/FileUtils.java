package com.manichord.sketchnotes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Misc fiel related util methods.
 */
public class FileUtils {

    /**
     * Copies src file to dst file. If the dst file does not exist, it will be
     * created.
     *
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copyToFile(InputStream src, File dst)
            throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = src.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // can't do anything about this
            }
        }
    }
}
