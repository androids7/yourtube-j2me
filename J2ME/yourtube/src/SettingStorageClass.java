import java.util.*;
import javax.microedition.rms.*;

public class SettingStorageClass extends Object {
    private static final String STORAGE_NAME = "SETTING_STORAGE";

    private static final int DESTINATION_DISK = 1;
    private static final int VIDEO_FORMAT_ID  = 3;
    private static final int PREVIEW_FORMAT   = 4;

    private static final int DEFAULT_VIDEO_FORMAT_ID = 0;
    private static final int DEFAULT_PREVIEW_FORMAT  = 0;

    private static Hashtable Hash = new Hashtable();

    public static synchronized void Initialize() throws Exception {
        int         id;
        Exception   exception = null;
        RecordStore storage = null;

        try {
            storage                    = RecordStore.openRecordStore(STORAGE_NAME, true);
            RecordEnumeration rec_enum = storage.enumerateRecords(null, null, false);

            while(rec_enum.hasNextElement()) {
                id = rec_enum.nextRecordId();

                SettingClass setting = new SettingClass(storage.getRecord(id));

                Hash.put(new Integer(setting.GetId()), setting);
            }
        } catch (Exception ex) {
            exception = ex;
        } finally {
            if (storage != null) {
                try {
                    storage.closeRecordStore();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
        if (exception != null) {
            throw (exception);
        }
    }

    private static synchronized SettingClass Get(int id) {
        return (SettingClass)Hash.get(new Integer(id));
    }

    private static synchronized void Set(SettingClass setting) throws Exception {
        Exception   exception = null;
        RecordStore storage = null;

        try {
            Hash.put(new Integer(setting.GetId()), setting);

            RecordStore.deleteRecordStore(STORAGE_NAME);

            storage = RecordStore.openRecordStore(STORAGE_NAME, true);

            Enumeration hash_enum = Hash.elements();

            while (hash_enum.hasMoreElements()) {
                SettingClass item = (SettingClass)hash_enum.nextElement();

                byte byte_array[] = item.ToByteArray();

                storage.addRecord(byte_array, 0, byte_array.length);
            }
        } catch (Exception ex) {
            exception = ex;
        } finally {
            if (storage != null) {
                try {
                    storage.closeRecordStore();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
        if (exception != null) {
            throw (exception);
        }
    }

    public static synchronized String GetDestinationDisk() {
        SettingClass setting = Get(DESTINATION_DISK);

        if (setting != null) {
            return setting.GetStringValue();
        } else {
            Vector roots    = UtilClass.GetFileSystemRoots();
            String def_disk = UtilClass.GetDefaultVideoDisk();

            if (roots.size() != 0) {
                String result = (String)roots.elementAt(0);

                for (int i = 0; i < roots.size(); i++) {
                    if (def_disk.equals((String)roots.elementAt(i))) {
                        result = def_disk;

                        break;
                    }
                }

                return result;
            } else {
                return "";
            }
        }
    }

    public static synchronized void SetDestinationDisk(String value) throws Exception {
        Set(new SettingClass(DESTINATION_DISK, false, 0, value));
    }

    public static synchronized int GetVideoFormatId() {
        SettingClass setting = Get(VIDEO_FORMAT_ID);

        if (setting != null) {
            return setting.GetIntValue();
        } else {
            return DEFAULT_VIDEO_FORMAT_ID;
        }
    }

    public static synchronized void SetVideoFormatId(int value) throws Exception {
        Set(new SettingClass(VIDEO_FORMAT_ID, false, value, ""));
    }

    public static synchronized int GetPreviewFormat() {
        SettingClass setting = Get(PREVIEW_FORMAT);

        if (setting != null) {
            return setting.GetIntValue();
        } else {
            return DEFAULT_PREVIEW_FORMAT;
        }
    }

    public static synchronized void SetPreviewFormat(int value) throws Exception {
        Set(new SettingClass(PREVIEW_FORMAT, false, value, ""));
    }
}
