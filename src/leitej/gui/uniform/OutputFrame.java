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
import leitej.gui.uniform.model.Area;
import leitej.gui.uniform.model.Background;
import leitej.gui.uniform.model.BorderLayoutConstraintsEnum;
import leitej.gui.uniform.model.Data;
import leitej.gui.uniform.model.Element;
import leitej.gui.uniform.model.ElementUpdate;
import leitej.gui.uniform.model.Font;
import leitej.gui.uniform.model.FontNameEnum;
import leitej.gui.uniform.model.FontStyleEnum;
import leitej.gui.uniform.model.Frame;
import leitej.gui.uniform.model.Layout;
import leitej.gui.uniform.model.LayoutEnum;
import leitej.gui.uniform.model.MetaData;
import leitej.gui.uniform.model.Scroll;
import leitej.gui.uniform.model.ScrollPolicyEnum;
import leitej.gui.uniform.model.Style;
import leitej.gui.uniform.model.TextArea;
import leitej.gui.uniform.model.TextAreaReplaceRange;
import leitej.gui.uniform.model.TextAreaUpdate;
import leitej.gui.util.StyleUtil;
import leitej.util.ColorEnum;
import leitej.xml.om.Xmlom;

/**
 *
 * @author Julio Leite
 */
public class OutputFrame extends UniformFrame {

	private static final String OUTPUT_TEXT_ID = "OUTPUT";
	private static final int DEFAULT_FONT_SIZE = 12;

	private static final int ROW_SIZE = 400; // 400 chars ?
	private static final int IDEAL_TEXT_CAPACITY = ROW_SIZE * 1000; // 1000 rows ?
	private static final int EXCESS_CAPACITY_TEXT = ROW_SIZE * 200; // 200 rows ?
	private static final int MAX_CAPACITY_TEXT = IDEAL_TEXT_CAPACITY + EXCESS_CAPACITY_TEXT;

	private static final int BUFFER_SIZE = ROW_SIZE * 20; // 20 rows ?

	private final StringBuilder buffer = new StringBuilder(BUFFER_SIZE);

	private int textCharCount;
	private final ElementUpdate textAreaUpdate;
	private final TextAreaReplaceRange textAreaReplaceRange;

	public OutputFrame() throws GuiLtException, InterruptedException {
		this(null, null, DEFAULT_FONT_SIZE, null, null);
	}

	public OutputFrame(final String title) throws GuiLtException, InterruptedException {
		this(title, null, DEFAULT_FONT_SIZE, null, null);
	}

	public OutputFrame(final String title, final byte[] iconImage, final int fontSize,
			final CloseOperationEnum closeOperation, final GraphicsConfiguration gc)
			throws GuiLtException, InterruptedException {
		super(null, closeOperation, gc);
		final Frame frame = getFrame(title, iconImage, fontSize);
		this.set(frame);
		this.textAreaUpdate = Xmlom.newInstance(ElementUpdate.class);
		this.textAreaUpdate.setId(OUTPUT_TEXT_ID);
		final TextAreaUpdate tmp = Xmlom.newInstance(TextAreaUpdate.class);
		this.textAreaUpdate.setTextAreaUpdate(tmp);
		this.textAreaReplaceRange = Xmlom.newInstance(TextAreaReplaceRange.class);
		this.textAreaReplaceRange.setText("");
		this.textAreaReplaceRange.setStart(0);
		cls();
	}

	protected Frame getFrame(final String title, final byte[] iconImage, final int fontSize) {
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
		element.setId(OUTPUT_TEXT_ID);

		final Style style = Xmlom.newInstance(Style.class);
		final Font font = Xmlom.newInstance(Font.class);
		font.setName(FontNameEnum.MONOSPACED);
		font.setStyle(FontStyleEnum.PLAIN);
		font.setSize(fontSize);
		style.setFont(font);
		final Background background = Xmlom.newInstance(Background.class);
		background.setTransparent(false);
		background.setColor(StyleUtil.newColor(ColorEnum.DIM_GRAY));
		style.setBackground(background);
		style.setForegroundColor(StyleUtil.newColor(ColorEnum.CYAN));
		element.setStyle(style);

		final TextArea textarea = Xmlom.newInstance(TextArea.class);
		final Area area = Xmlom.newInstance(Area.class);
		area.setRows(16);
		area.setColumns(120);
		textarea.setArea(area);
		textarea.setEditable(false);
		final Scroll scroll = Xmlom.newInstance(Scroll.class);
		scroll.setVertical(ScrollPolicyEnum.ALWAYS);
		scroll.setHorizontal(ScrollPolicyEnum.AS_NEEDED);
		textarea.setScroll(scroll);

		element.setBorderLayoutConstraints(BorderLayoutConstraintsEnum.CENTER);
		element.setTextArea(textarea);

		elementList.add(element);
		layout.setElements(elementList);
		data.setLayout(layout);
		frame.setData(data);
		return frame;
	}

	public synchronized final void print(final String txt) throws GuiLtException, InterruptedException {
		this.buffer.append(txt);
		if (this.buffer.length() > BUFFER_SIZE) {
			flush();
		}
	}

	public synchronized final void flush() throws GuiLtException, InterruptedException {
		final String textToAdd = this.buffer.toString();
		this.buffer.setLength(0);
		this.textCharCount += textToAdd.length();
		if (this.textCharCount > MAX_CAPACITY_TEXT) {
			final int charsToRemoveCount = this.textCharCount - IDEAL_TEXT_CAPACITY;
			this.textAreaReplaceRange.setEnd(charsToRemoveCount);
			this.textAreaUpdate.getTextAreaUpdate().setTextAreaReplaceRange(this.textAreaReplaceRange);
			this.textCharCount -= charsToRemoveCount;
			this.textAreaUpdate.getTextAreaUpdate().setCaretPosition(this.textCharCount);
		} else {
			this.textAreaUpdate.getTextAreaUpdate().setTextAreaReplaceRange(null);
			this.textAreaUpdate.getTextAreaUpdate().setAppend(textToAdd);
			this.textAreaUpdate.getTextAreaUpdate().setCaretPosition(this.textCharCount);
		}
		fastUpdate(this.textAreaUpdate);
	}

	public synchronized final void cls() throws GuiLtException, InterruptedException {
		this.textAreaUpdate.getTextAreaUpdate().setText(null);
		this.textAreaUpdate.getTextAreaUpdate().setAppend(null);
		this.textAreaUpdate.getTextAreaUpdate().setTextAreaReplaceRange(null);
		this.buffer.setLength(0);
		this.textCharCount = 0;
		this.textAreaUpdate.getTextAreaUpdate().setCaretPosition(this.textCharCount);
		fastUpdate(this.textAreaUpdate);
	}

}
