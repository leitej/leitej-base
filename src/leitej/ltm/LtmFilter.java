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

import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 * @author Julio Leite
 *
 */
public final class LtmFilter<T extends LtmObjectModelling> {

	private static final FilterProxy PROXY = new FilterProxy();

	public static enum OPERATOR_JOIN {
		AND, OR;
	};

	public static enum OPERATOR {
		EQUAL, NOT_EQUAL, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, LIKE;
	};

	private final Class<T> ltmClass;
	private final T data;
	private final FilterHandler fHandler;
	private final List<Object> paramList;
	private final List<DataMemoryType> typeList;
	private final StringBuilder filter;
	private final OPERATOR_JOIN opJoin;
	private OPERATOR nextOp;

	public LtmFilter(final Class<T> ltmClass, final OPERATOR_JOIN opJoin) {
		if (opJoin == null) {
			throw new NullPointerException();
		}
		this.ltmClass = ltmClass;
		this.data = PROXY.newProxyInstance(ltmClass, this);
		this.fHandler = PROXY.getHandler(this.data);
		this.paramList = new ArrayList<>();
		this.typeList = new ArrayList<>();
		this.filter = new StringBuilder();
		this.opJoin = opJoin;
		this.nextOp = null;
	}

	public T prepare(final OPERATOR op) {
		if (op == null) {
			throw new NullPointerException();
		}
		if (this.filter.length() != 0) {
			switch (this.opJoin) {
			case AND:
				this.filter.append(" and");
				break;

			case OR:
				this.filter.append(" or");
				break;

			default:
				throw new ImplementationLtRtException();
			}
		}
		this.nextOp = op;
		return this.data;
	}

	void setDataFilter(final String dataName, final Object value, final boolean obfuscatedValue) {
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
		this.filter.append(" \"");
		this.filter.append(dataName);
		this.filter.append("\" ");
		switch (this.nextOp) {
		case EQUAL:
			this.filter.append("=");
			break;

		case GREATER_THAN:
			this.filter.append(">");
			if (!type.isNumber()) {
				throw new IllegalStateLtRtException("Invalid operator: #0 to use on data type: #1", this.nextOp, type);
			}
			break;

		case GREATER_THAN_OR_EQUAL:
			this.filter.append(">=");
			if (!type.isNumber()) {
				throw new IllegalStateLtRtException("Invalid operator: #0 to use on data type: #1", this.nextOp, type);
			}
			break;

		case LESS_THAN:
			this.filter.append("<");
			if (!type.isNumber()) {
				throw new IllegalStateLtRtException("Invalid operator: #0 to use on data type: #1", this.nextOp, type);
			}
			break;

		case LESS_THAN_OR_EQUAL:
			this.filter.append("<=");
			if (!type.isNumber()) {
				throw new IllegalStateLtRtException("Invalid operator: #0 to use on data type: #1", this.nextOp, type);
			}
			break;

		case LIKE:
			this.filter.append("like");
			if (!type.isText()) {
				throw new IllegalStateLtRtException("Invalid operator: #0 to use on data type: #1", this.nextOp, type);
			}
			if (obfuscatedValue) {
				throw new IllegalStateLtRtException("Invalid operator: #0 on obfuscated data: #0", this.nextOp,
						dataName);
			}
			break;

		case NOT_EQUAL:
			this.filter.append("!=");
			break;

		default:
			throw new ImplementationLtRtException();
		}
		this.filter.append(" ?");
	}

	public void reset() {
		this.paramList.clear();
		this.typeList.clear();
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
