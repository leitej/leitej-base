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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import leitej.exception.IllegalStateLtRtException;

/**
 * @author Julio Leite
 *
 */
final class PreparedClassIndex {

	private final Map<String, SortedSet<IndexColumn>> indexMap;
	private final Map<String, String> indexCreateMap;
	private boolean prepared;

	PreparedClassIndex() {
		this.indexMap = new HashMap<>();
		this.indexCreateMap = new HashMap<>();
		this.prepared = false;
	}

	synchronized void prepare(final String tableName) {
		if (this.prepared) {
			throw new IllegalStateLtRtException();
		}
		this.prepared = true;
		String indexName;
		for (final SortedSet<IndexColumn> indexColumns : this.indexMap.values()) {
			indexName = HsqldbUtil.getIndexName(tableName, indexColumns.iterator());
			this.indexCreateMap.put(indexName,
					HsqldbUtil.getStatementCreateIndex(indexName, tableName, indexColumns.iterator()));
		}
	}

	synchronized void add(final Index[] indexes, final String columnName) {
		if (this.prepared) {
			throw new IllegalStateLtRtException();
		}
		if (indexes != null && columnName != null) {
			Index index;
			for (int i = 0; i < indexes.length; i++) {
				index = indexes[i];
				SortedSet<IndexColumn> set = this.indexMap.get(index.name());
				if (set == null) {
					set = new TreeSet<>();
					this.indexMap.put(index.name(), set);
				}
				set.add(new IndexColumn(index.position(), columnName));
			}
		}
	}

	Iterator<String> getIndexesNameIterator() {
		if (!this.prepared) {
			throw new IllegalStateLtRtException();
		}
		return this.indexCreateMap.keySet().iterator();
	}

	Collection<String> getIndexesNameCollection() {
		if (!this.prepared) {
			throw new IllegalStateLtRtException();
		}
		return this.indexCreateMap.keySet();
	}

	String getSttCreate(final String indexName) {
		if (!this.prepared) {
			throw new IllegalStateLtRtException();
		}
		return this.indexCreateMap.get(indexName);
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
