package org.oddjob.values.types;

import java.util.Iterator;

public class SequenceIterable implements Iterable<Integer> {

	private final int from;
	private final int to;
	private final int step;
	
	public SequenceIterable(int from, int to, int step) {
		this.from = from;
		this.to = to;
		this.step = step;
	}
	
	@Override
	public Iterator<Integer> iterator() {
		
		return new Iterator<Integer>() {
			
			int current = from;
			
			@Override
			public boolean hasNext() {
				if (to >= from) {
					return current <= to;
				}
				else {
					return current >= to;
				}
			}
			
			@Override
			public Integer next() {
				try {
					return new Integer(current);
				}
				finally {
					current += step;
				}
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public String toString() {
		return "Sequence from " + from + ", to " + to + ", step " + step;
	}
}
