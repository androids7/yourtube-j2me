import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

public class YourTube extends MIDlet implements CommandListener {
    private static final int APP_STARTED   = 0;
    private static final int APP_ACTIVE    = 1;
    private static final int APP_DESTROYED = 2;

    private static final Command CMD_OK            = new Command("OK",               Command.OK,     1);
    private static final Command CMD_BACK          = new Command("Back",             Command.BACK,   1);
    private static final Command CMD_EXIT          = new Command("Exit",             Command.EXIT,   2);

    private static final Command CMD_REFRESH       = new Command("Refresh",          Command.SCREEN, 1);

    private static final Command CMD_SEARCH        = new Command("Search",           Command.OK,     1);
    private static final Command CMD_DOWNLOAD      = new Command("Download",         Command.SCREEN, 2);
    private static final Command CMD_PROPERTIES    = new Command("Video Properties", Command.SCREEN, 2);
    private static final Command CMD_DOWNLOADSCR   = new Command("Downloads Screen", Command.SCREEN, 2);

    private static final Command CMD_DELETE        = new Command("Delete",           Command.ITEM,   1);
    private static final Command CMD_SEARCHSCR     = new Command("Search Screen",    Command.SCREEN, 2);
    
    private static final Command CMD_SETTINGS      = new Command("Settings",         Command.SCREEN, 2);
    private static final Command CMD_ABOUT         = new Command("About",            Command.SCREEN, 2);
    private static final Command CMD_HELP          = new Command("Help",             Command.HELP,   2);

    private static final String APP_ABOUT_ICON = "/icons/icon-about.png";

    private static final String VIDEO_FORMAT_NAMES[]      = {"MP4 (H.264 720p HD)", "MP4 (H.264 360p)", "FLV (H.264 480p)", "FLV (H.264 360p)", "FLV (H.263 240p)", "3GP (MPEG-4 240p)", "3GP (MPEG-4 144p)"};
    private static final String VIDEO_FORMAT_EXTENSIONS[] = {"mp4",                 "mp4",              "flv",              "flv",              "flv",              "3gp",               "3gp"};
    private static final int    VIDEO_FORMAT_IDS[]        = {22,                    18,                 35,                 34,                 5,                  36,                  17};

    private static final String PREVIEW_FORMATS[]         = {"Video", "Thumbnail"};
    private static final int    PREVIEW_FORMAT_VIDEO      = 0;
    private static final int    PREVIEW_FORMAT_THUMBNAIL  = 1;

    private static final int SLEEP_DELAY              = 1000;
    private static final int MAX_FNAME_DUPCHECK_TRIES = 100;

    private boolean PauseUpdate,
                    StopUpdate;

    private int AppState = APP_STARTED,
                SearchResultsSelectedIndex = -1,
                DownloadsListIds[];

    private String SearchString = "";

    private Vector AvailableVideoFormatNames = null,
                   AvailableVideoFormatExtensions = null,
                   AvailableVideoFormatIds = null;

    private Thread DownloadsListUpdateThread = null,
                   BgOperationThread = null;

    private Displayable LastDisplayable = null;
    private Alert       InitErrorAlert = null,
                        ProgressAlert = null;
    private List        DownloadsList = null;
    private Form        AboutForm = null,
                        HelpForm = null,
                        SearchForm = null,
                        PropertiesForm = null,
                        DownloadForm = null,
                        DeleteDownloadForm = null,
                        SettingsForm = null;
    private TextField   SearchSearchStringTextField = null,
                        DownloadFileNameTextField = null;
    private ChoiceGroup SearchSearchResultsChoiceGroup = null,
                        DownloadVideoFormatChoiceGroup = null,
                        SettingsPreviewFormatChoiceGroup = null,
                        SettingsDstFSRootChoiceGroup = null;
    private Player      PropertiesVideoPlayer = null;

    private void StartDownloadsListUpdate() {
        synchronized (this) {
            if (DownloadsListUpdateThread == null || !DownloadsListUpdateThread.isAlive()) {
                PauseUpdate = false;
                StopUpdate  = false;

                DownloadsListUpdateThread = new Thread() {
                    public void run() {
                        while (!DownloadsListUpdateStopped()) {
                            try {
                                Thread.sleep(SLEEP_DELAY);
                            } catch (Exception ex) {
                                // Ignore
                            }

                            UpdateDownloadsList();
                        }
                    }
                };

                DownloadsListUpdateThread.start();
            }
        }
    }

