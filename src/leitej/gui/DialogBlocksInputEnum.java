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

import java.awt.Dialog;

/**
 *
 * @author Julio Leite
 */
enum DialogBlocksInputEnum {
	/**
	 * <code>MODELESS</code> dialog doesn't block any top-level windows.
	 */
	MODELESS,
	/**
	 * A <code>DOCUMENT_MODAL</code> dialog blocks input to all top-level windows
	 * from the same document except those from its own child hierarchy. A document
	 * is a top-level window without an owner. It may contain child windows that,
	 * together with the top-level window are treated as a single solid document.
	 * Since every top-level window must belong to some document, its root can be
	 * found as the top-nearest window without an owner.
	 */
	DOCUMENT_MODAL,
	/**
	 * An <code>APPLICATION_MODAL</code> dialog blocks all top-level windows from
	 * the same Java application except those from its own child hierarchy. If there
	 * are several applets launched in a browser, they can be treated either as
	 * separate applications or a single one. This behavior is
	 * implementation-dependent.
	 */
	APPLICATION_MODAL,
	/**
	 * A <code>TOOLKIT_MODAL</code> dialog blocks all top-level windows run from the
	 * same toolkit except those from its own child hierarchy. If there are several
	 * applets launched in a browser, all of them run with the same toolkit; thus, a
	 * toolkit-modal dialog displayed by an applet may affect other applets and all
	 * windows of the browser instance which embeds the Java runtime environment for
	 * this toolkit. Special <code>AWTPermission</code> "toolkitModality" must be
	 * granted to use toolkit-modal dialogs. If a <code>TOOLKIT_MODAL</code> dialog
	 * is being created and this permission is not granted, a
	 * <code>SecurityException</code> will be thrown, and no dialog will be created.
	 * If a modality type is being changed to <code>TOOLKIT_MODAL</code> and this
	 * permission is not granted, a <code>SecurityException</code> will be thrown,
	 * and the modality type will be left unchanged.
	 */
	TOOLKIT_MODAL;

	Dialog.ModalityType modalityType() {
		switch (this) {
		case MODELESS:
			return Dialog.ModalityType.MODELESS;
		case DOCUMENT_MODAL:
			return Dialog.ModalityType.DOCUMENT_MODAL;
		case APPLICATION_MODAL:
			return Dialog.ModalityType.APPLICATION_MODAL;
		case TOOLKIT_MODAL:
			return Dialog.ModalityType.TOOLKIT_MODAL;
		default:
			throw new IllegalStateException(this.toString());
		}
	}
}
