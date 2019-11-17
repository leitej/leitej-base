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

package leitej.ltm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *
 * @author Julio Leite
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public abstract @interface OneToMany {
	public abstract CascadeTypeEnum[] cascade() default {};

	public abstract int fetchScale() default Integer.MAX_VALUE; // if ( fetchScale < 1 ) fetchScale = Integer.MAX_VALUE
	// TODO: implement the next commented option on tabeInvocationHandle and in
	// elementColumn
//	public abstract boolean alwaysScale() default false;				// if true the list will work as scaled even when it has less elements

	public abstract Class<?> mappedTableItf();

	public abstract String mappedBy();
}
