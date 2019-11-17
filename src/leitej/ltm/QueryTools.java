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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author Julio Leite
 */
final class QueryTools extends AbstractQueryPool {

	private final Map<String, QueryEntrance<?>> stateTableEntranceMap = new HashMap<>();
	private final Map<Integer, List<QueryEntrance<?>>> entrancesMap = new HashMap<>();
	private final Map<Integer, List<QueryExpression>> expressionsMap = new HashMap<>();
	private int level = 0;

	private final StringBuilder sb = new StringBuilder();

	QueryTools() {
	}

	@Override
	void release() {
		this.level = 0;
		this.stateTableEntranceMap.clear();
		for (final List<QueryEntrance<?>> entranceList : this.entrancesMap.values()) {
			for (final QueryEntrance<?> entrance : entranceList) {
				entrance.release();
			}
		}
		this.entrancesMap.clear();
		for (final List<QueryExpression> expressionList : this.expressionsMap.values()) {
			for (final QueryExpression expression : expressionList) {
				expression.release();
			}
		}
		this.expressionsMap.clear();
		QueryPool.offerQueryTools(this);
	}

	void incLevel() {
		this.level++;
	}

	void decLevel() {
		this.level--;
	}

	List<QueryEntrance<?>> getTableEntranceList() {
		List<QueryEntrance<?>> entrances = this.entrancesMap.get(Integer.valueOf(this.level));
		if (entrances == null) {
			entrances = new ArrayList<>();
			this.entrancesMap.put(Integer.valueOf(this.level), entrances);
		}
		return entrances;
	}

	List<QueryExpression> getExpressionList() {
		List<QueryExpression> expressions = this.expressionsMap.get(Integer.valueOf(this.level));
		if (expressions == null) {
			expressions = new ArrayList<>();
			this.expressionsMap.put(Integer.valueOf(this.level), expressions);
		}
		return expressions;
	}

	void addPrincipalTableEntrance(final QueryEntrance<?> tableEntrance) {
		addTableEntrance(tableEntrance, true);
	}

	private void addTableEntrance(final QueryEntrance<?> tableEntrance, final boolean force) {
		if (force) {
			getTableEntranceList().add(tableEntrance);
		}
		final String key = tableEntrance.getAlias();
		if (!this.stateTableEntranceMap.containsKey(key)) {
			if (!force) {
				getTableEntranceList().add(tableEntrance);
			}
			this.stateTableEntranceMap.put(key, tableEntrance);
		}
	}

	ElementColumn getParameterDraw(final StringBuilder sbDest, final QueryEntrance<?> tableEntrance,
			final String[] fields) {
		String key = tableEntrance.getAlias();
		addTableEntrance(tableEntrance, false);
		QueryEntrance<?> newEntrance = tableEntrance;
		QueryEntrance<?> newEntranceTmp;
		ElementColumn ecTmp = null;
		if (fields != null && fields.length > 0) {
			this.sb.setLength(0);
			this.sb.append(tableEntrance.getAlias());
			for (int i = 0; i < fields.length; i++) {
				this.sb.append(".");
				this.sb.append(fields[i]);
				key = this.sb.toString();
				newEntranceTmp = newEntrance;
				ecTmp = newEntranceTmp.getElementTable().getColumn(fields[i]);
				if (!ecTmp.isLink()) { // leaf, id or binaryStream
					if (ecTmp.isStream()) {
						throw new IllegalStateException();
					}
					if (i + 1 != fields.length) {
						throw new IllegalStateException();
					}
					sbDest.append(newEntranceTmp.getAlias());
					sbDest.append(".");
					sbDest.append(ecTmp.getSqlName());
				} else {
					if (!this.stateTableEntranceMap.containsKey(key) || (i + 1 == fields.length)) {
						// if(ecTmp.getListFromClassTable() == null){
						if (!ecTmp.isMapped()) { // tableItf
							if (i + 1 != fields.length) {
								newEntrance = QueryPool.pollQueryEntrance();
								newEntrance.setQueryTableEntrance(ecTmp.getRelatedElementTable());
								addTableEntrance(newEntrance, false);
								getExpressionList()
										.add(Query.expression(Query.field(newEntranceTmp, ecTmp.getJavaName()),
												Query.equal(), Query.field(newEntrance,
														newEntrance.getElementTable().getColumnId().getJavaName())));
								this.stateTableEntranceMap.put(key, newEntrance);
							} else {
								sbDest.append(newEntranceTmp.getAlias());
								sbDest.append(".");
								sbDest.append(ecTmp.getSqlName());
							}
						} else { // tableItf mapped
							newEntrance = QueryPool.pollQueryEntrance();
							newEntrance.setQueryTableEntrance(ecTmp.getRelatedElementTable());
							addTableEntrance(newEntrance, false);
							getExpressionList()
									.add(Query
											.expression(
													Query.field(newEntranceTmp,
															newEntranceTmp.getElementTable().getColumnId()
																	.getJavaName()),
													Query.equal(), Query.field(newEntrance, ecTmp.getMappedByName())));
							this.stateTableEntranceMap.put(key, newEntrance);
							if (i + 1 == fields.length) {
								sbDest.append(newEntrance.getAlias());
								sbDest.append(".");
								sbDest.append(newEntrance.getElementTable().getColumnId().getSqlName());
								ecTmp = newEntrance.getElementTable().getColumnId();
							}
						}
						// }else{ // set
						//
						// }
					} else {
						newEntrance = this.stateTableEntranceMap.get(key);
					}
				}
			}
		} else {
			sbDest.append(tableEntrance.getAlias());
			sbDest.append(".");
			sbDest.append(tableEntrance.getElementTable().getColumnId().getSqlName());
			ecTmp = tableEntrance.getElementTable().getColumnId();
		}
		return ecTmp;
	}

}
