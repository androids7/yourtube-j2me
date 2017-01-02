import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class DownloaderClass extends Object {
    private static final int SLEEP_DELAY            = 1000;
    private static final int THROTTLING_SLEEP_DELAY = 10000;
    private static final int BUF_SIZE               = 262144;

    private static final String YOUTUBEINMP3_URL          = "http://www.youtubeinmp3.com";
    private static final String YOUTUBEINMP3_DOWNLOAD_URL = "http://www.youtubeinmp3.com/fetch/";
    private static final String YOUTUBE_WATCH_URL         = "https://www.youtube.com/watch";
    private static final String USER_AGENT                = "YourMusic";

    private static boolean StopDownloader;

    private static Thread DownloaderThread = null;

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
                                boolean done = false, error = false;
                                String  error_msg = "";
                                String  url = YOUTUBEINMP3_DOWNLOAD_URL +
                                              "?video="                 + UtilClass.URLEncode(YOUTUBE_WATCH_URL +
                                                                                              "?v="             + UtilClass.URLEncode(active_download.GetVideoId()));

                                SetDownloadState(active_download, DownloadClass.STATE_ACTIVE, "");

                                while (!done && !error && !DownloaderStopped()) {
                                    HttpConnection connection = null;
                                    InputStream    input_stream = null;

                                    try {
                                        connection = (HttpConnection)Connector.open(url);

                                        connection.setRequestMethod(HttpConnection.GET);
                                        connection.setRequestProperty("User-Agent", USER_AGENT);

                                        if (active_download.GetSize() != 0 && active_download.GetDone() != 0) {
                                            connection.setRequestProperty("Range", "bytes=" + String.valueOf(active_download.GetDone()) + "-");
                                        }

                                        input_stream = connection.openInputStream();

                                        if (connection.getResponseCode() == HttpConnection.HTTP_OK ||
                                            connection.getResponseCode() == HttpConnection.HTTP_PARTIAL) {
                                            if (connection.getHeaderField("Content-Type").equals("text/html")) {
                                                int               chars_read;
                                                StringBuffer      buffer;
                                                InputStreamReader input_stream_reader = null;

                                                try {
                                                    SetDownloadSize(active_download, 0);
                                                    SetDownloadDone(active_download, 0);

                                                    input_stream_reader = new InputStreamReader(input_stream);
                                                    buffer              = new StringBuffer();
                                                    
                                                    char buf[] = new char[BUF_SIZE];
                                                    
                                                    while ((chars_read = input_stream_reader.read(buf, 0, BUF_SIZE)) != -1) {
                                                        buffer.append(buf, 0, chars_read);
                                                    }
                                                    
                                                    String new_url = "";
                                                    String tmpstr  = buffer.toString();
                                                    int    begin   = tmpstr.indexOf("<meta http-equiv=\"refresh\" content=\"0; url=");

                                                    if (begin != -1) {
                                                        int end = tmpstr.indexOf("\"", begin + 43);

                                                        if (end != -1) {
                                                            new_url = tmpstr.substring(begin + 43, end);
                                                        }
                                                    }

                                                    if (!new_url.equals("")) {
                                                        url = new_url;

                                                        if (url.startsWith("//")) {
                                                            url = "http:" + url;
                                                        } else if (url.startsWith("/")) {
                                                            url = YOUTUBEINMP3_URL + url;
                                                        }
                                                    } else {
                                                        error     = true;
                                                        error_msg = LocalizationClass.GetLocalizedString(LocalizationClass.DOWNLOADERCLASS_INVALIDRESPONSERETRYING);
                                                    }
                                                } catch (Exception ex) {
                                                    error     = true;
                                                    error_msg = LocalizationClass.GetLocalizedString(LocalizationClass.DOWNLOADERCLASS_CHECKCONNECTION) + " " + ex.toString();
                                                } finally {
                                                    if (input_stream_reader != null) {
                                                        try {
                                                            input_stream_reader.close();
                                                        } catch (Exception ex) {
                                                            // Ignore
                                                        }
                                                    }
                                                }
                                            } else {
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
                                                        error_msg = LocalizationClass.GetLocalizedString(LocalizationClass.DOWNLOADERCLASS_CHECKDISKSETTINGS) + " " + ex.toString();
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
                                                    error_msg = LocalizationClass.GetLocalizedString(LocalizationClass.DOWNLOADERCLASS_CHECKDISKSETTINGS);
                                                }
                                            }
                                        } else if (connection.getResponseCode() == HttpConnection.HTTP_MOVED_PERM ||
                                                   connection.getResponseCode() == HttpConnection.HTTP_SEE_OTHER ||
                                                   connection.getResponseCode() == HttpConnection.HTTP_MOVED_TEMP) {
                                            url = connection.getHeaderField("Location");
                                            
                                            if (url.startsWith("//")) {
                                                url = "http:" + url;
                                            }
                                        } else {
                                            error     = true;
                                            error_msg = LocalizationClass.GetLocalizedString(LocalizationClass.DOWNLOADERCLASS_INVALIDRESPONSE) + " " +
                                                        String.valueOf(connection.getResponseCode()) + " " +
                                                        connection.getResponseMessage();
                                        }
                                    } catch (Exception ex) {
                                        error     = true;
                                        error_msg = LocalizationClass.GetLocalizedString(LocalizationClass.DOWNLOADERCLASS_CHECKCONNECTION) + " " + ex.toString();
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
                            } catch (Exception ex) {
                                try {
                                    SetDownloadState(active_download, DownloadClass.STATE_ERROR, ex.toString());
                                } catch (Exception iex) {
                                    // Ignore
                                }
                            }

                            try {
                                Thread.sleep(THROTTLING_SLEEP_DELAY);
                            } catch (Exception ex) {
                                // Ignore
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
