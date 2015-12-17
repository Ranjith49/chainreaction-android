package com.ran.chainreaction.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ran.chainreaction.entities.BombValues;
import com.ran.chainreaction.entities.GridSizeValues;
import com.ran.chainreaction.entities.SavedGamesEntity;
import com.ran.chainreaction.gameplay.GamePlaySession;

import java.util.ArrayList;

import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_BOX_INFO;
import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_CELL_INFOS;
import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_CURRENT_PLAYER;
import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_GRID_TYPE;
import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_ID;
import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_NAME;
import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_ORB_TYPE;
import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_PLAYERS;
import static com.ran.chainreaction.database.ChainReactionDBHelper.GAME_TIME;
import static com.ran.chainreaction.database.ChainReactionDBHelper.TABLE_GAME;

/**
 * Created by ranjith on 17/12/15.
 * <p/>
 * DataBase Operations Helper , Will Create the DataBase if it is not ..
 */
public class ChainReactionDBOpsHelper {

    private static final Object LOCK = new Object();
    private static ChainReactionDBOpsHelper dbOpsHelper;
    private ChainReactionDBHelper chainReactionDBHelper;

    private ChainReactionDBOpsHelper(Context context) {
        chainReactionDBHelper = new ChainReactionDBHelper(context);
    }

    public static ChainReactionDBOpsHelper getDBInstance(Context context) {
        synchronized (LOCK) {
            if (dbOpsHelper == null) {
                dbOpsHelper = new ChainReactionDBOpsHelper(context);
            }
        }
        return dbOpsHelper;
    }

    /**
     * Method to add Current Game Play Session to the DB
     *
     * @param gamePlaySession -- Current Game Play Session
     * @param gameTime        -- Game Time Elapsed
     * @param gameName        -- Name of the game
     * @return -- Whether Operation is Success /Not
     */
    private boolean addCurrentGame(GamePlaySession gamePlaySession, long gameTime, String gameName) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(GAME_ID, gameTime);
        contentValues.put(GAME_NAME, gameName);
        contentValues.put(GAME_GRID_TYPE, GridSizeValues.getIndex(gamePlaySession.getPlayerGridType()));
        contentValues.put(GAME_ORB_TYPE, BombValues.getIndex(gamePlaySession.getGameBombType()));
        contentValues.put(GAME_TIME, gameTime);

        SQLiteDatabase sqLiteDatabase = chainReactionDBHelper.getWritableDatabase();
        long newRowId = sqLiteDatabase.insert(TABLE_GAME,
            null,
            contentValues);
        sqLiteDatabase.close();
        return newRowId != -1;
    }

    /**
     * Method to Delete the Game in the DB
     *
     * @param gameId -- Id of the Game to be Deleted
     * @return -- Whether Operation is success or not
     */
    private boolean deleteGame(long gameId) {
        String selectionQuery = GAME_ID + " =?";
        SQLiteDatabase sqLiteDatabase = chainReactionDBHelper.getWritableDatabase();
        int rowsEffected = sqLiteDatabase.delete(TABLE_GAME,
            selectionQuery, new String[]{String.valueOf(gameId)});
        sqLiteDatabase.close();
        return rowsEffected != 0;
    }

    /**
     * Method to generate the SavedGames ArrayList for Recycler
     *
     * @return -- ArrayList of the SavedGamesEntity
     */
    private ArrayList<SavedGamesEntity> retrieveGamesFromDB() {

        ArrayList<SavedGamesEntity> savedGamesEntities = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = chainReactionDBHelper.getReadableDatabase();

        String selectionColumns[] = new String[]{GAME_ID, GAME_NAME, GAME_PLAYERS};
        String selectionOrderBy = GAME_TIME + " DESC";
        Cursor cursor = sqLiteDatabase.query(TABLE_GAME, selectionColumns, null, null, null, null, selectionOrderBy);
        while (cursor.moveToNext()) {
            String gameName = cursor.getString(cursor.getColumnIndex(GAME_NAME));
            long gameId = cursor.getLong(cursor.getColumnIndex(GAME_ID));
            String gamePlayersInfo = cursor.getString(cursor.getColumnIndex(GAME_PLAYERS));
            //TODO [ranjith.suda] , convert the gamePlayernInfo to GamePlayerInfo List ..
            SavedGamesEntity entity = new SavedGamesEntity(gameName, gameId, null);
            savedGamesEntities.add(entity);
        }
        cursor.close();
        sqLiteDatabase.close();

        return savedGamesEntities;
    }

    /**
     * Method to generate the GamePlaySession
     *
     * @param gameId -- Game Id for which to be generated
     * @return -- GamePlaySession Created
     */
    private GamePlaySession retrieveGameSession(long gameId) {

        SQLiteDatabase sqLiteDatabase = chainReactionDBHelper.getReadableDatabase();
        String selectionQuery = GAME_ID + " =?";
        String selectionArgs[] = new String[]{String.valueOf(gameId)};

        Cursor cursor = sqLiteDatabase.query(TABLE_GAME, null, selectionQuery, selectionArgs, null, null, null);
        if (cursor.moveToNext()) {
            String gameCurrentPlayer = cursor.getString(cursor.getColumnIndex(GAME_CURRENT_PLAYER));
            String currentPlayers = cursor.getString(cursor.getColumnIndex(GAME_PLAYERS));
            String gameBoxInfo = cursor.getString(cursor.getColumnIndex(GAME_BOX_INFO));
            String gameCellInfoS = cursor.getString(cursor.getColumnIndex(GAME_CELL_INFOS));
            int OrbType = cursor.getInt(cursor.getColumnIndex(GAME_ORB_TYPE));
            int GridType = cursor.getInt(cursor.getColumnIndex(GAME_GRID_TYPE));

            cursor.close();
            sqLiteDatabase.close();
            //TODO [ranjith.suda] ,convert to GamePlaySession properly
            return new GamePlaySession(null, null, GridSizeValues.getEnumType(GridType), BombValues.getEnumType(OrbType), null, null);
        } else {
            cursor.close();
            sqLiteDatabase.close();
            return null;
        }

    }
}