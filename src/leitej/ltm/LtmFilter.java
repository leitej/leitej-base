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

import java.util.ArrayList;
import java.util.List;

import leitej.exception.ImplementationLtRtException;

/**
 * @author Julio Leite
 *
 */
public final class LtmFilter<T extends LtmObjectModelling> {

	private static final FilterProxy PROXY = new FilterProxy();

	private static enum OPERAND {
		EQ
	};

	private final Class<T> ltmClass;
	private final T data;
	private final FilterHandler fHandler;
	private final List<Object> paramList;
	private final List<DataMemoryType> typeList;
	private final StringBuilder filter;
	private OPERAND nextOp;
	private boolean needCondition;

	LtmFilter(final Class<T> ltmClass) {
		this.ltmClass = ltmClass;
		this.data = PROXY.newProxyInstance(ltmClass, this);
		this.fHandler = PROXY.getHandler(this.data);
		this.paramList = new ArrayList<>();
		this.typeList = new ArrayList<>();
		this.filter = new StringBuilder();
		this.nextOp = null;
		this.needCondition = false;
	}

	public LtmFilter<T> setAnd() {
		if (!this.needCondition) {
			throw new IllegalStateException();
		}
		this.filter.append(" and");
		this.needCondition = false;
		return this;
	}

	public LtmFilter<T> setOr() {
		if (!this.needCondition) {
			throw new IllegalStateException();
		}
		this.filter.append(" or");
		this.needCondition = false;
		return this;
	}

	public T setOperandEqual() {
		if (this.needCondition) {
			throw new IllegalStateException();
		}
		this.nextOp = OPERAND.EQ;
		return this.data;
	}

	public T getParam() {
		if (this.needCondition) {
			throw new IllegalStateException();
		}
		return this.data;
	}

	void setDataFilter(final String dataName, final Object value) {
		if (this.needCondition || this.nextOp == null) {
			throw new IllegalStateException();
		}
		this.filter.append(" \"");
		this.filter.append(dataName);
		this.filter.append("\" ");
		if (OPERAND.EQ.equals(this.nextOp)) {
			this.filter.append("=");
		} else {
			throw new ImplementationLtRtException();
		}
		this.filter.append(" ?");
		//
		final DataMemoryType type = DataMemoryType.getDataMemoryType(this.fHandler.getType(dataName));
		this.typeList.add(type);
		if (DataMemoryType.LARGE_MEMORY.equals(type)) {
			this.paramList.add(LargeMemory.class.cast(value).getId());
		} else if (DataMemoryType.LONG_TERM_MEMORY.equals(type)) {
			this.paramList.add(LtmObjectModelling.class.cast(value).getId());
		} else {
			this.paramList.add(value);
		}
		//
		this.needCondition = true;
		this.nextOp = null;
	}

	public void reset() {
		this.paramList.clear();
		this.typeList.clear();
		this.nextOp = null;
		this.needCondition = false;
		this.filter.setLength(0);
	}

	Class<T> getLTMClass() {
		return this.ltmClass;
	}

	String getQueryFilter() {
		return this.filter.toString();
	}

	Object[] getParams() {
		return this.paramList.toArray();
	}

	DataMemoryType[] getTypes() {
		return this.typeList.toArray(new DataMemoryType[this.typeList.size()]);
	}

}
