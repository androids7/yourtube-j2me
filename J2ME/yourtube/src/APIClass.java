import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import org.json.me.*;

public class APIClass extends Object {
    private static final int BUF_SIZE = 262144;

    private static final String YOUTUBE_SEARCH_URL         = "https://www.googleapis.com/youtube/v3/search";
    private static final String YOUTUBE_SEARCH_ORDER       = "relevance";
    private static final String YOUTUBE_SEARCH_PART        = "snippet";
    private static final String YOUTUBE_SEARCH_FIELDS      = "items(id%2Csnippet)";
    private static final String YOUTUBE_SEARCH_TYPE        = "video";
    private static final String YOUTUBE_SEARCH_MAX_RESULTS = "25";
    private static final String YOUTUBE_SEARCH_KEY         = "";

    private static final String YOUTUBE_VINFO_URL          = "http://www.youtube.com/get_video_info";
    private static final String YOUTUBE_VINFO_EL           = "detailpage";
    private static final String YOUTUBE_VINFO_PS           = "default";
    private static final String YOUTUBE_VINFO_EURL         = "";
    private static final String YOUTUBE_VINFO_GL           = "US";
    private static final String YOUTUBE_VINFO_HL           = "en";

    private static final String USER_AGENT = "YourTube";
    private static final String REFERER    = "http://yourtube.sourceforge.net/yourtube/";

    private static Vector SearchResults = null;

    public static Vector GetSearchResults() {
        return SearchResults;
    }

    public static void MakeSearch(String search_string) throws Exception {
        int               chars_read;
        StringBuffer      buffer;
        Vector            search_results = null;
        Exception         exception = null;
        HttpConnection    connection = null;
        InputStream       stream = null;
        InputStreamReader stream_reader = null;

        try {
            connection = (HttpConnection)Connector.open(YOUTUBE_SEARCH_URL +
                                                        "?q="              + UtilClass.URLEncode(search_string) +
                                                        "&order="          + YOUTUBE_SEARCH_ORDER +
                                                        "&part="           + YOUTUBE_SEARCH_PART +
                                                        "&fields="         + YOUTUBE_SEARCH_FIELDS +
                                                        "&type="           + YOUTUBE_SEARCH_TYPE +
                                                        "&maxResults="     + YOUTUBE_SEARCH_MAX_RESULTS +
                                                        "&key="            + YOUTUBE_SEARCH_KEY);

            connection.setRequestMethod(HttpConnection.GET);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Referer", REFERER);

            stream        = connection.openDataInputStream();
            stream_reader = new InputStreamReader(stream, "utf-8");
            buffer        = new StringBuffer();

            char buf[] = new char[BUF_SIZE];

            while ((chars_read = stream_reader.read(buf, 0, BUF_SIZE)) != -1) {
                buffer.append(buf, 0, chars_read);
            }

            JSONObject root_object = new JSONObject(buffer.toString());
            JSONArray  root_items  = root_object.getJSONArray("items");

            search_results = new Vector();

            for (int i = 0; i < root_items.length(); i++) {
                if (!root_items.isNull(i)) {
                    String video_id          = "";
                    String video_title       = "";
                    String video_description = "";
                    String thumbnail_url     = "";

                    JSONObject item_object = root_items.getJSONObject(i);
                    
                    if (item_object.optJSONObject("id") != null) {
                        JSONObject id_object = item_object.getJSONObject("id");
                        
                        if (id_object.optString("kind", "").equals("youtube#video")) {
                            video_id = id_object.optString("videoId", "");
                        }
                    }
                    
                    if (item_object.optJSONObject("snippet") != null) {
                        JSONObject snippet_object = item_object.getJSONObject("snippet");
                        
                        video_title       = snippet_object.optString("title", "");
                        video_description = snippet_object.optString("description", "");
                        
                        if (snippet_object.optJSONObject("thumbnails") != null) {
                            JSONObject thumbnails_object = snippet_object.getJSONObject("thumbnails");
                            
                            if (thumbnails_object.optJSONObject("medium") != null) {
                                thumbnail_url = thumbnails_object.getJSONObject("medium").optString("url", "");
                            } else if (thumbnails_object.optJSONObject("default") != null) {
                                thumbnail_url = thumbnails_object.getJSONObject("default").optString("url", "");
                            }
                        }
                    }

                    if (!video_title.equals("") && !video_description.equals("") &&
                        !video_id.equals("")    && !thumbnail_url.equals("")) {
                        search_results.addElement(new VideoClass(video_id, video_title, video_description, thumbnail_url));
                    }
                }
            }
        } catch (Exception ex) {
            exception = ex;
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
        if (exception != null) {
            throw (exception);
        } else {
            SearchResults = search_results;
        }
    }

    public static Vector GetAvailableFormats(String video_id) {
        int                   chars_read;
        StringBuffer          buffer;
        Vector                result = null;
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

                result = new Vector();

                Enumeration url_encoded_fmt_stream_map_enum = url_encoded_fmt_stream_map.elements();

                while (url_encoded_fmt_stream_map_enum.hasMoreElements()) {
                    tmpstr = (String)url_encoded_fmt_stream_map_enum.nextElement();

                    begin = tmpstr.indexOf("itag=");

                    if (begin != -1) {
                        end = tmpstr.indexOf("&", begin + 5);

                        if (end == -1) {
                            end = tmpstr.length();
                        }

                        result.addElement(Integer.valueOf(tmpstr.substring(begin + 5, end)));
                    }
                }
            } else {
                result = null;
            }
        } catch (Exception ex) {
            result = null;
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
}