    private void StopDownloadsListUpdate() {
        synchronized (this) {
            StopUpdate = true;
        }
    }

    private void PauseDownloadsListUpdate() {
        synchronized (this) {
            PauseUpdate = true;
        }
    }

    private void ResumeDownloadsListUpdate() {
        synchronized (this) {
            PauseUpdate = false;
        }
    }

    private boolean DownloadsListUpdatePaused() {
        synchronized (this) {
            return PauseUpdate;
        }
    }

    private boolean DownloadsListUpdateStopped() {
        synchronized (this) {
            return StopUpdate;
        }
    }

    private void ShowInitErrorMessage(String text) {
        InitErrorAlert = new Alert("Application Initialization Error", text, null, AlertType.ALARM);

        InitErrorAlert.setTimeout(Alert.FOREVER);
        InitErrorAlert.addCommand(CMD_EXIT);
        InitErrorAlert.setCommandListener(this);

        Display.getDisplay(this).setCurrent(InitErrorAlert);
    }

    private void ShowErrorMessage(String text) {
        Alert alert = new Alert("Error", text, null, AlertType.ERROR);
        alert.setTimeout(Alert.FOREVER);

        Display.getDisplay(this).setCurrent(alert, Display.getDisplay(this).getCurrent());
    }

    private void ShowErrorMessage(String text, Displayable next) {
        Alert alert = new Alert("Error", text, null, AlertType.ERROR);
        alert.setTimeout(Alert.FOREVER);

        Display.getDisplay(this).setCurrent(alert, next);
    }

    private void ShowProgressMessage(String text) {
        ProgressAlert = new Alert("Operation In Progress", text, null, AlertType.INFO);

        ProgressAlert.setTimeout(Alert.FOREVER);
        ProgressAlert.addCommand(CMD_REFRESH);
        ProgressAlert.setCommandListener(this);

        Display.getDisplay(this).setCurrent(ProgressAlert);
    }

    private void ShowAboutForm() {
        String version = getAppProperty("MIDlet-Version");
        String vendor  = getAppProperty("MIDlet-Vendor");
        String url     = getAppProperty("MIDlet-Info-URL");

        if (version == null) {
            version = "Unknown version";
        }
        if (vendor == null) {
            vendor = "Unknown vendor";
        }
        if (url == null) {
            url = "Unknown info URL";
        }

        AboutForm = new Form("About");
        try {
            ImageItem icon = new ImageItem(null, Image.createImage(APP_ABOUT_ICON),
                                           Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER, null);

            AboutForm.append(icon);
        } catch (Exception ex) {
            // Ignore
        }
        AboutForm.append("\n" + "YourTube Version " + version + "\n\n" + "Developer: " + vendor + "\n\n" + url);
        AboutForm.addCommand(CMD_BACK);
        AboutForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(AboutForm);
    }

    private void ShowHelpForm() {
        String url = getAppProperty("MIDlet-Info-URL");

        if (url == null) {
            url = "Unknown info URL";
        }

        HelpForm = new Form("Help");
        HelpForm.append("YourTube is an open source J2ME YouTube video downloader for mobile phones." + "\n\n" +
                        "If you have any questions regarding this application, you can contact application developer on this website:" + "\n\n" +
                        url);
        HelpForm.addCommand(CMD_BACK);
        HelpForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(HelpForm);
    }

