package net.querz.mca;

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NBTSerializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import top.hackchen.mcpt.MinecraftUUID;
import top.hackchen.mcpt.FileUtil;

import java.io.*;
import java.util.*;

public class MCAFile {
    private final List<Integer> indices = new ArrayList<>();
    private final Deque<Integer> offsets = new ArrayDeque<>();
    private final Deque<Integer> timestamps = new ArrayDeque<>();
    private final Deque<NamedTag> tags = new ArrayDeque<>();

    // private List
    protected void readMCA(String filename) throws IOException {
        // List<CompoundTag> tags = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(filename, "r")) {
            if (raf.length() == 0) return;
            for (int i = 0; i < 1024; ++i) {
                raf.seek(i * 4L);
                int offset = raf.read() << 16;
                offset += (raf.read() & 0xFF) << 8;
                offset += raf.read() & 0xFF;
                if (raf.read() == 0) {
                    continue;
                }

                raf.seek(4096 + i * 4L);
                int timestamp = raf.readInt();
                raf.seek(4096L * offset + 4L); //+4: skip data size

                byte compressionTypeByte = raf.readByte();
                CompressionType compressionType = CompressionType.getFromID(compressionTypeByte);
                if (compressionType == null) {
                    throw new IOException("invalid compression type " + compressionTypeByte);
                } else {
                    BufferedInputStream dis = new BufferedInputStream(compressionType.decompress(new FileInputStream(raf.getFD())));
                    NamedTag tag = (new NBTDeserializer(false)).fromStream(dis);
                    if (tag != null && tag.getTag() instanceof CompoundTag) {
                        indices.add(i);
                        offsets.offer(offset);
                        timestamps.offer(timestamp);
                        tags.offer(tag);
                    }
                }
            }
        }
    }

    protected void writeMCA(String file) throws IOException {
        Set<Integer> counter = new HashSet<>(Arrays.asList(indices.size(), offsets.size(), timestamps.size(), tags.size()));
        if (counter.size() != 1) {
            throw new RuntimeException("Data in memory has been broken.");
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            int lastWritten = 0, lastOffset = 0;
            for (int i : indices) {
                byte[] rawData = writeTag(tags.poll());
                lastWritten = rawData.length + 5;
                assert offsets.size() > 0;
                int offset = offsets.poll();
                int sectors = (lastWritten >> 12) + (lastWritten % 4096 == 0 ? 0 : 1);

                raf.seek(i * 4L);
                raf.write(offset >>> 16);
                raf.write(offset >> 8 & 0xFF);
                raf.write(offset & 0xFF);
                raf.write(sectors);
                lastOffset = offset;

                raf.seek(4096 + i * 4L);
                assert timestamps.size() > 0;
                raf.writeInt(timestamps.poll());

                raf.seek(4096L * offset);
                raf.writeInt(rawData.length);
                raf.writeByte(CompressionType.ZLIB.getID());
                raf.write(rawData);
            }
            // padding
            if (lastWritten % 4096 != 0) {
                raf.seek(lastOffset * 4096L - 1);
                raf.write(0);
            }
        }
    }

    private byte[] writeTag(NamedTag tag) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        try (BufferedOutputStream nbtOut = new BufferedOutputStream(CompressionType.ZLIB.compress(output))) {
            new NBTSerializer(false).toStream(tag, nbtOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    public void replaceUUID(MinecraftUUID target, MinecraftUUID replacement) {
        for (NamedTag tag : tags) {
            FileUtil.replaceUUIDReclusive(tag.getTag(), target, replacement);
        }
    }
}
