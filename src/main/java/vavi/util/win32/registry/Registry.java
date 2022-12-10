/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.win32.registry;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import vavi.util.ByteUtil;
import vavi.util.Debug;
import vavi.util.serdes.Element;
import vavi.util.serdes.Serdes;


/**
 * Registry represents Windows registry structure (CREG).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 990629 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 *          1.01 020430 nsano change <init> arg <br>
 *          1.02 030606 nsano chnage error trap <br>
 * @see "https://github.com/libyal/libcreg/blob/main/documentation/Windows%209x%20Registry%20File%20(CREG)%20format.asciidoc"
 * @see "https://github.com/yuval1024/Samba/blob/ed3ab6ec48e635ab4aaef445e67454b023d02efb/Samba/source/lib/registry/reg_backend_w95.c"
 */
public class Registry {

    /** data type for string */
    public static final int RegSZ = 0x00000001;
    /** data type for binary */
    public static final int RegBin = 0x00000003;
    /** data type for number */
    public static final int RegDWord = 0x00000004;

    /** CREG */
    private CREG creg;
    /** RGKN */
    private RGKN rgkn;
    /** RGDBs */
    private RGDB[] rgdbs;

    /** The encoding */
    private static final String encoding = "JISAutoDetect";

    /** */
    private SeekableByteChannel sbc;

    /**
     * Create registry from a stream.
     *
     * <pre>
     * CREG
     * RGKN
     *  TreeRecord[0]	???
     *  TreeRecord[1]
     *  TreeRecord[2]
     *  :
     * RGDB[0]
     *  RGDBRecord[0]
     *   ValueRecord[0]
     *   ValueRecord[1]
     *   ValueRecord[2]
     *   :
     *  RGDBRecord[1]
     *  RGDBRecord[2]
     *  :
     * RGDB[1]
     * RGDB[2]
     * :
     * </pre>
     *
     * @param sbc a registry file stream
     * @throws IOException if an error occurs
     */
    public Registry(SeekableByteChannel sbc) throws IOException {
        this.sbc = sbc;

        creg = new CREG();
        Serdes.Util.deserialize(sbc, creg);
Debug.println(Level.FINE, creg);
        rgkn = new RGKN();
        Serdes.Util.deserialize(sbc, rgkn);
Debug.println(Level.FINE, rgkn);

        sbc.position(creg.offsetOf1stRGDB);
Debug.printf(Level.FINE, "[0] pos: %08x", sbc.position());
        rgdbs = new RGDB[creg.numberOfRGDB];

        for (int i = 0; i < creg.numberOfRGDB; i++) {

            rgdbs[i] = new RGDB();
            Serdes.Util.deserialize(sbc, rgdbs[i]);
Debug.printf(Level.FINE, "[%d] %s", 1, rgdbs[i]);

            int o = 0;
            long baseOffset = sbc.position();
            // 0x20 sizeof CREG ??? */
Debug.printf(Level.FINE, "[%d] size: %08x", i, rgdbs[i].size - rgdbs[i].unusedSize);
            while (o < rgdbs[i].size - rgdbs[i].unusedSize) {
//Debug.printf("[%d][%d] offset: %08x, %08x", i, rgdbs[i].rrs.size(), o, sbc.position() - baseOffset);
                RGDBRecord rr = new RGDBRecord();
                Serdes.Util.deserialize(sbc, rr);
                if (rr.idNumber == 0xffff && rr.rgbd == 0xffff) {
Debug.printf(Level.FINE, "[%d] maybe end: offset: %08x, rr.size: %d", i, o, rgdbs[i].rrs.size());
                    break;
                }
//Debug.printf("[%d][%d]: %s", i, rgdbs[i].rrs.size(), rr);
                rgdbs[i].rrs.put(rr.idNumber, rr);


                rr.vrs = new ValueRecord[rr.numberOfValues];
                for (int j = 0; j < rr.numberOfValues; j++) {
                    rr.vrs[j] = new ValueRecord();
                    Serdes.Util.deserialize(sbc, rr.vrs[j]);
//Debug.printf("[%d][%d][%d]: %s", i, rgdbs[i].rrs.size(), j, rr.vrs[j]);
                }


                o += rr.length;
                sbc.position(baseOffset + o); // TODO
            }

            sbc.position(creg.offsetOf1stRGDB + rgdbs[i].size);
Debug.printf(Level.FINE, "[%d] pos: %08x", i + 1, sbc.position());
        }

//listTreeRecord(getRoot());
    }

    /** Test */
    private void listTreeRecord(TreeRecord tr) {
        while (true) {
            if (tr.offsetOf1stSubkey != -1) {
                listTreeRecord(new TreeRecord());
            }
            if (tr.offsetOfNext != -1) {
                tr = new TreeRecord();
            } else {
                break;
            }
        }
    }

