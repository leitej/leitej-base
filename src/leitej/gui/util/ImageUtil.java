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

package leitej.gui.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public final class ImageUtil {

	public static BufferedImage cropImage(final BufferedImage image, final int x, final int y, final int w,
			final int h) {
		return image.getSubimage(x, y, w, h);
	}

	public static BufferedImage resizeImage(final BufferedImage image, final int width, final int height,
			final ImageScaleEnum scale) {
		final BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
		final Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	public static byte[] encode(final BufferedImage img, final ImageFormatEnum format) {
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try {
			if (!ImageIO.write((RenderedImage) img, format.getName(), bOut)) {
				throw new IOException("No appropriate writer is found");
			}
			bOut.close();
		} catch (final IOException e) {
			throw new ImplementationLtRtException(e);
		}
		return bOut.toByteArray();
	}

	public static BufferedImage decode(final byte[] img) {
		final ByteArrayInputStream bIn = new ByteArrayInputStream(img);
		try {
			return ImageIO.read(bIn);
		} catch (final IOException e) {
			throw new ImplementationLtRtException(e);
		}
	}
}
