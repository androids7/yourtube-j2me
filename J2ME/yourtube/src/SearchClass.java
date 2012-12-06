import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import com.alsutton.xmlparser.objectmodel.*;

public class SearchClass extends Object {
    private static final String YOUTUBE_SEARCH_URL         = "http://gdata.youtube.com/feeds/api/videos";
    private static final String YOUTUBE_SEARCH_SORT_ORDER  = "relevance";
    private static final String YOUTUBE_SEARCH_START_INDEX = "1";
    private static final String YOUTUBE_SEARCH_MAX_RESULTS = "25";
    private static final String YOUTUBE_SEARCH_ALT         = "atom";
    private static final String USER_AGENT                 = "YourTube";

    private static Vector SearchResults = null;

    public static Vector GetSearchResults() {
        return SearchResults;
    }

    public static void MakeSearch(String search_string) throws Exception {
        Vector            search_results = null;
        Exception         exception = null;
        HttpConnection    connection = null;
        InputStream       stream = null;
        InputStreamReader stream_reader = null;

        try {
            connection = (HttpConnection)Connector.open(YOUTUBE_SEARCH_URL +
                                                        "?vq="             + UtilClass.URLEncode(search_string) +
                                                        "&orderby="        + YOUTUBE_SEARCH_SORT_ORDER +
                                                        "&start-index="    + YOUTUBE_SEARCH_START_INDEX +
                                                        "&max-results="    + YOUTUBE_SEARCH_MAX_RESULTS +
                                                        "&alt="            + YOUTUBE_SEARCH_ALT);

            connection.setRequestMethod(HttpConnection.GET);
            connection.setRequestProperty("User-Agent", USER_AGENT);

            stream        = connection.openDataInputStream();
            stream_reader = new InputStreamReader(stream, "utf-8");

            TreeBuilder builder = new TreeBuilder();
            Node        root    = builder.createTree(stream_reader);

            search_results = new Vector();

            if (root.getName().equals("feed")) {
                for (int i = 0; i < root.children.size(); i++) {
                    Node entry = (Node)root.children.elementAt(i);

                    if (entry.getName().equals("entry")) {
                        int    max_thumbnail_width = 0;
                        int    best_preview_format = 0;
                        int    video_duration      = 0;
                        int    view_count          = 0;
                        String video_title         = "";
                        String video_description   = "";
                        String video_author        = "";
                        String video_url           = "";
                        String thumbnail_url       = "";
                        String preview_url         = "";

                        for (int j = 0; j < entry.children.size(); j++) {
                            Node item = (Node)entry.children.elementAt(j);

                            if (item.getName().equals("title")) {
                                if (item.attributes.get("type") != null && item.attributes.get("type").equals("text")) {
                                    video_title = item.getText();
                                }
                            } else if (item.getName().equals("content")) {
                                if (item.attributes.get("type") != null && item.attributes.get("type").equals("text")) {
                                    video_description = item.getText();
                                }
                            } else if (item.getName().equals("author")) {
                                for (int k = 0; k < item.children.size(); k++) {
                                    Node author_item = (Node)item.children.elementAt(k);

                                    if (author_item.getName().equals("name")) {
                                        video_author = author_item.getText();
                                    }
                                }
                            } else if (item.getName().equals("media:group")) {
                                for (int k = 0; k < item.children.size(); k++) {
                                    Node media_item = (Node)item.children.elementAt(k);

                                    if (media_item.getName().equals("media:player") && media_item.attributes.get("url") != null) {
                                        video_url = (String)media_item.attributes.get("url");
                                    } else if (media_item.getName().equals("media:thumbnail") && media_item.attributes.get("url") != null) {
                                        if (media_item.attributes.get("width") != null) {
                                            try {
                                                int width = Integer.parseInt((String)media_item.attributes.get("width"));

                                                if (width > max_thumbnail_width) {
                                                    max_thumbnail_width = width;
                                                    thumbnail_url       = (String)media_item.attributes.get("url");
                                                }
                                            } catch (Exception ex) {
                                                // Ignore
                                            }
                                        }
                                    } else if (media_item.getName().equals("media:content") && media_item.attributes.get("url") != null &&
                                               media_item.attributes.get("type") != null && media_item.attributes.get("type").equals("video/3gpp")) {
                                        if (media_item.attributes.get("yt:format") != null) {
                                            try {
                                                int format = Integer.parseInt((String)media_item.attributes.get("yt:format"));

                                                if (format > best_preview_format) {
                                                    best_preview_format = format;
                                                    preview_url         = (String)media_item.attributes.get("url");
                                                }
                                            } catch (Exception ex) {
                                                // Ignore
                                            }
                                        }
                                    } else if (media_item.getName().equals("yt:duration") && media_item.attributes.get("seconds") != null) {
                                        try {
                                            video_duration = Integer.parseInt((String)media_item.attributes.get("seconds"));
                                        } catch (Exception ex) {
                                            // Ignore
                                        }
                                    }
                                }
                            } else if (item.getName().equals("yt:statistics") && item.attributes.get("viewCount") != null) {
                                try {
                                    view_count = Integer.parseInt((String)item.attributes.get("viewCount"));
                                } catch (Exception ex) {
                                    // Ignore
                                }
                            }
                        }

                        if (video_duration != 0 && !video_title.equals("") && !video_description.equals("") && !video_author.equals("") &&
                                                   !video_url.equals("")   && !thumbnail_url.equals("")     && !preview_url.equals("")) {
                            search_results.addElement(new VideoClass(video_duration, view_count, video_title, video_description, video_author, video_url, thumbnail_url, preview_url));
                        }
                    }
                }
            } else {
                throw (new Exception("Invalid XML data"));
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
}
