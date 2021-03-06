/**
 * 
 */
package com.reportolia.core.sql;

import java.util.List;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.reportolia.core.driver.DatabaseDriver;
import com.reportolia.core.handler.security.ReportoliaSecurityHandler;
import com.reportolia.core.model.variable.VariableValue;
import com.reportolia.core.model.variable.VariableValueConsumer;
import com.reportolia.core.repository.variable.VariableValueRepository;
import com.reportolia.core.sql.query.model.QC;
import com.reportolia.core.sql.query.model.Query;
import com.reportolia.core.sql.query.model.QueryColumn;
import com.reportolia.core.sql.query.model.QueryJoin;
import com.reportolia.core.sql.query.model.QueryOperand;
import com.reportolia.core.sql.query.model.QuerySortColumn;
import com.reportolia.core.sql.query.model.QueryTable;

/**
 * The SqlGeneratorManager class
 *
 * @author Batir Akhmerov
 * Created on Nov 25, 2015
 */
@Component
public class SqlGeneratorManager implements SqlGeneratorHandler {
	
	@Resource protected DatabaseDriver databaseDriver;
	@Resource protected ReportoliaSecurityHandler reportoliaSecurityHandler;
	@Resource protected VariableValueRepository variableValueRepository;
	
	public String toSql(Query query, List<Object> valueList) {
		StringBuilder builder = new StringBuilder();
		return toSql(query, builder, valueList);
	}
	
	public String toSql(Query query, StringBuilder builder, List<Object> valueList) {
		boolean isSimpleExpression = CollectionUtils.isEmpty(query.getTableList());
		
		if (isSimpleExpression) {
			toSqlColumns(query.getColumnList(), builder, valueList);
			return builder.toString();
		}
		
		builder.append(QC.SELECT);
		builder.append(QC.NL);
		builder.append(QC.TAB);
		
		if (query.getTop() > 0) {
			builder.append(QC.TOP);
			builder.append(QC.SPACE);
			builder.append(query.getTop());
			builder.append(QC.SPACE);
		}
		
		// column projection
		toSqlColumns(query.getColumnList(), builder, valueList);
		
		//FROM clause
		builder.append(QC.NL);
		builder.append(QC.FROM);		
		toSqlTables(query.getTableList(), builder, valueList, false);
		
		// WHERE clause
		if (!CollectionUtils.isEmpty(query.getFilterList())) {
			builder.append(QC.NL);
			builder.append(QC.WHERE);
			builder.append(QC.NL);
			builder.append(QC.TAB);
			toSqlOperands(query.getFilterList(), builder, valueList);
		}
		
		// ORDER BY clause
		if (!CollectionUtils.isEmpty(query.getSortingList())) {
			builder.append(QC.NL);
			builder.append(QC.ORDER_BY);
			builder.append(QC.NL);
			builder.append(QC.TAB);
			toSqlSortColumns(query.getSortingList(), builder, valueList);
		}
		// GROUP BY clause
		if (!CollectionUtils.isEmpty(query.getGroupList())) {
			builder.append(QC.NL);
			builder.append(QC.GROUP_BY);
			builder.append(QC.NL);
			builder.append(QC.TAB);
			toSqlOperands(query.getGroupList(), builder, valueList, QC.COMMA);
		}
		
		return builder.toString();
	}
	
