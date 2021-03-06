/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.activityinfo.server.database.hibernate.dao;

import java.util.List;

import org.activityinfo.server.database.hibernate.entity.UserDatabase;

/**
 * Data Access Object for {@link UserDatabase} domain classes. Implemented automatically
 * by {@link org.activityinfo.server.database.hibernate.dao.DAOInvocationHandler proxy}.
 */
public interface UserDatabaseDAO extends DAO<UserDatabase, Integer> {

    List<UserDatabase> queryAllUserDatabasesAlphabetically();

}