    private void ShowSearchForm() {
        String youtube_video_names[];
        Vector youtube_videos;

        youtube_videos = APIClass.GetSearchResults();

        if (youtube_videos != null) {
            youtube_video_names = new String[youtube_videos.size()];

            for (int i = 0; i < youtube_videos.size(); i++) {
                youtube_video_names[i] = ((VideoClass)youtube_videos.elementAt(i)).GetVisibleName();
            }
        } else {
            youtube_video_names = new String[0];
        }

        SearchSearchStringTextField    = new TextField("YouTube Search", SearchString, 128, TextField.ANY);
        SearchSearchResultsChoiceGroup = new ChoiceGroup("Search Results", ChoiceGroup.EXCLUSIVE, youtube_video_names, null);

        if (SearchResultsSelectedIndex >= 0 && SearchResultsSelectedIndex < youtube_video_names.length) {
            SearchSearchResultsChoiceGroup.setSelectedIndex(SearchResultsSelectedIndex, true);
        }
        SearchSearchResultsChoiceGroup.setFitPolicy(ChoiceGroup.TEXT_WRAP_ON);

        SearchForm = new Form("YouTube Search");
        SearchForm.append(SearchSearchStringTextField);
        SearchForm.append(SearchSearchResultsChoiceGroup);
        SearchForm.addCommand(CMD_SEARCH);
        SearchForm.addCommand(CMD_DOWNLOAD);
        SearchForm.addCommand(CMD_PROPERTIES);
        SearchForm.addCommand(CMD_DOWNLOADSCR);
        SearchForm.addCommand(CMD_SETTINGS);
        SearchForm.addCommand(CMD_ABOUT);
        SearchForm.addCommand(CMD_HELP);
        SearchForm.addCommand(CMD_EXIT);
        SearchForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(SearchForm);
    }

    private void AppendVideoPlayerToPropertiesForm(VideoClass youtube_video) throws Exception {
        PropertiesVideoPlayer = Manager.createPlayer(youtube_video.GetPreviewURL());
        PropertiesVideoPlayer.realize();

        VideoControl control = (VideoControl)PropertiesVideoPlayer.getControl("VideoControl");
        Item         video   = (Item)control.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);

        video.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        PropertiesForm.append(video);

