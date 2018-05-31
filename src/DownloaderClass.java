import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class DownloaderClass extends Object {
    private static final int SLEEP_DELAY = 1000;
    private static final int BUF_SIZE    = 262144;

    private static final String YOUTUBE_VINFO_URL  = "http://www.youtube.com/get_video_info";
    private static final String YOUTUBE_VINFO_EL   = "detailpage";
    private static final String YOUTUBE_VINFO_PS   = "default";
    private static final String YOUTUBE_VINFO_EURL = "";
    private static final String YOUTUBE_VINFO_GL   = "US";
    private static final String YOUTUBE_VINFO_HL   = "en";
    private static final String USER_AGENT         = "YourTube";

    private static boolean StopDownloader;

    private static Thread DownloaderThread = null;

    private static Hashtable GetFmtMetadata(String video_id, int format) {
        int                   chars_read;
        String                visitor_info1_live_cookie;
        StringBuffer          buffer;
        Hashtable             result;
        HttpConnection        connection = null;
        InputStream           stream = null;
        InputStreamReader     stream_reader = null;

        try {
            connection = (HttpConnection)Connector.open(YOUTUBE_VINFO_URL +
                                                        "?video_id="      + UtilClass.URLEncode(video_id) +
                                                        "&el="            + YOUTUBE_VINFO_EL +
                                                        "&ps="            + YOUTUBE_VINFO_PS +
                                                        "&eurl="          + YOUTUBE_VINFO_EURL +
                                                        "&gl="            + YOUTUBE_VINFO_GL +
                                                        "&hl="            + YOUTUBE_VINFO_HL);

            connection.setRequestMethod(HttpConnection.GET);
            connection.setRequestProperty("User-Agent", USER_AGENT);

            stream        = connection.openInputStream();
            stream_reader = new InputStreamReader(stream);
            buffer        = new StringBuffer();

            visitor_info1_live_cookie = "";

            String header;

            for (int i = 0; i < 255 && (header = connection.getHeaderField(i)) != null; i++) {
                int begin = header.indexOf("VISITOR_INFO1_LIVE=");

                if (begin != -1) {
                    int end = header.indexOf(";", begin + 19);

                    if (end == -1) {
                        end = header.length();
                    }
                    
                    visitor_info1_live_cookie = header.substring(begin + 19, end);
                }
            }

            char buf[] = new char[BUF_SIZE];

            while ((chars_read = stream_reader.read(buf, 0, BUF_SIZE)) != -1) {
                buffer.append(buf, 0, chars_read);
            }

            String tmpstr = buffer.toString();
            int    begin  = tmpstr.indexOf("url_encoded_fmt_stream_map=");

            if (begin != -1) {
                int end = tmpstr.indexOf("&", begin + 27);

                if (end == -1) {
                    end = tmpstr.length();
                }

                Vector url_encoded_fmt_stream_map = new Vector();

                tmpstr = UtilClass.URLDecode(tmpstr.substring(begin + 27, end));

                begin = 0;
                end   = tmpstr.indexOf(",");

                while (end != -1) {
                    url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, end));

                    begin = end + 1;
                    end   = tmpstr.indexOf(",", begin);
                }

                url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, tmpstr.length()));

                result = new Hashtable();

                Enumeration url_encoded_fmt_stream_map_enum = url_encoded_fmt_stream_map.elements();

                while (url_encoded_fmt_stream_map_enum.hasMoreElements()) {
                    tmpstr = (String)url_encoded_fmt_stream_map_enum.nextElement();

                    begin = tmpstr.indexOf("itag=");

                    if (begin != -1) {
                        end = tmpstr.indexOf("&", begin + 5);

                        if (end == -1) {
                            end = tmpstr.length();
                        }

                        int fmt = Integer.parseInt(tmpstr.substring(begin + 5, end));

                        if (fmt == format) {
                            String signature = "";

                            begin = tmpstr.indexOf("sig=");

                            if (begin != -1) {
                                end = tmpstr.indexOf("&", begin + 4);

                                if (end == -1) {
                                    end = tmpstr.length();
                                }

                                signature = UtilClass.URLDecode(tmpstr.substring(begin + 4, end));
                            }

                            begin = tmpstr.indexOf("url=");

                            if (begin != -1) {
                                end = tmpstr.indexOf("&", begin + 4);

                                if (end == -1) {
                                    end = tmpstr.length();
                                }

                                String url = UtilClass.URLDecode(tmpstr.substring(begin + 4, end));

                                if (url.indexOf("signature=") == -1 && !signature.equals("")) {
                                    url = url + "&signature=" + signature;
                                }

                                result.put(new String("VISITOR_INFO1_LIVE_COOKIE"), visitor_info1_live_cookie);
                                result.put(new String("URL"),                       url);

                                break;
                            }
                        }
                    }
                }
            } else {
                result = new Hashtable();
            }
        } catch (Exception ex) {
            result = new Hashtable();
        } finally {
            if (stream_reader != null) {
                try {
                    stream_reader.close();
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

    private static synchronized boolean DownloaderStopped() {
        return StopDownloader;
    }

    private static synchronized void SetDownloadState(DownloadClass download, int state, String error_msg) throws Exception {
        if (!DownloaderStopped()) {
            DownloadStorageClass.SetState(download.GetId(), state, error_msg);

            download.SetState(state);
            download.SetErrorMsg(error_msg);
        }
    }

    private static synchronized void SetDownloadSize(DownloadClass download, long size) throws Exception {
        if (!DownloaderStopped()) {
            DownloadStorageClass.SetSize(download.GetId(), size);

            download.SetSize(size);
        }
    }

    private static synchronized void SetDownloadDone(DownloadClass download, long done) throws Exception {
        if (!DownloaderStopped()) {
            DownloadStorageClass.SetDone(download.GetId(), done);

            download.SetDone(done);
        }
    }

    private static synchronized void WriteDownloadChunk(DownloadClass download, OutputStream stream, byte chunk[], int size) throws Exception {
        if (!DownloaderStopped()) {
            stream.write(chunk, 0, size);
            stream.flush();

            DownloadStorageClass.IncrDone(download.GetId(), size);

            download.SetDone(download.GetDone() + size);
        }
    }

    public static synchronized void Initialize() {
        if (DownloaderThread == null || !DownloaderThread.isAlive()) {
            StopDownloader = false;

            DownloaderThread = new Thread() {
                public void run() {
                    while (!DownloaderStopped()) {
                        Vector        downloads;
                        DownloadClass active_download;

                        try {
                            Thread.sleep(SLEEP_DELAY);
                        } catch (Exception ex) {
                            // Ignore
                        }

                        downloads       = DownloadStorageClass.GetListOfCopiesSortedByState();
                        active_download = null;

                        for (int i = 0; i < downloads.size(); i++) {
                            if (((DownloadClass)downloads.elementAt(i)).GetState() == DownloadClass.STATE_ACTIVE) {
                                active_download = (DownloadClass)downloads.elementAt(i);

                                break;
                            } else if (((DownloadClass)downloads.elementAt(i)).GetState() == DownloadClass.STATE_ERROR ||
                                       ((DownloadClass)downloads.elementAt(i)).GetState() == DownloadClass.STATE_QUEUED) {
                                active_download = (DownloadClass)downloads.elementAt(i);
                            }
                        }

                        if (active_download != null) {
                            try {
                                String visitor_info1_live_cookie;
                                String url;

                                SetDownloadState(active_download, DownloadClass.STATE_ACTIVE, "");

                                Hashtable metadata = GetFmtMetadata(active_download.GetVideoId(), active_download.GetFormat());

                                if (metadata != null) {
                                    if (metadata.containsKey(new String("VISITOR_INFO1_LIVE_COOKIE"))) {
                                        visitor_info1_live_cookie = (String)metadata.get(new String("VISITOR_INFO1_LIVE_COOKIE"));
                                    } else {
                                        visitor_info1_live_cookie = "";
                                    }
                                    if (metadata.containsKey(new String("URL"))) {
                                        url = (String)metadata.get(new String("URL"));
                                    } else {
                                        url = "";
                                    }
                                } else {
                                    visitor_info1_live_cookie = "";
                                    url                       = "";
                                }

                                if (!url.equals("")) {
                                    boolean done = false, error = false;
                                    String  error_msg = "";

                                    while (!done && !error && !DownloaderStopped()) {
                                        HttpConnection connection = null;
                                        InputStream    input_stream = null;

                                        try {
                                            connection = (HttpConnection)Connector.open(url);

                                            connection.setRequestMethod(HttpConnection.GET);
                                            connection.setRequestProperty("User-Agent", USER_AGENT);

                                            if (!visitor_info1_live_cookie.equals("")) {
                                                connection.setRequestProperty("Cookie", "VISITOR_INFO1_LIVE=" + visitor_info1_live_cookie);
                                            }
                                            if (active_download.GetSize() != 0 && active_download.GetDone() != 0) {
                                                connection.setRequestProperty("Range", "bytes=" + String.valueOf(active_download.GetDone()) + "-");
                                            }

                                            input_stream = connection.openInputStream();

                                            if (connection.getResponseCode() == HttpConnection.HTTP_OK ||
                                                connection.getResponseCode() == HttpConnection.HTTP_PARTIAL) {
                                                boolean        file_valid = false;
                                                String         file_url = UtilClass.MakeFullFileURL(active_download.GetFullFileName());
                                                FileConnection file = null;
                                                OutputStream   output_stream = null;

                                                if (!file_url.equals("")) {
                                                    try {
                                                        file = (FileConnection)Connector.open(file_url, Connector.READ_WRITE);

                                                        if (connection.getResponseCode() == HttpConnection.HTTP_PARTIAL) {
                                                            if (file.exists() &&
                                                                file.fileSize() == active_download.GetDone() &&
                                                                active_download.GetDone() + connection.getLength() == active_download.GetSize()) {
                                                                output_stream = file.openOutputStream(active_download.GetDone());

                                                                file_valid = true;
                                                            }
                                                        } else {
                                                            if (file.exists()) {
                                                                file.truncate(0);
                                                            } else {
                                                                file.create();
                                                            }

                                                            SetDownloadSize(active_download, connection.getLength());
                                                            SetDownloadDone(active_download, 0);

                                                            output_stream = file.openOutputStream();

                                                            file_valid = true;
                                                        }

                                                        if (file_valid) {
                                                            int  read;
                                                            byte buffer[] = new byte[BUF_SIZE];

                                                            while ((read = input_stream.read(buffer)) >= 0 && !DownloaderStopped()) {
                                                                WriteDownloadChunk(active_download, output_stream, buffer, read);
                                                            }

                                                            if (active_download.GetDone() == active_download.GetSize()) {
                                                                done = true;
                                                            }
                                                        } else {
                                                            SetDownloadSize(active_download, 0);
                                                            SetDownloadDone(active_download, 0);
                                                        }
                                                    } catch (Exception ex) {
                                                        error     = true;
                                                        error_msg = "Please check destination disk settings. " + ex.toString();
                                                    } finally {
                                                        if (output_stream != null) {
                                                            try {
                                                                output_stream.close();
                                                            } catch (Exception ex) {
                                                                // Ignore
                                                            }
                                                        }
                                                        if (file != null) {
                                                            try {
                                                                file.close();
                                                            } catch (Exception ex) {
                                                                // Ignore
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    error     = true;
                                                    error_msg = "Please check destination disk settings. Could not find or create video directory";
                                                }
                                            } else if (connection.getResponseCode() == HttpConnection.HTTP_MOVED_PERM ||
                                                       connection.getResponseCode() == HttpConnection.HTTP_SEE_OTHER ||
                                                       connection.getResponseCode() == HttpConnection.HTTP_MOVED_TEMP) {
                                                url = connection.getHeaderField("Location");
                                            } else {
                                                error     = true;
                                                error_msg = "Invalid server response: " + String.valueOf(connection.getResponseCode()) + " " +
                                                                                          connection.getResponseMessage();
                                            }
                                        } catch (Exception ex) {
                                            error     = true;
                                            error_msg = "Please check your Internet connection. " + ex.toString();
                                        } finally {
                                            if (input_stream != null) {
                                                try {
                                                    input_stream.close();
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
                                    }

                                    if (error) {
                                        SetDownloadState(active_download, DownloadClass.STATE_ERROR, error_msg);
                                    } else if (done) {
                                        SetDownloadState(active_download, DownloadClass.STATE_COMPLETED, "");
                                    }
                                } else {
                                    SetDownloadState(active_download, DownloadClass.STATE_ERROR, "Please try to select another video format. Specified format is not available");
                                }
                            } catch (Exception ex) {
                                try {
                                    SetDownloadState(active_download, DownloadClass.STATE_ERROR, ex.toString());
                                } catch (Exception iex) {
                                    // Ignore
                                }
                            }
                        }
                    }
                }
            };

            DownloaderThread.start();
        }
    }

    public static synchronized void Destroy() {
        StopDownloader = true;
    }
}
