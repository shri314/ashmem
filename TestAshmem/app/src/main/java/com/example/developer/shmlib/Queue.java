package com.example.developer.shmlib;

import android.os.SharedMemory;
import android.system.ErrnoException;

import java.util.HashMap;
import java.util.Map;

public class Queue {
    private static Map<String, Queue> queueByName = new HashMap<>();

    final String name;
    final int queueNum;
    final Segment segment;

    Queue(String name, int queueNum, Segment segment) {
        this.name = name;
        this.queueNum = queueNum;
        this.segment = segment;
    }

    public static Queue Create(String queueName, Segment segment, int queueSize) throws ErrnoException {
        Queue queue = queueByName.get(queueName);
        if (queue == null) {
            SharedMemory newShm = null;
            try {
                newShm = SharedMemory.create("seg_" + queueName, queueSize);
                int queueNum = Native.nAllocateQueueSegment(newShm);
                if (queueNum != -1) {
                    queue = new Queue(queueName, queueNum, segment);
                    queueByName.put(queueName, queue);
                }
            } finally {
                if (newShm != null) {
                    newShm.close();
                }
            }
        }

        return queue;
    }

    public void push(Record record) {
        Native.RawRecord rawRecord = record.toRawRecord();
        Native.nPushRecord(queueNum, rawRecord);
    }

    Record pop() {
        Native.RawRecord rawRecord = Native.nPopRecord(queueNum);
        if (rawRecord != null) {
            Segment segment = Segment.Load(rawRecord.segNum);
            return segment.readRecord(rawRecord.recNum, rawRecord.recSize);
        }
        return null;
    }
}
