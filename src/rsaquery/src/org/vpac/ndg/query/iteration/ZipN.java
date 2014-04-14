package org.vpac.ndg.query.iteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Iterates over any number of iterables in unison. They must all have the same
 * type.
 * @author Alex Fraser
 *
 * @param <T> The type contained in the iterables.
 */
public class ZipN<T> implements Iterable<Iterable<T>> {

	private Collection<? extends Iterable<T>> lists;

	public ZipN(Collection<? extends Iterable<T>> lists) {
		this.lists = lists;
	}

	@Override
	public Iterator<Iterable<T>> iterator() {
		return new ZipNIterator(lists);
	}

	final class ZipNIterator implements Iterator<Iterable<T>> {

		private List<Iterator<T>> iters;

		public ZipNIterator(Collection<? extends Iterable<T>> lists) {
			iters = new ArrayList<Iterator<T>>();
			for (Iterable<T> list : lists) {
				iters.add(list.iterator());
			}
		}

		@Override
		public boolean hasNext() {
			for (Iterator<T> i : iters) {
				if (!i.hasNext())
					return false;
			}
			return true;
		}

		@Override
		public Collection<T> next() {
			List<T> zip = new ArrayList<T>(iters.size());
			for (Iterator<T> i : iters)
				zip.add(i.next());
			return zip;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Can't remove value from a zip.");
		}
	}

}
