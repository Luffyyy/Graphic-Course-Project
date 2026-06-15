package app_interface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class RandomPixelIterable implements Iterable<Integer[]> {
    private final int width;
    private final int height;
    private final List<Integer> lookupTable;

    RandomPixelIterable(int width, int height) {
        this.width = width;
        this.height = height;
        this.lookupTable = generateLookupTable(); // Generate the random lookup table based on image width
    }

    // Generate a lookup table for randomization based on the width of the image
    private List<Integer> generateLookupTable() {
    	List<Integer> table = new ArrayList<>(width*height); 
        for (int i = 0; i < width*height; i++) {
            table.add(i); 
        }
        Collections.shuffle(table);
        return table;
    }

    // Custom iterator to return random (x, y) pixel coordinates twice for each pixel
    @Override
    public Iterator<Integer[]> iterator() {
        return new PixelIterator();
    }

    private class PixelIterator implements Iterator<Integer[]> {
        private int curIndex = 0;
        private Integer[] indexPercentArray = new Integer[2];

        @Override
        public boolean hasNext() {
            return curIndex < width * height;
        }

        @Override
        public Integer[] next() {
        	indexPercentArray[0] = lookupTable.get(curIndex++);
        	indexPercentArray[1] = 100*curIndex/(width * height);
        	return indexPercentArray;
        }
    }
}
