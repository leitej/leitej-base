/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
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

package leitej.xml.om;

import java.io.Serializable;

import leitej.util.data.AbstractDataProxy;

/**
 *
 * @author Julio Leite
 */
final class DataProxy extends AbstractDataProxy<XmlObjectModelling, DataProxyHandler> {

	private static final long serialVersionUID = -8958444462425861619L;

	private static final DataProxy INSTANCE = new DataProxy();

	static DataProxy getInstance() {
		return INSTANCE;
	}

	private DataProxy() {
		super(Serializable.class);
	}

	<I extends XmlObjectModelling> I newXmlObjectModelling(final Class<I> iClass) {
		return newProxyInstance(iClass, new DataProxyHandler(iClass));
	}

}
