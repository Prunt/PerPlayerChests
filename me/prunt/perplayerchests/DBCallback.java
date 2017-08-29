package me.prunt.perplayerchests;

import java.sql.ResultSet;

public interface DBCallback {
    public void onQueryDone(ResultSet rs);
}
