package com.example.developer.shmlib;

import android.os.SharedMemory;

class Native {

    static {
        System.loadLibrary("native-lib");
    }


    // Segment
    static native Segment nNewSegment(int size, String debugName);
    static native Segment nOpenSegment(int segNum);

    // Record
    static class RawRecord {
        int segNum;
        int recNum;
        int recSize;
    }

    static native int nAllocateRecord(int segNum, int recordSize);
    static native int nAddRefRecord(int segNum, int recNum);
    static native int nRelRefRecord(int segNum, int recNum);

    /// Queue
    static native int nAllocateQueueSegment(SharedMemory shm);
    static native void nPushRecord(int queueNum, RawRecord rawRecord);
    static native RawRecord nPopRecord(int queueNum);
}
