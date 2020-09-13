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

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import leitej.exception.ImplementationLtRtException;
import leitej.exception.UniformGuiLtRtException;
import leitej.gui.LtPictureComponent;
import leitej.gui.event.LtFocusListener;
import leitej.gui.event.LtKeyListener;
import leitej.gui.event.LtMouseListener;
import leitej.gui.uniform.model.Action;
import leitej.gui.uniform.model.ActionEnum;
import leitej.gui.uniform.model.ActionInput;
import leitej.gui.uniform.model.Data;
import leitej.gui.uniform.model.Element;
import leitej.gui.uniform.model.ElementUpdate;
import leitej.gui.uniform.model.Layout;
import leitej.gui.uniform.model.Point;
import leitej.gui.uniform.model.Value;
import leitej.gui.util.ImageUtil;
import leitej.util.AgnosticUtil;
import leitej.util.data.InvokeSignature;
import leitej.util.data.XmlomUtil;

/**
 *
 * @author Julio Leite
 */
final class UniformGuiBus {

	/*
	 * Set
	 */

	static void set(final InsetData insetData, final Data data) throws UniformGuiLtRtException {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new ImplementationLtRtException();
		}
		if (data != null) {
			insetData.getComponentMapById().clear();
			insetData.getOperationGroupMapByIndexAndId().clear();
			insetData.getOperationGroupMapByComponent().clear();
			JPanel.class.cast(insetData.mainComponent())
					.add(newPanel(new CascadeStyle(data.getStyle()), data.getLayout(), insetData), BorderLayout.CENTER);
		}
	}

	private static JComponent newPanel(final CascadeStyle cascadeStyle, final Layout layout, final InsetData insetData)
			throws UniformGuiLtRtException {
		JComponent result = new JPanel();
		if (layout != null) {
			if (layout.getType() != null) {
				switch (layout.getType()) {
				case FLOW:
					// FlowLayout is the default layout manager for every JPanel
//					result.setLayout(new FlowLayout());
					break;
				case BORDER:
					result.setLayout(new BorderLayout());
					break;
				default:
					throw new ImplementationLtRtException();
				}
			}
			if (layout.getElements() != null) {
				for (final Element element : layout.getElements()) {
					if (layout.getType() == null) {
						result.add(newComponent(cascadeStyle.clone(), element, insetData));
					} else {
						switch (layout.getType()) {
						case BORDER:
							result.add(newComponent(cascadeStyle.clone(), element, insetData),
									((element.getBorderLayoutConstraints() == null) ? BorderLayout.CENTER
											: element.getBorderLayoutConstraints().getConstraints()));
							break;
						default:
							result.add(newComponent(cascadeStyle.clone(), element, insetData));
						}
					}
				}
			}
			if (layout.getScroll() != null) {
				int vsbPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
				int hsbPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
				if (layout.getScroll().getVertical() != null) {
					vsbPolicy = layout.getScroll().getVertical().getVerticalPolicy();
				}
				if (layout.getScroll().getHorizontal() != null) {
					hsbPolicy = layout.getScroll().getHorizontal().getHorizontalPolicy();
				}
				result = new JScrollPane(result, vsbPolicy, hsbPolicy);
			}
		}
		return result;
	}

	private static JComponent newComponent(final CascadeStyle cascadeStyle, final Element element,
			final InsetData insetData) throws UniformGuiLtRtException {
		cascadeStyle.merge(element.getStyle());
		JComponent result;
		JComponent component;
		if (element.getLayout() == null) {
			if (element.getTextArea() != null) {
				if (element.getTextArea().getArea() == null) {
					component = new JTextArea(element.getTextArea().getText());
				} else {
					component = new JTextArea(element.getTextArea().getText(),
							element.getTextArea().getArea().getRows(), element.getTextArea().getArea().getColumns());
				}
				JTextArea.class.cast(component).setEditable(element.getTextArea().isEditable());
				if (element.getTextArea().getScroll() != null) {
					int vsbPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
					int hsbPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
					if (element.getTextArea().getScroll().getVertical() != null) {
						vsbPolicy = element.getTextArea().getScroll().getVertical().getVerticalPolicy();
					}
					if (element.getTextArea().getScroll().getHorizontal() != null) {
						hsbPolicy = element.getTextArea().getScroll().getHorizontal().getHorizontalPolicy();
					}
					result = new JScrollPane(component, vsbPolicy, hsbPolicy);
				} else {
					result = component;
				}
				applyStyle(cascadeStyle, JTextArea.class.cast(component));
			} else if (element.getPasswordField() != null) {
				if (element.getPasswordField().getColumns() == null) {
					component = new JPasswordField(element.getPasswordField().getText());
				} else {
					component = new JPasswordField(element.getPasswordField().getText(),
							element.getPasswordField().getColumns());
				}
				result = component;
				applyStyle(cascadeStyle, JPasswordField.class.cast(component));
			} else if (element.getImage() != null) {
				component = new LtPictureComponent();
				if (element.getImage().getDimension() != null) {
					LtPictureComponent.class.cast(component).setSize(element.getImage().getDimension().getX(),
							element.getImage().getDimension().getY());
				}
				if (element.getImage().getDataPosition() != null && element.getImage().getData() != null) {
					LtPictureComponent.class.cast(component).setPicture(element.getImage().getDataPosition().getX(),
							element.getImage().getDataPosition().getY(),
							ImageUtil.decode(element.getImage().getData()));
				} else {
					if (element.getImage().getData() != null) {
						LtPictureComponent.class.cast(component)
								.setPicture(ImageUtil.decode(element.getImage().getData()));
					}
				}
				if (element.getImage().getScroll() != null) {
					int vsbPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
					int hsbPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
					if (element.getImage().getScroll().getVertical() != null) {
						vsbPolicy = element.getImage().getScroll().getVertical().getVerticalPolicy();
					}
					if (element.getImage().getScroll().getHorizontal() != null) {
						hsbPolicy = element.getImage().getScroll().getHorizontal().getHorizontalPolicy();
					}
					result = new JScrollPane(component, vsbPolicy, hsbPolicy);
				} else {
					result = component;
				}
				applyStyle(cascadeStyle, LtPictureComponent.class.cast(component));
			} else {
				throw new UniformGuiLtRtException();
			}
		} else {
			component = newPanel(cascadeStyle, element.getLayout(), insetData);
			result = component;
			applyStyle(cascadeStyle, JPanel.class.cast(result));
		}
		if (element.getId() != null) {
			if (insetData.getComponentMapById().put(element.getId(), component) != null) {
				throw new UniformGuiLtRtException();
			}
			registryActions(element.getActions(), component, element.getId(), insetData);
		}
		applyTips(element.getToolTipText(), element.getAccessibleName(), element.getAccessibleDescription(), component);
		return result;
	}

	private static void registryActions(final Action[] actions, final JComponent component, final String elementId,
			final InsetData insetData) throws UniformGuiLtRtException {
		if (actions != null && actions.length != 0) {
			Map<String, OperationGroup> indexedMap;
			OperationGroup operationGroup = null;
			boolean addActionListener = false;
			boolean focusForward = false;
			for (final Action action : actions) {
				if (action.getAction() != null) {
					operationGroup = insetData.getOperationGroupMapByComponent().get(component);
					if (operationGroup == null) {
						operationGroup = new OperationGroup(elementId, component);
						insetData.getOperationGroupMapByComponent().put(component, operationGroup);
						indexedMap = insetData.getOperationGroupMapByIndexAndId().get(action.getIndex());
						if (indexedMap == null) {
							indexedMap = new HashMap<>();
							insetData.getOperationGroupMapByIndexAndId().put(action.getIndex(), indexedMap);
						}
						indexedMap.put(elementId, operationGroup);
					}
					operationGroup.getOperationList()
							.add(new Operation(action.getIndex(), action.getAction(), action.getArguments()));
					switch (action.getAction()) {
					case SUBMIT:
						addActionListener = true;
						break;
					case INPUT:
					case INPUT_CONSTANT:
						break;
					case FOCUS_FORWARD:
						if (focusForward) {
							throw new UniformGuiLtRtException();
						}
						if (action.getArguments() == null || action.getArguments()[0] == null) {
							throw new UniformGuiLtRtException();
						}
						focusForward = true;
						break;
					default:
						throw new ImplementationLtRtException();
					}
				}
			}
			if (addActionListener) {
				if (JTextArea.class.isInstance(component) || JPasswordField.class.isInstance(component)) {
					try {
						component.addKeyListener(new LtKeyListener(
								new InvokeSignature(insetData,
										AgnosticUtil.getMethod(insetData,
												InsetData.METHOD_NAME_ENTER_KEY_TYPED_LISTENER, KeyEvent.class)),
								true));
					} catch (final NoSuchMethodException e) {
						new ImplementationLtRtException(e);
					}
				} else if (LtPictureComponent.class.isInstance(component)) {
					try {
						component.addMouseListener(
								new LtMouseListener(new InvokeSignature(insetData, AgnosticUtil.getMethod(insetData,
										InsetData.METHOD_NAME_MOUSE_CLICKED_LISTENER, MouseEvent.class))));
					} catch (final NoSuchMethodException e) {
						new ImplementationLtRtException(e);
					}
				} else if (JPanel.class.isInstance(component)) {
					throw new UniformGuiLtRtException();
				} else {
					throw new ImplementationLtRtException();
				}
			}
			if (focusForward) {
				if (JTextArea.class.isInstance(component) || LtPictureComponent.class.isInstance(component)
						|| JPanel.class.isInstance(component)) {
					try {
						component
								.addFocusListener(new LtFocusListener(
										new InvokeSignature(insetData,
												AgnosticUtil.getMethod(insetData,
														InsetData.METHOD_NAME_FOCUS_GAINED_LISTENER, FocusEvent.class)),
										null));
					} catch (final NoSuchMethodException e) {
						new ImplementationLtRtException(e);
					}
				} else {
					throw new ImplementationLtRtException();
				}
			}
		}
	}

	private static void applyTips(final String toolTipText, final String accessibleName,
			final String accessibleDescription, final JComponent component) {
		component.setToolTipText(toolTipText);
		component.getAccessibleContext().setAccessibleName(accessibleName);
		component.getAccessibleContext().setAccessibleDescription(accessibleDescription);
	}

	private static void applyStyle(final CascadeStyle cascadeStyle, final JPanel panel) {
		// TODO:
	}

	private static void applyStyle(final CascadeStyle cascadeStyle, final JTextArea textArea) {
		if (cascadeStyle.getBorder() != null) {
			textArea.setBorder(cascadeStyle.getBorder());
		}
		if (cascadeStyle.getFont() != null) {
			textArea.setFont(cascadeStyle.getFont());
		}
		textArea.setOpaque(!cascadeStyle.isBackgroundTransparent());
		if (cascadeStyle.getBackgroundColor() != null) {
			textArea.setBackground(cascadeStyle.getBackgroundColor());
		}
		if (cascadeStyle.getForegroundColor() != null) {
			textArea.setForeground(cascadeStyle.getForegroundColor());
			textArea.setCaretColor(cascadeStyle.getForegroundColor());
		}
	}

	private static void applyStyle(final CascadeStyle cascadeStyle, final JPasswordField passwordField) {
		if (cascadeStyle.getBorder() != null) {
			passwordField.setBorder(cascadeStyle.getBorder());
		}
		if (cascadeStyle.getFont() != null) {
			passwordField.setFont(cascadeStyle.getFont());
		}
		passwordField.setOpaque(!cascadeStyle.isBackgroundTransparent());
		if (cascadeStyle.getBackgroundColor() != null) {
			passwordField.setBackground(cascadeStyle.getBackgroundColor());
		}
		if (cascadeStyle.getForegroundColor() != null) {
			passwordField.setForeground(cascadeStyle.getForegroundColor());
			passwordField.setCaretColor(cascadeStyle.getForegroundColor());
		}
	}

	private static void applyStyle(final CascadeStyle cascadeStyle, final LtPictureComponent picture) {
		// TODO:
	}

	/*
	 * Update
	 */

	static void update(final ElementUpdate element, final Map<String, JComponent> elementMap)
			throws UniformGuiLtRtException {
		if (element.getId() == null) {
			throw new NullPointerException();
		}
		final JComponent component = elementMap.get(element.getId());
		if (component == null) {
			throw new UniformGuiLtRtException();
		}
		if (JPanel.class.isInstance(component)) {
			if (element.getStyle() != null) {
				applyStyle(new CascadeStyle(element.getStyle()), JPanel.class.cast(component));
			}
		} else if (JTextArea.class.isInstance(component)) {
			if (element.getTextAreaUpdate() != null) {
				if (element.getTextAreaUpdate().getAppend() == null) {
					JTextArea.class.cast(component).setText(element.getTextAreaUpdate().getText());
				} else {
					JTextArea.class.cast(component).append(element.getTextAreaUpdate().getAppend());
				}
				if (element.getTextAreaUpdate().getTextAreaReplaceRange() != null) {
					JTextArea.class.cast(component).replaceRange(
							element.getTextAreaUpdate().getTextAreaReplaceRange().getText(),
							element.getTextAreaUpdate().getTextAreaReplaceRange().getStart(),
							element.getTextAreaUpdate().getTextAreaReplaceRange().getEnd());
				}
				if (element.getTextAreaUpdate().getCaretPosition() != null) {
					JTextArea.class.cast(component)
							.setCaretPosition(element.getTextAreaUpdate().getCaretPosition().intValue());
				}
				if (element.getTextAreaUpdate().getEditable() != null) {
					JTextArea.class.cast(component)
							.setEditable(element.getTextAreaUpdate().getEditable().booleanValue());
				}
				if (element.getStyle() != null) {
					applyStyle(new CascadeStyle(element.getStyle()), JTextArea.class.cast(component));
				}
			} else {
				throw new UniformGuiLtRtException();
			}
		} else if (LtPictureComponent.class.isInstance(component)) {
			if (element.getImageUpdate() != null) {
				if (element.getImageUpdate().getDataPosition() != null) {
					LtPictureComponent.class.cast(component).setPicture(
							element.getImageUpdate().getDataPosition().getX(),
							element.getImageUpdate().getDataPosition().getY(),
							ImageUtil.decode(element.getImageUpdate().getData()));
				} else {
					LtPictureComponent.class.cast(component)
							.setPicture(ImageUtil.decode(element.getImageUpdate().getData()));
				}
				if (element.getStyle() != null) {
					applyStyle(new CascadeStyle(element.getStyle()), LtPictureComponent.class.cast(component));
				}
			} else {
				throw new UniformGuiLtRtException();
			}
		} else {
			throw new ImplementationLtRtException();
		}
		applyTips(element.getToolTipText(), element.getAccessibleName(), element.getAccessibleDescription(), component);
	}

	/*
	 * Listener
	 */

	static void enterKeyTypedListener(final KeyEvent keyEvent, final InsetData insetData)
			throws UniformGuiLtRtException {
		eventListener(keyEvent, insetData);
	}

	static void mouseClickedListener(final MouseEvent mouseEvent, final InsetData insetData)
			throws UniformGuiLtRtException {
		eventListener(mouseEvent, insetData);
	}

	private static void eventListener(final InputEvent event, final InsetData insetData)
			throws UniformGuiLtRtException {
		final ActionInput actionInput = XmlomUtil.newXmlObjectModelling(ActionInput.class);
		try {
			final List<Value> valueList = new ArrayList<>();
			final OperationGroup operationGroup = insetData.getOperationGroupMapByComponent().get(event.getSource());
			if (operationGroup.getOperationList() != null) {
				for (final Operation operation : operationGroup.getOperationList()) {
					switch (operation.getAction()) {
					case SUBMIT:
						valueList.add(
								newValue(operation.getIndex(), operationGroup.getElementId(), operation.getAction()));
						fillInput(event, operation.getIndex(), valueList, insetData);
						break;
					case INPUT:
					case INPUT_CONSTANT:
					case FOCUS_FORWARD:
						break;
					default:
						throw new ImplementationLtRtException();
					}
				}
			}
			actionInput.setValues(valueList);
		} finally {
			try {
				insetData.addInput(actionInput);
			} catch (final InterruptedException e) {
				new UniformGuiLtRtException(e);
			}
		}
	}

	private static void fillInput(final InputEvent event, final String index, final List<Value> valueList,
			final InsetData insetData) {
		final Map<String, OperationGroup> inputElementMap = insetData.getOperationGroupMapByIndexAndId().get(index);
		if (inputElementMap != null) {
			Value value;
			for (final OperationGroup operationGroup : inputElementMap.values()) {
				if (operationGroup.getOperationList() != null) {
					for (final Operation operation : operationGroup.getOperationList()) {
						switch (operation.getAction()) {
						case SUBMIT:
							break;
						case INPUT:
							value = newValue(index, operationGroup.getElementId(), operation.getAction());
							fillValue(event, value, operationGroup.getComponent());
							valueList.add(value);
							break;
						case INPUT_CONSTANT:
							value = newValue(index, operationGroup.getElementId(), operation.getAction());
							value.setConstants(operation.getArguments());
							valueList.add(value);
							break;
						case FOCUS_FORWARD:
							break;
						default:
							throw new ImplementationLtRtException();
						}
					}
				}
			}
		}
	}

	private static void fillValue(final InputEvent event, final Value value, final JComponent component) {
		if (JPanel.class.isInstance(component)) {
			/* ignored */
		} else if (JTextArea.class.isInstance(component)) {
			value.setTextInput(JTextArea.class.cast(component).getText());
		} else if (JPasswordField.class.isInstance(component)) {
			value.setPasswordInput(JPasswordField.class.cast(component).getPassword());
		} else if (LtPictureComponent.class.isInstance(component)) {
			if (MouseEvent.class.isInstance(event)) {
				final Point point = XmlomUtil.newXmlObjectModelling(Point.class);
				point.setX(MouseEvent.class.cast(event).getPoint().x);
				point.setY(MouseEvent.class.cast(event).getPoint().y);
				value.setPointInput(point);
			} else {
				/* ignored */
			}
		} else {
			throw new ImplementationLtRtException();
		}
	}

	private static Value newValue(final String index, final String elementId, final ActionEnum action) {
		final Value value = XmlomUtil.newXmlObjectModelling(Value.class);
		value.setIndex(index);
		value.setElementId(elementId);
		value.setAction(action);
		return value;
	}

	static void focusGainedListener(final FocusEvent focusEvent, final InsetData insetData) {
		final OperationGroup operationGroup = insetData.getOperationGroupMapByComponent().get(focusEvent.getSource());
		if (operationGroup.getOperationList() != null) {
			for (final Operation operation : operationGroup.getOperationList()) {
				switch (operation.getAction()) {
				case SUBMIT:
				case INPUT:
				case INPUT_CONSTANT:
					break;
				case FOCUS_FORWARD:
					insetData.getComponentMapById().get(operation.getArguments()[0]).requestFocusInWindow();
					break;
				default:
					throw new ImplementationLtRtException();
				}
			}
		}
	}

}