    /** Gets the root of the registry. */
    public TreeRecord getRoot() {
        return newInstance(0x20 + rgkn.offsetOfRootRecord);
    }

    /** Returns new TreeRecord instance. */
    private TreeRecord newInstance(int offset) {
        try {
            TreeRecord treeRecord = new TreeRecord();

            sbc.position(offset);
            Serdes.Util.deserialize(sbc, treeRecord);

            if (treeRecord.idNumber == 0xffff && treeRecord.rgdb == 0xffff) {
Debug.println(Level.FINE, "no rgdb data, maybe root");
            } else {
                RGDBRecord rgdbRecord = rgdbs[treeRecord.rgdb].rrs.get(treeRecord.idNumber);
                if (rgdbRecord == null) {
Debug.printf(Level.WARNING, "rgdb Record not found %d:%d", treeRecord.rgdb, treeRecord.idNumber);
                } else {
                    treeRecord.setRGDBRecord(rgdbRecord);
                }
            }
Debug.printf(Level.FINE, "offset: %08x: %s", offset, treeRecord.toDebugString());

            return treeRecord;
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw new IllegalStateException(e);
        }
    }

    /** Returns first TreeRecord child. */
    public TreeRecord get1stChildTreeRecord(TreeRecord treeRecord) {
Debug.printf(Level.FINE, "pos: 0x%08x", 0x20 + treeRecord.offsetOf1stSubkey);
        return newInstance(0x20 + treeRecord.offsetOf1stSubkey);
    }

    /** Gets next TreeRecord. */
    public TreeRecord getNextTreeRecord(TreeRecord treeRecord) {
Debug.printf(Level.FINE, "pos: 0x%08x", 0x20 + treeRecord.offsetOfNext);
        return newInstance(0x20 + treeRecord.offsetOfNext);
    }

    /**
     * CREG represents registry header information.
     */
    @Serdes(bigEndian = false)
    private static final class CREG {

        @Element(sequence = 1, value = "4", validation = "\"CREG\".getBytes()")
        byte[] signature;
        @Element(sequence = 2)
        short minorFormatVersion;
        @Element(sequence = 3)
        short majorFormatVersion;
        /** offset for the first RGDB */
        @Element(sequence = 4)
        private int offsetOf1stRGDB;
        @Element(sequence = 5)
        private int checksum;
        /** the number of RGDB */
        @Element(sequence = 6, value = "unsigned short")
        private int numberOfRGDB;
        @Element(sequence = 7, value = "unsigned short")
        private int flags;
        @Element(sequence = 8)
        private short copyOfMinorFormatVersion;
        @Element(sequence = 9)
        private short copyOfMajorFormatVersion;
        @Element(sequence = 10)
        byte[] unknown1 = new byte[8];

        @Override
        public String toString() {
            return "CREG{" +
                    "signature=" + Arrays.toString(signature) +
                    ", minorFormatVersion=" + minorFormatVersion +
                    ", majorFormatVersion=" + majorFormatVersion +
                    String.format(", offsetOf1stRGDB=0x%08x", offsetOf1stRGDB) +
                    ", checksum=" + checksum +
                    ", flags=" + flags +
                    ", idNumber=" + numberOfRGDB +
                    ", copyOfMinorFormatVersion=" + copyOfMinorFormatVersion +
                    ", copyOfMajorFormatVersion=" + copyOfMajorFormatVersion +
                    ", nextFree=" + Arrays.toString(unknown1) +
                    '}';
        }
    }

    /**
     * RGKN represents the header of registry tree hierarchy.
     */
    @Serdes(bigEndian = false)
    public static final class RGKN {
        @Element(sequence = 1, value = "4", validation = "\"RGKN\".getBytes()")
        byte[] signature;
        /** size of RGKN? */
        @Element(sequence = 2)
        int size;
        /** offset for the root TreeRecord */
        @Element(sequence = 3)
        public int offsetOfRootRecord;
        @Element(sequence = 4)
        int offsetOfFree;
        @Element(sequence = 5)
        int flags;
        @Element(sequence = 6)
        int checksum;
        @Element(sequence = 7)
        byte[] unknown1 = new byte[8];

        /** size of CREG? */
        static final int offset = 0x20;

        @Override
        public String toString() {
            return "RGKN{" +
                    "signature=" + Arrays.toString(signature) +
                    String.format(", size=0x%08x", size) +
                    String.format(", offsetOfRootRecord=0x%08x", offsetOfRootRecord) +
                    String.format(", offsetOfFree=0x%08x", offsetOfFree) +
                    ", flags=" + flags +
                    ", checksum=" + checksum +
                    ", unknown1=" + Arrays.toString(unknown1) +
                    '}';
        }
    }

