package com.kamenbrot.util;

import java.util.Arrays;

public class RingBuffer<T> {
    private final Object[] items;
    private int start;
    private int count;
    private final int mask;

    public RingBuffer(int initialCapacity) {
        this.items = new Object[initialCapacity];
        this.mask = items.length - 1;
    }

    public int capacity() {
        return items.length;
    }

    public boolean enqueue(T item) {
        items[(start + count) & mask] = item;
        if (count == capacity()) {
            start = (start + 1) & mask;
        } else {
            count++;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        final T res = (T) items[start];
        start = (start + 1) & mask;
        return res;
    }

    @Override
    public String toString() {
        return "head=" + start + Arrays.toString(items);
    }
}