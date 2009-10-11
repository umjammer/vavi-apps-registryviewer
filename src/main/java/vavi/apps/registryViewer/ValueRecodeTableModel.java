/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.registryViewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

import vavi.util.Debug;


/**
 * ValueRecode �̃e�[�u�����f���ł��D
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 */
public class ValueRecodeTableModel extends AbstractTableModel {

    /** �f�[�^�̖��O�̔z�� */
    List<JLabel> names = new ArrayList<JLabel>();

    /** �f�[�^�̒l�̔z�� */
    List<Object> values = new ArrayList<Object>();

    /** �J��������Ԃ��܂��D */
    public int getColumnCount() {
        return 2;
    }

    /** �s����Ԃ��܂��D */
    public int getRowCount() {
        return names.size();
    }

    /** ���l�Ƃ��Ēl��ǉ����܂��D */
    public void addValue(String name, int value) {
        names.add(new JLabel(name, UIManager.getIcon("registryViewer.binaryIcon"), SwingConstants.LEFT));
        values.add(new Integer(value));
    }

    /** ������Ƃ��Ēl��ǉ����܂��D */
    public void addValue(String name, String value) {
        names.add(new JLabel(name.equals("") ? "(�W��)" : name, UIManager.getIcon("registryViewer.stringIcon"), SwingConstants.LEFT));
        values.add(value);
    }

    /** �o�C�i���f�[�^�Ƃ��Ēl��ǉ����܂��D */
    public void addValue(String name, byte[] value) {
        names.add(new JLabel(name, UIManager.getIcon("registryViewer.binaryIcon"), SwingConstants.LEFT));
        values.add(value);
    }

    /** ���m�̌^�Ƃ��Ēl��ǉ����܂��D */
    public void addValue(String name, byte[] value, int type) {
        names.add(new JLabel(name, UIManager.getIcon("registryViewer.unknownIcon"), SwingConstants.LEFT));
        values.add(value);
    }

    /** �w�肵���J�������ҏW�\���ǂ�����Ԃ��܂��D */
    public boolean isCellEditable(int c) {
        if (c == 1)
            return true;
        else
            return false;
    }

    /** �w�肵���J�����̃N���X��Ԃ��܂��D */
    public Class<?> getColumnClass(int c) {
        if (c == 0) {
            return JLabel.class;
        } else {
            return String.class;
        }
    }

    /** �w�肵���s�C�J�����̃f�[�^��Ԃ��܂��D */
    public Object getValueAt(int row, int col) {
        // Debug.println("values: " + getRowCount());
        // Debug.println("cell: " + row + ", " + col);
        if (col == 0)
            return names.get(row);
        else if (col == 1) {
            Object value = values.get(row);
            if (value instanceof String) {
                return "\"" + value + "\"";
            }
            if (value instanceof Integer) {
                String h = Integer.toHexString(((Integer) value).intValue());
                h = ("0000000" + h).substring(7 + h.length() - 8);
                return "0x" + h + "(" + value + ")";
            } else {
                String tmp = "";
                byte[] b = (byte[]) value;
                for (int j = 0; j < b.length; j++) {
                    String h = Integer.toHexString(b[j] & 0xff).toUpperCase();
                    tmp += " " + (h.length() == 2 ? "" : "0") + h;
                }
                return tmp;
            }
        } else {
Debug.println("col: " + col);
            return null;
        }
    }

    /** �A�C�R�������[�h���܂��D */
    static {
        Class<?> clazz = ValueRecodeTableModel.class;
        UIDefaults table = UIManager.getDefaults();
        table.put("registryViewer.stringIcon", LookAndFeel.makeIcon(clazz, "resources/string.gif"));
        table.put("registryViewer.binaryIcon", LookAndFeel.makeIcon(clazz, "resources/binary.gif"));
        table.put("registryViewer.unknownIcon", LookAndFeel.makeIcon(clazz, "resources/unknown.gif"));
    }
}

/* */
