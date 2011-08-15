/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.report;

import java.io.File;
import java.io.InputStreamReader;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sigmah.server.report.generator.ReportGenerator;
import org.sigmah.server.report.renderer.RendererFactory;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.report.model.Report;
import org.sigmah.test.InjectionSupport;
import org.sigmah.test.MockHibernateModule;
import org.sigmah.test.Modules;
import org.sigmah.test.ServletStubModule;

import com.google.inject.Inject;

//@Ignore("Needs to be rewritten -- figure out what to do with dependency on the map icons folder")
@RunWith(InjectionSupport.class)
@Modules({ReportModule.class, MockHibernateModule.class, ServletStubModule.class})
public class ReportsTest {

    @Inject
    private EntityManager em;

    @Inject
    private ReportGenerator reportGenerator;

    @Inject
    private RendererFactory factory;

	private User user;

	@Before
	public void setup() {
        //user = getUser();
    	//createDirectoriesIfNecessary();    	
    }

//	@Test
//    public void testFullReport() throws Throwable {
//
//        Report report = getReport("full-test.xml");
//
//        reportGenerator.generate(user, report, null, null);
//
//        // Render
//        for (RenderElement.Format format : RenderElement.Format.values()) {
//            if (format != RenderElement.Format.Excel) {
//                Renderer renderer = factory.get(format);
//                FileOutputStream fos = new FileOutputStream("target/report-tests/full-test" + renderer.getFileSuffix());
//
//                renderer.render(report, fos);
//                fos.close();
//            }
//        }
//    }
	
//	@Test
//	public void testApplesReport() throws Throwable {
//
//		// Setup test
//        Report report = getReport("realworld/ApplesReport.xml");
//
//        reportGenerator.generate(user, report, null, null);
//
//        ReportElement element = report.getElements().get(0);
//        PivotTableElement pivotTable = (PivotTableElement) element.getContent();
//
//        // Asserts
//        Assert.assertEquals("Expected different report title", 
//        		report.getTitle().equals("Phase one apple report"));
//        
//        Assert.assertEquals("Expected different report description", 
//        		report.getDescription().equals("Apples come in different shapes, colors and taste"));
//        
//        Assert.assertEquals("Expected only one filter", 
//        		report.getFilter().getRestrictedDimensions().size()==0);
//        
//        Assert.assertEquals("Expected one element", 
//        		report.getElements().size()==0);
//        
//        Assert.assertEquals("Expected pivottable element", 
//        		element.getContent() instanceof PivotTableElement);
//        
//        Assert.assertEquals("Expected title: 'Apples, bananas and oranges'", 
//        		pivotTable.getTitle().equals("Apples, bananas and oranges"));
//	}
	
	@Test
	public void testConsolideDesActivitesReport() throws Throwable {

		// Setup test
        Report report = getReport("realworld/ConsolideDesActivites.xml");

//        //reportGenerator.generate(user, report, null, null);
//
//        ReportElement elementWithMap1 = report.getElements().get(2);
//        TableElement pivotTable = (TableElement) elementWithMap1.getContent();
//        MapElement map1 = (MapElement) elementWithMap1.getContent();
//        BubbleMapLayer bubbleMap = (BubbleMapLayer) map1.getLayers().get(0);
//        
//        Assert.assertEquals("Arabic numbering expected", 
//        		bubbleMap.getLabelSequence() instanceof ArabicNumberSequence);
//        
//        Assert.assertEquals("MinRadius of 8 expected", 
//        		bubbleMap.getMinRadius() == 8);
//        
//        Assert.assertEquals("MaxRadius of 14 expected", 
//        		bubbleMap.getMaxRadius()==14);
//        
//        Assert.assertEquals("Graduated scaling expected", 
//        		bubbleMap.getScaling().equals(ScalingType.Graduated));
	}
    
    private void createDirectoriesIfNecessary() {
        File file = new File("target/report-test");
        file.mkdirs();
	}

	private User getUser() {
        return (User) em.createQuery("select u from User u where u.email = :email")
        		.setParameter("email", "akbertram@gmail.com").getResultList().get(0);
	}

	public Report getReport(String reportNameWithPath) throws JAXBException {
    	return ReportParserJaxb.parseXML(new InputStreamReader(
                getClass().getResourceAsStream("/report-def/" + reportNameWithPath)));
    }
}