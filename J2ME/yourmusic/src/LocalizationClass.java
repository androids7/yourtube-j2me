import java.util.*;

public class LocalizationClass extends Object {
    public static final String YOURMUSIC_CMDOK_LABEL                          = "OK";
    public static final String YOURMUSIC_CMDBACK_LABEL                        = "Back";
    public static final String YOURMUSIC_CMDEXIT_LABEL                        = "Exit";
    public static final String YOURMUSIC_CMDREFRESH_LABEL                     = "Refresh";
    public static final String YOURMUSIC_CMDSEARCH_LABEL                      = "Search";
    public static final String YOURMUSIC_CMDDOWNLOAD_LABEL                    = "Download";
    public static final String YOURMUSIC_CMDPROPERTIES_LABEL                  = "Music Properties";
    public static final String YOURMUSIC_CMDDOWNLOADSSCREEN_LABEL             = "Downloads Screen";
    public static final String YOURMUSIC_CMDSETTINGS_LABEL                    = "Settings";
    public static final String YOURMUSIC_CMDABOUT_LABEL                       = "About";
    public static final String YOURMUSIC_CMDHELP_LABEL                        = "Help";
    public static final String YOURMUSIC_CMDDELETE_LABEL                      = "Delete";

    public static final String YOURMUSIC_INITERRORALERT_TITLE                 = "Application Initialization Error";
    public static final String YOURMUSIC_ERRORALERT_TITLE                     = "Error";
    public static final String YOURMUSIC_PROGRESSALERT_TITLE                  = "Operation In Progress";
    
    public static final String YOURMUSIC_ABOUTFORM_UNKNOWNNAME                = "Unknown name";
    public static final String YOURMUSIC_ABOUTFORM_UNKNOWNVERSION             = "Unknown version";
    public static final String YOURMUSIC_ABOUTFORM_UNKNOWNVENDOR              = "Unknown vendor";
    public static final String YOURMUSIC_ABOUTFORM_UNKNOWNINFOURL             = "Unknown info URL";
    public static final String YOURMUSIC_ABOUTFORM_TITLE                      = "About";
    public static final String YOURMUSIC_ABOUTFORM_VERSION                    = "Version";
    public static final String YOURMUSIC_ABOUTFORM_DEVELOPER                  = "Developer:";

    public static final String YOURMUSIC_HELPFORM_UNKNOWNINFOURL              = "Unknown info URL";
    public static final String YOURMUSIC_HELPFORM_TITLE                       = "Help";
    public static final String YOURMUSIC_HELPFORM_TEXT                        = "J2ME music downloader for mobile phones.\n\nIf you have any questions regarding this application, you can contact application developer on this website:";

    public static final String YOURMUSIC_SEARCHSEARCHSTRINGTEXTFIELD_LABEL    = "Music Search";
    public static final String YOURMUSIC_SEARCHSEARCHRESULTSCHOICEGROUP_LABEL = "Search Results";
    public static final String YOURMUSIC_SEARCHFORM_TITLE                     = "Music Search";

    public static final String YOURMUSIC_PROPERTIESFORM_TITLE                 = "Music Properties";

    public static final String YOURMUSIC_DOWNLOADFILENAMETEXTFIELD_LABEL      = "File Name";
    public static final String YOURMUSIC_DOWNLOADFORM_TITLE                   = "Download Music";

    public static final String YOURMUSIC_DOWNLOADSLIST_TITLE                  = "Music Downloads";

    public static final String YOURMUSIC_SETTINGSDSTFSROOTCHOICEGROUP_LABEL   = "Destination Disk";
    public static final String YOURMUSIC_SETTINGSFORM_TITLE                   = "Settings";
    
    public static final String YOURMUSIC_APPUNAVAILABLE                       = "Application is not available on this platform";
    public static final String YOURMUSIC_SEARCHINPROGRESS                     = "Searching for music...";
    public static final String YOURMUSIC_CHECKCONNECTION                      = "Please check your Internet connection.";
    public static final String YOURMUSIC_LOADINGPROPERTIES                    = "Loading music properties...";
    public static final String YOURMUSIC_FILENAMEEMPTY                        = "File name is empty";
    public static final String YOURMUSIC_DELETEDOWNLOAD_HEADER                = "Delete Download";
    public static final String YOURMUSIC_DELETEDOWNLOAD_TEXT                  = "Delete download";
    public static final String YOURMUSIC_NODISKSELECTED                       = "No disk selected";
    
