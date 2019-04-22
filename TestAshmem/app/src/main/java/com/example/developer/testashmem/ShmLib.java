package com.example.developer.testashmem;

import android.os.SharedMemory;
import android.system.ErrnoException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


public class ShmLib {
    static {
        System.loadLibrary("native-lib");
    }

    private static HashMap<String,Integer> memAreas = new HashMap<>();

    private static class Segment {
        final String name;
        final int segNum;
        final ByteBuffer byteBuffer;

        Segment(String name, int segNum, ByteBuffer byteBuffer) {
            this.name = name;
            this.segNum = segNum;
            this.byteBuffer = byteBuffer;
        }

        class Record {
            final int recNum;

            Segment owner() {
                return Segment.this;
            }

            Record(int recNum) {
                this.recNum = recNum;
            }
        }

        Record allocateRecord(int recordSize) {
            int pos = nAllocateRecord(segNum, recordSize);
            return new Record(pos);
        }

        void freeRecord(Record record) {
            if(record.owner() == this) {
                nFreeRecord(segNum, record.recNum);
            }
        }
    }

    private static Map<String, Segment> dataSegments = new HashMap<>();

    public static Segment CreateSegment(String segName, int segSize) throws ErrnoException {
        Segment segment = dataSegments.get(segName);
        if(segment == null) {
            SharedMemory newShm = null;
            try {
                newShm = SharedMemory.create("seg_" + segName, segSize);
                int segNum = nPrepareSegment(newShm);
                if (segNum != -1) {
                    segment = new Segment(segName, segNum, newShm.mapReadWrite());
                    dataSegments.put(segName, segment);
                }
            } finally {
                if(newShm != null) {
                    newShm.close();
                }
            }
        }

        return segment;
    }

    private static native int nPrepareSegment(SharedMemory shm);
    private static native int nAllocateRecord(int segNum, int recordSize);
    private static native int nFreeRecord(int segNum, int recNum);

    /////////////////////////////////////////////////////////////////////////////////////////

    private static class Queue {
        Segment dataSeg;

        Queue(Segment dataSeg) {
            this.dataSeg = dataSeg;
        }

        interface Filler {
            void doWrite(ByteBuffer bb);
        }

        public void push(final byte[] data) {
            pushFill(data.length, bb -> bb.put(data));
        }

        public void pushFill(int sz, Filler filler) {

            Segment.Record record = dataSeg.allocateRecord(sz);
            ByteBuffer bb = dataSeg.byteBuffer.duplicate();
            bb.position(record.recNum);
            bb.limit(sz);
            filler.doWrite(bb);

            // nQueuePush(queueSeg.segNum, dataSeg.segNum, record.recNum, sz)
            // queue.push(record);
        }

        ByteBuffer pop() {
            nQueuePop()
        }
    }

    public static Queue CreateQueue(String name, int numRec) throws ErrnoException {
        Segment segment = ShmLib.CreateSegment("q_" + name, numRec * 10);
        return new Queue(segment);
    }

    public static ByteBuffer popQueue(String name) {
        return null;
    }

    public static ByteBuffer peakQueue(String name) {
        return null;
    }

    private static native int nPrepareQueue(SharedMemory shm);

    /////////////////////////////////////////////////////////////////////////////////////////

    public static int OpenSharedMem(String name, int size, boolean create)  {
        Integer i = memAreas.get(name);
        if (create && i != null)
            return -1;
        if (i == null){
            i = new Integer(getFD(name, size));
            memAreas.put(name, i);
        }
        return i.intValue();

    }
    public static int setValue(String name, int pos, int val){
        Integer fd = memAreas.get(name);
        if(fd != null)
            return setVal(fd.intValue(),pos,val);
        return -1;
    }
    public static int getValue(String name, int pos ){
        Integer fd = memAreas.get(name);
        if(fd != null)
            return getVal(fd.intValue(),pos);
        return -1;
    }
    private static native int setVal(int fd,int pos, int val);
    private static native int getVal(int fd,int pos);
    private static native int getFD(String name , int size);
}
