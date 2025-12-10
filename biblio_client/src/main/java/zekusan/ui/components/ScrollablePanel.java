package zekusan.ui.components;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;

public class ScrollablePanel extends JPanel implements Scrollable {
    private static final long serialVersionUID = 1L;

    public ScrollablePanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(64, visibleRect.height - 64);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport viewport) {
            return getPreferredSize().height < viewport.getHeight();
        }
        return false;
    }
}
