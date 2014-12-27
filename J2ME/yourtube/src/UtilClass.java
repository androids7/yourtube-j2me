import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;

public class UtilClass extends Object {
    private static final String HEXSTR         = "0123456789ABCDEF";
    private static final char   HEX[]          = "0123456789ABCDEF".toCharArray();
    private static final String URL_UNRESERVED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~";

    private static final String USER_AGENT = "YourTube";

    public static String URLDecode(String str) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '%' && i < str.length() - 2) {
                if (HEXSTR.indexOf(str.charAt(i + 1)) >= 0 &&
                    HEXSTR.indexOf(str.charAt(i + 2)) >= 0) {
                    buf.append((char)(((HEXSTR.indexOf(str.charAt(i + 1)) << 4) + HEXSTR.indexOf(str.charAt(i + 2))) & 0xff));
                    i = i + 2;
                } else {
                    buf.append(str.charAt(i));
                }
            } else {
                buf.append(str.charAt(i));
            }
        }

        return buf.toString();
    }

    public static String URLEncode(String str) {
        byte                  bytes[] = new byte[0];
        ByteArrayOutputStream stream = null;
        DataOutputStream      data_stream = null;
        StringBuffer          buf = new StringBuffer();

        try {
            stream      = new ByteArrayOutputStream();
            data_stream = new DataOutputStream(stream);

            data_stream.writeUTF(str);

            bytes = stream.toByteArray();
        } catch (Exception ex) {
            // Ignore
        } finally {
            if (data_stream != null) {
                try {
                    data_stream.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        for (int i = 2; i < bytes.length; i++) {
            if (URL_UNRESERVED.indexOf(bytes[i]) >= 0) {
                buf.append((char)bytes[i]);
            } else {
                buf.append('%').append(HEX[(bytes[i] >> 4) & 0x0f]).append(HEX[bytes[i] & 0x0f]);
            }
        }

        return buf.toString();
    }

    public static String DurationToString(String duration) {
        final String NUM_CHARS = "0123456789";

        boolean parsing_time   = false,
                ignore_digits  = false;
        int     years          = 0,
                months         = 0,
                weeks          = 0,
                days           = 0,
                hours          = 0,
                mins           = 0,
                secs           = 0;
        String  result         = "",
                current_number = "";

        for (int i = 0; i < duration.length(); i++) {
            if (duration.charAt(i) == '.' || duration.charAt(i) == ',') {
                ignore_digits = true;
            } else if (NUM_CHARS.indexOf(duration.charAt(i)) != -1) {
                if (!ignore_digits) {
                    current_number += duration.charAt(i);
                }
            } else if (duration.charAt(i) == 'P') {
                parsing_time   = false;
                ignore_digits  = false;
                current_number = "";
            } else if (duration.charAt(i) == 'Y') {
                try {
                    years = Integer.parseInt(current_number);
                } catch (Exception ex) {
                    years = 0;
                }

                ignore_digits  = false;
                current_number = "";
            } else if (duration.charAt(i) == 'M') {
                if (parsing_time) {
                    try {
                        mins = Integer.parseInt(current_number);
                    } catch (Exception ex) {
                        mins = 0;
                    }
                } else {
                    try {
                        months = Integer.parseInt(current_number);
                    } catch (Exception ex) {
                        months = 0;
                    }
                }

                ignore_digits  = false;
                current_number = "";
            } else if (duration.charAt(i) == 'W') {
                try {
                    weeks = Integer.parseInt(current_number);
                } catch (Exception ex) {
                    weeks = 0;
                }

                ignore_digits  = false;
                current_number = "";
            } else if (duration.charAt(i) == 'D') {
                try {
                    days = Integer.parseInt(current_number);
                } catch (Exception ex) {
                    days = 0;
                }

                ignore_digits  = false;
                current_number = "";
            } else if (duration.charAt(i) == 'T') {
                parsing_time   = true;
                ignore_digits  = false;
                current_number = "";
            } else if (duration.charAt(i) == 'H') {
                try {
                    hours = Integer.parseInt(current_number);
                } catch (Exception ex) {
                    hours = 0;
                }

                ignore_digits  = false;
                current_number = "";
            } else if (duration.charAt(i) == 'S') {
                try {
                    secs = Integer.parseInt(current_number);
                } catch (Exception ex) {
                    secs = 0;
                }

                ignore_digits  = false;
                current_number = "";
            }
        }

        if (years != 0) {
            result += String.valueOf(years) + "y ";
        }
        if (months != 0) {
            result += String.valueOf(months) + "m ";
        }
        if (weeks != 0) {
            result += String.valueOf(weeks) + "w ";
        }
        if (days != 0) {
            result += String.valueOf(days) + "d ";
        }
        result += ZeroPad(hours) + ":" + ZeroPad(mins) + ":" + ZeroPad(secs);

        return result;
    }

    public static String ZeroPad(int num) {
        if (num < 10) {
            return new String("0") + String.valueOf(num);
        } else {
            return                   String.valueOf(num);
        }
    }

    public static String MakeValidFilename(String string) {
        String file_name = new String(string);

        file_name = file_name.replace('\\', '_');
        file_name = file_name.replace('/',  '_');
        file_name = file_name.replace(':',  '_');
        file_name = file_name.replace('*',  '_');
        file_name = file_name.replace('?',  '_');
        file_name = file_name.replace('"',  '_');
        file_name = file_name.replace('\'', '_');
        file_name = file_name.replace('>',  '_');
        file_name = file_name.replace('<',  '_');
        file_name = file_name.replace('|',  '_');

        return file_name;
    }

    public static String MakeFullFileURL(String file_name) {
        FileConnection file = null;

        try {
            file = (FileConnection)Connector.open("file:///" + SettingStorageClass.GetDestinationDisk() + "DATA/Videos");

            if (file.exists()) {
                return "file:///" + SettingStorageClass.GetDestinationDisk() + "DATA/Videos/" + file_name;
            }
        } catch (Exception ex) {
            // Ignore
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        file = null;

        try {
            file = (FileConnection)Connector.open("file:///" + SettingStorageClass.GetDestinationDisk() + "Videos");

            if (file.exists()) {
                return "file:///" + SettingStorageClass.GetDestinationDisk() + "Videos/" + file_name;
            }
        } catch (Exception ex) {
            // Ignore
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        file = null;

        try {
            file = (FileConnection)Connector.open("file:///" + SettingStorageClass.GetDestinationDisk() + "DATA/Videos");

            file.mkdir();

            return "file:///" + SettingStorageClass.GetDestinationDisk() + "DATA/Videos/" + file_name;
        } catch (Exception ex) {
            // Ignore
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        file = null;

        try {
            file = (FileConnection)Connector.open("file:///" + SettingStorageClass.GetDestinationDisk() + "Videos");

            file.mkdir();

            return "file:///" + SettingStorageClass.GetDestinationDisk() + "Videos/" + file_name;
        } catch (Exception ex) {
            // Ignore
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        return "";
    }

    public static boolean ValidateFileConnectionAPI() {
        if (System.getProperty("microedition.io.file.FileConnection.version") != null) {
            return true;
        } else {
            return false;
        }
    }

    public static Vector GetFileSystemRoots() {
        Vector roots = new Vector();

        try {
            Enumeration root_enum = FileSystemRegistry.listRoots();

            while (root_enum.hasMoreElements()) {
                roots.addElement(root_enum.nextElement());
            }
        } catch (Exception ex) {
            // Ignore
        }

        return roots;
    }

    public static String GetDefaultVideoDisk() {
        String path = System.getProperty("fileconn.dir.videos");

        if (path != null) {
            int pos = path.indexOf("file:///");

            if (pos != -1) {
                path = path.substring(pos + 8, path.length());
            }

            pos = path.indexOf("/");

            if (pos != -1) {
                path = path.substring(0, pos + 1);
            }

            return path;
        } else {
            return "";
        }
    }

    public static Image LoadImageFromURL(String url) {
        Image          result = null;
        HttpConnection connection = null;
        InputStream    stream = null;

        try {
            connection = (HttpConnection)Connector.open(url);

            connection.setRequestMethod(HttpConnection.GET);
            connection.setRequestProperty("User-Agent", USER_AGENT);

            stream = connection.openInputStream();

            result = Image.createImage(stream);
        } catch (Exception ex) {
            result = null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        return result;
    }

    public static Image ResizeImageToWidth(Image image, int width) {
        int src_width  = image.getWidth();
        int src_height = image.getHeight();
        int height     = width * src_height / src_width;

        Image result      = Image.createImage(width, height);
        Graphics graphics = result.getGraphics();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                graphics.setClip(x, y, 1, 1);

                int dx = x * src_width  / width;
                int dy = y * src_height / height;

                graphics.drawImage(image, x - dx, y - dy, Graphics.LEFT | Graphics.TOP);
            }
        }

        return Image.createImage(result);
    }
}
