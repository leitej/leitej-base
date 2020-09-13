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

import leitej.exception.GuiLtException;
import leitej.exception.NotifyGuiLtRtException;
import leitej.gui.uniform.model.Background;
import leitej.gui.uniform.model.Border;
import leitej.gui.uniform.model.BorderLayoutConstraintsEnum;
import leitej.gui.uniform.model.BorderThickness;
import leitej.gui.uniform.model.BorderTypeEnum;
import leitej.gui.uniform.model.Data;
import leitej.gui.uniform.model.Element;
import leitej.gui.uniform.model.ElementUpdate;
import leitej.gui.uniform.model.Font;
import leitej.gui.uniform.model.FontNameEnum;
import leitej.gui.uniform.model.FontStyleEnum;
import leitej.gui.uniform.model.Layout;
import leitej.gui.uniform.model.LayoutEnum;
import leitej.gui.uniform.model.LocationEnum;
import leitej.gui.uniform.model.Scroll;
import leitej.gui.uniform.model.ScrollPolicyEnum;
import leitej.gui.uniform.model.Style;
import leitej.gui.uniform.model.TextArea;
import leitej.gui.uniform.model.TextAreaUpdate;
import leitej.gui.uniform.model.Window;
import leitej.gui.uniform.model.WindowMetaData;
import leitej.gui.util.StyleUtil;
import leitej.util.ColorEnum;
import leitej.util.data.XmlomUtil;

/**
 *
 * @author Julio Leite
 */
public final class NotifyWindow extends UniformWindow {

	private static final String OUTPUT_TEXT_ID = "OUTPUT";

	private final ElementUpdate textAreaUpdate;

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public NotifyWindow() throws GuiLtException, InterruptedException {
		this(null, null);
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public NotifyWindow(final java.awt.Window owner, final GraphicsConfiguration gc)
			throws InterruptedException, GuiLtException {
		super(null, owner, gc);
		final Window window = getWindow();
		this.set(window);
		this.textAreaUpdate = XmlomUtil.newXmlObjectModelling(ElementUpdate.class);
		this.textAreaUpdate.setId(OUTPUT_TEXT_ID);
		final TextAreaUpdate tmp = XmlomUtil.newXmlObjectModelling(TextAreaUpdate.class);
		this.textAreaUpdate.setTextAreaUpdate(tmp);
	}

	protected Window getWindow() {
		final Window window = XmlomUtil.newXmlObjectModelling(Window.class);
		final WindowMetaData windowMetaData = XmlomUtil.newXmlObjectModelling(WindowMetaData.class);
		windowMetaData.setAlwaysOnTop(true);
		windowMetaData.setLocation(LocationEnum.BOTTOM_RIGHT);
		windowMetaData.setLocationMargin(2);
		window.setWindowMetaData(windowMetaData);
		final Data data = XmlomUtil.newXmlObjectModelling(Data.class);
		final Layout layout = XmlomUtil.newXmlObjectModelling(Layout.class);
		layout.setType(LayoutEnum.BORDER);
		final List<Element> elementList = new ArrayList<>();

		final Element element = XmlomUtil.newXmlObjectModelling(Element.class);
		element.setId(OUTPUT_TEXT_ID);
		final Style style = XmlomUtil.newXmlObjectModelling(Style.class);
		final Border border = XmlomUtil.newXmlObjectModelling(Border.class);
		border.setType(BorderTypeEnum.LINE);
		final BorderThickness thickness = XmlomUtil.newXmlObjectModelling(BorderThickness.class);
		thickness.setAll(2);
		border.setThickness(thickness);
		border.setColor(StyleUtil.newColor(ColorEnum.BLACK));
		style.setBorder(border);
		final Font font = XmlomUtil.newXmlObjectModelling(Font.class);
		font.setName(FontNameEnum.MONOSPACED);
		font.setStyle(FontStyleEnum.PLAIN);
		font.setSize(12);
		style.setFont(font);
		final Background background = XmlomUtil.newXmlObjectModelling(Background.class);
		background.setTransparent(false);
		background.setColor(StyleUtil.newColor(ColorEnum.DIM_GRAY));
		style.setBackground(background);
		style.setForegroundColor(StyleUtil.newColor(ColorEnum.LIGHT_GRAY));
		element.setStyle(style);
		final TextArea textarea = XmlomUtil.newXmlObjectModelling(TextArea.class);
		textarea.setEditable(false);
		final Scroll scroll = XmlomUtil.newXmlObjectModelling(Scroll.class);
		scroll.setVertical(ScrollPolicyEnum.NEVER);
		scroll.setHorizontal(ScrollPolicyEnum.NEVER);
		textarea.setScroll(scroll);
		element.setBorderLayoutConstraints(BorderLayoutConstraintsEnum.CENTER);
		element.setTextArea(textarea);

		elementList.add(element);
		layout.setElements(elementList);
		data.setLayout(layout);
		window.setData(data);
		return window;
//		JPanel panel = new JPanel(new BorderLayout());
//	    panel.setBorder(new LineBorder(Color.BLACK, 1));
//	    jTextArea = new JTextArea();
//    	jTextArea.setEditable(false);
//    	jTextArea.setBackground(Color.DARK_GRAY);
//    	jTextArea.setForeground(Color.LIGHT_GRAY);
		// TODO: implement this listener on uniform
//    	try {
//			jTextArea.addMouseListener(new LtMouseListener(new InvokeSignature(this, AgnosticUtil.getMethod(this, METHOD_HIDE))));
//		} catch (NoSuchMethodException e) {
//			throw new ImplementationLtRtException(e);
//		}
//    	jTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//    	JScrollPane jScrollPane = new JScrollPane(jTextArea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//    	panel.add(jScrollPane, BorderLayout.CENTER);
//		return panel;
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void out(final String txt) throws GuiLtException, InterruptedException {
		if (isDisposed()) {
			throw new NotifyGuiLtRtException();
		}
		this.textAreaUpdate.getTextAreaUpdate().setText(txt);
		update(this.textAreaUpdate);
		pack();
		unhide();
	}

	@Override
	public void hide() throws InterruptedException, GuiLtException {
		if (isDisposed()) {
			throw new NotifyGuiLtRtException();
		}
		super.hide();
	}

	@Override
	public void unhide() throws InterruptedException, GuiLtException {
		if (isDisposed()) {
			throw new NotifyGuiLtRtException();
		}
		super.unhide();
	}

}
