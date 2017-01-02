import java.util.*;
import javax.microedition.rms.*;

public class SettingStorageClass extends Object {
    private static final String STORAGE_NAME = "SETTING_STORAGE";

    private static final int DESTINATION_DISK        = 1;
    private static final int SHOW_SETTINGS_ON_LAUNCH = 5;

    private static final boolean DEFAULT_SHOW_SETTINGS_ON_LAUNCH = true;

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
            String def_disk = UtilClass.GetDefaultMusicDisk();

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

    public static synchronized boolean GetShowSettingsOnLaunch() {
        SettingClass setting = Get(SHOW_SETTINGS_ON_LAUNCH);

        if (setting != null) {
            return setting.GetBoolValue();
        } else {
            return DEFAULT_SHOW_SETTINGS_ON_LAUNCH;
        }
    }

    public static synchronized void SetShowSettingsOnLaunch(boolean value) throws Exception {
        Set(new SettingClass(SHOW_SETTINGS_ON_LAUNCH, value, 0, ""));
    }
}
