import java.util.*;
import javax.microedition.rms.*;

public class DownloadStorageClass extends Object {
    private static final String STORAGE_NAME = "DOWNLOAD_STORAGE";

    private static String ValidateErrorMsg;

    private static Hashtable Hash = new Hashtable();

    private static void SortByState(Vector vector, int left, int right) {
        if (right > left) {
            int i = left - 1;
            int j = right;

            while (true) {
                while (((DownloadClass)vector.elementAt(++i)).GetState() < ((DownloadClass)vector.elementAt(right)).GetState());
                while (j > 0) {
                    if (((DownloadClass)vector.elementAt(--j)).GetState() <= ((DownloadClass)vector.elementAt(right)).GetState()) {
                        break;
                    }
                }
                if (i >= j) {
                    break;
                }

                DownloadClass item = (DownloadClass)vector.elementAt(i);

                vector.setElementAt(vector.elementAt(j), i);
                vector.setElementAt(item, j);
            }

            DownloadClass item = (DownloadClass)vector.elementAt(i);

            vector.setElementAt(vector.elementAt(right), i);
            vector.setElementAt(item, right);

            SortByState(vector, left,  i - 1);
            SortByState(vector, i + 1, right);
        }
    }

    public static synchronized void Initialize() throws Exception {
        int         id;
        Exception   exception = null;
        RecordStore storage = null;

        try {
            storage                    = RecordStore.openRecordStore(STORAGE_NAME, true);
            RecordEnumeration rec_enum = storage.enumerateRecords(null, null, false);

            while(rec_enum.hasNextElement()) {
                id = rec_enum.nextRecordId();

                Hash.put(new Integer(id), new DownloadClass(id, storage.getRecord(id)));
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

    public static synchronized boolean Validate(DownloadClass download) {
        if (download.GetTitle().equals("")) {
            ValidateErrorMsg = "Download task title is empty";

            return false;
        } else if (download.GetVideoId().equals("")) {
            ValidateErrorMsg = "Download task video id is empty";

            return false;
        } else if (download.GetFullFileName().equals("")) {
            ValidateErrorMsg = "Download task file name is empty";

            return false;
        } else {
            Enumeration hash_enum = Hash.elements();

            while (hash_enum.hasMoreElements()) {
                DownloadClass item = (DownloadClass)hash_enum.nextElement();

                if (download.GetFullFileName().toLowerCase().equals(item.GetFullFileName().toLowerCase())) {
                    ValidateErrorMsg = "Download task with the same file name already exists";

                    return false;
                }
            }

            return true;
        }
    }

    public static synchronized String GetValidateErrorMsg() {
        return ValidateErrorMsg;
    }

    public static synchronized boolean ValidateFileNameForDups(String file_name) {
        Enumeration hash_enum = Hash.elements();

        while (hash_enum.hasMoreElements()) {
            DownloadClass item = (DownloadClass)hash_enum.nextElement();

            if (file_name.toLowerCase().equals(item.GetFileName().toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    public static synchronized DownloadClass Get(int id) {
        return (DownloadClass)Hash.get(new Integer(id));
    }

    public static synchronized DownloadClass GetCopy(int id) {
        if (Hash.containsKey(new Integer(id))) {
            return new DownloadClass((DownloadClass)Hash.get(new Integer(id)));
        } else {
            return null;
        }
    }

    public static synchronized void Add(DownloadClass download) throws Exception {
        Exception   exception = null;
        RecordStore storage = null;

        try {
            storage = RecordStore.openRecordStore(STORAGE_NAME, true);

            byte byte_array[] = download.ToByteArray();

            int id = storage.addRecord(byte_array, 0, byte_array.length);

            download.SetId(id);

            Hash.put(new Integer(id), download);
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

    public static synchronized void Delete(int id) throws Exception {
        Exception   exception = null;
        RecordStore storage = null;

        try {
            storage = RecordStore.openRecordStore(STORAGE_NAME, false);
            storage.deleteRecord(id);

            Hash.remove(new Integer(id));
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

    public static synchronized void Replace(int id, DownloadClass download) throws Exception {
        Exception   exception = null;
        RecordStore storage = null;

        try {
            storage = RecordStore.openRecordStore(STORAGE_NAME, false);

            byte byte_array[] = download.ToByteArray();

            storage.setRecord(id, byte_array, 0, byte_array.length);

            download.SetId(id);

            Hash.put(new Integer(id), download);
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

    public static synchronized int GetCount() {
        return Hash.size();
    }

    public static synchronized Vector GetListOfCopiesSortedByState() {
        Vector      result = new Vector();
        Enumeration hash_enum = Hash.elements();

        while (hash_enum.hasMoreElements()) {
            result.addElement(new DownloadClass((DownloadClass)hash_enum.nextElement()));
        }

        SortByState(result, 0, result.size() - 1);

        return result;
    }

    public static synchronized void SetState(int id, int state, String error_msg) throws Exception {
        DownloadClass download = GetCopy(id);

        if (download != null) {
            download.SetState(state);
            download.SetErrorMsg(error_msg);

            Replace(id, download);
        } else {
            throw (new Exception("Invalid id"));
        }
    }

    public static synchronized void SetSize(int id, long size) throws Exception {
        DownloadClass download = GetCopy(id);

        if (download != null) {
            download.SetSize(size);

            Replace(id, download);
        } else {
            throw (new Exception("Invalid id"));
        }
    }

    public static synchronized void SetDone(int id, long done) throws Exception {
        DownloadClass download = GetCopy(id);

        if (download != null) {
            download.SetDone(done);

            Replace(id, download);
        } else {
            throw (new Exception("Invalid id"));
        }
    }

    public static synchronized void IncrDone(int id, int incr) throws Exception {
        DownloadClass download = GetCopy(id);

        if (download != null) {
            download.SetDone(download.GetDone() + incr);

            Replace(id, download);
        } else {
            throw (new Exception("Invalid id"));
        }
    }
}