	/**
	 * TABLES
	 */
	protected void toSqlTables(List<QueryTable> tableList, StringBuilder builder, List<Object> valueList, boolean isMainFound) {
		if (CollectionUtils.isEmpty(tableList)){
			return;
		}
		for (QueryTable table: tableList) {
			if (table.isMain()) {
				builder.append(QC.SPACE);
				builder.append(table.getTableName());
				builder.append(QC.SPACE);
				builder.append(table.getAlias());
				builder.append(QC.NL);
				builder.append(QC.TAB);
				isMainFound = true;
				
				if (!CollectionUtils.isEmpty(table.getTableList())) {
					toSqlTables(table.getTableList(), builder, valueList, true);
				}
			}
			else if (table.isSecurityFilterSql()) {
				builder.append( replaceSqlMarkers(table.getSecurityFilterSql(), valueList) );
			}
			else {
				Assert.isTrue(isMainFound, "Main QueryTable must be the first table in the list!");
				
				builder.append(table.getJoinType().getSql());
				builder.append(QC.SPACE);
				
				if (CollectionUtils.isEmpty(table.getTableList())) {
					builder.append(table.getTableName());
					builder.append(QC.SPACE);
					builder.append(table.getAlias());
					builder.append(QC.ON);
					
					toSqlJoins(table, builder, valueList);
					
					builder.append(QC.NL);
					builder.append(QC.TAB);
				}
				else {
					builder.append(QC.PL);
					builder.append(QC.SPACE);
					builder.append(table.getTableName());
					builder.append(QC.SPACE);
					builder.append(table.getAlias());
					builder.append(QC.SPACE);
					
					toSqlTables(table.getTableList(), builder, valueList, true);
					
					builder.append(QC.PR);
					builder.append(QC.ON);
					toSqlJoins(table, builder, valueList);
					builder.append(QC.NL);
					builder.append(QC.TAB);
				}
			}
		}
	}
	
	protected void toSqlJoins(QueryTable table, StringBuilder builder, List<Object> valueList) {
		Assert.isTrue(!CollectionUtils.isEmpty(table.getJoinList()), "QueryTable.joinList cannot be null!");
		boolean isFirst = true;
		for (QueryJoin join: table.getJoinList()) {
			if (!isFirst) {
				builder.append(QC.SPACE);
				builder.append(QC.AND);
			}
			if (StringUtils.isEmpty(join.getJoinValue())) {
				builder.append(QC.SPACE);
				builder.append(join.getPkColumn().getTable().getAlias());
				builder.append(QC.DOT);
				builder.append(join.getPkColumn().getSql());
				builder.append(QC.EQ);
				builder.append(join.getJoinColumn().getTable().getAlias());
				builder.append(QC.DOT);
				builder.append(join.getJoinColumn().getSql());
				builder.append(QC.SPACE);
			}
			else {
				builder.append(QC.SPACE);
				if (join.getPkColumn() != null) {
					builder.append(join.getPkColumn().getTable().getAlias());
					builder.append(QC.DOT);
					builder.append(join.getPkColumn().getSql());
				}
				else if (join.getJoinColumn() != null) {
					builder.append(join.getJoinColumn().getTable().getAlias());
					builder.append(QC.DOT);
					builder.append(join.getJoinColumn().getSql());
				}
				else {
					Assert.isTrue(false, "One of QueryJoin Columns is expected!");
				}
				builder.append(QC.EQ);
				//builder.append(QC.Q);
				builder.append( replaceSqlMarkers(join.getJoinValue(), valueList) );
				builder.append(QC.SPACE);
			}
			isFirst = false;
		}
	}
	
	protected String replaceSqlMarkers(String sql, List<Object> valueList) {
		if (this.reportoliaSecurityHandler != null) {
			int count = StringUtils.countOccurrencesOf(sql, QC.MARKER_USER_ID);
			if (count > 0) {
				sql = sql.replace(QC.MARKER_USER_ID, QC.Q);
				for(; count-- != 0;) {
					valueList.add(this.reportoliaSecurityHandler.getUserId());
				}
			}
			else {
				valueList.add(sql);
				sql = QC.Q;
			}
		}
		return sql;
	}
	
	
	protected void toSqlColumns(List<QueryColumn> columnList, StringBuilder builder, List<Object> valueList) {
		Assert.isTrue(!CollectionUtils.isEmpty(columnList), "Query.columnList cannot be empty");
		boolean isFirst = true;
		for (QueryColumn column: columnList) {
			builder.append(QC.NL);
			builder.append(QC.TAB);
			if (!isFirst) {
				builder.append(QC.COMMA);
			}
			toSqlColumn(column, builder, valueList);
			isFirst = false;
		}
	}
	
	protected void toSqlColumn(QueryColumn column, StringBuilder builder, List<Object> valueList) {
		if (CollectionUtils.isEmpty(column.getOperandList()) && column.getNestedQuery() == null) {
			if (column.getTable() != null) {
				builder.append(column.getTable().getAlias());
				builder.append(QC.DOT);
			}
			builder.append(column.getSql());
		}
		else if (column.getNestedQuery() == null) {
			toSqlOperands(column.getOperandList(), builder, valueList);
		}
		else {
			builder.append(QC.PL);
			toSql(column.getNestedQuery(), builder, valueList);
			builder.append(QC.PR);
		}
	}
	
