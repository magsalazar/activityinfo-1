/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.activityinfo.server.command;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.activityinfo.MockDb;
import org.activityinfo.server.command.handler.UpdateUserPermissionsHandler;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.dao.PartnerDAO;
import org.activityinfo.server.database.hibernate.dao.UserDAO;
import org.activityinfo.server.database.hibernate.dao.UserDatabaseDAO;
import org.activityinfo.server.database.hibernate.dao.UserPermissionDAO;
import org.activityinfo.server.database.hibernate.entity.Partner;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.database.hibernate.entity.UserDatabase;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.server.mail.InvitationMessage;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.server.mail.MailSenderStubModule;
import org.activityinfo.shared.command.GetUsers;
import org.activityinfo.shared.command.UpdateUserPermissions;
import org.activityinfo.shared.command.result.UserResult;
import org.activityinfo.shared.dto.PartnerDTO;
import org.activityinfo.shared.dto.UserPermissionDTO;
import org.activityinfo.shared.exception.CommandException;
import org.activityinfo.shared.exception.IllegalAccessCommandException;
import org.activityinfo.test.InjectionSupport;
import org.activityinfo.test.Modules;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(InjectionSupport.class)
@Modules({MailSenderStubModule.class})
public class UpdateUserPermissionsHandlerTest extends CommandTestCase {

    private Partner NRC;
    private Partner IRC;
    private PartnerDTO NRC_DTO;

    private MockDb db = new MockDb();
    private MailSender mailer;
    private UpdateUserPermissionsHandler handler;
    private User owner;

    @Before
    public void setup() {

        NRC = new Partner();
        NRC.setId(1);
        NRC.setName("NRC");
        NRC.setFullName("Norwegian Refugee Council");
        db.persist(NRC);

        IRC = new Partner();
        IRC.setId(2);
        IRC.setName("IRC");
        IRC.setFullName("International Rescue Committee");
        db.persist(IRC);

        NRC_DTO = new PartnerDTO(1, "NRC");

        mailer = createMock("InvitationMailer", MailSender.class);

        handler = new UpdateUserPermissionsHandler(
                db.getDAO(UserDatabaseDAO.class), db.getDAO(PartnerDAO.class), db.getDAO(UserDAO.class),
                db.getDAO(UserPermissionDAO.class), mailer);


        owner = new User();
        owner.setId(99);
        owner.setName("Alex");
        db.persist(owner);

        UserDatabase udb = new UserDatabase(1, "PEAR");
        udb.setOwner(owner);
        db.persist(udb);

    }

    @Test
    public void ownerCanAddUser() throws Exception {

        mailer.send(isA(InvitationMessage.class));
        replay(mailer);

        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail("other@foobar");
        user.setPartner(NRC_DTO);
        user.setAllowView(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, user);

        handler.execute(cmd, owner);

        verify(mailer);
    }


    /**
     * Asserts that someone with ManageUsersPermission will
     * be permitted to grant some one edit rights.
     */
    @Test
    public void testVerifyAuthorityForViewPermissions() throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.setPartner(NRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPartner(NRC_DTO);
        dto.setAllowView(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    /**
     * Asserts that someone with ManageUsersPermission will
     * be permitted to grant some one edit rights.
     */
    @Test
    public void testVerifyAuthorityForEditPermissions() throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.setPartner(NRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPartner(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    @Test(expected = IllegalAccessCommandException.class)
    public void testFailingVerifyAuthorityForView() throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.setPartner(IRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPartner(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    @Test
    public void testVerifyAuthorityForViewByOtherPartner() throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.setPartner(IRC);
        executingUserPermissions.setAllowManageUsers(true);
        executingUserPermissions.setAllowManageAllUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPartner(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }




    /**
     * Verifies that a user with the manageUsers permission can
     * add another user to the UserDatabase
     *
     * @throws CommandException
     */
    @Test
    @OnDataSet("/dbunit/schema1.db.xml")
    public void testAuthorizedCreate() throws CommandException {

        setUser(2);

        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail("ralph@lauren.com");
        user.setName("Ralph");
        user.setPartner(new PartnerDTO(1, "NRC"));
        user.setAllowView(true);
        user.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, user);
        execute(cmd);

        UserResult result = execute(new GetUsers(1));
        Assert.assertEquals(1, result.getTotalLength());
        Assert.assertEquals("ralph@lauren.com", result.getData().get(0).getEmail());
        Assert.assertTrue("edit permissions", result.getData().get(0).getAllowEdit());
    }

    /**
     * Verifies that the owner of a database can update an existing users
     * permission
     *
     * @throws CommandException
     */
    @Test
    @OnDataSet("/dbunit/schema1.db.xml")
    public void testOwnerUpdate() throws CommandException {
        setUser(1);

        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail("bavon@nrcdrc.org");
        user.setPartner(new PartnerDTO(1, "NRC"));
        user.setAllowView(true);
        user.setAllowViewAll(false);
        user.setAllowEdit(true);
        user.setAllowEdit(false);
        user.setAllowDesign(true);

        execute(new UpdateUserPermissions(1, user));

        UserResult result = execute(new GetUsers(1));
        UserPermissionDTO reUser = result.getData().get(0);
        Assert.assertEquals("bavon@nrcdrc.org", reUser.getEmail());
        Assert.assertTrue("design rights", user.getAllowDesign());
    }
}
