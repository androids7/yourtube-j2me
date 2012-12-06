public class VideoClass extends Object {
    private int    Duration, ViewCount;
    private String Title, Description, Author, URL, ThumbnailURL, PreviewURL;

    public VideoClass(int duration, int view_count, String title, String description, String author, String url, String thumbnail_url, String preview_url) {
        super();

        Duration     = duration;
        ViewCount    = view_count;
        Title        = title;
        Description  = description;
        Author       = author;
        URL          = url;
        ThumbnailURL = thumbnail_url;
        PreviewURL   = preview_url;
    }

    public int GetDuration() {
        return Duration;
    }

    public int GetViewCount() {
        return ViewCount;
    }

    public String GetTitle() {
        return Title;
    }

    public String GetDescription() {
        return Description;
    }

    public String GetAuthor() {
        return Author;
    }

    public String GetURL() {
        return URL;
    }

    public String GetThumbnailURL() {
        return ThumbnailURL;
    }

    public String GetPreviewURL() {
        return PreviewURL;
    }

    public String GetVisibleName() {
        return Title + " (" + UtilClass.DurationToString(Duration) + ")";
    }
}
