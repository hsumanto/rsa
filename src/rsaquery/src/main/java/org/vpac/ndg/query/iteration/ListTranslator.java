package org.vpac.ndg.query.iteration;

import java.util.Iterator;

import org.vpac.ndg.query.math.ElementInt;

/**
 * Allows iteration over basic types as though they were lists of Elements.
 * 
 * @author Alex Fraser
 */
public class ListTranslator {

	/**
	 * @param source A source iterable.
	 * @return An iterable and iterator over the source. Don't re-use this
	 *         iterable, and don't retain a reference to the values it returns
	 *         because they may change on the following call to next().
	 */
	public static Iterable<ElementInt> ints(Iterable<Integer> source) {
		return new ElementIntTranslator(source);
	}

	static class ElementIntTranslator implements Iterable<ElementInt>,
			Iterator<ElementInt> {

		Iterator<Integer> base;
		ElementInt value;

		public ElementIntTranslator(Iterable<Integer> baseList) {
			this.base = baseList.iterator();
			value = new ElementInt();
		}

		@Override
		public Iterator<ElementInt> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return base.hasNext();
		}

		@Override
		public ElementInt next() {
			return value.set(base.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Can't remove value from this generator.");
		}

	}
}
