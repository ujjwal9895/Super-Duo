package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ujjwal Jain on 02-03-2016.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

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


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                Uri scoreForDateUri = DatabaseContract.scores_table.buildScoreWithDate();

                Date fragmentDate = new Date(System.currentTimeMillis());
                SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
                String[] mDate = new String[1];
                mDate[0] = mFormat.format(fragmentDate);

                data = getContentResolver().query(scoreForDateUri,
                        SCORES_COLUMNS,
                        DatabaseContract.scores_table.DATE_COL,
                        mDate,
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String homeTeam = data.getString(INDEX_HOME_COL);
                String awayTeam = data.getString(INDEX_AWAY_COL);
                int homeGoals = data.getInt(INDEX_HOME_GOALS_COL);
                int awayGoals = data.getInt(INDEX_AWAY_GOALS_COL);

                String score;

                if (homeGoals == -1)
                    score = "-";
                else
                    score = Utilies.getScores(homeGoals, awayGoals);

                views.setTextViewText(R.id.home_name, homeTeam);
                views.setTextViewText(R.id.away_name, awayTeam);
                views.setTextViewText(R.id.score_textview, score);
                views.setImageViewResource(R.id.home_crest,
                        Utilies.getTeamCrestByTeamName(homeTeam));
                views.setImageViewResource(R.id.away_crest,
                        Utilies.getTeamCrestByTeamName(awayTeam));

                final Intent fillInIntent = new Intent();

                Uri uri = DatabaseContract.scores_table.buildScoreWithDate();

                fillInIntent.setData(uri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {

                if (data.moveToPosition(position))
                    return data.getLong(1);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