	protected void toSqlOperands(List<QueryOperand> operandList, StringBuilder builder, List<Object> valueList) {
		toSqlOperands(operandList, builder, valueList, null);
	}
	
	protected void toSqlOperands(List<QueryOperand> operandList, StringBuilder builder, List<Object> valueList, String delimeter) {
		if (CollectionUtils.isEmpty(operandList)) {
			return;
		}
		//builder.append(QC.PL);
		boolean isFirst = true;
		for (QueryOperand operand: operandList) {
			if (!isFirst && !StringUtils.isEmpty(delimeter)) {
				builder.append(delimeter);
			}
			toSqlOperand(operand, builder, valueList);
			builder.append(QC.SPACE);
			isFirst = false;
		}
		//builder.append(QC.PR);
	}
	
	protected void toSqlOperand(QueryOperand operand, StringBuilder builder, List<Object> valueList) {
		if (operand.isConvertToString() && (!operand.isContentBlockOperand() || !operand.isBlockEnd())) {
			builder.append(this.databaseDriver.toVarcharStart());
		}
		if (StringUtils.isEmpty(operand.getSql())) {
			if (operand instanceof QueryColumn) {
				toSqlColumn((QueryColumn) operand, builder, valueList);				
			}
			else if (operand.getVariable() != null) {
				VariableValueConsumer valueConsumer = operand.getVariableValueConsumer();
				Assert.isTrue(valueConsumer != null, "VariableValueConsumer cannot be null!");
				Long userId = this.reportoliaSecurityHandler.getUserId();
				List<VariableValue> variableValueList = this.variableValueRepository.findByConsumer(operand.getVariable().getId(), valueConsumer.getType(), valueConsumer.getConsumerId(), userId);
				if (CollectionUtils.isEmpty(variableValueList)) {
					throw new ValidationException(String.format("Assign values to Variable [%s]!", operand.getVariable().getName()));
				}
				Assert.isTrue(!CollectionUtils.isEmpty(variableValueList), "variableValueList cannot be null!");
				boolean isFirst = true;
				for (VariableValue v: variableValueList) {
					if (!isFirst) {
						builder.append(QC.COMMA);
					}
					builder.append(QC.Q);
					valueList.add(v.getValue());
					isFirst = false;
				}
			}
			else if (operand.getNestedQuery() != null) {
				builder.append(QC.PL);
				toSql(operand.getNestedQuery(), builder, valueList);
				builder.append(QC.PR);
			}
			else {
				Assert.isTrue(false, "QueryOperand.sql cannot be null!");
			}
		}
		else {
			if (operand.getTable() != null) {
				builder.append(operand.getTable().getAlias());
				builder.append(QC.DOT);
			}
			builder.append(operand.getSql());
			if (!CollectionUtils.isEmpty(operand.getValueList())) {
				valueList.addAll(operand.getValueList());
			}
		}
		if (operand.isConvertToString() && (!operand.isContentBlockOperand() || !operand.isBlockStart())) {
			builder.append(this.databaseDriver.toVarcharEnd());
		}
	}
	
	protected void toSqlSortColumns(TreeSet<QuerySortColumn> list, StringBuilder builder, List<Object> valueList) {
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		boolean isFirst = true;
		for (QuerySortColumn sortColumns: list) {
			builder.append(QC.NL);
			builder.append(QC.TAB);
			if (!isFirst) {
				builder.append(QC.COMMA);
			}
			toSqlSortColumn(sortColumns, builder, valueList);
			isFirst = false;
		}
	}
	
	protected void toSqlSortColumn(QuerySortColumn sortColumn, StringBuilder builder, List<Object> valueList) {
		if (sortColumn.getColumn() != null) {
			toSqlColumn(sortColumn.getColumn(), builder, valueList);
		}
		else {
			builder.append(sortColumn.getProjectionIndex());			
		}
		if (sortColumn.isSortDesc()) {
			builder.append(QC.ORDER_BY_DESC);
		}
		
	}
	
	
	

}
