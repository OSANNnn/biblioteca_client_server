package zekusan.ui.components;

import java.awt.event.MouseWheelEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public final class ScrollUtil {
	private ScrollUtil() {
	}

	public static void enableEdgeWheelPropagation(JScrollPane inner, JScrollPane outer) {
		if (inner == null || outer == null) {
			return;
		}

		inner.addMouseWheelListener(e -> {
			if (e.isShiftDown()) {
				return;
			}

			JScrollBar verticalBar = inner.getVerticalScrollBar();
			if (verticalBar == null) {
				return;
			}

			BoundedRangeModel model = verticalBar.getModel();
			int value = model.getValue();
			int extent = model.getExtent();
			int min = model.getMinimum();
			int max = model.getMaximum();

			boolean atTop = value <= min;
			boolean atBottom = value + extent >= max;

			int direction = e.getWheelRotation() > 0 ? 1 : -1;
			boolean shouldBubbleUp = (direction < 0 && atTop) || (direction > 0 && atBottom);

			if (shouldBubbleUp) {
				MouseWheelEvent forwarded = new MouseWheelEvent(
						outer,
						e.getID(),
						e.getWhen(),
						e.getModifiersEx(),
						e.getX(),
						e.getY(),
						e.getClickCount(),
						e.isPopupTrigger(),
						e.getScrollType(),
						e.getScrollAmount(),
						e.getWheelRotation());

				outer.dispatchEvent(forwarded);
				e.consume();
			}
		});

	}
}
