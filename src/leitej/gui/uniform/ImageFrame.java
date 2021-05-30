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

import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import leitej.exception.GuiLtException;
import leitej.gui.uniform.model.BorderLayoutConstraintsEnum;
import leitej.gui.uniform.model.Data;
import leitej.gui.uniform.model.Element;
import leitej.gui.uniform.model.ElementUpdate;
import leitej.gui.uniform.model.Frame;
import leitej.gui.uniform.model.Image;
import leitej.gui.uniform.model.ImageUpdate;
import leitej.gui.uniform.model.Layout;
import leitej.gui.uniform.model.LayoutEnum;
import leitej.gui.uniform.model.MetaData;
import leitej.gui.uniform.model.Point;
import leitej.gui.util.ImageFormatEnum;
import leitej.gui.util.ImageUtil;
import leitej.xml.om.Xmlom;

/**
 *
 * @author Julio Leite
 */
public class ImageFrame extends UniformFrame {

	protected static final String OUTPUT_ID = "OUTPUT";
	protected static final ImageFormatEnum DEFAULT_MIDDLE_IMAGE_FORMAT = ImageFormatEnum.BMP;

	private final ElementUpdate imageUpdate;
	private final ImageFormatEnum middleImageFormat;

	public ImageFrame() throws GuiLtException, InterruptedException {
		this(null, null, null, DEFAULT_MIDDLE_IMAGE_FORMAT, null);
	}

	public ImageFrame(final String title) throws GuiLtException, InterruptedException {
		this(title, null, null, DEFAULT_MIDDLE_IMAGE_FORMAT, null);
	}

	public ImageFrame(final String title, final CloseOperationEnum closeOperation)
			throws GuiLtException, InterruptedException {
		this(title, null, closeOperation, DEFAULT_MIDDLE_IMAGE_FORMAT, null);
	}

	public ImageFrame(final String title, final byte[] iconImage, final ImageFormatEnum middleImageFormat,
			final GraphicsConfiguration gc) throws GuiLtException, InterruptedException {
		this(title, iconImage, null, DEFAULT_MIDDLE_IMAGE_FORMAT, gc);
	}

	public ImageFrame(final String title, final byte[] iconImage, final CloseOperationEnum closeOperation,
			final ImageFormatEnum middleImageFormat, final GraphicsConfiguration gc)
			throws GuiLtException, InterruptedException {
		super(null, closeOperation, gc);
		this.set(getFrame(title, iconImage));
		this.middleImageFormat = middleImageFormat;
		this.imageUpdate = Xmlom.newInstance(ElementUpdate.class);
		this.imageUpdate.setId(OUTPUT_ID);
		final ImageUpdate tmp = Xmlom.newInstance(ImageUpdate.class);
		this.imageUpdate.setImageUpdate(tmp);
		final Point imagePosition = Xmlom.newInstance(Point.class);
		tmp.setDataPosition(imagePosition);
	}

	protected Frame getFrame(final String title, final byte[] iconImage) {
		final Frame frame = Xmlom.newInstance(Frame.class);
		final MetaData metaData = Xmlom.newInstance(MetaData.class);
		metaData.setTitle(title);
		metaData.setIconImage(iconImage);
		frame.setMetaData(metaData);
		final Data data = Xmlom.newInstance(Data.class);
		final Layout layout = Xmlom.newInstance(Layout.class);
		layout.setType(LayoutEnum.BORDER);
		final List<Element> elementList = new ArrayList<>();
		final Element element = Xmlom.newInstance(Element.class);
		element.setId(OUTPUT_ID);

		final Image image = Xmlom.newInstance(Image.class);
		final Point dimension = Xmlom.newInstance(Point.class);
		dimension.setX(640);
		dimension.setY(480);
		image.setDimension(dimension);

		element.setBorderLayoutConstraints(BorderLayoutConstraintsEnum.CENTER);
		element.setImage(image);
		elementList.add(element);
		layout.setElements(elementList);
		data.setLayout(layout);
		frame.setData(data);
		return frame;
	}

	public synchronized void setImage(final BufferedImage image) throws GuiLtException, InterruptedException {
		setImage(0, 0, image);
	}

	public synchronized void setImage(final int x, final int y, final BufferedImage image)
			throws GuiLtException, InterruptedException {
		this.imageUpdate.getImageUpdate().getDataPosition().setX(x);
		this.imageUpdate.getImageUpdate().getDataPosition().setY(y);
		this.imageUpdate.getImageUpdate().setData(ImageUtil.encode(image, this.middleImageFormat));
		fastUpdate(this.imageUpdate);
	}

}
