/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.registryViewer;

import java.io.IOException;
import java.io.InputStream;

import vavi.util.Debug;
import vavi.util.win32.registry.Registry;


/**
 * �c���[�m�[�h�̃��[�U�I�u�W�F�N�g�ł��D {@link vavi.util.win32.registry.Registry} �̎����̃T���v���ɂȂ��Ă��܂��D
 * <ul>
 * <li>�R���X�g���N�^���I�[�o���C�h����
 * <li>getRoot ���\�b�h��ǉ����� (super.getRoot ���g�p)
 * <li>TreeRecodeImpl ���p�������N���X���쐬����
 * <ul>
 * <li>�R���X�g���N�^���I�[�o���C�h����
 * <li>���[�U���g�p����f�[�^��Ԃ����\�b�h��ǉ����� (getKeySize, getValueName, getValueDataXXX ���g�p)
 * </ul>
 * </ul>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 *          1.01 020430 nsano change <init> arg <br>
 */
public class ValueRecode extends Registry {

    /** ���W�X�g���̎������\�z���܂��D */
    public ValueRecode(InputStream is) throws IOException {
        super(is);
    }

    /** ���W�X�g���̃��[�g���擾���܂��D */
    public TreeRecode getRoot() {
        return (TreeRecode) super.getRoot(TreeRecode.class);
    }

    /** ���W�X�g���c���[�̂P���R�[�h�ł��D */
    public class TreeRecode extends TreeRecodeImpl {

        /** Value to display. */
        private ValueRecodeTableModel value;

        /** TreeRecode ���\�z���܂��D */
        public TreeRecode(int offset) {
            super(offset);
        }

        /** �e�[�u���p�̃f�[�^���擾���܂��D */
        public ValueRecodeTableModel getValue() {

            if (value == null) {
                value = new ValueRecodeTableModel();

                for (int i = 0; i < getKeySize(); i++) {
                    String name = getValueName(i);
                    switch (getValueType(i)) {
                    case RegSZ:
                        value.addValue(name, getValueDataAsString(i));
                        break;
                    case RegBin:
                        value.addValue(name, getValueData(i));
                        break;
                    case RegDWord:
                        value.addValue(name, getValueDataAsDWord(i));
                        break;
                    default:
                        Debug.println("type: Unknown: " + getValueType(i));
                        value.addValue(name, getValueData(i), getValueType(i));
                        break;
                    }
                }
            }

            return value;
        }

        /** */
        public String toString() {
            return getKeyName();
        }

        /** */
        public boolean contains(String str) {
            if (value == null) {
                return false;
            }

            for (int i = 0; i < value.getRowCount(); i++) {
                // Debug.println(value.getValueAt(i, 1).getClass().getName());
                if (((String) value.getValueAt(i, 1)).toLowerCase().indexOf(str.toLowerCase()) != -1) {
                    return true;
                }
            }

            return false;
        }
    }
}

/* */
