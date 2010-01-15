/*
 * This file is part of ActivityInfo.
 *
 * ActivityInfo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ActivityInfo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ActivityInfo.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2010 Alex Bertram and contributors.
 */

package org.activityinfo.server.dao.hibernate;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlConnection;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoadDataSet extends Statement {
    private final Statement next;
    private final Object target;
    private final EntityManagerFactory emf;
    private String name;


    public LoadDataSet(EntityManagerFactory emf, Statement next, String name, Object target) {
        this.next = next;
        this.target = target;
        this.emf = emf;
        this.name = name;
    }

    @Override
    public void evaluate() throws Throwable {
        System.err.println("DBUnit: loading " + name + " into the database.");
        IDataSet data = loadDataSet();

        List<Throwable> errors = new ArrayList<Throwable>();
        errors.clear();
        try {
            populate(data);
            next.evaluate();
        } catch (Throwable e) {
            errors.add(e);
        } finally {
            try {
                System.err.println("DBUnit: removing " + name + " from the database");
                depopulate(data);
            } catch (Throwable e) {
                errors.add(e);
            }
        }
        MultipleFailureException.assertEmpty(errors);
    }

    private void dropAndCreateSchema() {
        System.err.println("DBUnit: deletion failed, dropping and recreating schema");

    }

    private IDataSet loadDataSet() throws IOException, DataSetException {
        InputStream in = target.getClass().getResourceAsStream(name);
        return new FlatXmlDataSet(new InputStreamReader(in), false, true, false);
    }

    private void populate(final IDataSet dataSet) {
        executeOperation(InsertIdentityOperation.INSERT, dataSet);
    }

    private void depopulate(IDataSet dataSet) {
        executeOperation(InsertIdentityOperation.DELETE_ALL, dataSet);
    }

    private void executeOperation(final DatabaseOperation op, final IDataSet dataSet) {
        HibernateEntityManager hem = (HibernateEntityManager) emf.createEntityManager();
        Session session = hem.getSession();
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                try {
                    IDatabaseConnection dbUnitConnection = createDbUnitConnection(connection);
                    op.execute(dbUnitConnection, dataSet);
                } catch (DatabaseUnitException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        hem.close();
    }

    private IDatabaseConnection createDbUnitConnection(Connection connection) throws DatabaseUnitException {
        IDatabaseConnection con = new MsSqlConnection(connection);
        con.getConfig().setProperty("http://www.dbunit.org/properties/escapePattern", "[?]");
        return con;
    }


}
