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

package leitej.gui;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;

/**
 *
 * @author Julio Leite
 */
public final class LtPictureComponent extends JComponent implements Accessible {

	private static final long serialVersionUID = 6765211143474035898L;

	private final Point position;
	private BufferedImage buffImage;
	private boolean adjustSize;

	public LtPictureComponent() {
		super();
		this.position = new Point();
	}

	public LtPictureComponent(final BufferedImage screenImage) {
		super();
		this.position = new Point();
		setPicture(screenImage);
	}

	public synchronized final void setPicture(final BufferedImage screenImage) {
		setPicture(0, 0, screenImage, true);
	}

	public synchronized final void setPicture(final int x, final int y, final BufferedImage screenImage) {
		setPicture(x, y, screenImage, false);
	}

	public synchronized final void setPicture(final int x, final int y, final BufferedImage screenImage,
			final boolean adjustSize) {
		if (screenImage != null) {
			this.position.move(x, y);
			this.buffImage = screenImage;
			this.adjustSize = adjustSize;
			repaint();
		}
	}

	@Override
	protected synchronized final void paintComponent(final Graphics g) {
		if (this.buffImage != null) {
			if (this.adjustSize) {
				setSize(this.position.x + this.buffImage.getWidth(), this.position.y + this.buffImage.getHeight());
			}
			g.drawImage(this.buffImage, this.position.x, this.position.y,
					Math.min(this.buffImage.getWidth(), getWidth()), Math.min(this.buffImage.getHeight(), getHeight()),
					this);
		}
	}

//	@Override
//	public synchronized final void update(Graphics g) {
//		paint(g);
//	}

	@Override
	public AccessibleContext getAccessibleContext() {
		if (this.accessibleContext == null) {
			this.accessibleContext = new AccessiblePicture();
		}
		return this.accessibleContext;
	}

	private final class AccessiblePicture extends AccessibleJComponent {
		private static final long serialVersionUID = 295159572883438612L;
	}

}
