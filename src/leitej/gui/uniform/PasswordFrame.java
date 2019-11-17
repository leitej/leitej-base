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
import java.util.ArrayList;
import java.util.List;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.gui.exception.GuiLtException;
import leitej.gui.uniform.model.Action;
import leitej.gui.uniform.model.ActionEnum;
import leitej.gui.uniform.model.ActionInput;
import leitej.gui.uniform.model.Background;
import leitej.gui.uniform.model.Border;
import leitej.gui.uniform.model.BorderLayoutConstraintsEnum;
import leitej.gui.uniform.model.BorderThickness;
import leitej.gui.uniform.model.BorderTitle;
import leitej.gui.uniform.model.BorderTitlePositionEnum;
import leitej.gui.uniform.model.BorderTypeEnum;
import leitej.gui.uniform.model.Data;
import leitej.gui.uniform.model.Element;
import leitej.gui.uniform.model.Font;
import leitej.gui.uniform.model.FontNameEnum;
import leitej.gui.uniform.model.FontStyleEnum;
import leitej.gui.uniform.model.Frame;
import leitej.gui.uniform.model.Layout;
import leitej.gui.uniform.model.LayoutEnum;
import leitej.gui.uniform.model.MetaData;
import leitej.gui.uniform.model.PasswordField;
import leitej.gui.uniform.model.Style;
import leitej.gui.uniform.model.Value;
import leitej.gui.util.GraphicsUtil;
import leitej.gui.util.StyleUtil;
import leitej.util.ColorEnum;
import leitej.util.machine.ConsoleUtil;
import leitej.xml.om.XmlomIOStream;

/**
 *
 * @author Julio Leite
 */
public final class PasswordFrame extends UniformFrame {

	private static final String PASSWORD_FIELD_ID = "PWD_ID";

	public static final char[] readPassword(final String title, final String prompt, final boolean prefereGraphic)
			throws GuiLtException, InterruptedException {
		if (ConsoleUtil.hasConsole() && !prefereGraphic || ConsoleUtil.hasConsole() && GraphicsUtil.isSupported()) {
			// password read from console
			if (title != null) {
				ConsoleUtil.write(title);
			}
			if (prompt != null) {
				return ConsoleUtil.readPassword(prompt);
			} else {
				return ConsoleUtil.readPassword();
			}
		} else if (GraphicsUtil.isSupported()) {
			// password read from GUI
			return (new PasswordFrame(null, null, title, prompt)).readPassword();
		} else {
			// cannot read password
			throw new IllegalStateLtRtException();
		}
	}

	private PasswordFrame(final java.awt.Window owner, final GraphicsConfiguration gc, final String title,
			final String prompt) throws GuiLtException, InterruptedException {
		super(null, CloseOperationEnum.DISPOSE_ON_CLOSE, gc);
		final Frame frame = getFrame(title, prompt);
		this.set(frame);
		pack();
		unhide();
	}

	protected Frame getFrame(final String title, final String prompt) {
		final Frame frame = XmlomIOStream.newXmlObjectModelling(Frame.class);
		final MetaData metaData = XmlomIOStream.newXmlObjectModelling(MetaData.class);
		metaData.setTitle(title);
		frame.setMetaData(metaData);
		final Data data = XmlomIOStream.newXmlObjectModelling(Data.class);
		final Layout layout = XmlomIOStream.newXmlObjectModelling(Layout.class);
		layout.setType(LayoutEnum.BORDER);
		final List<Element> elementList = new ArrayList<>();

		final Element element = XmlomIOStream.newXmlObjectModelling(Element.class);
		element.setId(PASSWORD_FIELD_ID);
		final Style style = XmlomIOStream.newXmlObjectModelling(Style.class);
		final Border border = XmlomIOStream.newXmlObjectModelling(Border.class);
		border.setType(BorderTypeEnum.LINE);
		final BorderThickness thickness = XmlomIOStream.newXmlObjectModelling(BorderThickness.class);
		thickness.setAll(2);
		border.setThickness(thickness);
		border.setColor(StyleUtil.newColor(ColorEnum.ORANGE));
		if (prompt != null) {
			final BorderTitle borderTitle = XmlomIOStream.newXmlObjectModelling(BorderTitle.class);
			borderTitle.setTitle(prompt);
			borderTitle.setPosition(BorderTitlePositionEnum.TOP);
			borderTitle.setColor(StyleUtil.newColor(ColorEnum.LIGHT_GRAY));
			border.setTitle(borderTitle);
		}
		style.setBorder(border);
		final Font font = XmlomIOStream.newXmlObjectModelling(Font.class);
		font.setName(FontNameEnum.MONOSPACED);
		font.setStyle(FontStyleEnum.PLAIN);
		font.setSize(12);
		style.setFont(font);
		final Background background = XmlomIOStream.newXmlObjectModelling(Background.class);
		background.setTransparent(false);
		background.setColor(StyleUtil.newColor(ColorEnum.DIM_GRAY));
		style.setBackground(background);
		style.setForegroundColor(StyleUtil.newColor(ColorEnum.LIGHT_GRAY));
		element.setStyle(style);
		final PasswordField passwordField = XmlomIOStream.newXmlObjectModelling(PasswordField.class);
		passwordField.setColumns(25);
		element.setBorderLayoutConstraints(BorderLayoutConstraintsEnum.CENTER);
		element.setPasswordField(passwordField);

		final Action[] actions = new Action[2];
		actions[0] = XmlomIOStream.newXmlObjectModelling(Action.class);
		actions[0].setAction(ActionEnum.SUBMIT);
		actions[1] = XmlomIOStream.newXmlObjectModelling(Action.class);
		actions[1].setAction(ActionEnum.INPUT);

		element.setActions(actions);
		elementList.add(element);
		layout.setElements(elementList);
		data.setLayout(layout);
		frame.setData(data);
		return frame;
	}

	private final char[] readPassword() throws ClosedLtRtException, GuiLtException, InterruptedException {
		final ActionInput actionInput = pollActionInput();
		char[] result = null;
		for (final Value value : actionInput.getValues()) {
			result = value.getPasswordInput();
		}
		close();
		if (result == null) {
			throw new ImplementationLtRtException();
		}
		return result;
	}

}
