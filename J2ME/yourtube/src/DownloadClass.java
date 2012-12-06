import java.io.*;

public class DownloadClass extends Object {
    public static final int STATE_ACTIVE    = 1;
    public static final int STATE_COMPLETED = 2;
    public static final int STATE_ERROR     = 3;
    public static final int STATE_QUEUED    = 4;

    private int    Id, State, Format;
    private long   Size, Done;
    private String Title, URL, FileName, FileExtension, ErrorMsg;

    public DownloadClass(int state, int format, long size, long done, String title, String url, String file_name, String file_extension, String error_msg) {
        super();

        Id            = 0;
        State         = state;
        Format        = format;
        Size          = size;
        Done          = done;
        Title         = title;
        URL           = url;
        FileName      = file_name;
        FileExtension = file_extension;
        ErrorMsg      = error_msg;
    }

    public DownloadClass(DownloadClass download) {
        super();

        Id            = download.Id;
        State         = download.State;
        Format        = download.Format;
        Size          = download.Size;
        Done          = download.Done;
        Title         = new String(download.Title);
        URL           = new String(download.URL);
        FileName      = new String(download.FileName);
        FileExtension = new String(download.FileExtension);
        ErrorMsg      = new String(download.ErrorMsg);
    }

    public DownloadClass(int id, byte record[]) throws IOException {
        super();
        
        DataInputStream data_stream;

        data_stream   = new DataInputStream(new ByteArrayInputStream(record));
        Id            = id;
        State         = data_stream.readInt();
        Format        = data_stream.readInt();
        Size          = data_stream.readLong();
        Done          = data_stream.readLong();
        Title         = data_stream.readUTF();
        URL           = data_stream.readUTF();
        FileName      = data_stream.readUTF();
        FileExtension = data_stream.readUTF();
        ErrorMsg      = data_stream.readUTF();
    }

    public void SetId(int id) {
        Id = id;
    }

    public void SetState(int state) {
        State = state;
    }

    public void SetFormat(int format) {
        Format = format;
    }

    public void SetSize(long size) {
        Size = size;
    }

    public void SetDone(long done) {
        Done = done;
    }

    public void SetErrorMsg(String error_msg) {
        ErrorMsg = error_msg;
    }

    public int GetId() {
        return Id;
    }

    public int GetState() {
        return State;
    }

    public int GetFormat() {
        return Format;
    }

    public long GetSize() {
        return Size;
    }

    public long GetDone() {
        return Done;
    }

    public String GetTitle() {
        return Title;
    }

    public String GetURL() {
        return URL;
    }

    public String GetFileName() {
        return FileName;
    }

    public String GetFileExtension() {
        return FileExtension;
    }

    public String GetErrorMsg() {
        return ErrorMsg;
    }

    public String GetVisibleName() {
        long percent_done;

        if (Size == 0) {
            percent_done = 0;
        } else {
            percent_done = (Done * 100) / Size;
        }

        if (State == STATE_ACTIVE) {
            return "[" + String.valueOf(percent_done) + "%] " + Title;
        } else if (State == STATE_COMPLETED) {
            return "[DONE] " + Title;
        } else if (State == STATE_ERROR) {
            return "[" + ErrorMsg + "] " + Title;
        } else if (State == STATE_QUEUED) {
            return "[QUEUED] " + Title;
        } else {
            return "[???] " + Title;
        }
    }

    public String GetFullFileName() {
        return FileName + "." + FileExtension;
    }

    public byte[] ToByteArray() throws IOException {
        ByteArrayOutputStream byte_stream;
        DataOutputStream      data_stream;

        byte_stream = new ByteArrayOutputStream();
        data_stream = new DataOutputStream(byte_stream);

        data_stream.writeInt(State);
        data_stream.writeInt(Format);
        data_stream.writeLong(Size);
        data_stream.writeLong(Done);
        data_stream.writeUTF(Title);
        data_stream.writeUTF(URL);
        data_stream.writeUTF(FileName);
        data_stream.writeUTF(FileExtension);
        data_stream.writeUTF(ErrorMsg);

        return byte_stream.toByteArray();
    }
}