    /**
     * TreeRecord represents registry tree hierarchy. (rgkn_key)
     */
    @Serdes(bigEndian = false)
    public static class TreeRecord {
        /** 0x00000000 = normal key, 0x80000000 = free block */
        @Element(sequence = 1)
        int type;
        /** the hashcode of RGDBRecord? */
        @Element(sequence = 2)
        int hash;
        @Element(sequence = 3)
        int nextFree;
        /** offset for parent TreeRecord */
        @Element(sequence = 4)
        int offsetOfParent;
        /** offset for the first child of TreeRecord */
        @Element(sequence = 5)
        int offsetOf1stSubkey;
        /** offset for the next child of TreeRecord */
        @Element(sequence = 6)
        int offsetOfNext;
        /** RGDB index that has RGDBRecord */
        @Element(sequence = 7, value = "unsigned short")
        int idNumber;
        /** internal RGDB index of RGDBRecord */
        @Element(sequence = 8, value = "unsigned short")
        int rgdb;

        /** RGDBRecord which has TreeRecord's ValueRecord */
        private RGDBRecord rgdbRecord;

        /** Creates TreeRecordImpl. */
        void setRGDBRecord(RGDBRecord rgdbRecord) {
            this.rgdbRecord = rgdbRecord;
        }

        /** Returns having child TreeRecord or not. */
        public boolean hasChildTreeRecords() {
Debug.println(Level.FINER, offsetOf1stSubkey != -1);
            return offsetOf1stSubkey != -1;
        }

        /** Returns having next TreeRecord ot not. */
        public boolean hasNextTreeRecord() {
Debug.println(Level.FINER, offsetOfNext != -1);
            return offsetOfNext != -1;
        }

        /** Gets the key name of the TreeRecord. */
        @Override
        public String toString() {
            if (rgdbRecord != null) {
                return rgdbRecord.keyName;
            } else if (idNumber == 0xffff) {
                return "HKEY_root";
            } else {
                return "???"; // maybe symbolic link?
            }
        }

        /** Returns the number of TreeRecord's key. */
        public int getKeySize() {
            if (rgdbRecord != null && rgdbRecord.vrs != null) {
                return rgdbRecord.vrs.length;
            } else {
                return 0;
            }
        }

        /** Gets the name of specified index. */
        public String getValueName(int index) {
            return rgdbRecord.vrs[index].valueName;
        }

        /** Gets the data type of specified index. */
        public int getValueType(int index) {
            return rgdbRecord.vrs[index].type;
        }

        /** Gets the data value of specified index. */
        public byte[] getValueData(int index) {
            return rgdbRecord.vrs[index].valueData;
        }

        /** Gets the data value of specified index as String. */
        public String getValueDataAsString(int index) {
            return new String(getValueData(index), Charset.forName(encoding));
        }

        /** Gets the data value of specified index as Integer. */
        public int getValueDataAsDWord(int index) {
            byte[] b = getValueData(index);
            return ByteUtil.readLeInt(b, 0);
        }

        public String toDebugString() {
            return getClass().getSimpleName() + "{" +
                    "type=" + type +
                    String.format(", hash=0x%08x", hash) +
                    String.format(", nextFree=0x%08x", nextFree) +
                    String.format(", offsetOfParent=0x%08x", offsetOfParent) +
                    String.format(", offsetOf1stSubkey=0x%08x", offsetOf1stSubkey) +
                    String.format(", offsetOfNext=0x%08x", offsetOfNext) +
                    ", idNumber=" + idNumber +
                    ", rgdb=" + rgdb +
                    '}';
        }
    }

    /**
     * RGDB represents registry block. Including multiple RGDBRecord.
     */
    @Serdes(bigEndian = false)
    private static final class RGDB {

        @Element(sequence = 1, value = "4", validation = "\"RGDB\".getBytes()")
        byte[] signature;
        /** size of RGDB */
        @Element(sequence = 2)
        int size;
        @Element(sequence = 3)
        int unusedSize;
        @Element(sequence = 4, value = "unsigned short")
        int flags;
        @Element(sequence = 5, value = "unsigned short")
        int section;
        /** -1 if there is no free space */
        @Element(sequence = 6)
        int freeOffset;
        @Element(sequence = 7, value = "unsigned short")
        int maxId;
        @Element(sequence = 8, value = "unsigned short")
        int firstFreeId;
        @Element(sequence = 9)
        int unknown1;
        @Element(sequence = 10)
        int checksum;

