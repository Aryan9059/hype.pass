package com.pass.hype.data.local.cards;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CardDao_Impl implements CardDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCard;

  private final EntityUpsertionAdapter<Cards> __upsertionAdapterOfCards;

  public CardDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteCard = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM Cards WHERE cardId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfCards = new EntityUpsertionAdapter<Cards>(new EntityInsertionAdapter<Cards>(__db) {
      @Override
      public String createQuery() {
        return "INSERT INTO `Cards` (`cardId`,`cardHolder`,`cardNumber`,`expires`,`cvv`,`baseColor`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Cards value) {
        stmt.bindLong(1, value.getCardId());
        if (value.getCardHolder() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getCardHolder());
        }
        if (value.getCardNumber() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getCardNumber());
        }
        if (value.getExpires() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getExpires());
        }
        if (value.getCvv() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getCvv());
        }
        if (value.getBaseColor() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getBaseColor());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<Cards>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE `Cards` SET `cardId` = ?,`cardHolder` = ?,`cardNumber` = ?,`expires` = ?,`cvv` = ?,`baseColor` = ? WHERE `cardId` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Cards value) {
        stmt.bindLong(1, value.getCardId());
        if (value.getCardHolder() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getCardHolder());
        }
        if (value.getCardNumber() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getCardNumber());
        }
        if (value.getExpires() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getExpires());
        }
        if (value.getCvv() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getCvv());
        }
        if (value.getBaseColor() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getBaseColor());
        }
        stmt.bindLong(7, value.getCardId());
      }
    });
  }

  @Override
  public void deleteCard(final int cardId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCard.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, cardId);
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteCard.release(_stmt);
    }
  }

  @Override
  public void upsertCard(final Cards cards) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __upsertionAdapterOfCards.upsert(cards);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Cards getCardById(final int cardId) {
    final String _sql = "SELECT * FROM Cards WHERE cardId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cardId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfCardId = CursorUtil.getColumnIndexOrThrow(_cursor, "cardId");
      final int _cursorIndexOfCardHolder = CursorUtil.getColumnIndexOrThrow(_cursor, "cardHolder");
      final int _cursorIndexOfCardNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "cardNumber");
      final int _cursorIndexOfExpires = CursorUtil.getColumnIndexOrThrow(_cursor, "expires");
      final int _cursorIndexOfCvv = CursorUtil.getColumnIndexOrThrow(_cursor, "cvv");
      final int _cursorIndexOfBaseColor = CursorUtil.getColumnIndexOrThrow(_cursor, "baseColor");
      final Cards _result;
      if(_cursor.moveToFirst()) {
        final int _tmpCardId;
        _tmpCardId = _cursor.getInt(_cursorIndexOfCardId);
        final String _tmpCardHolder;
        if (_cursor.isNull(_cursorIndexOfCardHolder)) {
          _tmpCardHolder = null;
        } else {
          _tmpCardHolder = _cursor.getString(_cursorIndexOfCardHolder);
        }
        final String _tmpCardNumber;
        if (_cursor.isNull(_cursorIndexOfCardNumber)) {
          _tmpCardNumber = null;
        } else {
          _tmpCardNumber = _cursor.getString(_cursorIndexOfCardNumber);
        }
        final String _tmpExpires;
        if (_cursor.isNull(_cursorIndexOfExpires)) {
          _tmpExpires = null;
        } else {
          _tmpExpires = _cursor.getString(_cursorIndexOfExpires);
        }
        final String _tmpCvv;
        if (_cursor.isNull(_cursorIndexOfCvv)) {
          _tmpCvv = null;
        } else {
          _tmpCvv = _cursor.getString(_cursorIndexOfCvv);
        }
        final String _tmpBaseColor;
        if (_cursor.isNull(_cursorIndexOfBaseColor)) {
          _tmpBaseColor = null;
        } else {
          _tmpBaseColor = _cursor.getString(_cursorIndexOfBaseColor);
        }
        _result = new Cards(_tmpCardId,_tmpCardHolder,_tmpCardNumber,_tmpExpires,_tmpCvv,_tmpBaseColor);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<Cards>> getAllCards() {
    final String _sql = "SELECT * FROM Cards";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"Cards"}, false, new Callable<List<Cards>>() {
      @Override
      public List<Cards> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCardId = CursorUtil.getColumnIndexOrThrow(_cursor, "cardId");
          final int _cursorIndexOfCardHolder = CursorUtil.getColumnIndexOrThrow(_cursor, "cardHolder");
          final int _cursorIndexOfCardNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "cardNumber");
          final int _cursorIndexOfExpires = CursorUtil.getColumnIndexOrThrow(_cursor, "expires");
          final int _cursorIndexOfCvv = CursorUtil.getColumnIndexOrThrow(_cursor, "cvv");
          final int _cursorIndexOfBaseColor = CursorUtil.getColumnIndexOrThrow(_cursor, "baseColor");
          final List<Cards> _result = new ArrayList<Cards>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Cards _item;
            final int _tmpCardId;
            _tmpCardId = _cursor.getInt(_cursorIndexOfCardId);
            final String _tmpCardHolder;
            if (_cursor.isNull(_cursorIndexOfCardHolder)) {
              _tmpCardHolder = null;
            } else {
              _tmpCardHolder = _cursor.getString(_cursorIndexOfCardHolder);
            }
            final String _tmpCardNumber;
            if (_cursor.isNull(_cursorIndexOfCardNumber)) {
              _tmpCardNumber = null;
            } else {
              _tmpCardNumber = _cursor.getString(_cursorIndexOfCardNumber);
            }
            final String _tmpExpires;
            if (_cursor.isNull(_cursorIndexOfExpires)) {
              _tmpExpires = null;
            } else {
              _tmpExpires = _cursor.getString(_cursorIndexOfExpires);
            }
            final String _tmpCvv;
            if (_cursor.isNull(_cursorIndexOfCvv)) {
              _tmpCvv = null;
            } else {
              _tmpCvv = _cursor.getString(_cursorIndexOfCvv);
            }
            final String _tmpBaseColor;
            if (_cursor.isNull(_cursorIndexOfBaseColor)) {
              _tmpBaseColor = null;
            } else {
              _tmpBaseColor = _cursor.getString(_cursorIndexOfBaseColor);
            }
            _item = new Cards(_tmpCardId,_tmpCardHolder,_tmpCardNumber,_tmpExpires,_tmpCvv,_tmpBaseColor);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
