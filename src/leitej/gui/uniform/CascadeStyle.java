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

package leitej.gui.uniform;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import leitej.exception.ImplementationLtRtException;
import leitej.gui.uniform.model.BorderThickness;
import leitej.gui.uniform.model.BorderTitleJustificationEnum;
import leitej.gui.uniform.model.BorderTitlePositionEnum;
import leitej.gui.uniform.model.Style;

/**
 *
 * @author Julio Leite
 */
final class CascadeStyle {

	// In general, when you want to set a border on a standard Swing component other
	// than JPanel or JLabel,
	// we recommend that you put the component in a JPanel and set the border on the
	// JPanel

	private Border border;
	private Font font;
	private boolean backgroundTransparent;
	private Color backgroundColor;
	private Color foregroundColor;

	CascadeStyle() {
	}

	CascadeStyle(final Style style) {
		this();
		parse(style);
	}

	private final void parse(final Style style) {
		if (style != null) {
			if (style.getBorder() != null) {
				this.border = parseBorder(style.getBorder());
			}
			if (style.getFont() != null) {
				this.font = parseFont(style.getFont());
			}
			if (style.getBackground() != null) {
				this.backgroundTransparent = style.getBackground().isTransparent();
				if (style.getBackground().getColor() != null) {
					this.backgroundColor = parseColor(style.getBackground().getColor());
				} else {
					this.backgroundColor = null;
				}
			}
			if (style.getForegroundColor() != null) {
				this.foregroundColor = parseColor(style.getForegroundColor());
			}
		}
	}

	private Color parseColor(final leitej.gui.uniform.model.Color color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	private Font parseFont(final leitej.gui.uniform.model.Font font) {
		return new Font(font.getName().getName(), font.getStyle().getStyle(), font.getSize());
	}

	private Border parseBorder(final leitej.gui.uniform.model.Border border) {
		Border result;
		if (border.getType() == null) {
			if (border.getColor() == null) {
				result = parseEmptyBorder(border.getThickness());
			} else {
				if (border.getThickness().getAll() != null) {
					result = new LineBorder(parseColor(border.getColor()), border.getThickness().getAll(),
							border.isRoundedCorners());
				} else {
					result = parseMatteBorder(border.getThickness(), border.getColor(), border.getIconImage());
				}
			}
		} else {
			switch (border.getType()) {
			case EMPTY:
				result = parseEmptyBorder(border.getThickness());
				break;
			case BEVEL_LOWERED:
				result = new BevelBorder(BevelBorder.LOWERED, parseColor(border.getColor()),
						parseColor(border.getShadow()));
				break;
			case BEVEL_RAISED:
				result = new BevelBorder(BevelBorder.RAISED, parseColor(border.getColor()),
						parseColor(border.getShadow()));
				break;
			case ETCHED_LOWERED:
				result = new EtchedBorder(EtchedBorder.LOWERED, parseColor(border.getColor()),
						parseColor(border.getShadow()));
				break;
			case ETCHED_RAISED:
				result = new EtchedBorder(EtchedBorder.RAISED, parseColor(border.getColor()),
						parseColor(border.getShadow()));
				break;
			case LINE:
				result = new LineBorder(parseColor(border.getColor()), border.getThickness().getAll(),
						border.isRoundedCorners());
				break;
			case MATTE:
				result = parseMatteBorder(border.getThickness(), border.getColor(), border.getIconImage());
				break;
			case BEVEL_SOFT_LOWERED:
				result = new SoftBevelBorder(SoftBevelBorder.LOWERED, parseColor(border.getColor()),
						parseColor(border.getShadow()));
				break;
			case BEVEL_SOFT_RAISED:
				result = new SoftBevelBorder(SoftBevelBorder.RAISED, parseColor(border.getColor()),
						parseColor(border.getShadow()));
				break;
			default:
				throw new ImplementationLtRtException();
			}
		}
		if (border.getTitle() != null && border.getTitle().getTitle() != null) {
			result = new TitledBorder(result, border.getTitle().getTitle(),
					((border.getTitle().getJustification() != null)
							? border.getTitle().getJustification().getJustification()
							: BorderTitleJustificationEnum.DEFAULT_JUSTIFICATION.getJustification()),
					((border.getTitle().getPosition() != null) ? border.getTitle().getPosition().getPosition()
							: BorderTitlePositionEnum.DEFAULT_POSITION.getPosition()),
					((border.getTitle().getFont() != null) ? parseFont(border.getTitle().getFont()) : null),
					((border.getTitle().getColor() != null) ? parseColor(border.getTitle().getColor()) : null));
		}
		if (border.getCompound() != null) {
			result = new CompoundBorder(result, parseBorder(border.getCompound()));
		}
		return result;
	}

	private Border parseMatteBorder(final BorderThickness thickness, final leitej.gui.uniform.model.Color color,
			final byte[] iconImage) {
		if (iconImage != null) {
			if (thickness.getAll() != null) {
				return new MatteBorder(thickness.getAll(), thickness.getAll(), thickness.getAll(), thickness.getAll(),
						new ImageIcon(iconImage));
			} else {
				return new MatteBorder(thickness.getTop(), thickness.getLeft(), thickness.getBottom(),
						thickness.getRight(), new ImageIcon(iconImage));
			}
		} else {
			if (thickness.getAll() != null) {
				return new MatteBorder(thickness.getAll(), thickness.getAll(), thickness.getAll(), thickness.getAll(),
						parseColor(color));
			} else {
				return new MatteBorder(thickness.getTop(), thickness.getLeft(), thickness.getBottom(),
						thickness.getRight(), parseColor(color));
			}
		}
	}

	private Border parseEmptyBorder(final BorderThickness thickness) {
		if (thickness.getAll() != null) {
			return new EmptyBorder(thickness.getAll(), thickness.getAll(), thickness.getAll(), thickness.getAll());
		} else {
			return new EmptyBorder(thickness.getTop(), thickness.getLeft(), thickness.getBottom(),
					thickness.getRight());
		}
	}

	final void merge(final Style style) {
		parse(style);
	}

	final Border getBorder() {
		return this.border;
	}

	final Font getFont() {
		return this.font;
	}

	final boolean isBackgroundTransparent() {
		return this.backgroundTransparent;
	}

	final Color getBackgroundColor() {
		return this.backgroundColor;
	}

	final Color getForegroundColor() {
		return this.foregroundColor;
	}

	@Override
	public final CascadeStyle clone() {
		final CascadeStyle result = new CascadeStyle();
		result.border = this.border;
		result.font = this.font;
		result.backgroundTransparent = this.backgroundTransparent;
		result.backgroundColor = this.backgroundColor;
		result.foregroundColor = this.foregroundColor;
		return result;
	}

}
