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
 * ValueRecode のテーブルモデルです．
 * 
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 */
public class ValueRecodeTableModel extends AbstractTableModel {

    /** データの名前の配列 */
    List<JLabel> names = new ArrayList<>();

    /** データの値の配列 */
    List<Object> values = new ArrayList<>();

    /** カラム数を返します． */
    public int getColumnCount() {
        return 2;
    }

    /** 行数を返します． */
    public int getRowCount() {
        return names.size();
    }

    /** 数値として値を追加します． */
    public void addValue(String name, int value) {
        names.add(new JLabel(name, UIManager.getIcon("registryViewer.binaryIcon"), SwingConstants.LEFT));
        values.add(value);
    }

    /** 文字列として値を追加します． */
    public void addValue(String name, String value) {
        names.add(new JLabel(name.equals("") ? "(標準)" : name, UIManager.getIcon("registryViewer.stringIcon"), SwingConstants.LEFT));
        values.add(value);
    }

    /** バイナリデータとして値を追加します． */
    public void addValue(String name, byte[] value) {
        names.add(new JLabel(name, UIManager.getIcon("registryViewer.binaryIcon"), SwingConstants.LEFT));
        values.add(value);
    }

    /** 未知の型として値を追加します． */
    public void addValue(String name, byte[] value, int type) {
        names.add(new JLabel(name, UIManager.getIcon("registryViewer.unknownIcon"), SwingConstants.LEFT));
        values.add(value);
    }

    /** 指定したカラムが編集可能かどうかを返します． */
    public static boolean isCellEditable(int c) {
        if (c == 1)
            return true;
        else
            return false;
    }

    /** 指定したカラムのクラスを返します． */
    public Class<?> getColumnClass(int c) {
        if (c == 0) {
            return JLabel.class;
        } else {
            return String.class;
        }
    }

    /** 指定した行，カラムのデータを返します． */
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
                String h = Integer.toHexString((Integer) value);
                h = ("0000000" + h).substring(7 + h.length() - 8);
                return "0x" + h + "(" + value + ")";
            } else {
                StringBuilder tmp = new StringBuilder();
                byte[] b = (byte[]) value;
                for (byte item : b) {
                    String h = Integer.toHexString(item & 0xff).toUpperCase();
                    tmp.append(" ").append(h.length() == 2 ? "" : "0").append(h);
                }
                return tmp.toString();
            }
        } else {
Debug.println("col: " + col);
            return null;
        }
    }

    /* アイコンをロードします． */
    static {
        Class<?> clazz = ValueRecodeTableModel.class;
        UIDefaults table = UIManager.getDefaults();
        table.put("registryViewer.stringIcon", LookAndFeel.makeIcon(clazz, "resources/string.gif"));
        table.put("registryViewer.binaryIcon", LookAndFeel.makeIcon(clazz, "resources/binary.gif"));
        table.put("registryViewer.unknownIcon", LookAndFeel.makeIcon(clazz, "resources/unknown.gif"));
    }
}

/* */
