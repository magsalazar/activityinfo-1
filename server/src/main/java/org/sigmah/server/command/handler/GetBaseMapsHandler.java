/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.command.handler;

import java.util.List;

import org.sigmah.shared.command.GetBaseMaps;
import org.sigmah.shared.command.handler.CommandHandlerAsync;
import org.sigmah.shared.command.handler.ExecutionContext;
import org.sigmah.shared.command.result.BaseMapResult;
import org.sigmah.shared.map.TileBaseMap;

import com.bedatadriven.rebar.sql.client.SqlResultCallback;
import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Alex Bertram
 * @see org.sigmah.shared.command.GetBaseMaps
 */
public class GetBaseMapsHandler implements CommandHandlerAsync<GetBaseMaps, BaseMapResult> {


	@Override
	public void execute(GetBaseMaps command, ExecutionContext context,
			final AsyncCallback<BaseMapResult> callback) {
		
		SqlQuery.select()
			.appendColumn("id")
			.appendColumn("copyright")
			.appendColumn("maxZoom")
			.appendColumn("minZoom")
			.appendColumn("name")
			.appendColumn("tileUrlPattern")
			.appendColumn("thumbnailUrl")
			.from("BaseMap")
			.execute(context.getTransaction(), new SqlResultCallback() {
				
				@Override
				public void onSuccess(SqlTransaction tx, SqlResultSet results) {
					List<TileBaseMap> maps = Lists.newArrayList();
					for(SqlResultSetRow row : results.getRows()) {
						TileBaseMap map = new TileBaseMap();
						map.setId(row.getString("id"));
						map.setCopyright(row.getString("copyright"));
						map.setMaxZoom(row.getInt("maxZoom"));
						map.setMinZoom(row.getInt("minZoom"));
						map.setName(row.getString("name"));
						map.setThumbnailUrl(row.getString("tileUrlPattern"));
						maps.add(map);
					}
					callback.onSuccess(new BaseMapResult(maps));
				}
			});
	}
}
