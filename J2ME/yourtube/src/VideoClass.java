public class VideoClass extends Object {
    private String VideoId, Title, Description, ThumbnailURL;

    public VideoClass(String video_id, String title, String description, String thumbnail_url) {
        super();

        VideoId      = video_id;
        Title        = title;
        Description  = description;
        ThumbnailURL = thumbnail_url;
    }

    public String GetVideoId() {
        return VideoId;
    }

    public String GetTitle() {
        return Title;
    }

    public String GetDescription() {
        return Description;
    }

    public String GetThumbnailURL() {
        return ThumbnailURL;
    }
}
