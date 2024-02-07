package pt.tecnico.distledger.vectorclock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VectorClock {
    private ArrayList<Integer> vectorClock;

    public VectorClock(List<Integer> vectorClock) {
        this.vectorClock = new ArrayList<>(vectorClock);
    }

    public VectorClock() {
        this.vectorClock = new ArrayList<Integer>(Arrays.asList(0,0));
    }

    public ArrayList<Integer> getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(ArrayList<Integer> vectorClock) {
        this.vectorClock = vectorClock;
    }

    public int getSize() {
        return this.vectorClock.size();
    }

    public int getVectorClockPosition(int position) {
        return this.vectorClock.get(position);
    }

    public void incrementVectorClockPosition(int index) {
        this.vectorClock.set(index, this.getVectorClockPosition(index) + 1);
    }

    public void setVectorClockPosition(int index, int value) {
        this.vectorClock.set(index, value);
    }

    @Override
    public String toString() {
        return "VectorClock{" +
                "vectorClock=" + vectorClock +
                '}';
    }

    public boolean greaterOrEqual(VectorClock v) {
        for (int i = 0; i < getSize(); i++) {
            if (this.getVectorClockPosition(i) < v.getVectorClockPosition(i))
                return false;
        }
        return true;
    }

    public static VectorClock merge(VectorClock v1, VectorClock v2) {
        for (int i = 0; i < v1.getSize(); i++) {
            if (v1.getVectorClockPosition(i) < v2.getVectorClockPosition(i))
                v1.vectorClock.set(i, v2.getVectorClockPosition(i));
        }
        return v1;
    }
}
