import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class YourMusic extends MIDlet implements CommandListener {
    private static final int APP_STARTED   = 0;
    private static final int APP_ACTIVE    = 1;
    private static final int APP_DESTROYED = 2;

    private static final String APP_ABOUT_ICON = "/icons/icon-about.png";

    private static final int SLEEP_DELAY              = 1000;
    private static final int MAX_FNAME_DUPCHECK_TRIES = 100;

    private static Command CMD_OK               = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDOK_LABEL),               Command.OK,     1);
    private static Command CMD_BACK             = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDBACK_LABEL),             Command.BACK,   1);
    private static Command CMD_EXIT             = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDEXIT_LABEL),             Command.EXIT,   2);

    private static Command CMD_REFRESH          = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDREFRESH_LABEL),          Command.SCREEN, 1);

    private static Command CMD_SEARCH           = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDSEARCH_LABEL),           Command.OK,     1);
    private static Command CMD_DOWNLOAD         = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDDOWNLOAD_LABEL),         Command.SCREEN, 2);
    private static Command CMD_PROPERTIES       = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDPROPERTIES_LABEL),       Command.SCREEN, 2);
    private static Command CMD_DOWNLOADS_SCREEN = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDDOWNLOADSSCREEN_LABEL),  Command.SCREEN, 2);
    private static Command CMD_SETTINGS         = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDSETTINGS_LABEL),         Command.SCREEN, 2);
    private static Command CMD_ABOUT            = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDABOUT_LABEL),            Command.SCREEN, 2);
    private static Command CMD_HELP             = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDHELP_LABEL),             Command.HELP,   2);

    private static Command CMD_DELETE           = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDDELETE_LABEL),           Command.ITEM,   1);

    private boolean PauseUpdate,
                    StopUpdate;

    private int AppState = APP_STARTED,
                SearchResultsSelectedIndex = -1,
                DownloadsListIds[];

    private String SearchString = "";

    private Thread DownloadsListUpdateThread = null,
                   BgOperationThread = null;

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
                        SettingsDstFSRootChoiceGroup = null;

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
        InitErrorAlert = new Alert(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_INITERRORALERT_TITLE), text, null, AlertType.ALARM);

        InitErrorAlert.setTimeout(Alert.FOREVER);
        InitErrorAlert.addCommand(CMD_EXIT);
        InitErrorAlert.setCommandListener(this);

        Display.getDisplay(this).setCurrent(InitErrorAlert);
    }

    private void ShowErrorMessage(String text) {
        Alert alert = new Alert(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ERRORALERT_TITLE), text, null, AlertType.ERROR);
        alert.setTimeout(Alert.FOREVER);

        Display.getDisplay(this).setCurrent(alert, Display.getDisplay(this).getCurrent());
    }

    private void ShowErrorMessage(String text, Displayable next) {
        Alert alert = new Alert(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ERRORALERT_TITLE), text, null, AlertType.ERROR);
        alert.setTimeout(Alert.FOREVER);

        Display.getDisplay(this).setCurrent(alert, next);
    }

    private void ShowProgressMessage(String text) {
        ProgressAlert = new Alert(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_PROGRESSALERT_TITLE), text, null, AlertType.INFO);

        ProgressAlert.setTimeout(Alert.FOREVER);
        ProgressAlert.addCommand(CMD_REFRESH);
        ProgressAlert.setCommandListener(this);

        Display.getDisplay(this).setCurrent(ProgressAlert);
    }

    private void ShowAboutForm() {
        String name    = getAppProperty("MIDlet-Name");
        String version = getAppProperty("MIDlet-Version");
        String vendor  = getAppProperty("MIDlet-Vendor");
        String url     = getAppProperty("MIDlet-Info-URL");

        if (name == null) {
            name = LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ABOUTFORM_UNKNOWNNAME);
        }
        if (version == null) {
            version = LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ABOUTFORM_UNKNOWNVERSION);
        }
        if (vendor == null) {
            vendor = LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ABOUTFORM_UNKNOWNVENDOR);
        }
        if (url == null) {
            url = LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ABOUTFORM_UNKNOWNINFOURL);
        }

        AboutForm = new Form(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ABOUTFORM_TITLE));

        try {
            ImageItem icon = new ImageItem(null, Image.createImage(APP_ABOUT_ICON),
                                           Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER, null);

            AboutForm.append(icon);
        } catch (Exception ex) {
            // Ignore
        }

        Spacer spacer = new Spacer(0, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM).getHeight());
        spacer.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        AboutForm.append(spacer);

        StringItem str_item = new StringItem("", name + " " +
                                                 LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ABOUTFORM_VERSION) + " " + version + "\n\n" +
                                                 LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_ABOUTFORM_DEVELOPER) + " " + vendor + "\n\n" + url);
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_CENTER);

        AboutForm.append(str_item);

        AboutForm.addCommand(CMD_BACK);
        AboutForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(AboutForm);
    }

    private void ShowHelpForm() {
        String url = getAppProperty("MIDlet-Info-URL");

        if (url == null) {
            url = LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_HELPFORM_UNKNOWNINFOURL);
        }

        HelpForm = new Form(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_HELPFORM_TITLE));

        Spacer spacer = new Spacer(0, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM).getHeight());
        spacer.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        HelpForm.append(spacer);

        StringItem str_item = new StringItem("", LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_HELPFORM_TEXT) + "\n\n" + url);
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_CENTER);

        HelpForm.append(str_item);

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

        SearchSearchStringTextField    = new TextField(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_SEARCHSEARCHSTRINGTEXTFIELD_LABEL), SearchString, 128, TextField.ANY);
        SearchSearchResultsChoiceGroup = new ChoiceGroup(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_SEARCHSEARCHRESULTSCHOICEGROUP_LABEL), ChoiceGroup.EXCLUSIVE, youtube_video_names, null);

        if (SearchResultsSelectedIndex >= 0 && SearchResultsSelectedIndex < youtube_video_names.length) {
            SearchSearchResultsChoiceGroup.setSelectedIndex(SearchResultsSelectedIndex, true);
        }
        SearchSearchResultsChoiceGroup.setFitPolicy(ChoiceGroup.TEXT_WRAP_ON);

        SearchForm = new Form(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_SEARCHFORM_TITLE));
        SearchForm.append(SearchSearchStringTextField);
        SearchForm.append(SearchSearchResultsChoiceGroup);
        SearchForm.addCommand(CMD_SEARCH);
        SearchForm.addCommand(CMD_DOWNLOAD);
        SearchForm.addCommand(CMD_PROPERTIES);
        SearchForm.addCommand(CMD_DOWNLOADS_SCREEN);
        SearchForm.addCommand(CMD_SETTINGS);
        SearchForm.addCommand(CMD_ABOUT);
        SearchForm.addCommand(CMD_HELP);
        SearchForm.addCommand(CMD_EXIT);
        SearchForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(SearchForm);
    }

    private void ShowPropertiesForm(VideoClass youtube_video, Image video_thumbnail) {
        int        image_width;
        Spacer     spacer;
        StringItem str_item;

        PropertiesForm = new Form(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_PROPERTIESFORM_TITLE));

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

        spacer = new Spacer(0, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM).getHeight());
        spacer.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        PropertiesForm.append(spacer);

        str_item = new StringItem("", youtube_video.GetVisibleName());
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        PropertiesForm.append(str_item);

        spacer = new Spacer(0, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM).getHeight());
        spacer.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        PropertiesForm.append(spacer);

        str_item = new StringItem("", youtube_video.GetDescription());
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_CENTER);

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

        DownloadFileNameTextField = new TextField(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_DOWNLOADFILENAMETEXTFIELD_LABEL), file_name, 256, TextField.ANY);

        DownloadForm = new Form(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_DOWNLOADFORM_TITLE));
        DownloadForm.append(DownloadFileNameTextField);
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

                DownloadsList = new List(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_DOWNLOADSLIST_TITLE), Choice.IMPLICIT, download_names, null);

                DownloadsList.setFitPolicy(List.TEXT_WRAP_ON);

                DownloadsList.addCommand(CMD_DELETE);
                DownloadsList.addCommand(CMD_BACK);
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

        Spacer spacer = new Spacer(0, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM).getHeight());
        spacer.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);

        DeleteDownloadForm.append(spacer);

        StringItem str_item = new StringItem("", text);
        str_item.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        str_item.setLayout(Item.LAYOUT_CENTER);

        DeleteDownloadForm.append(str_item);

        DeleteDownloadForm.addCommand(CMD_OK);
        DeleteDownloadForm.addCommand(CMD_BACK);
        DeleteDownloadForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(DeleteDownloadForm);
    }

    private void ShowSettingsForm() {
        int    selected_disk = 0;
        String root_names[];
        Vector roots;

        roots = UtilClass.GetFileSystemRoots();

        root_names = new String[roots.size()];

        for (int i = 0; i < roots.size(); i++) {
            root_names[i] = (String)roots.elementAt(i);

            if (SettingStorageClass.GetDestinationDisk().equals((String)roots.elementAt(i))) {
                selected_disk = i;
            }
        }

        SettingsDstFSRootChoiceGroup = new ChoiceGroup(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_SETTINGSDSTFSROOTCHOICEGROUP_LABEL), ChoiceGroup.POPUP, root_names, null);

        if (SettingsDstFSRootChoiceGroup.size() != 0) {
            SettingsDstFSRootChoiceGroup.setSelectedIndex(selected_disk, true);
        }

        SettingsForm = new Form(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_SETTINGSFORM_TITLE));
        SettingsForm.append(SettingsDstFSRootChoiceGroup);
        SettingsForm.addCommand(CMD_OK);
        SettingsForm.addCommand(CMD_BACK);
        SettingsForm.setCommandListener(this);

        Display.getDisplay(this).setCurrent(SettingsForm);
    }

    private void LocalizeCommands() {
        CMD_OK               = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDOK_LABEL),               Command.OK,     1);
        CMD_BACK             = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDBACK_LABEL),             Command.BACK,   1);
        CMD_EXIT             = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDEXIT_LABEL),             Command.EXIT,   2);

        CMD_REFRESH          = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDREFRESH_LABEL),          Command.SCREEN, 1);

        CMD_SEARCH           = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDSEARCH_LABEL),           Command.OK,     1);
        CMD_DOWNLOAD         = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDDOWNLOAD_LABEL),         Command.SCREEN, 2);
        CMD_PROPERTIES       = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDPROPERTIES_LABEL),       Command.SCREEN, 2);
        CMD_DOWNLOADS_SCREEN = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDDOWNLOADSSCREEN_LABEL),  Command.SCREEN, 2);
        CMD_SETTINGS         = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDSETTINGS_LABEL),         Command.SCREEN, 2);
        CMD_ABOUT            = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDABOUT_LABEL),            Command.SCREEN, 2);
        CMD_HELP             = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDHELP_LABEL),             Command.HELP,   2);

        CMD_DELETE           = new Command(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CMDDELETE_LABEL),           Command.ITEM,   1);
    }
    
    public void startApp() {
        synchronized (this) {
            if (AppState == APP_STARTED) {
                if (UtilClass.ValidateFileConnectionAPI()) {
                    try {
                        LocalizationClass.Initialize();
                        SettingStorageClass.Initialize();
                        DownloadStorageClass.Initialize();
                        DownloaderClass.Initialize();

                        LocalizeCommands();

                        if (SettingStorageClass.GetShowSettingsOnLaunch()) {
                            SettingStorageClass.SetShowSettingsOnLaunch(false);
                            
                            ShowSettingsForm();
                        } else {
                            ShowSearchForm();
                        }

                        StartDownloadsListUpdate();
                    } catch (Exception ex) {
                        ShowInitErrorMessage(ex.toString());
                    }
                } else {
                    ShowInitErrorMessage(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_APPUNAVAILABLE));
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
                    ShowSearchForm();
                }
            } else if (displayable.equals(HelpForm)) {
                if (command == CMD_BACK) {
                    ShowSearchForm();
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
                                        ShowProgressMessage(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_SEARCHINPROGRESS));

                                        APIClass.MakeSearch(search_string);

                                        ShowSearchForm();
                                    } catch (Exception ex) {
                                        ShowErrorMessage(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_CHECKCONNECTION) + " " + ex.toString(), SearchForm);
                                    }
                                }
                            };

                            BgOperationThread.start();
                        }
                    }
                } else if (command == CMD_DOWNLOAD) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    if (SearchSearchResultsChoiceGroup.getSelectedIndex() != -1) {
                        ShowDownloadForm((VideoClass)(APIClass.GetSearchResults().elementAt(SearchSearchResultsChoiceGroup.getSelectedIndex())));
                    }
                } else if (command == CMD_PROPERTIES) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    if (SearchSearchResultsChoiceGroup.getSelectedIndex() != -1) {
                        final VideoClass youtube_video = (VideoClass)(APIClass.GetSearchResults().elementAt(SearchSearchResultsChoiceGroup.getSelectedIndex()));

                        if (BgOperationThread == null || !BgOperationThread.isAlive()) {
                            BgOperationThread = new Thread() {
                                public void run() {
                                    ShowProgressMessage(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_LOADINGPROPERTIES));

                                    Image video_thumbnail = UtilClass.LoadImageFromURL(youtube_video.GetThumbnailURL());

                                    ShowPropertiesForm(youtube_video, video_thumbnail);
                                }
                            };

                            BgOperationThread.start();
                        }
                    }
                } else if (command == CMD_DOWNLOADS_SCREEN) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    ShowDownloadsList();
                } else if (command == CMD_SETTINGS) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    ShowSettingsForm();
                } else if (command == CMD_ABOUT) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    ShowAboutForm();
                } else if (command == CMD_HELP) {
                    SearchResultsSelectedIndex = SearchSearchResultsChoiceGroup.getSelectedIndex();

                    ShowHelpForm();
                }
            } else if (displayable.equals(PropertiesForm)) {
                if (command == CMD_BACK) {
                    ShowSearchForm();
                }
            } else if (displayable.equals(DownloadForm)) {
                if (command == CMD_OK) {
                    if (SearchSearchResultsChoiceGroup.getSelectedIndex() != -1) {
                        if (!DownloadFileNameTextField.getString().equals("")) {
                            try {
                                VideoClass youtube_video = (VideoClass)(APIClass.GetSearchResults().elementAt(SearchSearchResultsChoiceGroup.getSelectedIndex()));

                                DownloadClass download = new DownloadClass(DownloadClass.STATE_QUEUED,
                                                                           0, 0, youtube_video.GetTitle(), youtube_video.GetVideoId(),
                                                                           DownloadFileNameTextField.getString(), "mp3", "");

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
                            ShowErrorMessage(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_FILENAMEEMPTY));
                        }
                    }
                } else if (command == CMD_BACK) {
                    ShowSearchForm();
                }
            } else if (displayable.equals(DownloadsList)) {
                if (command == CMD_DELETE) {
                    if (DownloadsList.getSelectedIndex() != -1) {
                        PauseDownloadsListUpdate();

                        ShowDeleteDownloadForm(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_DELETEDOWNLOAD_HEADER),
                                               LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_DELETEDOWNLOAD_TEXT) + " \"" +
                                               DownloadStorageClass.GetCopy(DownloadsListIds[DownloadsList.getSelectedIndex()]).GetTitle() + "\" ?");
                    }
                } else if (command == CMD_BACK) {
                    ShowSearchForm();
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
                    if (SettingsDstFSRootChoiceGroup.getSelectedIndex() != -1) {
                        try {
                            SettingStorageClass.SetDestinationDisk(SettingsDstFSRootChoiceGroup.getString(SettingsDstFSRootChoiceGroup.getSelectedIndex()));

                            ShowSearchForm();
                        } catch (Exception ex) {
                            ShowErrorMessage(ex.toString());
                        }
                    } else {
                        ShowErrorMessage(LocalizationClass.GetLocalizedString(LocalizationClass.YOURMUSIC_NODISKSELECTED));
                    }
                } else if (command == CMD_BACK) {
                    ShowSearchForm();
                }
            }
        }
    }
}
