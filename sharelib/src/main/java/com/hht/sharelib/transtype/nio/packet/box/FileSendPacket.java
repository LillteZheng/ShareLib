package com.hht.sharelib.transtype.nio.packet.box;

import com.hht.sharelib.transtype.nio.packet.SendPacket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author by  zhengshaorui on 2019/9/10
 * Describe:
 */
public class FileSendPacket extends SendPacket<FileInputStream> {

    private File file;

    public FileSendPacket(File file) {
        this.file = file;
        length = file.length();
    }

    @Override
    public byte type() {
        return TYPE_STREAM_FILE;
    }

    @Override
    protected FileInputStream createStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