    public static final String DOWNLOADCLASS_DONE                             = "DONE";
    public static final String DOWNLOADCLASS_QUEUED                           = "QUEUED";
    
    public static final String DOWNLOADERCLASS_INVALIDRESPONSERETRYING        = "Invalid server response, retrying";
    public static final String DOWNLOADERCLASS_CHECKCONNECTION                = "Please check your Internet connection.";
    public static final String DOWNLOADERCLASS_CHECKDISKSETTINGS              = "Please check destination disk settings.";
    public static final String DOWNLOADERCLASS_COULDNOTCREATEDIR              = "Please check destination disk settings. Could not find or create music directory";
    public static final String DOWNLOADERCLASS_INVALIDRESPONSE                = "Invalid server response:";
    
    public static final String DOWNLOADSTORAGECLASS_TITLEEMPTY                = "Download task title is empty";
    public static final String DOWNLOADSTORAGECLASS_VIDEOIDEMPTY              = "Download task video id is empty";
    public static final String DOWNLOADSTORAGECLASS_FILENAMEEMPTY             = "Download task file name is empty";
    public static final String DOWNLOADSTORAGECLASS_ALREADYEXISTS             = "Download task with the same file name already exists";
    public static final String DOWNLOADSTORAGECLASS_INVALIDID                 = "Invalid id";
    
    private static Hashtable RuRuLocale = null;

