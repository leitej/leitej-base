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

import leitej.exception.ClosedLtRtException;
import leitej.exception.GuiLtException;
import leitej.gui.uniform.model.Action;
import leitej.gui.uniform.model.ActionEnum;
import leitej.gui.uniform.model.ActionInput;
import leitej.gui.uniform.model.Area;
import leitej.gui.uniform.model.Background;
import leitej.gui.uniform.model.BorderLayoutConstraintsEnum;
import leitej.gui.uniform.model.Element;
import leitej.gui.uniform.model.ElementUpdate;
import leitej.gui.uniform.model.Font;
import leitej.gui.uniform.model.FontNameEnum;
import leitej.gui.uniform.model.FontStyleEnum;
import leitej.gui.uniform.model.Frame;
import leitej.gui.uniform.model.Style;
import leitej.gui.uniform.model.TextArea;
import leitej.gui.uniform.model.TextAreaUpdate;
import leitej.gui.uniform.model.Value;
import leitej.gui.util.StyleUtil;
import leitej.util.ColorEnum;
import leitej.util.data.XmlomUtil;

/**
 *
 * @author Julio Leite
 */
public final class InputOutputFrame extends OutputFrame {

	private static final String INPUT_TEXT_ID = "INPUT";

	private final ElementUpdate textAreaUpdate;

	public InputOutputFrame(final String title, final byte[] iconImage, final int fontSize,
			final CloseOperationEnum closeOperation, final GraphicsConfiguration gc)
			throws GuiLtException, InterruptedException {
		super(title, iconImage, fontSize, closeOperation, gc);
		this.textAreaUpdate = XmlomUtil.newXmlObjectModelling(ElementUpdate.class);
		this.textAreaUpdate.setId(INPUT_TEXT_ID);
		final TextAreaUpdate tmp = XmlomUtil.newXmlObjectModelling(TextAreaUpdate.class);
		tmp.setText("");
		this.textAreaUpdate.setTextAreaUpdate(tmp);
	}

	@Override
	protected Frame getFrame(final String title, final byte[] iconImage, final int fontSize) {
		final Frame frame = super.getFrame(title, iconImage, fontSize);

		Action[] actions = new Action[1];
		actions[0] = XmlomUtil.newXmlObjectModelling(Action.class);
		actions[0].setAction(ActionEnum.FOCUS_FORWARD);
		actions[0].setArguments(new String[] { INPUT_TEXT_ID });
		frame.getData().getLayout().getElements().get(0).setActions(actions);

		final Element element = XmlomUtil.newXmlObjectModelling(Element.class);
		element.setId(INPUT_TEXT_ID);

		final Style style = XmlomUtil.newXmlObjectModelling(Style.class);
		final Font font = XmlomUtil.newXmlObjectModelling(Font.class);
		font.setName(FontNameEnum.MONOSPACED);
		font.setStyle(FontStyleEnum.PLAIN);
		font.setSize(fontSize);
		style.setFont(font);
		final Background background = XmlomUtil.newXmlObjectModelling(Background.class);
		background.setTransparent(false);
		background.setColor(StyleUtil.newColor(ColorEnum.DIM_GRAY));
		style.setBackground(background);
		style.setForegroundColor(StyleUtil.newColor(ColorEnum.YELLOW));

		element.setStyle(style);

		final TextArea textarea = XmlomUtil.newXmlObjectModelling(TextArea.class);
		final Area area = XmlomUtil.newXmlObjectModelling(Area.class);
		area.setRows(1);
		area.setColumns(120);
		textarea.setArea(area);
		textarea.setEditable(true);
//			Scroll scroll = XmlomIOStream.newXmlObjectModelling(Scroll.class);
//			scroll.setVertical(ScrollPolicyEnum.NEVER);
//			scroll.setHorizontal(ScrollPolicyEnum.NEVER);
//			textarea.setScroll(scroll);

		element.setBorderLayoutConstraints(BorderLayoutConstraintsEnum.PAGE_END);
		element.setTextArea(textarea);

		actions = new Action[2];
		actions[0] = XmlomUtil.newXmlObjectModelling(Action.class);
		actions[0].setAction(ActionEnum.SUBMIT);
		actions[1] = XmlomUtil.newXmlObjectModelling(Action.class);
		actions[1].setAction(ActionEnum.INPUT);

		element.setActions(actions);
		frame.getData().getLayout().getElements().add(element);
		return frame;
	}

	public final String readLine() throws ClosedLtRtException, GuiLtException, InterruptedException {
		String result = null;
		ActionInput actionInput;
		while (result == null) {
			actionInput = pollActionInput();
			if (actionInput.getValues() != null) {
				for (final Value value : actionInput.getValues()) {
					if (ActionEnum.INPUT.equals(value.getAction()) && INPUT_TEXT_ID.equals(value.getElementId())) {
						result = value.getTextInput();
					}
				}
			}
		}
		fastUpdate(this.textAreaUpdate);
		return result;
	}

}
