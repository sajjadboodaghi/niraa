package ir.sajjadboodaghi.niraa;

/**
 * Created by Sajjad on 02/10/2018.
 */

public class Urls {
    public static final String BASE_URL               = "http://sajjadboodaghi.ir/niraa/";
    public static final String CLIENT_URL             = BASE_URL + "client/";

    public static final String SRC_URL                = CLIENT_URL + "v5/";
    public static final String IMAGES_BASE_URL        = CLIENT_URL + "images/";
    public static final String USERS_IMAGES_DIR       = CLIENT_URL + "users_images/";
    public static final String STORIES_BASE_URL       = CLIENT_URL + "stories/";
    public static final String GET_CATEGORY           = CLIENT_URL + "categories.php";
    public static final String GET_CATEGORY_FILTER    = CLIENT_URL + "categories_filter.php";

    public static final String GET_ITEMS              = SRC_URL + "items_select_all.php";
    public static final String GET_FILTERED_ITEMS     = SRC_URL + "items_select_filtered.php";

    public static final String REGISTER_USER          = SRC_URL + "users_insert_update.php";
    public static final String CREATE_ITEM            = SRC_URL + "items_insert.php";
    public static final String DELETE_ITEM            = SRC_URL + "items_delete.php";
    public static final String GET_USER_ITEMS         = SRC_URL + "items_select_for_user.php";
    public static final String SAVE_SUGGEST           = SRC_URL + "suggests_insert.php";
    public static final String REPORT_ITEM            = SRC_URL + "reports_insert.php";
    public static final String CAN_CREATE_MORE_ITEM   = SRC_URL + "items_select_can_make_another.php";
    public static final String GET_USER_NOTIFICATIONS = SRC_URL + "notifications_select_for_user.php";
    public static final String SEND_VERIFICATION_CODE = SRC_URL + "send_verification_code.php";
    public static final String CREATE_STORY           = SRC_URL + "stories_insert.php";
    public static final String GET_STORIES            = SRC_URL + "stories_select_all.php";
    public static final String GET_USER_STORIES       = SRC_URL + "stories_select_for_user.php";
    public static final String DELETE_STORY           = SRC_URL + "stories_delete.php";
    public static final String PUBLISH_STORY          = SRC_URL + "stories_update_publish.php";
    public static final String SAVE_USER_NAME         = SRC_URL + "users_update_name.php";
    public static final String SAVE_USER_IMAGE        = SRC_URL + "users_update_image.php";
    public static final String REMOVE_USER_IMAGE      = SRC_URL + "users_update_remove_image.php";
    public static final String GET_USER_BOOKMARK_INFO = SRC_URL + "users_bookmarks_select_info.php";
    public static final String BOOKMARK               = SRC_URL + "bookmarks_insert_delete.php";
    public static final String GET_BOOKMARKS          = SRC_URL + "bookmarks_select_for_user.php";
    public static final String SEARCH                 = SRC_URL + "items_select_search.php";
    public static final String GET_USER_IMAGE_NAME    = SRC_URL + "users_select_image_name.php";
    public static final String UPDATE_ITEM            = SRC_URL + "items_update_time.php";
    public static final String GET_STORY_VISITS       = SRC_URL + "stories_select_visits_count.php";
    public static final String INCREMENT_VISIT_STORY  = SRC_URL + "stories_update_visits_count.php";

}