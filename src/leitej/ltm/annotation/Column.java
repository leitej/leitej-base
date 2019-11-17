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
public abstract @interface Column {

	public static final boolean DEFAULT_UNIQUE = false;
	public static final boolean DEFAULT_NULLABLE = true;
	public static final boolean DEFAULT_INSERTABLE = true;
	public static final boolean DEFAULT_UPDATABLE = true;
	public static final int DEFAULT_MAX_ARRAY_LENGTH = -1;
	public static final int DEFAULT_LENGTH = 255;
	public static final int DEFAULT_PRECISION = 0;
	public static final int DEFAULT_SCALE = 0;

	public abstract boolean unique() default DEFAULT_UNIQUE;

	public abstract boolean nullable() default DEFAULT_NULLABLE;

	public abstract boolean insertable() default DEFAULT_INSERTABLE;

	public abstract boolean updatable() default DEFAULT_UPDATABLE;

	public abstract int maxArrayLength() default DEFAULT_MAX_ARRAY_LENGTH;

	public abstract String columnDefinition() default "";

	public abstract int length() default DEFAULT_LENGTH;

	public abstract int precision() default DEFAULT_PRECISION;

	public abstract int scale() default DEFAULT_SCALE;
}
