/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.activityinfo.server.command.handler;

import org.activityinfo.server.command.handler.sync.AdminUpdateBuilder;
import org.activityinfo.server.command.handler.sync.LocationUpdateBuilder;
import org.activityinfo.server.command.handler.sync.SchemaUpdateBuilder;
import org.activityinfo.server.command.handler.sync.SiteTableUpdateBuilder;
import org.activityinfo.server.command.handler.sync.SiteUpdateBuilder;
import org.activityinfo.server.command.handler.sync.UpdateBuilder;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.shared.command.GetSyncRegionUpdates;
import org.activityinfo.shared.command.result.CommandResult;
import org.activityinfo.shared.exception.CommandException;
import org.json.JSONException;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class GetSyncRegionUpdatesHandler implements CommandHandler<GetSyncRegionUpdates> {

    private final Injector injector;

    @Inject
    public GetSyncRegionUpdatesHandler(Injector injector) {
        this.injector = injector;
    }

    @Override
    public CommandResult execute(GetSyncRegionUpdates cmd, User user) throws CommandException {

        UpdateBuilder builder;

        if(cmd.getRegionId().equals("schema")) {
            builder = injector.getInstance(SchemaUpdateBuilder.class);

        } else if(cmd.getRegionId().startsWith("admin/")) {
            builder = injector.getInstance(AdminUpdateBuilder.class);

        } else if(cmd.getRegionId().equals("locations")) {
            builder = injector.getInstance(LocationUpdateBuilder.class);

        } else if(cmd.getRegionId().startsWith("site/")) {
            builder = injector.getInstance(SiteUpdateBuilder.class);

        } else if(cmd.getRegionId().equals("site-tables")) {
            builder = injector.getInstance(SiteTableUpdateBuilder.class);

        } else {
            throw new CommandException("Unknown sync region: " + cmd.getRegionId());
        }

        try {
            return builder.build(user, cmd);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
