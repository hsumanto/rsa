package org.vpac.ndg.query.iteration;

import java.util.Collection;
import java.util.Iterator;

/**
 * Flattens any number of iterables of the same type.
 * @author Alex Fraser
 *
 * @param <T> The type contained in the iterables.
 */
public class Flatten<T> implements Iterable<T> {

	private Collection<? extends Iterable<T>> lists;

	public Flatten(Collection<? extends Iterable<T>> lists) {
		this.lists = lists;
	}

	@Override
	public Iterator<T> iterator() {
		return new FlattenIterator(lists);
	}

	final class FlattenIterator implements Iterator<T> {

		private Iterator<? extends Iterable<T>> outerIter;
		private Iterator<T> innerIter;

		public FlattenIterator(Collection<? extends Iterable<T>> lists) {
			outerIter = lists.iterator();
			innerIter = null;
		}

		@Override
		public boolean hasNext() {
			advanceOuter();
			return innerIter != null && innerIter.hasNext();
		}

		void advanceOuter() {
			if (innerIter != null && innerIter.hasNext())
				return;
			if (outerIter.hasNext())
				innerIter = outerIter.next().iterator();
		}

		@Override
		public T next() {
			advanceOuter();
			return innerIter.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Can't remove value from a flatten.");
		}
	}

}
