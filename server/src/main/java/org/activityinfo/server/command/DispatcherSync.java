package org.activityinfo.server.command;

import org.activityinfo.shared.command.Command;
import org.activityinfo.shared.command.result.CommandResult;

import com.google.inject.ImplementedBy;


@ImplementedBy(DispatcherSyncImpl.class)
public interface DispatcherSync {

	<C extends Command<R>, R extends CommandResult> R execute(C command);

}
