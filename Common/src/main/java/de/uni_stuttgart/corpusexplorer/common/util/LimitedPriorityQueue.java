package de.uni_stuttgart.corpusexplorer.common.util;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.SortedSet;

/**
 * A priority queue that keeps at most a limit number of items.
 * 
 * @author mo
 * 
 * @param <T>
 */
public class LimitedPriorityQueue<T> extends PriorityQueue<T> {

	private static final long serialVersionUID = -6703025983615232090L;

	/**
	 * The maximum size of items in the priority queue.
	 */
	private int limit;

	public LimitedPriorityQueue(int limit) {
		super();
		this.limit = limit;
	}

	public LimitedPriorityQueue(int limit, int initialCapacity) {
		super(initialCapacity);
		this.limit = limit;
	}

	public LimitedPriorityQueue(int limit, int initialCapacity,
			Comparator<? super T> comparator) {
		super(initialCapacity, comparator);
		this.limit = limit;
	}

	public LimitedPriorityQueue(int limit,
			PriorityQueue<? extends T> priorityQueue) {
		super(priorityQueue);
		this.limit = limit;
	}

	public LimitedPriorityQueue(int limit, SortedSet<? extends T> priorityQueue) {
		super(priorityQueue);
		this.limit = limit;
	}

	@Override
	public boolean add(T item) {
		super.add(item);
		while (this.size() > limit) {
			super.remove();
		}
		return true;
	}
}
