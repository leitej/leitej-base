/*******************************************************************************
 * Copyright Julio Leite
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package leitej.ltm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Julio Leite
 *
 */
final class PreparedClassIndex {

	private final Map<String, SortedSet<IndexColumn>> indexMap;

	PreparedClassIndex() {
		this.indexMap = new HashMap<>();
	}

	synchronized void add(final Index[] indexes, final String columnName) {
		if (indexes != null && columnName != null) {
			Index index;
			for (int i = 0; i < indexes.length; i++) {
				index = indexes[i];
				SortedSet<IndexColumn> set = this.indexMap.get(index.name());
				if (set == null) {
					set = new TreeSet<>();
				}
				set.add(new IndexColumn(index.position(), columnName));
			}
		}
	}

	Iterator<String> getIndexesNameIterator() {
		return this.indexMap.keySet().iterator();
	}

	Iterator<IndexColumn> getIndexesColumnIterator(final String indexName) {
		return this.indexMap.get(indexName).iterator();
	}

	static final class IndexColumn implements Comparable<IndexColumn> {

		private final int position;
		private final String columnName;

		private IndexColumn(final int indexPosition, final String columnName) {
			this.position = indexPosition;
			this.columnName = columnName;
		}

		@Override
		public int compareTo(final IndexColumn o) {
			return this.position - o.position;
		}

		String getColumnName() {
			return this.columnName;
		}

	}
}
