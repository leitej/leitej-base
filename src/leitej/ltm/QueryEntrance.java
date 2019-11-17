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

/**
 * Use this like an alias to the SQL This can differ from one manager to other
 *
 * @author Julio Leite
 */
public final class QueryEntrance<T extends LtmObjectModelling> extends AbstractQueryPool {

	private static final AbstractLongTermMemory LTM_MANAGER = LongTermMemory.getInstance();

	private ElementTable elementTable;
	private final String alias;

	QueryEntrance() {
		this.alias = LTM_MANAGER.generateUniqueTableAlias();
	}

	void setQueryTableEntrance(final ElementTable eTable) {
		this.elementTable = eTable;
	}

	String getAlias() {
		return this.alias;
	}

	ElementTable getElementTable() {
		return this.elementTable;
	}

	@SuppressWarnings("unchecked")
	@Override
	void release() {
		this.elementTable = null;
		QueryPool.offerQueryEntrance((QueryEntrance<LtmObjectModelling>) this);
	}

}
