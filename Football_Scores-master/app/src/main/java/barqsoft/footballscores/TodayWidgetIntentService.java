package barqsoft.footballscores;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ujjwal Jain on 01-03-2016.
 */
public class TodayWidgetIntentService extends IntentService {

    private String LOG_TAG = TodayWidgetIntentService.class.getSimpleName();

    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.MATCH_DAY
    };

    private static final int INDEX_LEAGUE_COL = 0;
    private static final int INDEX_DATE_COL = 1;
    private static final int INDEX_TIME_COL = 2;
    private static final int INDEX_HOME_COL = 3;
    private static final int INDEX_AWAY_COL = 4;
    private static final int INDEX_HOME_GOALS_COL = 5;
    private static final int INDEX_AWAY_GOALS_COL = 6;
    private static final int INDEX_MATCH_ID = 7;
    private static final int INDEX_MATCH_DAY = 8;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.v(LOG_TAG, "onHandleIntent");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, TodayWidgetProvider.class));

        Date fragmentDate = new Date(System.currentTimeMillis());
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
        String[] mDate = new String[1];
        mDate[0] = mFormat.format(fragmentDate);

        Uri scoreForDateUri = DatabaseContract.scores_table.buildScoreWithDate();

        Cursor data = getContentResolver().query(scoreForDateUri, SCORES_COLUMNS,
                DatabaseContract.scores_table.DATE_COL,
                mDate,
                null);

        if (data == null) {
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        String homeName = data.getString(INDEX_HOME_COL);
        String awayName = data.getString(INDEX_AWAY_COL);
        int homeGoals = data.getInt(INDEX_HOME_GOALS_COL);
        int awayGoals = data.getInt(INDEX_AWAY_GOALS_COL);
        String score = Utilies.getScores(homeGoals, awayGoals);

        data.close();

        Log.v(LOG_TAG, homeName);
        Log.v(LOG_TAG, awayName);
        Log.v(LOG_TAG, score);

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(
                    getPackageName(),
                    R.layout.score_today_small);

            views.setTextViewText(R.id.home_name, homeName);
            views.setTextViewText(R.id.away_name, awayName);
            views.setTextViewText(R.id.score_textview, score);

            Intent launchIntent = new Intent(this, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent
                    .getActivity(this, 0, launchIntent, 0);

            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}
