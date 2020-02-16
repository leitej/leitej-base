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
import java.awt.Point;

import leitej.exception.ClosedLtRtException;
import leitej.gui.exception.GuiLtException;
import leitej.gui.uniform.model.Action;
import leitej.gui.uniform.model.ActionEnum;
import leitej.gui.uniform.model.ActionInput;
import leitej.gui.uniform.model.Element;
import leitej.gui.uniform.model.Frame;
import leitej.gui.uniform.model.Value;
import leitej.gui.util.ImageFormatEnum;
import leitej.util.data.XmlomUtil;

/**
 *
 * @author Julio Leite
 */
public class InteractiveImageFrame extends ImageFrame {

	public InteractiveImageFrame() throws GuiLtException, InterruptedException {
		this(null, null, null, DEFAULT_MIDDLE_IMAGE_FORMAT, null);
	}

	public InteractiveImageFrame(final String title) throws GuiLtException, InterruptedException {
		this(title, null, null, DEFAULT_MIDDLE_IMAGE_FORMAT, null);
	}

	public InteractiveImageFrame(final String title, final CloseOperationEnum closeOperation)
			throws GuiLtException, InterruptedException {
		this(title, closeOperation, null, DEFAULT_MIDDLE_IMAGE_FORMAT, null);
	}

	public InteractiveImageFrame(final String title, final byte[] iconImage, final GraphicsConfiguration gc)
			throws GuiLtException, InterruptedException {
		this(title, null, iconImage, DEFAULT_MIDDLE_IMAGE_FORMAT, gc);
	}

	public InteractiveImageFrame(final String title, final CloseOperationEnum closeOperation, final byte[] iconImage,
			final ImageFormatEnum middleImageFormat, final GraphicsConfiguration gc)
			throws GuiLtException, InterruptedException {
		super(title, iconImage, closeOperation, middleImageFormat, gc);

	}

	@Override
	protected Frame getFrame(final String title, final byte[] iconImage) {
		final Frame frame = super.getFrame(title, iconImage);
		final Element element = frame.getData().getLayout().getElements().get(0);
		final Action[] actions = new Action[2];
		actions[0] = XmlomUtil.newXmlObjectModelling(Action.class);
		actions[0].setAction(ActionEnum.SUBMIT);
		actions[1] = XmlomUtil.newXmlObjectModelling(Action.class);
		actions[1].setAction(ActionEnum.INPUT);
		element.setActions(actions);
		return frame;
	}

	public final Point readClickedPoint() throws ClosedLtRtException, GuiLtException, InterruptedException {
		Point result = null;
		ActionInput actionInput;
		while (result == null) {
			actionInput = pollActionInput();
			if (actionInput.getValues() != null) {
				for (final Value value : actionInput.getValues()) {
					if (ActionEnum.INPUT.equals(value.getAction()) && OUTPUT_ID.equals(value.getElementId())) {
						result = new Point();
						result.x = value.getPointInput().getX();
						result.y = value.getPointInput().getY();
					}
				}
			}
			actionInput.release();
		}
		fastUpdate();
		return result;
	}

}
