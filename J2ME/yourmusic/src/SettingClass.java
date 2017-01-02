import java.io.*;

public class SettingClass extends Object {
    private int     Id;
    private boolean BoolValue;
    private int     IntValue;
    private String  StringValue;

    public SettingClass(int id, boolean bool_value, int int_value, String string_value) {
        super();

        Id          = id;
        BoolValue   = bool_value;
        IntValue    = int_value;
        StringValue = string_value;
    }

    public SettingClass(byte record[]) throws IOException {
        super();

        DataInputStream data_stream;

        data_stream = new DataInputStream(new ByteArrayInputStream(record));
        Id          = data_stream.readInt();
        BoolValue   = data_stream.readBoolean();
        IntValue    = data_stream.readInt();
        StringValue = data_stream.readUTF();
    }

    public int GetId() {
        return Id;
    }

    public boolean GetBoolValue() {
        return BoolValue;
    }

    public int GetIntValue() {
        return IntValue;
    }

    public String GetStringValue() {
        return StringValue;
    }

    public byte[] ToByteArray() throws IOException {
        ByteArrayOutputStream byte_stream;
        DataOutputStream      data_stream;

        byte_stream = new ByteArrayOutputStream();
        data_stream = new DataOutputStream(byte_stream);

        data_stream.writeInt(Id);
        data_stream.writeBoolean(BoolValue);
        data_stream.writeInt(IntValue);
        data_stream.writeUTF(StringValue);

        return byte_stream.toByteArray();
    }
}
