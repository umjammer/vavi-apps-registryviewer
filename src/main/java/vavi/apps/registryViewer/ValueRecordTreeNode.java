/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.registryViewer;

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import vavi.util.win32.registry.Registry;


/**
 * The tree node for {@link vavi.util.win32.registry.Registry.TreeRecord}.
 * 
 * <pre><tt>
 * 
 *   Registry.TreeRecord tr = ... // gets current
 * 
 *   if (tr.hasChildTreeRecords()) { // has child or not
 * 	   tr = tr.get1stChildTreeRecord(); // get first child and move the point to
 * 	   ...
 * 
 * 	   while (tr.hasNextTreeRecord()) { // has next or not
 * 	     tr = tr.getNextTreeRecord(); // get next child and move the point to
 * 	     ...
 * 	   }
 *   }
 *  
 * </tt></pre>
 * 
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 */
public class ValueRecordTreeNode extends DefaultMutableTreeNode {

    /** Have the children of this node been loaded yet? */
    private boolean hasLoaded;

    /** */
    private Registry registry;

    /**
     * Constructs a new RegistryViewerTreeNode instance with o as the user object.
     */
    public ValueRecordTreeNode(Registry registry, Object userObject) {
        super(userObject);
        this.registry = registry;
    }

    /**
     * If hasLoaded is false, meaning the children have not yet been loaded,
     * loadChildren is messaged and super is messaged for the return value.
     */
    public int getChildCount() {
        if (!hasLoaded) {
            try {
                loadChildren();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return super.getChildCount();
    }

    /** Returns data contains str. */
    public boolean contains(String str) {
        if (((Registry.TreeRecord) userObject).toString().toLowerCase().contains(str.toLowerCase()))
            return true;
// Debug.println(userObject.getClass().getName());

        if (model == null) {
            return false;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            // Debug.println(value.getValueAt(i, 1).getClass().getName());
            if (((String) model.getValueAt(i, 1)).toLowerCase().contains(str.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /** */
    private ValueRecordTableModel model;

    /** */
    public void setValueRecordTableModel(ValueRecordTableModel model) {
        this.model = model;
    }

    /** Returns fqdn. */
    public String getAbsoluteName() {
        TreeNode[] nodes = getPath();
        StringBuilder name = new StringBuilder();

        for (TreeNode node : nodes) {
            name.append(((ValueRecordTreeNode) node).getUserObject().toString()).append("\\");
        }

        return name.substring(0, name.length() - 1);
    }

    /**
     * Messaged the first time getChildCount is messaged. Creates children with random names from names.
     */
    protected void loadChildren() throws IOException {
        ValueRecordTreeNode node;
        Registry.TreeRecord tr = (Registry.TreeRecord) userObject;

        if (tr.hasChildTreeRecords()) {

            int i = 0;
            tr = registry.get1stChildTreeRecord(tr);
            node = new ValueRecordTreeNode(registry, tr);
            insert(node, i);
            i++;

            while (tr.hasNextTreeRecord()) {
                tr = registry.getNextTreeRecord(tr);
                node = new ValueRecordTreeNode(registry, tr);

                // Don't use add() here, add calls insert(newNode, getChildCount()) so if you want to use add,
                // just be sure to set hasLoaded = true first.

                insert(node, i);
                i++;
            }
        }

        // This node has now been loaded, mark it so.
        hasLoaded = true;
    }
}

/* */
