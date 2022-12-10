/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.registryViewer;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * The cell renderer for the table.
 * 
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 */
public class ValueRecordTableCellRenderer extends DefaultTableCellRenderer {

    /**
     * @param value should be {@link JLabel}
     */
    public void setValue(Object value) {
        /* Set the text. */
        setText(((JLabel) value).getText());
        /* Tooltips used by the table. */
        setToolTipText(((JLabel) value).getText());

        setIcon(((JLabel) value).getIcon());
    }
}

/* */
