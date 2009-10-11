/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.registryViewer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;


/**
 * ValueRecode �̃c���[�m�[�h�ł��D {@link vavi.util.win32.registry.Registry} �̎����̃T���v���ɂȂ��Ă��܂��D ��K�w���̓W�J�̎d��
 * 
 * <pre><tt>
 * 
 *   Foo.BarExtendsRegistryTreeRecodeImpl tr = ... // �J�����g���擾
 * 
 *   if (tr.hasChildTreeRecodes()) {	 // �q�������邩�ǂ���
 * 	tr = tr.get1stChildTreeRecode(); // �ŏ��̎q�����擾���|�C���^���ړ�
 * 	...
 * 
 * 	while (tr.hasNextTreeRecode()) { // �������邩�ǂ���
 * 	    tr = tr.getNextTreeRecode(); // �����擾���|�C���^���ړ�
 * 	    ...
 * 	}
 *   }
 *  
 * </tt></pre>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 */
public class ValueRecodeTreeNode extends DefaultMutableTreeNode {

    /** Have the children of this node been loaded yet? */
    private boolean hasLoaded;

    /**
     * Constructs a new RegistryViewerTreeNode instance with o as the user object.
     */
    public ValueRecodeTreeNode(Object userObject) {
        super(userObject);
    }

    /**
     * If hasLoaded is false, meaning the children have not yet been loaded, loadChildren is messaged and super is messaged for
     * the return value.
     */
    public int getChildCount() {
        if (!hasLoaded) {
            loadChildren();
        }

        return super.getChildCount();
    }

    /** �����񂪃f�[�^�Ɋ܂܂�Ă��邩��Ԃ��܂��D */
    public boolean contains(String str) {
        if (((ValueRecode.TreeRecode) userObject).getKeyName().toLowerCase().indexOf(str.toLowerCase()) != -1)
            return true;
        // Debug.println(userObject.getClass().getName());
        return ((ValueRecode.TreeRecode) userObject).contains(str);
    }

    /** ���[�g����̃p�X�����擾���܂��D */
    public String getAbsoluteName() {
        TreeNode[] nodes = getPath();
        String name = new String();

        for (int i = 0; i < nodes.length; i++) {
            name += ((ValueRecode.TreeRecode) ((ValueRecodeTreeNode) nodes[i]).getUserObject()).getKeyName() + "\\";
        }

        return name.substring(0, name.length() - 1);
    }

    /**
     * Messaged the first time getChildCount is messaged. Creates children with random names from names.
     */
    protected void loadChildren() {
        ValueRecodeTreeNode node;
        ValueRecode.TreeRecode tr = (ValueRecode.TreeRecode) userObject;

        if (tr.hasChildTreeRecodes()) {

            int i = 0;
            tr = (ValueRecode.TreeRecode) tr.get1stChildTreeRecode();
            node = new ValueRecodeTreeNode(tr);
            insert(node, i);
            i++;

            while (tr.hasNextTreeRecode()) {
                tr = (ValueRecode.TreeRecode) tr.getNextTreeRecode();
                node = new ValueRecodeTreeNode(tr);

                /*
                 * Don't use add() here, add calls insert(newNode, getChildCount()) so if you want to use add, just be sure to set
                 * hasLoaded = true first.
                 */

                insert(node, i);
                i++;
            }
        }

        /* This node has now been loaded, mark it so. */
        hasLoaded = true;
    }
}

/* */
