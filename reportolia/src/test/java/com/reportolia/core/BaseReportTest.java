package com.reportolia.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.reportolia.core.config.ColumnDetectorXmlDataSetLoader;
import com.reportolia.core.handler.DbHandler;
import com.reportolia.core.handler.report.ReportHandler;
import com.reportolia.core.model.report.Report;
import com.reportolia.core.repository.report.ReportColumnRepository;
import com.reportolia.core.repository.report.ReportRepository;
import com.reportolia.core.repository.table.DbTableColumnRepository;
import com.reportolia.core.repository.table.DbTableRepository;
import com.reportolia.core.sql.ReportQueryGeneratorHandler;
import com.reportolia.core.sql.SqlGeneratorHandler;
import com.reportolia.core.sql.query.QueryGeneratorHandler;
import com.reportolia.core.sql.query.model.Query;

/**
 * 
 * The BaseReportTest class
 *
 * @author Batir Akhmerov
 * Created on Dec 3, 2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {PersistenceContext.class})
@ContextConfiguration(locations = {"classpath:/com/reportolia/core/testContext-persistence.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader = ColumnDetectorXmlDataSetLoader.class)
@DatabaseSetup("database-data.xml")
public class BaseReportTest {

	@Resource protected DbTableRepository tableRepository;
	@Resource protected DbTableColumnRepository tableColumnRepository;
	@Resource protected DbHandler dbManager;
	@Resource protected ReportRepository reportRepository;
	@Resource protected ReportColumnRepository reportColumnRepository;
	@Resource protected ReportHandler reportManager;
	@Resource protected QueryGeneratorHandler queryGeneratorManager;
	@Resource protected ReportQueryGeneratorHandler reportQueryGeneratorManager;
	@Resource protected SqlGeneratorHandler sqlGeneratorManager;
	
	protected void testReportSql(String testName, Long reportId, String expectedSql) {
		List<Object> valueList = new ArrayList<>();
		testReportSql(testName, reportId, expectedSql, valueList); 
	}
	
	protected void testReportSql(String testName, Long reportId, String expectedSql, Object... expectedValues) {
		List<Object> valueList = new ArrayList<>();
		testReportSql(testName, reportId, expectedSql, valueList); 
		if (expectedValues != null) {
			assertEquals(testName + ". Message: Value list size should match!", expectedValues.length, valueList.size());
			int i = 0;
			for (Object v: expectedValues) {
				assertEquals(testName + ". Message: Sql Value should match!", v, valueList.get(i++));
			}
		}
	}
	
    protected void testReportSql(String testName, Long reportId, String expectedSql, List<Object> valueList) {
		Report report = this.reportRepository.findById(reportId);
		
        Query query = this.reportQueryGeneratorManager.getReportQuery(report);
        
        String sql = this.sqlGeneratorManager.toSql(query, valueList);
        
        sql = cleanSpecialSymbols(sql);
        

        assertEquals(testName + "!", expectedSql, sql);
        
    }
	
	protected String cleanSpecialSymbols(String sql) {
		sql = sql.replace('\n', ' ');
		sql = sql.replace('\t', ' ');
		sql = sql.replace("  ", " ");
		sql = sql.replace("  ", " ");
		return sql;
	}
    
	
}
