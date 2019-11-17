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

package leitej.xml.om;

/**
 *
 * @author Julio Leite
 */
public abstract interface XmlomDataItf {

	/**
	 * This method should be overridden such as
	 * xmlomDataItf.equals(XmlomDataItf.valueOf(xmlomDataItf.toString())).
	 *
	 * @return String value representation
	 */
	@Override
	public abstract String toString();

	/**
	 * ValueOf(String).<br/>
	 * This method has to exists at class level.<br/>
	 * Returns the type that is implementing this interface.<br/>
	 * {@link Constant.VALUEOF_METHOD_NAME}
	 */
	// public static ? valueOf(String dataItf){}

	/**
	 * Returns a more friendly name for this class to be written at XML level.<br/>
	 * This method is optional at class level.
	 * {@link Constant.ALIAS_CLASS_NAME_METHOD_NAME}
	 */
	// public static String aliasClassName(){}

}