        PropertiesVideoPlayer.start();
    }

    private void AppendThumbnailToPropertiesForm(Image video_thumbnail) {
        int image_width;

        if (PropertiesForm.getWidth() < PropertiesForm.getHeight()) {
            image_width = PropertiesForm.getWidth()  - 10;
        } else {
            image_width = PropertiesForm.getHeight() - 10;
        }

        if (video_thumbnail != null) {
            video_thumbnail = UtilClass.ResizeImageToWidth(video_thumbnail, image_width);

            if (video_thumbnail != null) {
                ImageItem thumbnail = new ImageItem(null, video_thumbnail, Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER, null);

                PropertiesForm.append(thumbnail);
            }
        }
    }

    private void ShowPropertiesForm(VideoClass youtube_video, Image video_thumbnail) {
        StringItem str_item;

        PropertiesForm = new Form("YouTube Video Properties");

        if (SettingStorageClass.GetPreviewFormat() == PREVIEW_FORMAT_VIDEO) {
            try {
                AppendVideoPlayerToPropertiesForm(youtube_video);
            } catch (Exception ex) {
                PropertiesForm.deleteAll();

                AppendThumbnailToPropertiesForm(video_thumbnail);
            }
        } else {
            AppendThumbnailToPropertiesForm(video_thumbnail);
        }

        str_item = new StringItem("", youtube_video.GetVisibleName());
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        PropertiesForm.append(str_item);

        str_item = new StringItem("", " ");
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        PropertiesForm.append(str_item);

        str_item = new StringItem("", "By " + youtube_video.GetAuthor() + ", " + String.valueOf(youtube_video.GetViewCount()) + " views");
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_SMALL));
        str_item.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        PropertiesForm.append(str_item);

        str_item = new StringItem("", " ");
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        PropertiesForm.append(str_item);

        str_item = new StringItem("", youtube_video.GetDescription());
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_LEFT);

        PropertiesForm.append(str_item);

        PropertiesForm.addCommand(CMD_BACK);
        PropertiesForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(PropertiesForm);
    }

    private void ShowDownloadForm(VideoClass youtube_video) {
        String file_name = "";

        for (int i = 0; i < MAX_FNAME_DUPCHECK_TRIES; i++) {
            if (i == 0) {
                file_name = UtilClass.MakeValidFilename(youtube_video.GetTitle());
            } else {
                file_name = UtilClass.MakeValidFilename(youtube_video.GetTitle() + " (" + String.valueOf(i) + ")");
            }

            if (DownloadStorageClass.ValidateFileNameForDups(file_name)) {
                break;
            }
        }

        int selected_format = 0;

        for (int i = 0; i < AvailableVideoFormatIds.size(); i++) {
            if (((Integer)AvailableVideoFormatIds.elementAt(i)).intValue() == SettingStorageClass.GetVideoFormatId()) {
                selected_format = i;
            }
        }

        String avail_video_fmt_names[] = new String[AvailableVideoFormatNames.size()];

        for (int i = 0; i < AvailableVideoFormatNames.size(); i++) {
            avail_video_fmt_names[i] = (String)AvailableVideoFormatNames.elementAt(i);
        }

        DownloadFileNameTextField      = new TextField("File Name", file_name, 256, TextField.ANY);
        DownloadVideoFormatChoiceGroup = new ChoiceGroup("Video Format", ChoiceGroup.EXCLUSIVE, avail_video_fmt_names, null);

        DownloadVideoFormatChoiceGroup.setSelectedIndex(selected_format, true);

        DownloadForm = new Form("Download YouTube Video");
        DownloadForm.append(DownloadFileNameTextField);
        DownloadForm.append(DownloadVideoFormatChoiceGroup);
        DownloadForm.addCommand(CMD_OK);
        DownloadForm.addCommand(CMD_BACK);
        DownloadForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(DownloadForm);
    }

    private void ShowDownloadsList() {
        String download_names[];
        Vector downloads;

        synchronized (this) {
            try {
                downloads = DownloadStorageClass.GetListOfCopiesSortedByState();

                DownloadsListIds = new int[downloads.size()];
                download_names   = new String[downloads.size()];

                for (int i = 0; i < downloads.size(); i++) {
                    DownloadsListIds[i] = ((DownloadClass)downloads.elementAt(i)).GetId();
                    download_names[i]   = ((DownloadClass)downloads.elementAt(i)).GetVisibleName();
                }

                DownloadsList = new List("YouTube Downloads", Choice.IMPLICIT, download_names, null);

                DownloadsList.setFitPolicy(List.TEXT_WRAP_ON);

                DownloadsList.addCommand(CMD_DELETE);
                DownloadsList.addCommand(CMD_SEARCHSCR);
                DownloadsList.addCommand(CMD_SETTINGS);
                DownloadsList.addCommand(CMD_ABOUT);
                DownloadsList.addCommand(CMD_HELP);
                DownloadsList.addCommand(CMD_EXIT);
                DownloadsList.setCommandListener(this);

                Display.getDisplay(this).setCurrent(DownloadsList);
            } catch (Exception ex) {
                ShowErrorMessage(ex.toString());
            }
        }
    }

    private void UpdateDownloadsList() {
        int    selected_download = 0, selected_download_id;
        Vector downloads;

        synchronized (this) {
            if (DownloadsList != null && !DownloadsListUpdatePaused()) {
                try {
                    if (DownloadsList.getSelectedIndex() != -1) {
                        selected_download_id = DownloadsListIds[DownloadsList.getSelectedIndex()];
                    } else {
                        selected_download_id = 0;
                    }

                    downloads = DownloadStorageClass.GetListOfCopiesSortedByState();

                    DownloadsListIds = new int[downloads.size()];

                    for (int i = 0; i < downloads.size(); i++) {
                        if (selected_download_id == ((DownloadClass)downloads.elementAt(i)).GetId()) {
                            selected_download = i;
                        }

                        DownloadsListIds[i] = ((DownloadClass)downloads.elementAt(i)).GetId();

                        if (i < DownloadsList.size()) {
                            DownloadsList.set(i, ((DownloadClass)downloads.elementAt(i)).GetVisibleName(), null);
                        } else {
                            DownloadsList.append(((DownloadClass)downloads.elementAt(i)).GetVisibleName(), null);
                        }
                    }

                    for (int i = downloads.size(); i < DownloadsList.size(); i++) {
                        DownloadsList.delete(i);
                    }

                    if (DownloadsList.size() != 0) {
                        DownloadsList.setSelectedIndex(selected_download, true);
                    }
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
    }

    private void ShowDeleteDownloadForm(String header, String text) {
        DeleteDownloadForm = new Form(header);
        DeleteDownloadForm.append(text);
        DeleteDownloadForm.addCommand(CMD_OK);
        DeleteDownloadForm.addCommand(CMD_BACK);
        DeleteDownloadForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(DeleteDownloadForm);
    }

    private void ShowSettingsForm() {
        int    selected_format = 0,
               selected_disk   = 0;
        String root_names[];
        Vector roots;

        if (SettingStorageClass.GetPreviewFormat() == PREVIEW_FORMAT_VIDEO) {
            selected_format = PREVIEW_FORMAT_VIDEO;
        } else if (SettingStorageClass.GetPreviewFormat() == PREVIEW_FORMAT_THUMBNAIL) {
            selected_format = PREVIEW_FORMAT_THUMBNAIL;
        }

        roots = UtilClass.GetFileSystemRoots();

        root_names = new String[roots.size()];

        for (int i = 0; i < roots.size(); i++) {
            root_names[i] = (String)roots.elementAt(i);

            if (SettingStorageClass.GetDestinationDisk().equals((String)roots.elementAt(i))) {
                selected_disk = i;
            }
        }

        SettingsPreviewFormatChoiceGroup = new ChoiceGroup("Preferred Preview Format", ChoiceGroup.POPUP, PREVIEW_FORMATS, null);
        SettingsDstFSRootChoiceGroup     = new ChoiceGroup("Destination Disk", ChoiceGroup.POPUP, root_names, null);

        SettingsPreviewFormatChoiceGroup.setSelectedIndex(selected_format, true);

        if (SettingsDstFSRootChoiceGroup.size() != 0) {
            SettingsDstFSRootChoiceGroup.setSelectedIndex(selected_disk, true);
        }

        SettingsForm = new Form("Settings");
        SettingsForm.append(SettingsPreviewFormatChoiceGroup);
        SettingsForm.append(SettingsDstFSRootChoiceGroup);
        SettingsForm.addCommand(CMD_OK);
        SettingsForm.addCommand(CMD_BACK);
        SettingsForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(SettingsForm);
    }

    public void startApp() {
        synchronized (this) {
            if (AppState == APP_STARTED) {
                if (UtilClass.ValidateFileConnectionAPI()) {
                    try {
                        SettingStorageClass.Initialize();
                        DownloadStorageClass.Initialize();
                        DownloaderClass.Initialize();

                        if (SettingStorageClass.GetShowSettingsOnLaunch()) {
                            SettingStorageClass.SetShowSettingsOnLaunch(false);
                            
                            LastDisplayable = null;
                            
                            ShowSettingsForm();
                        } else {
                            ShowSearchForm();
                        }

                        StartDownloadsListUpdate();
                    } catch (Exception ex) {
                        ShowInitErrorMessage(ex.toString());
                    }
                } else {
                    ShowInitErrorMessage("Application is not available on this platform");
                }
            }

            AppState = APP_ACTIVE;
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        synchronized (this) {
            if (AppState == APP_ACTIVE) {
                StopDownloadsListUpdate();

                DownloaderClass.Destroy();
            }

            AppState = APP_DESTROYED;
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        synchronized (this) {
            if (command == CMD_EXIT) {
                destroyApp(true);

                notifyDestroyed();
            } else if (displayable.equals(ProgressAlert)) {
                // Ignore
            } else if (displayable.equals(AboutForm)) {
                if (command == CMD_BACK) {
                    if (LastDisplayable == SearchForm) {
                        ShowSearchForm();
                    } else if (LastDisplayable == DownloadsList) {
                        ShowDownloadsList();
                    } else {
                        ShowSearchForm();
                    }
                }
            } else if (displayable.equals(HelpForm)) {
                if (command == CMD_BACK) {
                    if (LastDisplayable == SearchForm) {
                        ShowSearchForm();
                    } else if (LastDisplayable == DownloadsList) {
                        ShowDownloadsList();
                    } else {
                        ShowSearchForm();
                    }
                }
            } else if (displayable.equals(SearchForm)) {
                if (command == CMD_SEARCH) {
                    SearchResultsSelectedIndex = -1;
                    SearchString               = SearchSearchStringTextField.getString();

                    final String search_string = SearchString;

                    if (!search_string.equals("")) {
                        if (BgOperationThread == null || !BgOperationThread.isAlive()) {
                            BgOperationThread = new Thread() {
                                public void run() {
                                    try {
                                        ShowProgressMessage("Searching YouTube...");

                                        APIClass.MakeSearch(search_string);

                                        ShowSearchForm();
                                    } catch (Exception ex) {
                                        ShowErrorMessage("Please check your Internet connection. " + ex.toString(), SearchForm);
                                    }
                                }
                            };

                            BgOperationThread.start();
                        }
                    }
                } else if (command == CMD_DOWNLOAD) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    if (SearchSearchResultsChoiceGroup.getSelectedIndex() != -1) {
                        final VideoClass youtube_video = (VideoClass)(APIClass.GetSearchResults().elementAt(SearchSearchResultsChoiceGroup.getSelectedIndex()));

                        if (BgOperationThread == null || !BgOperationThread.isAlive()) {
                            BgOperationThread = new Thread() {
                                public void run() {
                                    ShowProgressMessage("Loading video properties...");

                                    Vector available_formats = APIClass.GetAvailableFormats(youtube_video.GetVideoId());

                                    if (available_formats != null) {
                                        AvailableVideoFormatNames      = new Vector();
                                        AvailableVideoFormatExtensions = new Vector();
                                        AvailableVideoFormatIds        = new Vector();

                                        for (int i = 0; i < VIDEO_FORMAT_IDS.length; i++) {
                                            if (available_formats.contains(new Integer(VIDEO_FORMAT_IDS[i]))) {
                                                AvailableVideoFormatNames.addElement(VIDEO_FORMAT_NAMES[i]);
                                                AvailableVideoFormatExtensions.addElement(VIDEO_FORMAT_EXTENSIONS[i]);
                                                AvailableVideoFormatIds.addElement(new Integer(VIDEO_FORMAT_IDS[i]));
                                            }
                                        }

                                        if (AvailableVideoFormatNames.size() != 0) {
                                            ShowDownloadForm(youtube_video);
                                        } else {
                                            ShowErrorMessage("No video formats are available for download", SearchForm);
                                        }
                                    } else {
                                        ShowErrorMessage("No video formats are available for download", SearchForm);
                                    }
                                }
                            };

                            BgOperationThread.start();
                        }
                    }
                } else if (command == CMD_PROPERTIES) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    if (SearchSearchResultsChoiceGroup.getSelectedIndex() != -1) {
                        final VideoClass youtube_video = (VideoClass)(APIClass.GetSearchResults().elementAt(SearchSearchResultsChoiceGroup.getSelectedIndex()));

                        if (BgOperationThread == null || !BgOperationThread.isAlive()) {
                            BgOperationThread = new Thread() {
                                public void run() {
                                    ShowProgressMessage("Loading video properties...");

                                    Image video_thumbnail = UtilClass.LoadImageFromURL(youtube_video.GetThumbnailURL());

                                    ShowPropertiesForm(youtube_video, video_thumbnail);
                                }
                            };

                            BgOperationThread.start();
                        }
                    }
                } else if (command == CMD_DOWNLOADSCR) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    ShowDownloadsList();
                } else if (command == CMD_SETTINGS) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();
                    LastDisplayable            = Display.getDisplay(this).getCurrent();

                    ShowSettingsForm();
                } else if (command == CMD_ABOUT) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();
                    LastDisplayable            = Display.getDisplay(this).getCurrent();

                    ShowAboutForm();
                } else if (command == CMD_HELP) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();
                    LastDisplayable            = Display.getDisplay(this).getCurrent();

                    ShowHelpForm();
                }
            } else if (displayable.equals(PropertiesForm)) {
                if (command == CMD_BACK) {
                    if (PropertiesVideoPlayer != null && PropertiesVideoPlayer.getState() != Player.CLOSED) {
                        PropertiesVideoPlayer.deallocate();
                        PropertiesVideoPlayer.close();
                    }

                    ShowSearchForm();
                }
            } else if (displayable.equals(DownloadForm)) {
                if (command == CMD_OK) {
                    if (SearchSearchResultsChoiceGroup.getSelectedIndex() != -1) {
                        if (DownloadVideoFormatChoiceGroup.getSelectedIndex() != -1) {
                            if (!DownloadFileNameTextField.getString().equals("")) {
                                try {
                                    SettingStorageClass.SetVideoFormatId(((Integer)AvailableVideoFormatIds.elementAt(DownloadVideoFormatChoiceGroup.getSelectedIndex())).intValue());

                                    VideoClass youtube_video = (VideoClass)(APIClass.GetSearchResults().elementAt(SearchSearchResultsChoiceGroup.getSelectedIndex()));

                                    DownloadClass download = new DownloadClass(DownloadClass.STATE_QUEUED,
                                                                               ((Integer)AvailableVideoFormatIds.elementAt(DownloadVideoFormatChoiceGroup.getSelectedIndex())).intValue(),
                                                                               0, 0, youtube_video.GetTitle(), youtube_video.GetVideoId(),
                                                                               DownloadFileNameTextField.getString(),
                                                                               (String)AvailableVideoFormatExtensions.elementAt(DownloadVideoFormatChoiceGroup.getSelectedIndex()),
                                                                               "");

                                    if (DownloadStorageClass.Validate(download)) {
                                        try {
                                            DownloadStorageClass.Add(download);

                                            ShowDownloadsList();
                                        } catch (Exception ex) {
                                            ShowErrorMessage(ex.toString());
                                        }
                                    } else {
                                        ShowErrorMessage(DownloadStorageClass.GetValidateErrorMsg());
                                    }
                                } catch (Exception ex) {
                                    ShowErrorMessage(ex.toString());
                                }
                            } else {
                                ShowErrorMessage("File name is empty");
                            }
                        } else {
                            ShowErrorMessage("No video format selected");
                        }
                    }
                } else if (command == CMD_BACK) {
                    ShowSearchForm();
                }
            } else if (displayable.equals(DownloadsList)) {
                if (command == CMD_DELETE) {
                    if (DownloadsList.getSelectedIndex() != -1) {
                        PauseDownloadsListUpdate();

                        ShowDeleteDownloadForm("Delete Download", "Delete download \"" +
                                                DownloadStorageClass.GetCopy(DownloadsListIds[DownloadsList.getSelectedIndex()]).GetTitle() + "\" ?");
                    }
                } else if (command == CMD_SEARCHSCR) {
                    ShowSearchForm();
                } else if (command == CMD_SETTINGS) {
                    LastDisplayable = Display.getDisplay(this).getCurrent();

                    ShowSettingsForm();
                } else if (command == CMD_ABOUT) {
                    LastDisplayable = Display.getDisplay(this).getCurrent();

                    ShowAboutForm();
                } else if (command == CMD_HELP) {
                    LastDisplayable = Display.getDisplay(this).getCurrent();

                    ShowHelpForm();
                }
            } else if (displayable.equals(DeleteDownloadForm)) {
                if (command == CMD_OK) {
                    if (DownloadsList.getSelectedIndex() != -1) {
                        try {
                            DownloadStorageClass.Delete(DownloadsListIds[DownloadsList.getSelectedIndex()]);

                            ShowDownloadsList();

                            ResumeDownloadsListUpdate();
                        } catch (Exception ex) {
                            ShowErrorMessage(ex.toString());
                        }
                    }
                } else if (command == CMD_BACK) {
                    ShowDownloadsList();

                    ResumeDownloadsListUpdate();
                }
            } else if (displayable.equals(SettingsForm)) {
                if (command == CMD_OK) {
                    if (SettingsPreviewFormatChoiceGroup.getSelectedIndex() != -1) {
                        if (SettingsDstFSRootChoiceGroup.getSelectedIndex() != -1) {
                            try {
                                SettingStorageClass.SetPreviewFormat(SettingsPreviewFormatChoiceGroup.getSelectedIndex());
                                SettingStorageClass.SetDestinationDisk(SettingsDstFSRootChoiceGroup.getString(SettingsDstFSRootChoiceGroup.getSelectedIndex()));

                                if (LastDisplayable == SearchForm) {
                                    ShowSearchForm();
                                } else if (LastDisplayable == DownloadsList) {
                                    ShowDownloadsList();
                                } else {
                                    ShowSearchForm();
                                }
                            } catch (Exception ex) {
                                ShowErrorMessage(ex.toString());
                            }
                        } else {
                            ShowErrorMessage("No disk selected");
                        }
                    } else {
                        ShowErrorMessage("No preferred preview format selected");
                    }
                } else if (command == CMD_BACK) {
                    if (LastDisplayable == SearchForm) {
                        ShowSearchForm();
                    } else if (LastDisplayable == DownloadsList) {
                        ShowDownloadsList();
                    } else {
                        ShowSearchForm();
                    }
                }
            }
        }
    }
}
