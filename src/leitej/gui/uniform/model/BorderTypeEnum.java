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

package leitej.gui.uniform.model;

/**
 *
 * @author Julio Leite
 */
public enum BorderTypeEnum {
	EMPTY, // provides an empty, transparent border which takes up space but does no
			// drawing
	BEVEL_LOWERED, // simple two-line bevel border
	BEVEL_RAISED, // simple two-line bevel border
	ETCHED_LOWERED, // simple etched-in
	ETCHED_RAISED, // simple etched-out
	LINE, // line border of arbitrary thickness and of a single color
	MATTE, // matte-like border of either a solid color or a tiled icon
	BEVEL_SOFT_LOWERED, // lowered bevel with softened corners
	BEVEL_SOFT_RAISED // raised bevel with softened corners
	;

}
