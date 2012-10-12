/**
 * ********************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.math.sparse;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A list of Integers, that avoids boxing and unboxing
 * 
 * @author John McCrae
 */
public class IntList {

    private int[] vals;
    private int size;
    private static final int INITIAL_SIZE = 1024;

    public IntList() {
        this.vals = new int[INITIAL_SIZE];
        this.size = 0;
    }

    private void expand() {
        int[] newVals = new int[size * 2];
        System.arraycopy(vals, 0, newVals, 0, size);
        vals = newVals;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(int n) {
        for (int i = 0; i < size; i++) {
            if (vals[i] == n) {
                return true;
            }
        }
        return false;
    }

    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {

            int n = 0;

            @Override
            public boolean hasNext() {
                return n < size;
            }

            @Override
            public Integer next() {
                if (n < size) {
                    return vals[n++];
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                if (n > 0) {
                    IntList.this.remove(n - 1);
                }
            }
        };
    }

    public int[] toArray() {
        return Arrays.copyOfRange(vals, 0, size);
    }

    public boolean add(int e) {
        if (size >= vals.length) {
            expand();
        }
        vals[size++] = e;
        return true;
    }

    public boolean addAll(IntList c) {
        while (size + c.size >= vals.length) {
            expand();
        }
        System.arraycopy(c.vals, 0, vals, size, c.size);
        size += c.size;
        return true;
    }

    public boolean addAll(int index, IntList c) {
        if (index == size) {
            return addAll(c);
        } else if (index < size) {
            while (size + c.size >= vals.length) {
                expand();
            }
            System.arraycopy(vals, index, vals, index + c.size, size - index);
            System.arraycopy(c.vals, 0, vals, index, c.size);
            size += c.size;
            return true;
        } else {
            throw new UnsupportedOperationException("Inserting list after end of this list");
        }
    }

    public boolean retainAll(IntList c) {
        int[] retained = new int[vals.length];
        int retIdx = 0;
        boolean changed = false;
        L: for(int i = 0; i < size; i++) {
            for(int j = 0; j < c.size; j++) {
                if(vals[i] == c.vals[j]) {
                    retained[retIdx++] = vals[i];
                    continue L;
                }
            }
            changed = true;
        }
        if(changed) {
            vals = retained;
            size = retIdx;
            return changed;
        } else {
            return false;
        }
    }

    public void clear() {
        size = 0;
    }

    public int get(int index) {
        if(index >= size) {
            throw new IndexOutOfBoundsException();
        } else {
            return vals[index];
        }
    }

    public int set(int index, int element) {
        if(index >= size) {
            throw new IndexOutOfBoundsException();
        } else {
            final int old = vals[index];
            vals[index] = element;
            return old;
        }
    }

    public void add(int index, Integer element) {
        if(index > size) {
            throw new IndexOutOfBoundsException();
        } else if(index == size) {
            add(element);
        } else {
            System.arraycopy(vals, index, vals, index+1, size-index);
            vals[index] = element;
            size++;
        }
    }

    public int remove(int index) {
        if(index >= size) {
            throw new IndexOutOfBoundsException();
        } else if(index == size-1) {
            int old = vals[index];
            size--;
            return old;
        } else {
            int old = vals[index];
            System.arraycopy(vals,index+1,vals,index,size-index-1);
            size--;
            return old;
        }
    }

    public int indexOf(int n) {
        for(int i = 0; i < size; i++) {
            if(vals[i] == n)
                return i;
        }
        return -1;
    }

    public int lastIndexOf(int n) {
        for(int i = size-1; i >= 0; i--) {
            if(vals[i] == n)
                return i;
        }
        return -1;
    }

    public IntList subList(int fromIndex, int toIndex) {
        if(toIndex < fromIndex || fromIndex < 0 || toIndex > size) {
            throw new IndexOutOfBoundsException();
        }
        final IntList subList = new IntList();
        System.arraycopy(vals, fromIndex, subList.vals, 0, toIndex-fromIndex);
        subList.size = toIndex - fromIndex;
        return subList;
    }
    
    public void sort() {
        Arrays.sort(vals, 0, size);
    }
}
