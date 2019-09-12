package com.hht.sharelib.transtype.nio.packet;

import com.hht.sharelib.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe:公共的数据封装，封装了类型和长度
 */
public abstract class Packet<T extends Closeable> implements Closeable {
    // BYTES 类型
    public static final byte TYPE_MEMORY_BYTES = 1;
    // String 类型
    public static final byte TYPE_MEMORY_STRING = 2;
    // 文件 类型
    public static final byte TYPE_STREAM_FILE = 3;
    // 长链接流 类型
    public static final byte TYPE_STREAM_DIRECT = 4;
    public long length;
    protected T stream;




    public long length() {
        return length;
    }

    @Override
    public final void close() throws IOException {
        if (stream != null){
            closeStream(stream);
            stream = null;
        }
    }

    public final T open(){
        if (stream == null){
            stream = createStream();
        }
        return stream;
    }
    /**
     * 类型，直接通过方法得到:
     * <p>
     * {@link #TYPE_MEMORY_BYTES}
     * {@link #TYPE_MEMORY_STRING}
     * {@link #TYPE_STREAM_FILE}
     * {@link #TYPE_STREAM_DIRECT}
     *
     * @return 类型
     */
    public abstract byte type();

    protected abstract T createStream();

    /**
     * close 之前，可以自己先处理事件
     * @param stream
     */
    protected void closeStream(T stream){
        CloseUtils.close(stream);
    }

}