        /** list of RGDBRecord */
        Map<Integer, RGDBRecord> rrs = new HashMap<>();

        @Override
        public String toString() {
            return "RGDB{" +
                    "signature=" + Arrays.toString(signature) +
                    String.format(", size=%1$d (%1$08x)", size) +
                    ", unusedSize=" + unusedSize +
                    ", flags=" + flags +
                    ", section=" + section +
                    ", freeOffset=" + freeOffset +
                    ", maxId=" + maxId +
                    ", firstFreeId=" + firstFreeId +
                    ", flags=" + unknown1 +
                    ", checksum=" + checksum +
                    ", rrs=" + rrs +
                    '}';
        }
    }

    /**
     * RGDBRecord represents key. Including multiple ValueRecord.
     */
    @Serdes(bigEndian = false)
    private static final class RGDBRecord {

        /** size of RGDBRecord */
        @Element(sequence = 1)
        int length;
        /** ID referred by TreeRecord */
        @Element(sequence = 2, value = "unsigned short")
        int idNumber;
        @Element(sequence = 3, value = "unsigned short")
        int rgbd;
        /** size of RGDBRecord? */
        @Element(sequence = 4)
        int size;
        /** the length of the key name */
        @Element(sequence = 5, value = "unsigned short")
        int textLength;
        /** number of the registry values */
        @Element(sequence = 6, value = "unsigned short")
        int numberOfValues;
        @Element(sequence = 7)
        int unknown1;
        /** the name of the key */
        @Element(sequence = 8, value = "$5")
        String keyName;

        /** value of the registry */
        ValueRecord[] vrs;

        @Override
        public String toString() {
            return "RGDBRecord{" +
                    "length=" + length +
                    ", idNumber=" + idNumber +
                    ", rgbd=" + rgbd +
                    ", size=" + size +
                    ", textLength=" + textLength +
                    ", numberOfValues=" + numberOfValues +
                    ", unknown1=" + unknown1 +
                    ", keyName=" + keyName +
                    '}';
        }

        /** Gets a hashcode referred by a TreeRecord. */
        int getHash() {

            int hash = 0;
            byte[] name = keyName.getBytes(Charset.forName(encoding));

            for (int i = 0; i < textLength; i++) {
                if ((name[i] & 0xff) < 0x80) {
                    hash += name[i];
                }
            }

            return hash;
        }
    }

    /**
     * ValueRecord represents one data of the registry.
     */
    @Serdes(bigEndian = false)
    public static final class ValueRecord {

        /** data type */
        @Element(sequence = 1)
        int type;
        @Element(sequence = 2)
        int dummy;
        /** data name length */
        @Element(sequence = 3, value = "unsigned short")
        int lengthOfValueName;
        /** data length */
        @Element(sequence = 4, value = "unsigned short")
        int lengthOfValueData;
        /** data name */
        @Element(sequence = 5, value = "$3")
        String valueName;
        /** data */
        @Element(sequence = 6, value = "$4")
        byte[] valueData;

        @Override
        public String toString() {
            return "ValueRecord{" +
                    String.format("type=%1$d(%1$08x)", type) +
                    ", dummy=" + dummy +
                    ", lengthOfValueName=" + lengthOfValueName +
                    ", lengthOfValueData=" + lengthOfValueData +
                    ", valueName='" + valueName + '\'' +
                    ", valueData=" + Arrays.toString(valueData) +
                    '}';
        }

        /** Creates a ValueRecord. */
        void x() {
            switch (type) {
            case RegSZ: // 0x00000001
//Debug.println("valueData: " + new String(valueData, Charset.forName(encoding)));
                break;
            case RegBin: // 0x00000003
//Debug.print("valueData:");
//for(int i = 0; i < lengthOfValueData; i++) {
//System.err.printf(" %02x", valueData[i]);
//}Debug.out.println();
                break;
            case RegDWord: // 0x00000004
            case 0x00000000:
            case 0x00000002:
            case 0x00000007:
            default:
                Debug.println(Level.WARNING, "data: unknown(" + type + ")");
                break;
            }
        }

        /** Returns data type as String. */
        String getTypeName(int type) {

            switch (type) {
            case RegSZ: // 0x00000001
                return "RegSZ";
            case RegBin: // 0x00000003
                return "RegBin";
            case RegDWord: // 0x00000004
                return "RegDWord";
            case 0x00000000:
            case 0x00000007:
            default:
                return "Unknown";
            }
        }
    }

    /**
     * Tests this clas.
     *
     * @param args registry file
     */
    public static void main(String[] args) throws Exception {
        Registry reg = new Registry(Files.newByteChannel(Paths.get(args[0])));
    }
}

/* */
