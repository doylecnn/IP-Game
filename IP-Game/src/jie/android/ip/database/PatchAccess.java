package jie.android.ip.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PatchAccess extends BaseAccess {

	private static final int ATTR_TARGET_VERSION	=	1;
	
	public PatchAccess(Connection connection) {
		super(connection);
	}

	public int getTargetVersion() {
		final String sql = "SELECT int FROM info WHERE attr=" + ATTR_TARGET_VERSION;
		final ResultSet rs = querySQL(sql);
		try {
			try {
				if (rs.next()) {
					return rs.getInt(1);
				}
			} finally {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void patch(final DBAccess dbAccess) {
		check_add(dbAccess);
		check_update(dbAccess);
		check_delete(dbAccess);
	}

	private void check_add(final DBAccess dbAccess) {
		
		final String sql = "SELECT id,pack_id,script, status, base_score, ctime FROM script_add";
		final ResultSet rs = querySQL(sql);
		try {
			try {
				while(rs.next()) {
					final ArrayList<String> val = new ArrayList<String>();
					val.add(rs.getString(1));
					val.add(rs.getString(2));
					val.add(rs.getString(3));
					val.add(rs.getString(4));
					val.add(rs.getString(5));
					val.add(rs.getString(6));
					
					if (!scriptExist(dbAccess, rs.getInt(1))) {
						addScript(dbAccess, val);
					}
				}
			} finally {
				rs.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void check_update(DBAccess dbAccess) {
		final String sql = "SELECT id,pack_id, base_score, ctime FROM script_update";
		final ResultSet rs = querySQL(sql);
		try {
			try {
				while(rs.next()) {
					final ArrayList<String> val = new ArrayList<String>();
					val.add(rs.getString(2));
					val.add(rs.getString(3));
					val.add(rs.getString(4));
					val.add(rs.getString(1));
					
					if (scriptExist(dbAccess, rs.getInt(1))) {
						updateScript(dbAccess, val);
					}
				}
			} finally {
				rs.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void check_delete(DBAccess dbAccess) {
		final String sql = "SELECT id FROM script_delete";
		final ResultSet rs = querySQL(sql);
		try {
			try {
				while(rs.next()) {
					deleteScript(dbAccess, rs.getInt(1));
				}
			} finally {
				rs.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private boolean scriptExist(final DBAccess dbAccess, int id) {
		return dbAccess.scriptExist(id);
	}	

	private void addScript(final DBAccess dbAccess, final ArrayList<String> val) {
		dbAccess.addScript(val);
	}

	private void updateScript(final DBAccess dbAccess, final ArrayList<String> val) {
		dbAccess.updateScript(val);
		
	}

	private void deleteScript(final DBAccess dbAccess, int id) {
		dbAccess.deleteScript(id);		
	}
	
}