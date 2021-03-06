/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.activityinfo.server.command;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.shared.command.CreateEntity;
import org.activityinfo.shared.command.GetSchema;
import org.activityinfo.shared.command.result.CreateResult;
import org.activityinfo.shared.dto.SchemaDTO;
import org.activityinfo.shared.dto.UserDatabaseDTO;
import org.activityinfo.shared.exception.CommandException;
import org.activityinfo.test.InjectionSupport;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(InjectionSupport.class)
public class CreateDatabaseTest extends CommandTestCase {

    @Test
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void testCreate() throws CommandException {

        UserDatabaseDTO db = new UserDatabaseDTO();
        db.setName("RIMS");
        db.setFullName("Reintegration Management Information System");

        CreateResult cr = execute(new CreateEntity(db));

        SchemaDTO schema = execute(new GetSchema());

        UserDatabaseDTO newdb = schema.getDatabaseById(cr.getNewId());

        assertNotNull(newdb);
        assertEquals(db.getName(), newdb.getName());
        assertEquals(db.getFullName(), newdb.getFullName());
        assertNotNull(newdb.getCountry());
        assertEquals("Alex", newdb.getOwnerName());
    }

    @Test
    @OnDataSet("/dbunit/multicountry.db.xml")
    public void createWithSpecificCountry() throws CommandException {

        UserDatabaseDTO db = new UserDatabaseDTO();
        db.setName("Warchild Haiti");
        db.setFullName("Warchild Haiti");

        setUser(1);

        CreateEntity cmd = new CreateEntity(db);
        cmd.getProperties().put("countryId", 2);
        CreateResult cr = execute(cmd);

        SchemaDTO schema = execute(new GetSchema());

        UserDatabaseDTO newdb = schema.getDatabaseById(cr.getNewId());

        assertNotNull(newdb);
        assertThat(newdb.getCountry(), is(notNullValue()));
        assertThat(newdb.getCountry().getName(), is(equalTo("Haiti")));
    }

}