    public static synchronized void Initialize() {
        RuRuLocale = new Hashtable();

        RuRuLocale.put(YOURMUSIC_CMDOK_LABEL,                          "OK");
        RuRuLocale.put(YOURMUSIC_CMDBACK_LABEL,                        "Назад");
        RuRuLocale.put(YOURMUSIC_CMDEXIT_LABEL,                        "Выход");
        RuRuLocale.put(YOURMUSIC_CMDREFRESH_LABEL,                     "Обновить");
        RuRuLocale.put(YOURMUSIC_CMDSEARCH_LABEL,                      "Поиск");
        RuRuLocale.put(YOURMUSIC_CMDDOWNLOAD_LABEL,                    "Скачать");
        RuRuLocale.put(YOURMUSIC_CMDPROPERTIES_LABEL,                  "Свойства музыки");
        RuRuLocale.put(YOURMUSIC_CMDDOWNLOADSSCREEN_LABEL,             "Экран закачек");
        RuRuLocale.put(YOURMUSIC_CMDSETTINGS_LABEL,                    "Настройки");
        RuRuLocale.put(YOURMUSIC_CMDABOUT_LABEL,                       "О программе");
        RuRuLocale.put(YOURMUSIC_CMDHELP_LABEL,                        "Справка");
        RuRuLocale.put(YOURMUSIC_CMDDELETE_LABEL,                      "Удалить");

        RuRuLocale.put(YOURMUSIC_INITERRORALERT_TITLE,                 "Ошибка инициализации");
        RuRuLocale.put(YOURMUSIC_ERRORALERT_TITLE,                     "Ошибка");
        RuRuLocale.put(YOURMUSIC_PROGRESSALERT_TITLE,                  "Выполняется операция");

        RuRuLocale.put(YOURMUSIC_ABOUTFORM_UNKNOWNNAME,                "Неизвестное имя");
        RuRuLocale.put(YOURMUSIC_ABOUTFORM_UNKNOWNVERSION,             "Неизвестная версия");
        RuRuLocale.put(YOURMUSIC_ABOUTFORM_UNKNOWNVENDOR,              "Неизвестный вендор");
        RuRuLocale.put(YOURMUSIC_ABOUTFORM_UNKNOWNINFOURL,             "Неизвестный информационный URL");
        RuRuLocale.put(YOURMUSIC_ABOUTFORM_TITLE,                      "О программе");
        RuRuLocale.put(YOURMUSIC_ABOUTFORM_VERSION,                    "версия");
        RuRuLocale.put(YOURMUSIC_ABOUTFORM_DEVELOPER,                  "Разработчик:");

        RuRuLocale.put(YOURMUSIC_HELPFORM_UNKNOWNINFOURL,              "Неизвестный информационный URL");
        RuRuLocale.put(YOURMUSIC_HELPFORM_TITLE,                       "Справка");
        RuRuLocale.put(YOURMUSIC_HELPFORM_TEXT,                        "Java-приложение для поиска и загрузки музыки на мобильный телефон.\n\nПри возникновении любых вопросов по использованию этого приложения вы можете связаться с разработчиком через этот Web-сайт:");

        RuRuLocale.put(YOURMUSIC_SEARCHSEARCHSTRINGTEXTFIELD_LABEL,    "Поиск музыки");
        RuRuLocale.put(YOURMUSIC_SEARCHSEARCHRESULTSCHOICEGROUP_LABEL, "Результаты поиска");
        RuRuLocale.put(YOURMUSIC_SEARCHFORM_TITLE,                     "Поиск музыки");

        RuRuLocale.put(YOURMUSIC_PROPERTIESFORM_TITLE,                 "Свойства музыки");

        RuRuLocale.put(YOURMUSIC_DOWNLOADFILENAMETEXTFIELD_LABEL,      "Имя файла");
        RuRuLocale.put(YOURMUSIC_DOWNLOADFORM_TITLE,                   "Скачать музыку");

        RuRuLocale.put(YOURMUSIC_DOWNLOADSLIST_TITLE,                  "Закачки музыки");

        RuRuLocale.put(YOURMUSIC_SETTINGSDSTFSROOTCHOICEGROUP_LABEL,   "Диск - хранилище музыки");
        RuRuLocale.put(YOURMUSIC_SETTINGSFORM_TITLE,                   "Настройки");

        RuRuLocale.put(YOURMUSIC_APPUNAVAILABLE,                       "Приложение недоступно на этой платформе");
        RuRuLocale.put(YOURMUSIC_SEARCHINPROGRESS,                     "Поиск музыки...");
        RuRuLocale.put(YOURMUSIC_CHECKCONNECTION,                      "Пожалуйста, проверьте свое Интернет-соединение.");
        RuRuLocale.put(YOURMUSIC_LOADINGPROPERTIES,                    "Загрузка свойств музыки...");
        RuRuLocale.put(YOURMUSIC_FILENAMEEMPTY,                        "Имя файла пусто");
        RuRuLocale.put(YOURMUSIC_DELETEDOWNLOAD_HEADER,                "Удалить закачку");
        RuRuLocale.put(YOURMUSIC_DELETEDOWNLOAD_TEXT,                  "Удалить закачку");
        RuRuLocale.put(YOURMUSIC_NODISKSELECTED,                       "Диск не выбран");

        RuRuLocale.put(DOWNLOADCLASS_DONE,                             "ГОТОВО");
        RuRuLocale.put(DOWNLOADCLASS_QUEUED,                           "ОЖИДАНИЕ");

        RuRuLocale.put(DOWNLOADERCLASS_INVALIDRESPONSERETRYING,        "Некорректный ответ сервера, повтор попытки");
        RuRuLocale.put(DOWNLOADERCLASS_CHECKCONNECTION,                "Пожалуйста, проверьте свое Интернет-соединение.");
        RuRuLocale.put(DOWNLOADERCLASS_CHECKDISKSETTINGS,              "Пожалуйста, проверьте настройки целевого диска.");
        RuRuLocale.put(DOWNLOADERCLASS_COULDNOTCREATEDIR,              "Пожалуйста, проверьте настройки целевого диска. Невозможно найти каталог для хранения музыки");
        RuRuLocale.put(DOWNLOADERCLASS_INVALIDRESPONSE,                "Некорректный ответ сервера:");

        RuRuLocale.put(DOWNLOADSTORAGECLASS_TITLEEMPTY,                "Не указано имя закачки");
        RuRuLocale.put(DOWNLOADSTORAGECLASS_VIDEOIDEMPTY,              "Не указан ID закачки");
        RuRuLocale.put(DOWNLOADSTORAGECLASS_FILENAMEEMPTY,             "Не указано имя файла закачки");
        RuRuLocale.put(DOWNLOADSTORAGECLASS_ALREADYEXISTS,             "Закачка с таким именем файла уже существует");
        RuRuLocale.put(DOWNLOADSTORAGECLASS_INVALIDID,                 "Неверный ID");
    }

    public static String GetLocalizedString(String str) {
        String device_locale = "";

        device_locale = System.getProperty("microedition.locale");

        if (device_locale == null) {
            device_locale = "";
        }

        if (device_locale.equals("ru-RU") && RuRuLocale != null) {
            if (RuRuLocale.containsKey(str)) {
                return (String)RuRuLocale.get(str);
            } else {
                return str;
            }
        } else {
            return str;
        }
    }
}
