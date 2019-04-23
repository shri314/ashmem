package com.example.developer.shmlib;

import android.os.SharedMemory;
import android.system.ErrnoException;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

class Segment {
    private static Map<Integer, Segment> segmentByNum = new HashMap<>();
    private static Map<String, Segment> segmentByName = new HashMap<>();

    private final int segNum;
    private final ByteBuffer byteBuffer;

    Segment(int segNum, SharedMemory shm) throws ErrnoException {
        this.segNum = segNum;
        this.byteBuffer = shm.mapReadWrite();
    }

    public static Segment Load(int segNum) {
        Segment segment = segmentByNum.get(segNum);
        if (segment == null) {
            segment = Native.nOpenSegment(segNum);
        }

        return segment;
    }

    public static Segment Create(String segName, int segSize) throws ErrnoException {
        Segment segment = segmentByName.get(segName);
        if (segment == null) {
            segment = Native.nNewSegment(segSize, "data_" + segName);
        }

        return segment;
    }

    Record newRecord(int recSize) {
        int recNum = Native.nAllocateRecord(segNum, recSize);
        return readRecord(recNum, recSize);
    }

    Record readRecord(int recNum, int recSize) {
        ByteBuffer bb = byteBuffer.duplicate();
        bb.position(recNum);
        bb.limit(recSize);

        return new Record(segNum, recNum, recSize, bb);
    }

    interface RecordFiller {
        void doWrite(ByteBuffer bb);
    }
}
