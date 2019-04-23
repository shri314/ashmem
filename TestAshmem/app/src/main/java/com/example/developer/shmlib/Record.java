package com.example.developer.shmlib;

import java.nio.ByteBuffer;

public class Record {

    private final int recNum;
    private final int recSize;
    private final int segNum;
    private ByteBuffer byteBuffer;

    Record(int segNum, int recNum, int recSize, ByteBuffer byteBuffer) {
        this.recNum = recNum;
        this.recSize = recSize;
        this.segNum = segNum;
        this.byteBuffer = byteBuffer;
    }

    Native.RawRecord toRawRecord() {
        Native.RawRecord rawRecord = new Native.RawRecord();
        rawRecord.recNum = recNum;
        rawRecord.recSize = recSize;
        rawRecord.segNum = segNum;

        return rawRecord;
    }

    Record duplicate() {
        Native.nAddRefRecord(segNum, recNum);
        return this;
    }

    void free() {
        Native.nRelRefRecord(segNum, recNum);
    }

    void fill(int sz, Segment.RecordFiller filler) {
        ByteBuffer bb = byteBuffer.duplicate();
        bb.limit(Math.min(sz, recSize));
        filler.doWrite(bb);
    }
}
