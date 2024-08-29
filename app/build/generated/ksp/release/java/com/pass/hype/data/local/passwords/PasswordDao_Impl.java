package com.pass.hype.data.local.passwords;

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
public final class PasswordDao_Impl implements PasswordDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeletePassword;

  private final EntityUpsertionAdapter<Passwords> __upsertionAdapterOfPasswords;

  public PasswordDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeletePassword = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM Passwords WHERE passwordId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfPasswords = new EntityUpsertionAdapter<Passwords>(new EntityInsertionAdapter<Passwords>(__db) {
      @Override
      public String createQuery() {
        return "INSERT INTO `Passwords` (`passwordId`,`appName`,`appIcon`,`email`,`password`,`editTime`,`edited`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Passwords value) {
        stmt.bindLong(1, value.getPasswordId());
        if (value.getAppName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getAppName());
        }
        if (value.getAppIcon() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getAppIcon());
        }
        if (value.getEmail() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getEmail());
        }
        if (value.getPassword() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getPassword());
        }
        if (value.getEditTime() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getEditTime());
        }
        final int _tmp = value.getEdited() ? 1 : 0;
        stmt.bindLong(7, _tmp);
      }
    }, new EntityDeletionOrUpdateAdapter<Passwords>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE `Passwords` SET `passwordId` = ?,`appName` = ?,`appIcon` = ?,`email` = ?,`password` = ?,`editTime` = ?,`edited` = ? WHERE `passwordId` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Passwords value) {
        stmt.bindLong(1, value.getPasswordId());
        if (value.getAppName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getAppName());
        }
        if (value.getAppIcon() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getAppIcon());
        }
        if (value.getEmail() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getEmail());
        }
        if (value.getPassword() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getPassword());
        }
        if (value.getEditTime() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getEditTime());
        }
        final int _tmp = value.getEdited() ? 1 : 0;
        stmt.bindLong(7, _tmp);
        stmt.bindLong(8, value.getPasswordId());
      }
    });
  }

  @Override
  public void deletePassword(final int passwordId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePassword.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, passwordId);
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeletePassword.release(_stmt);
    }
  }

  @Override
  public void upsertPassword(final Passwords passwords) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __upsertionAdapterOfPasswords.upsert(passwords);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Passwords getPasswordById(final int passwordId) {
    final String _sql = "SELECT * FROM Passwords WHERE passwordId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, passwordId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPasswordId = CursorUtil.getColumnIndexOrThrow(_cursor, "passwordId");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfAppIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "appIcon");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfEditTime = CursorUtil.getColumnIndexOrThrow(_cursor, "editTime");
      final int _cursorIndexOfEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "edited");
      final Passwords _result;
      if(_cursor.moveToFirst()) {
        final int _tmpPasswordId;
        _tmpPasswordId = _cursor.getInt(_cursorIndexOfPasswordId);
        final String _tmpAppName;
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _tmpAppName = null;
        } else {
          _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        }
        final String _tmpAppIcon;
        if (_cursor.isNull(_cursorIndexOfAppIcon)) {
          _tmpAppIcon = null;
        } else {
          _tmpAppIcon = _cursor.getString(_cursorIndexOfAppIcon);
        }
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        final String _tmpEditTime;
        if (_cursor.isNull(_cursorIndexOfEditTime)) {
          _tmpEditTime = null;
        } else {
          _tmpEditTime = _cursor.getString(_cursorIndexOfEditTime);
        }
        final boolean _tmpEdited;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfEdited);
        _tmpEdited = _tmp != 0;
        _result = new Passwords(_tmpPasswordId,_tmpAppName,_tmpAppIcon,_tmpEmail,_tmpPassword,_tmpEditTime,_tmpEdited);
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
  public LiveData<List<Passwords>> getAllPasswords() {
    final String _sql = "SELECT * FROM Passwords";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"Passwords"}, false, new Callable<List<Passwords>>() {
      @Override
      public List<Passwords> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPasswordId = CursorUtil.getColumnIndexOrThrow(_cursor, "passwordId");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAppIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "appIcon");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfEditTime = CursorUtil.getColumnIndexOrThrow(_cursor, "editTime");
          final int _cursorIndexOfEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "edited");
          final List<Passwords> _result = new ArrayList<Passwords>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Passwords _item;
            final int _tmpPasswordId;
            _tmpPasswordId = _cursor.getInt(_cursorIndexOfPasswordId);
            final String _tmpAppName;
            if (_cursor.isNull(_cursorIndexOfAppName)) {
              _tmpAppName = null;
            } else {
              _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            }
            final String _tmpAppIcon;
            if (_cursor.isNull(_cursorIndexOfAppIcon)) {
              _tmpAppIcon = null;
            } else {
              _tmpAppIcon = _cursor.getString(_cursorIndexOfAppIcon);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            final String _tmpEditTime;
            if (_cursor.isNull(_cursorIndexOfEditTime)) {
              _tmpEditTime = null;
            } else {
              _tmpEditTime = _cursor.getString(_cursorIndexOfEditTime);
            }
            final boolean _tmpEdited;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEdited);
            _tmpEdited = _tmp != 0;
            _item = new Passwords(_tmpPasswordId,_tmpAppName,_tmpAppIcon,_tmpEmail,_tmpPassword,_tmpEditTime,_tmpEdited);
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
